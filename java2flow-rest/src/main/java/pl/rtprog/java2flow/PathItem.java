package pl.rtprog.java2flow;

import pl.rtprog.java2flow.structs.NamedTypeInfo;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedType;
import java.lang.reflect.Type;

/**
 * Interface for path fragments.
 *
 * @author Ryszard Trojacki
 */
public interface PathItem {


    /**
     * Path item, that is just a const value.
     */
    class ConstPathItem implements PathItem {
        /**
         * Path item value
         */
        private final String value;

        public ConstPathItem(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    class ParamPathItem extends NamedTypeInfo implements PathItem {
        public ParamPathItem(Class<?> clazz, Type type, AnnotatedType annotatedType, Annotation[] annotations, String name) {
            super(clazz, type, annotatedType, annotations, name);
        }
    }


    static ConstPathItem of(String value) {
        return new ConstPathItem(value);
    }

    static ParamPathItem of(Class<?> clazz, Type type, AnnotatedType annotatedType, Annotation[] annotations, String name) {
        return new ParamPathItem(clazz, type, annotatedType, annotations, name);
    }
}
