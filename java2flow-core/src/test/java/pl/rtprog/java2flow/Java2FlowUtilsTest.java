package pl.rtprog.java2flow;

import org.junit.Test;

import static org.junit.Assert.*;

public class Java2FlowUtilsTest {

    @Test
    public void formatComment() {
        assertEquals(" * comment line 1\n", Java2FlowUtils.formatComment("comment line 1"));
    }
}