package pl.rtprog.java2flow;

import org.junit.Test;
import pl.rtprog.java2flow.interfaces.ClassAnnotations;
import pl.rtprog.java2flow.samples.JavaBeanClass;
import pl.rtprog.java2flow.samples.NoAnnotationsClass;
import pl.rtprog.java2flow.samples.NotNullFieldsClass;

import static org.junit.Assert.*;

/**
 * Tests for classes {@link ClassAnnotationReader}.
 *
 * @author Ryszard Trojnacki
 */
public class ClassAnnotationReaderTests {
    @Test
    public void noAnnotationClasses() {
        ClassAnnotationReader cr=new ClassAnnotationReader(Thread.currentThread().getContextClassLoader());
        ClassAnnotations ca=cr.get(NoAnnotationsClass.class);

        assertNotNull(ca);
        assertFalse(ca.isNotNull("privateString"));
        assertFalse(ca.isNotNull("publicString"));
    }

    @Test
    public void notNullFields() {
        ClassAnnotationReader cr=new ClassAnnotationReader(Thread.currentThread().getContextClassLoader());
        ClassAnnotations ca=cr.get(NotNullFieldsClass.class);

        assertNotNull(ca);
        assertTrue(ca.isNotNull("privateString"));
        assertTrue(ca.isNotNull("publicString"));
        assertFalse(ca.isNotNull("other"));
    }

    @Test
    public void notNullGetter() {
        ClassAnnotationReader cr=new ClassAnnotationReader(Thread.currentThread().getContextClassLoader());
        ClassAnnotations ca=cr.get(JavaBeanClass.class);

        assertNotNull(ca);
        assertTrue(ca.isNotNull("privateString"));
        assertTrue(ca.isNotNull("publicString"));
        assertFalse(ca.isNotNull("other"));
    }

}
