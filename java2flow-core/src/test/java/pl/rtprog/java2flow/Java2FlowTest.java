package pl.rtprog.java2flow;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.junit.Test;

import java.util.List;
import java.util.Map;

public class Java2FlowTest {
    static class Class1 {
        public String field1;
        public int field2;
        public Map<String, Integer> data;
        @JsonInclude(JsonInclude.Include.NON_NULL)
        public List<Boolean> booleans;

    }

    private static String generate(Class<?> clazz) {
        Java2Flow c=new Java2Flow();
        c.addHeader();
        c.export(clazz);
        return c.toString();
    }

    @Test
    public void class1Test() {
        System.out.print(generate(Class1.class));

    }
}