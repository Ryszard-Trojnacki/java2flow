package pl.rtprog.java2flow.interfaces;

/**
 * Interface for field JavaDoc provider.
 *
 * @author Ryszard Trojnacki
 */
public interface FieldJavaDoc {
    /** Get field name about which is this comment */
    String getAbout();

    /** Get comment content */
    String getComment();
}
