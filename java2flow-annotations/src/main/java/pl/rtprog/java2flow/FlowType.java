package pl.rtprog.java2flow;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for class with additional information for given class.
 *
 * @author Ryszard Trojnacki
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.TYPE})
public @interface FlowType {
    /**
     * Name of this type in Flow code.
     * When empty then it is Java class name ({@link Class#getSimpleName()}).
     * @return name of this type in Flow code.
     */
    String value() default "";

    /**
     * Custom Flow code to use for this type.
     * If empty then it will be generated.
     * @return custom Flow code to use for this type.
     */
    String custom() default "";

    /**
     * Extra/optional description. If provided then it will be added in JsDoc.
     * @return extra/optional description.
     */
    String description() default "";
}
