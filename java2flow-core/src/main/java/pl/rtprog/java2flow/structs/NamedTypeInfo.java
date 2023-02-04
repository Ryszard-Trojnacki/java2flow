package pl.rtprog.java2flow.structs;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.Collection;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Class that extends {@link JavaTypeInfo} with name.
 *
 * @author Ryszard Trojnacki
 */
public class NamedTypeInfo extends JavaTypeInfo {
    private final String name;

    public NamedTypeInfo(Class<?> clazz, Type type, AnnotatedType annotatedType, Annotation[] annotations, String name) {
        super(clazz, type, annotatedType, annotations);
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public static NamedTypeInfo forParameter(Method method, String name, int paramNum) {
        return new NamedTypeInfo(
                method.getParameterTypes()[paramNum],
                method.getGenericParameterTypes()[paramNum],
                method.getAnnotatedParameterTypes()[paramNum],
                method.getParameterAnnotations()[paramNum],
                name
        );
    }

    public static <T extends NamedTypeInfo> Map<String, T> map(Collection<T> items) {
        return items.stream().collect(Collectors.toMap(NamedTypeInfo::getName, Function.identity()));
    }
}
