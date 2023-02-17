package pl.rtprog.java2flow;

import org.junit.Test;

import static org.junit.Assert.*;

public class Java2FlowUtilsTest {

    @Test
    public void formatComment() {
        assertEquals(" * comment line 1\n", Java2FlowUtils.formatComment("comment line 1"));
    }

    @Test
    public void processGeneric() {
        assertEquals("type.Test", Java2FlowUtils.processGeneric("Test", v -> "type."+v));
        assertEquals("type.Array<type.Test>", Java2FlowUtils.processGeneric("Array<Test>", v -> "type."+v));
        assertEquals("type.Map<type.Key, type.Value>", Java2FlowUtils.processGeneric("Map<Key, Value>", v -> "type."+v));
        assertEquals("type.Map<type.Key, type.Array<type.Value>>",
                Java2FlowUtils.processGeneric("Map<Key, Array<Value>>", v -> "type."+v));

    }
}