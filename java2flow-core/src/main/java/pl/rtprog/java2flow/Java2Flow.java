package pl.rtprog.java2flow;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.type.CollectionType;
import pl.rtprog.java2flow.interfaces.ClassJavaDoc;
import pl.rtprog.java2flow.interfaces.FieldJavaDoc;
import pl.rtprog.java2flow.interfaces.JavaDocProvider;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Java types converter to JavaScript Flow types.
 *
 * @author Ryszard Trojnacki
 */
public class Java2Flow {
    private final ObjectMapper mapper=new ObjectMapper();

    private final JavaDocProvider javaDocProvider;

    /** Already known types */
    protected final HashMap<Class<?>, String> types=new HashMap<>();

    protected final StringBuilder out=new StringBuilder(1024);

    public Java2Flow(JavaDocProvider javaDocProvider) {
        this.javaDocProvider=javaDocProvider;
        registerCoreTypes();
    }

    public Java2Flow() {
        this(null);
    }

    /**
     * Register core Java types/primitives.
     */
    public void registerCoreTypes() {
        types.put(byte.class, "number");
        types.put(short.class, "number");
        types.put(int.class, "number");
        types.put(long.class, "number");
        types.put(float.class, "number");
        types.put(double.class, "number");

        types.put(Byte.class, "number");
        types.put(Short.class, "number");
        types.put(Integer.class, "number");
        types.put(Long.class, "number");
        types.put(Float.class, "number");
        types.put(Double.class, "number");

        types.put(String.class, "string");

        types.put(boolean.class, "boolean");
        types.put(Boolean.class, "boolean");

        types.put(Object.class, "any");
        types.put(void.class, "void");
        types.put(Void.class, "void");

        types.put(BigInteger.class, "BigInt");
    }

    /**
     * Register Java date time types as string.
     */
    public void registerDateTypesAsString() {
        types.put(LocalDateTime.class, "string");
        types.put(LocalDate.class, "string");
        types.put(Instant.class, "string");
        types.put(BigDecimal.class, "string");
        types.put(BigInteger.class, "string");
    }

    /**
     * Register {@link BigDecimal} as string type
     */
    public void registerBigDecimalAsString() {
        types.put(BigDecimal.class, "string");
    }

    /**
     * Override if You want to add something at beaning of the file.
     */
    public void addHeader() {
        out.append("//@flow\n\n");
    }

    private static String getTypename(Class<?> clazz) {
        FlowType ft=clazz.getAnnotation(FlowType.class);
        if(ft!=null && ft.value().length()>0) return ft.value();
        return clazz.getSimpleName();
    }

    /**
     * Method for registering custom Flow code for given Java class.
     * @param type java type
     * @param flowCode flow code to use for this type
     */
    public void registerCustomType(Class<?> type, String flowCode) {
        types.put(type, flowCode);
    }

    private String getType(Class<?> type, final Type typeInfo) {
        String t=types.get(type);
        if(t!=null) return t;

//        System.out.println("Get type for: "+type);

        FlowType ft=type.getAnnotation(FlowType.class);
        if(ft!=null && ft.custom().length()>0) {
            String name=getTypename(type);
            out.append("export ").append(name).append(" = ").append(ft.custom()).append('\n');
            types.put(type, name);
            return name;
        }

        // Inline types
        if(type.isArray()) {
            return "Array<"+getType(type.getComponentType(), typeInfo)+">";
        }
        if(List.class.isAssignableFrom(type)) {
            if(typeInfo instanceof CollectionType) {
                CollectionType ct=(CollectionType)typeInfo;
                return "Array<"+getType(ct.getContentType().getRawClass(), ct.getContentType())+">";
            }
            if(typeInfo instanceof ParameterizedType) {
                Class<?> comp=(Class<?>)((ParameterizedType)typeInfo).getActualTypeArguments()[0];
                return "Array<"+getType(comp, comp)+">";
            } else return "Array<any>";
        }
        if(Map.class.isAssignableFrom(type)) {
            if((typeInfo instanceof JavaType) && ((JavaType)typeInfo).isMapLikeType()) {
                JavaType mt=(JavaType)typeInfo;
                return "{ ["+getType(mt.getKeyType().getRawClass(), mt.getKeyType())
                        +"]: "+getType(mt.getContentType().getRawClass(), mt.getContentType())+" }";
            }
            Type ti=typeInfo;
            if(!(ti instanceof ParameterizedType)) ti=type.getGenericSuperclass();
            if(ti instanceof ParameterizedType) {
                Class<?> key=(Class<?>)((ParameterizedType)ti).getActualTypeArguments()[0];
                Class<?> value=(Class<?>)((ParameterizedType)ti).getActualTypeArguments()[1];
                return "{ ["+ getType(key, key)+"]: "+ getType(value, value)+" }";
            }
            return "{}";    // any map
        }

        // Typed types
        String name=getTypename(type);
        if(type.isEnum()) {
            if(ft!=null && ft.description().length()>0) out.append("/**\n * ").append(ft.description()).append("\n **/\n");
            out.append("export type ").append(name).append(" = ");
            boolean first=true;
            for(Object e: type.getEnumConstants()) {
                if(first) first=false;
                else out.append("|");
                out.append('\'').append(((Enum<?>)e).name()).append('\'');
            }
            out.append(";\n\n");
            types.put(type, name);
            return name;
        }

        final StringBuilder out=new StringBuilder();
        final ClassJavaDoc javaDoc=javaDocProvider==null?null:javaDocProvider.getComments(type);
        types.put(type, name);
        if(ft!=null && ft.description().length()>0) {
            out.append("/**\n");
            Java2FlowUtils.formatOutput(out, " * ", ft.description());
            out.append(" **/\n");
        }
        else if(javaDoc!=null) {
            if(!Java2FlowUtils.isBlank(javaDoc.getComment())) {
                out.append("/**\n");
                Java2FlowUtils.formatOutput(out, " * ", javaDoc.getComment());
                if(!Java2FlowUtils.isBlank(javaDoc.getAuthor())) {
                    Java2FlowUtils.formatOutput(out, " * ", javaDoc.getAuthor());
                }
                out.append(" **/\n");
            }
        }
        out.append("export type ").append(name).append(" =");
        String superClass=null;
        if(type.getSuperclass()!=Object.class) {
            String sup=getType(type.getSuperclass(), type.getGenericSuperclass());
            if(sup!=null) {
                superClass=sup;
            }
        }

        final boolean hasParentClass=(superClass!=null);
        if(hasParentClass) out.append(" ").append(superClass).append(" &");

        out.append(" {\n");


        JsonFormatVisitorWrapper.Base visitor=new JsonFormatVisitorWrapper.Base() {
            @Override
            public JsonObjectFormatVisitor expectObjectFormat(JavaType type) {
                return new JsonObjectFormatVisitor.Base() {
                    private void process(BeanProperty prop) {
                        // Skip parent types members if parent type generated
                        if(hasParentClass && prop.getMember().getDeclaringClass()!=type.getRawClass()) return;

                        String name=prop.getName();
                        FlowProperty fp=prop.getAnnotation(FlowProperty.class);
                        if(fp!=null && fp.value().length()>0) name=fp.value();

                        if(fp!=null && fp.description().length()>0) {
                            out.append("\t/** \n");
                            Java2FlowUtils.formatOutput(out, "\t * ", fp.description());
                            out.append("\t **/\n");
                        } else if(javaDoc!=null) {
                            FieldJavaDoc doc=javaDoc.get(prop.getName());
                            if(doc!=null && !Java2FlowUtils.isBlank(doc.getComment())) {
                                out.append("\t/** \n");
                                Java2FlowUtils.formatOutput(out, "\t * ", doc.getComment());
                                out.append("\t **/\n");
                            }
                        }
                        out.append("\t").append(name);
                        JsonInclude ji=prop.getAnnotation(JsonInclude.class);
                        if(ji!=null && ji.value()!= JsonInclude.Include.ALWAYS) out.append('?');
                        out.append(": ");

                        if(fp!=null && fp.custom().length()>0) {
                            out.append(fp.custom()).append(";\n");
                            return;
                        }
                        out.append(getType(prop.getType().getRawClass(), prop.getType()));
                        if(!prop.getType().isPrimitive()) {
                            if(!Java2FlowUtils.isNotNull(prop)) {
                                out.append("|null");
                            }
                        }
                        out.append(";\n");
                    }

                    @Override
                    public void optionalProperty(BeanProperty prop) {
                        process(prop);
                    }

                    @Override
                    public void property(BeanProperty prop) {
                        process(prop);
                    }
                };
            }
        };
        try {
            mapper.acceptJsonFormatVisitor(type, visitor);
        } catch (JsonMappingException e) {
            throw new RuntimeException(e);
        }
        out.append("};\n\n");
        this.out.append(out);
        return name;
    }

    /**
     * Metod for generating Flow types for given java type.
     * @param type Java
     */
    public void export(Class<?> type) {
        getType(type, type);
    }

    public String toString() {
        return out.toString();
    }
}
