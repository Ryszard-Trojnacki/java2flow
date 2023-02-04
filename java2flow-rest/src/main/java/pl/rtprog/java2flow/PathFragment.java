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
public interface PathFragment {


    /**
     * Path item, that is just a const value.
     */
    class ConstPathFragment implements PathFragment {
        /**
         * Path item value
         */
        private final String value;

        public ConstPathFragment(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    class ParamPathFragment extends NamedTypeInfo implements PathFragment {
        public ParamPathFragment(Class<?> clazz, Type type, AnnotatedType annotatedType, Annotation[] annotations, String name) {
            super(clazz, type, annotatedType, annotations, name);
        }
    }


    static ConstPathFragment of(String value) {
        return new ConstPathFragment(value);
    }

    static ParamPathFragment of(Class<?> clazz, Type type, AnnotatedType annotatedType, Annotation[] annotations, String name) {
        return new ParamPathFragment(clazz, type, annotatedType, annotations, name);
    }
}
