package pl.rtprog.java2flow;

import org.junit.Test;
import pl.rtprog.java2flow.interfaces.ClassJavaDoc;
import pl.rtprog.java2flow.sample.*;

import java.nio.file.Paths;

import static org.junit.Assert.*;

/**
 * @author Ryszard Trojnacki
 */
public class JavadocProcessorTests {
    @Test
    public void invalidDirectory() {
        JavadocProcessor proc=new JavadocProcessor(Paths.get("not","exists").toAbsolutePath());
        assertNull(proc.getComments(NoJavadoc.class));
        assertNull(proc.getComments(Object.class));
    }

    @Test
    public void noJavadoc() {
        JavadocProcessor proc=new JavadocProcessor(Paths.get("src","test","java").toAbsolutePath());

        ClassJavaDoc res=proc.getComments(NoJavadoc.class);
        assertNotNull(res);
        assertNull(res.getAuthor());
        assertNull(res.getComment());
        assertNull(res.get("privateString"));
        assertNull(res.get("publicString"));

        assertNull(proc.getComments(Object.class));
    }

    @Test
    public void classJavadoc() {
        JavadocProcessor proc=new JavadocProcessor(Paths.get("src","test","java").toAbsolutePath());

        ClassJavaDoc res=proc.getComments(ClassWithJavadoc.class);
        assertNotNull(res);
        assertEquals("Javadoc for class.", res.getComment());
        assertEquals("Ryszard Trojnacki", res.getAuthor());
        assertNull(res.get("privateString"));
        assertNull(res.get("publicString"));
    }

    @Test
    public void fieldsJavadoc() {
        JavadocProcessor proc=new JavadocProcessor(Paths.get("src","test","java").toAbsolutePath());

        ClassJavaDoc res=proc.getComments(FieldsJavadoc.class);
        assertNotNull(res);
        assertEquals("Javadoc for private field", res.get("privateString").getComment().trim());
        assertEquals("Javadoc for public field", res.get("publicString").getComment().trim());
    }

    @Test
    public void beanJavadoc() {
        JavadocProcessor proc=new JavadocProcessor(Paths.get("src","test","java").toAbsolutePath());

        ClassJavaDoc res=proc.getComments(JavaBeanJavadoc.class);
        assertNotNull(res);
        assertEquals("Private field javadoc", res.get("privateString").getComment().trim());
        assertNull(res.get("publicString"));
    }

    @Test
    public void bean2Javadoc() {
        JavadocProcessor proc=new JavadocProcessor(Paths.get("src","test","java").toAbsolutePath());

        ClassJavaDoc res=proc.getComments(JavaBean2Javadoc.class);
        assertNotNull(res);
        assertEquals("Getter for private field", res.get("privateString").getComment().trim());
        assertNull(res.get("publicString"));
    }
}
