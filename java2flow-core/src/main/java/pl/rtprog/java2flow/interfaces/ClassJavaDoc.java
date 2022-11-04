package pl.rtprog.java2flow.interfaces;

/**
 * Interface for class JavaDoc comment.
 *
 * @author Ryszard Trojnacki
 */
public interface ClassJavaDoc {
    /** Get class for which this comment is */
    Class<?> getAbout();
    /** Get author block if provided */
    String getAuthor();
    /** Get main comment content */
    String getComment();

    /**
     * Get comment for given field.
     * @param field field for which to get comment
     * @return comment for field or null
     */
    FieldJavaDoc get(String field);
}
