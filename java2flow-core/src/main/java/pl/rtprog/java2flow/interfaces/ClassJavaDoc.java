package pl.rtprog.java2flow.interfaces;

import pl.rtprog.java2flow.Java2FlowUtils;

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

    /**
     * Returns true if comments has some text.
     * @param jdoc JavaDoc object to test
     * @return true if comment has some text
     */
    static boolean isNotEmpty(ClassJavaDoc jdoc) {
        if(jdoc==null) return false;
        return Java2FlowUtils.isNotBlank(jdoc.getAuthor()) || Java2FlowUtils.isNotBlank(jdoc.getComment());
    }
}
