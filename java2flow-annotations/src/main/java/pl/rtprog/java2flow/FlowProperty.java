package pl.rtprog.java2flow;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Additional information for class field for Flow types generation.
 *
 * @author Ryszard Trojnacki
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.FIELD, ElementType.METHOD})
public @interface FlowProperty {
    /**
     * Name of the field in Flow type.
     * @return name of the field in Flow type.
     */
    String value() default "";

    /**
     * Description to add to JsDoc.
     * @return description to add to JsDoc.
     */
    String description() default "";

    /**
     * Custom Flow type code.
     * @return custom Flow type code.
     */
    String custom() default "";
}
