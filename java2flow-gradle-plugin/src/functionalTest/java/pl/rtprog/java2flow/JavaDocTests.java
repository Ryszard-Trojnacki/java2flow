package pl.rtprog.java2flow;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

public class JavaDocTests extends Java2FlowTestBase{
    @Test
    void simpleClass() throws IOException {
        writeBuildFile("pl.rtprog.SimpleBean");
        writeClassFile("pl.rtprog.SimpleBean",
                "package pl.rtprog;\n\n" +
                        "/**\n" +
                        " * SimpleBean description\n" +
                        " * @author Ryszard Trojnacki\n" +
                        " */\n" +
                        "public class SimpleBean {\n" +
                        "\tpublic String field1;\n" +
                        "\t/**\n" +
                        "\t * field 2 description\n" +
                        "\t */\n"+
                        "\tpublic int field2;\n" +
                        "\tpublic boolean field3;\n" +
                        "}\n"
        );
        runGradle();
        String file=readTypes();
        assertNotNull(file);
        log("simpleClass", file);
        assertTrue(file.contains("SimpleBean"));
        assertTrue(file.contains("field1: string|null"));
        assertTrue(file.contains("field2: number"));
        assertTrue(file.contains("field3: boolean"));
        assertTrue(file.contains("/**\n" +
                " * SimpleBean description\n" +
                " * @author Ryszard Trojnacki\n" +
                " */"));
        assertTrue(file.contains("/**\n" +
                "\t * field 2 description\n" +
                "\t */"));
    }

    @Test
    void enumWithJavaDoc() throws IOException {
        writeBuildFile("pl.rtprog.ExampleEnum");
        writeClassFile("pl.rtprog.ExampleEnum",
                "package pl.rtprog;\n\n" +
                        "/**\n" +
                        " * ExampleEnum description\n" +
                        " * @author Ryszard Trojnacki\n" +
                        " */\n" +
                        "public enum ExampleEnum {\n" +
                        "\tEnumValue1,\n" +
                        "\tEnumValue2,\n" +
                        "\tEnumValueLast;\n" +
                        "}\n"
        );
        runGradle();
        String types=readTypes();
        assertNotNull(types);
        log("enumWithJavaDoc", types);

        assertTrue(types.contains("ExampleEnum"));
        assertTrue(types.contains("'EnumValue1'"));
        assertTrue(types.contains("/**\n" +
                " * ExampleEnum description\n" +
                " * @author Ryszard Trojnacki\n" +
                " */"));
    }
}
