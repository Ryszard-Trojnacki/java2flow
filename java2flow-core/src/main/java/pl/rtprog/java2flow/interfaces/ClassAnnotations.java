package pl.rtprog.java2flow.interfaces;

/**
 * Interface for extended class annotation information.
 *
 * @author Ryszard Trojnacki
 */
public interface ClassAnnotations {
    /**
     * Returns true if given field is not null.
     * @param field field to check
     * @return true if not null annotations exists
     */
    boolean isNotNull(String field);
}
