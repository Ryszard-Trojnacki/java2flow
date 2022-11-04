package pl.rtprog.java2flow.interfaces;

/**
 * Interface for plugin that provides JavaDoc information about a class.
 *
 * @author Ryszard Trojnacki
 */
public interface JavaDocProvider {
    /**
     * Returns JavaDoc for given class
     * @param clazz class for which JavaDoc should be returned
     * @return JavaDoc for given class or null elsewhere
     */
    ClassJavaDoc getComments(Class<?> clazz);
}
