package pl.rtprog.java2flow;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.BeanProperty;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonFormatVisitorWrapper;
import com.fasterxml.jackson.databind.jsonFormatVisitors.JsonObjectFormatVisitor;
import com.fasterxml.jackson.databind.type.CollectionType;
import pl.rtprog.java2flow.interfaces.*;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Java types converter to JavaScript Flow types.
 *
 * @author Ryszard Trojnacki
 */
public class Java2Flow {
    private final ObjectMapper mapper=new ObjectMapper();

    private final JavaDocProvider javaDocProvider;

    private final JavaAnnotationProvider javaAnnotationProvider;

    /** Already known types */
    protected final HashMap<Class<?>, String> types=new HashMap<>();

    protected final StringBuilder out=new StringBuilder(1024);

    /**
     * Generate Flow output (types)
     */
    protected final boolean flow;

    /**
     * Generate JSDoc output (types)
     */
    protected final boolean jsdoc;

    /**
     * Constructor for JavaScript generator
     * @param javaDocProvider Java doc provider to use
     * @param javaAnnotationProvider Java Annotation provider to use
     * @param flow should Flow types be generated
     * @param jsdoc should JSDoc types be generated
     */
    public Java2Flow(JavaDocProvider javaDocProvider, JavaAnnotationProvider javaAnnotationProvider, boolean flow, boolean jsdoc) {
        this.javaDocProvider=javaDocProvider;
        this.javaAnnotationProvider=javaAnnotationProvider;
        this.flow=flow;
        this.jsdoc=jsdoc;
        if(!flow && !jsdoc) throw new IllegalArgumentException("At last one generator has to be enabled");
        registerCoreTypes();
    }

    /**
     * Default constructor without any additional providers.
     */
    public Java2Flow() {
        this(null, null, true, true);
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
        if(flow) out.append("//@flow\n\n");
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
            boolean hasDoc=!Java2FlowUtils.isBlank(ft.description()) || jsdoc;
            if(hasDoc) out.append("/**\n");
            if(!Java2FlowUtils.isBlank(ft.description())) Java2FlowUtils.formatComment(out, ft.description());
            if(jsdoc) out.append(" * @typedef {").append(ft.custom()).append("} ").append(name).append("\n");
            if(hasDoc) out.append(" */\n");
            if(flow) out.append("export type ").append(name).append(" = ").append(ft.custom()).append(";\n");
            out.append("\n");
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
        final ClassJavaDoc javaDoc=javaDocProvider==null?null:javaDocProvider.getComments(type);
        final ClassAnnotations ca=(javaAnnotationProvider==null)?null: javaAnnotationProvider.get(type);
        final StringBuilder comment=new StringBuilder();

        if(ft!=null && Java2FlowUtils.isNotBlank(ft.description())) Java2FlowUtils.formatComment(comment, ft.description());
        if(javaDoc!=null) {
            Java2FlowUtils.formatComment(comment, javaDoc.getComment());
            if(Java2FlowUtils.isNotBlank(javaDoc.getAuthor())) {
                comment.append(" * @author ").append(javaDoc.getAuthor()).append("\n");
            }
        }

        final boolean hasDoc=this.jsdoc || comment.length()>0;

        if(type.isEnum()) {
            String values=Arrays.stream(type.getEnumConstants())
                    .map(e -> '\''+((Enum<?>)e).name()+'\'' )
                    .collect(Collectors.joining("|"));

            if(hasDoc) out.append("/**\n").append(comment);
            if(this.jsdoc) out.append(" * @typedef {").append(values).append("} ").append(name).append("\n");
            if(hasDoc) out.append(" */\n");

            if(flow) out.append("export type ").append(name).append(" = ").append(values).append(";\n");
            out.append("\n");
            types.put(type, name);
            return name;
        }

        final StringBuilder out=new StringBuilder();
        final StringBuilder doc=new StringBuilder();

        types.put(type, name);

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

        if(hasDoc) doc.append("/**\n");

        if(jsdoc) {
            if(hasParentClass) doc.append(" * @typedef {Object} ").append(name).append("_Int").append('\n');
            else {
                doc.append(comment);
                doc.append(" * @typedef {Object} ").append(name).append('\n');
            }
        }

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

                        String comment=null;
                        if(fp!=null && fp.description().length()>0) {
                            comment=fp.description();
                        } else if(javaDoc!=null) {
                            FieldJavaDoc doc=javaDoc.get(prop.getName());
                            if(doc!=null && !Java2FlowUtils.isBlank(doc.getComment())) {
                                comment=doc.getComment();
                            }
                        }
                        if(comment!=null) {
                            out.append("\t/**\n");
                            Java2FlowUtils.formatOutput(out, "\t * ", comment);
                            out.append("\t */\n");
                        }

                        JsonInclude ji=prop.getAnnotation(JsonInclude.class);
                        final boolean optional=ji!=null && ji.value()!= JsonInclude.Include.ALWAYS;

                        out.append("\t").append(name);
                        if(optional) out.append('?');
                        out.append(": ");

                        String type;

                        if(fp!=null && fp.custom().length()>0) {
                            type=fp.custom();
                        } else {
                            type=getType(prop.getType().getRawClass(), prop.getType());
                            if (!prop.getType().isPrimitive()) {
                                if (!Java2FlowUtils.isNotNull(prop) && (ca == null || !ca.isNotNull(prop.getName()))) {
                                    type+="|null";
                                }
                            }
                        }

                        out.append(type).append(";\n");
                        if(jsdoc) {
                            doc.append(" * @property {").append(type).append("} ");
                            if(optional) doc.append('[');
                            doc.append(name);
                            if(optional) doc.append(']');
                            if(comment!=null) {
                                doc.append(' ');
                                Java2FlowUtils.formatOutput(doc, " * ", comment, false);
                            } else doc.append('\n');
                        }
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
        out.append("};\n");

        if(jsdoc && hasParentClass) {
            doc.append(comment);
            doc.append(" *\n * @typedef {")
                    .append(superClass).append(" & ").append(name).append("_Int").append("} ")
                    .append(name).append("\n");
        }

        if(hasDoc) doc.append(" */\n");

        this.out.append(doc);
        if(flow) this.out.append(out);
        this.out.append("\n");

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
