package pl.rtprog.java2flow;

import org.junit.Test;

import javax.ws.rs.FormParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class JavaRest2FlowUtilsTest {
    @Test
    public void isVariable() {
        assertEquals("id", JavaRest2FlowUtils.isVariable("{id}"));
        assertEquals("param1", JavaRest2FlowUtils.isVariable("{param1}"));
        assertNull(JavaRest2FlowUtils.isVariable("api"));
    }

    private static void testMethod(@PathParam("id") @FormParam("ble") String test) {}

    @Test
    public void find() throws NoSuchMethodException {
        Method m=JavaRest2FlowUtilsTest.class.getDeclaredMethod("testMethod", String.class);
        assertNotNull(JavaRest2FlowUtils.find(m.getParameterAnnotations()[0], PathParam.class));
        assertNotNull(JavaRest2FlowUtils.find(m.getParameterAnnotations()[0], FormParam.class));
        assertNull(JavaRest2FlowUtils.find(m.getParameterAnnotations()[0], Path.class));

        assertEquals("id", JavaRest2FlowUtils.find(m.getParameterAnnotations()[0], PathParam.class).value());

    }

}