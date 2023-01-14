package pl.rtprog.java2flow;

import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

/**
 * A simple functional test for the 'pl.rtprog.java2flow.greeting' plugin.
 */
class Java2flowPluginFunctionalTest extends Java2FlowTestBase{

    @Test void canRunTask() throws IOException {
        writeString(getSettingsFile(), "");
        writeString(getBuildFile(),
                "plugins {\n" +
                        "  id 'java'\n" +
                        "  id('pl.rtprog.java2flow')\n" +
                        "}\n\n" +
                        "java2flow {\n" +
                        "  generateEmpty = true\n" +
                        "}\n");

        runGradle();
        assertNotNull(readTypes());
    }

    @Test void simpleTypeTest() throws IOException {
        writeBuildFile("pl.rtprog.SimpleBean");
        writeClassFile("pl.rtprog.SimpleBean",
                "package pl.rtprog;\n\n" +
                        "public class SimpleBean {\n" +
                        "\tpublic String field1;\n" +
                        "\tpublic int field2;\n" +
                        "\tpublic boolean field3;\n" +
                        "}\n"
        );
        runGradle();
        String file=readTypes();
        assertNotNull(file);
        log("simpleTypeTest", file);
        assertTrue(file.contains("SimpleBean"));
        assertTrue(file.contains("field1: string|null"));
        assertTrue(file.contains("field2: number"));
        assertTrue(file.contains("field3: boolean"));
    }

    @Test
    void enumTest() throws IOException {
        writeBuildFile("pl.rtprog.ExampleEnum");
        writeClassFile("pl.rtprog.ExampleEnum",
                "package pl.rtprog;\n\n" +
                        "public enum ExampleEnum {\n" +
                        "\tEnumValue1,\n" +
                        "\tEnumValue2,\n" +
                        "\tEnumValueLast;\n" +
                        "}\n"
                );
        runGradle();
        String types=readTypes();
        assertNotNull(types);
        log("enumTest", types);

        assertTrue(types.contains("ExampleEnum"));
        assertTrue(types.contains("'EnumValue1'"));
    }

}