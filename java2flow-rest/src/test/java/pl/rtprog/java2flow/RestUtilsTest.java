package pl.rtprog.java2flow;

import org.junit.Test;

import javax.ws.rs.FormParam;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import java.lang.reflect.Method;

import static org.junit.Assert.*;

public class RestUtilsTest {
    @Test
    public void isVariable() {
        assertEquals("id", RestUtils.isVariable("{id}"));
        assertEquals("param1", RestUtils.isVariable("{param1}"));
        assertNull(RestUtils.isVariable("api"));
    }

    private static void testMethod(@PathParam("id") @FormParam("ble") String test) {}

    @Test
    public void find() throws NoSuchMethodException {
        Method m= RestUtilsTest.class.getDeclaredMethod("testMethod", String.class);
        assertNotNull(RestUtils.find(m.getParameterAnnotations()[0], PathParam.class));
        assertNotNull(RestUtils.find(m.getParameterAnnotations()[0], FormParam.class));
        assertNull(RestUtils.find(m.getParameterAnnotations()[0], Path.class));

        assertEquals("id", RestUtils.find(m.getParameterAnnotations()[0], PathParam.class).value());
    }

    @Test
    public void pathJoin() {
        assertEquals("/api/user/{id}", RestUtils.pathJoin("/api", "/user/{id}"));
        assertEquals("/api/user/{id}", RestUtils.pathJoin("/api", "user/{id}"));
        assertEquals("/api/user/{id}", RestUtils.pathJoin("/api/user", "{id}"));
        assertEquals("/api/user/{id}", RestUtils.pathJoin("/api/user", "/{id}"));
        assertEquals("/api/user/{id}", RestUtils.pathJoin("/api/user/", "/{id}"));
        assertEquals("/api/user/{id}", RestUtils.pathJoin("/api/user/", "{id}"));
        assertEquals("/api/user", RestUtils.pathJoin("/api/user", null));
    }

}