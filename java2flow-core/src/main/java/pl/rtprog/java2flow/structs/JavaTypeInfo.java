package pl.rtprog.java2flow.structs;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.lang.reflect.Type;

/**
 * Class that holds information about a type.
 *
 * @author Ryszard Trojnacki
 */
public class JavaTypeInfo {
    private final Class<?> clazz;
    private final Type type;
    private final AnnotatedType annotatedType;
    private final Annotation[] annotations;

    public JavaTypeInfo(Class<?> clazz, Type type, AnnotatedType annotatedType, Annotation[] annotations) {
        this.clazz = clazz;
        this.type = type;
        this.annotatedType = annotatedType;
        this.annotations = annotations;
    }

    public static JavaTypeInfo of(Class<?> clazz, Type type, AnnotatedType annotatedType, Annotation[] annotations) {
        return new JavaTypeInfo(clazz, type==null?clazz:type, annotatedType, annotations);
    }

    public static JavaTypeInfo of(Class<?> clazz) {
        return new JavaTypeInfo(clazz, clazz, null, null);
    }

    /**
     * Return instance for method return type
     * @param method method for which create type info
     * @return return type info for given method
     */
    public static JavaTypeInfo returnOf(Method method) {
        return new JavaTypeInfo(method.getReturnType(), method.getGenericReturnType(),
                method.getAnnotatedReturnType(), method.getAnnotations()
        );
    }

    public Class<?> getClazz() {
        return clazz;
    }

    public Type getType() {
        return type;
    }

    public AnnotatedType getAnnotatedType() {
        return annotatedType;
    }

    public Annotation[] getAnnotations() {
        return annotations;
    }
}
