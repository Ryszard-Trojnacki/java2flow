package pl.rtprog.java2flow;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.HashMap;
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

    private static String generate(Class<?> clazz, boolean flow, boolean jsdoc) {
        Java2Flow c=new Java2Flow(null, null, flow, jsdoc);
        c.addHeader();
        c.export(clazz);
        String res=c.toString();
        System.out.println("Class "+clazz.getName()+(flow?" flow":"")+(jsdoc?" jsdoc":""));
        System.out.println(res);
        System.out.println("EOF");
        return res;
    }

    @Test
    public void class1Test() {
        assertEquals(
                "//@flow\n" +
                        "\n" +
                        "export type Class1 = {\n" +
                        "\tfield1: string|null;\n" +
                        "\tfield2: number;\n" +
                        "\tdata: { [string]: number }|null;\n" +
                        "\tbooleans?: Array<boolean>|null;\n" +
                        "};\n" +
                        "\n"
                ,generate(Class1.class, true, false)
        );
        assertEquals(
                "/**\n" +
                        " * @typedef {Object} Class1\n" +
                        " * @property {string|null} field1\n" +
                        " * @property {number} field2\n" +
                        " * @property {{ [string]: number }|null} data\n" +
                        " * @property {Array<boolean>|null} [booleans]\n" +
                        " */\n" +
                        "\n"
                ,generate(Class1.class, false, true)
        );

        assertEquals(
                "//@flow\n" +
                        "\n" +
                        "/**\n" +
                        " * @typedef {Object} Class1\n" +
                        " * @property {string|null} field1\n" +
                        " * @property {number} field2\n" +
                        " * @property {{ [string]: number }|null} data\n" +
                        " * @property {Array<boolean>|null} [booleans]\n" +
                        " */\n" +
                        "export type Class1 = {\n" +
                        "\tfield1: string|null;\n" +
                        "\tfield2: number;\n" +
                        "\tdata: { [string]: number }|null;\n" +
                        "\tbooleans?: Array<boolean>|null;\n" +
                        "};\n" +
                        "\n"
                ,generate(Class1.class, true, true)
        );
    }

    static class Class2 extends Class1 {
        public float number;
    }

    @Test
    public void class2Test() {
        assertEquals(
                "//@flow\n" +
                        "\n" +
                        "export type Class1 = {\n" +
                        "\tfield1: string|null;\n" +
                        "\tfield2: number;\n" +
                        "\tdata: { [string]: number }|null;\n" +
                        "\tbooleans?: Array<boolean>|null;\n" +
                        "};\n" +
                        "\n" +
                        "export type Class2 = Class1 & {\n" +
                        "\tnumber: number;\n" +
                        "};\n" +
                        "\n"
                , generate(Class2.class, true, false)
        );

        assertEquals(
                "/**\n" +
                        " * @typedef {Object} Class1\n" +
                        " * @property {string|null} field1\n" +
                        " * @property {number} field2\n" +
                        " * @property {{ [string]: number }|null} data\n" +
                        " * @property {Array<boolean>|null} [booleans]\n" +
                        " */\n" +
                        "\n" +
                        "/**\n" +
                        " * @typedef {Object} Class2_Int\n" +
                        " * @property {number} number\n" +
                        " *\n" +
                        " * @typedef {Class1 & Class2_Int} Class2\n" +
                        " */\n" +
                        "\n"
                , generate(Class2.class, false, true)
        );

        assertEquals(
                "//@flow\n" +
                        "\n" +
                        "/**\n" +
                        " * @typedef {Object} Class1\n" +
                        " * @property {string|null} field1\n" +
                        " * @property {number} field2\n" +
                        " * @property {{ [string]: number }|null} data\n" +
                        " * @property {Array<boolean>|null} [booleans]\n" +
                        " */\n" +
                        "export type Class1 = {\n" +
                        "\tfield1: string|null;\n" +
                        "\tfield2: number;\n" +
                        "\tdata: { [string]: number }|null;\n" +
                        "\tbooleans?: Array<boolean>|null;\n" +
                        "};\n" +
                        "\n" +
                        "/**\n" +
                        " * @typedef {Object} Class2_Int\n" +
                        " * @property {number} number\n" +
                        " *\n" +
                        " * @typedef {Class1 & Class2_Int} Class2\n" +
                        " */\n" +
                        "export type Class2 = Class1 & {\n" +
                        "\tnumber: number;\n" +
                        "};\n" +
                        "\n"
                , generate(Class2.class, true, true)
        );

    }

    @Test
    public void registerExternalTypeTest() {
        Java2Flow c=new Java2Flow(null, null, true, true);
        c.addHeader();
        c.registerExternalType(Map.class, "Map", "./core.js");

        assertEquals(
                "//@flow\n" +
                        "import type { Map } from './core.js';\n" +
                        "\n"
                , c.toString());;
    }

    record DataStruct(String str, int number){}

    @Test
    public void recordTest() {
        Java2Flow c=new Java2Flow(null, null, true, true);
        c.export(DataStruct.class);
        var s=c.toString();
        assertFalse(s.contains("Record"));
    }

    // --- Chained generics tests ---

    /** Reproduces the ClassCastException: value type is Map<String,Object>, a ParameterizedType, not a raw Class. */
    static class ChainedGenericMap extends HashMap<String, Map<String, Object>> {}

    @Test
    public void chainedGenericMapTest() {
        Java2Flow c = new Java2Flow(null, null, true, false);
        // Before the fix this threw ClassCastException because Map<String,Object>
        // is a ParameterizedType, not a plain Class.
        String type = c.getJavaScriptType(ChainedGenericMap.class);
        assertEquals("{ [string]: { [string]: any } }", type);
    }

    /** Tests the List ParameterizedType path triggered when the list element type is itself generic. */
    static class ChainedGenericMapWithList extends HashMap<String, List<Map<String, Integer>>> {}

    @Test
    public void chainedGenericMapWithListTest() {
        Java2Flow c = new Java2Flow(null, null, true, false);
        // Before the fix this also threw ClassCastException when resolving List<Map<String,Integer>>
        // as the map value type and then Map<String,Integer> as the list element type.
        String type = c.getJavaScriptType(ChainedGenericMapWithList.class);
        assertEquals("{ [string]: Array<{ [string]: number }> }", type);
    }

}