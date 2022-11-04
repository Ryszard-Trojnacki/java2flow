package pl.rtprog.java2flow.interfaces;

/**
 * Interface for plugins that provides additional information about fields.
 *
 * @author Ryszard Trojnacki
 */
public interface JavaAnnotationProvider {
    /**
     * Returns additional information about a class
     * @param clazz class for which get additional information
     * @return object with additional class information
     */
    ClassAnnotations get(Class<?> clazz);
}
