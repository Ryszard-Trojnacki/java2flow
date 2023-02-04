package pl.rtprog.java2flow;

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

    class ParamPathItem implements PathItem {
        /**
         * Variable name
         */
        private final String name;

        /**
         * Type information about path item
         */
        private final Type type;

        public ParamPathItem(String name, Type type) {
            this.name = name;
            this.type = type;
        }
    }


    static ConstPathItem of(String value) {
        return new ConstPathItem(value);
    }

    static ParamPathItem of(String name, Type type) {
        return new ParamPathItem(name, type);
    }
}
