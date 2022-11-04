package pl.rtprog.java2flow;

import org.gradle.internal.impldep.org.apache.commons.io.FileUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * A simple functional test for the 'pl.rtprog.java2flow.greeting' plugin.
 */
class Java2flowPluginFunctionalTest {
    @TempDir
    File projectDir;

    private File getBuildFile() {
        return new File(projectDir, "build.gradle");
    }

    private File getSettingsFile() {
        return new File(projectDir, "settings.gradle");
    }

    @Test void canRunTask() throws IOException {
        writeString(getSettingsFile(), "");
        writeString(getBuildFile(),
                "plugins {\n" +
                        "  id 'java'\n" +
                        "  id('pl.rtprog.java2flow')\n" +
                        "}\n\n" +
                        "java2flow {\n" +
                        "generateEmpty = true\n" +
                        "}\n");

        // Run the build
        GradleRunner runner = GradleRunner.create();
        runner.forwardOutput();
        runner.withPluginClasspath();
        runner.withArguments("--info", "java2flow");
        runner.withProjectDir(projectDir);
        BuildResult result = runner.build();

//        System.out.println("TestRes: "+ result.getOutput());
        assertTrue(new File(new File(projectDir, "build"), "types.js").exists());
    }

    @Test void simpleTypeTest() throws IOException {
        writeString(getBuildFile(), "plugins {\n" +
                "  id 'java'\n" +
                "  id('pl.rtprog.java2flow')\n" +
                "}\n\n" +
                "java2flow {\n" +
                " classes = [ 'pl.rtprog.SimpleBean' ] \n" +
                " output = 'types2.js'\n" +
                "}\n");
        File srcFile=new File(projectDir, String.join(File.separator, "src", "main", "java",
                "pl", "rtprog", "SimpleBean.java"));
        srcFile.getParentFile().mkdirs();
        writeString(srcFile, "package pl.rtprog;\n\n" +
                "public class SimpleBean {\n" +
                "\tpublic String field1;\n" +
                "\tpublic int field2;\n" +
                "\tpublic boolean field3;\n" +
                "}\n");
        GradleRunner runner = GradleRunner.create();
        runner.forwardOutput();
        runner.withPluginClasspath();
        runner.withArguments("--info", "java2flow");
        runner.withProjectDir(projectDir);
        try {
            BuildResult result = runner.build();
        }catch (Exception e) {
            throw e;
        }
        File in=new File(projectDir, "types2.js");
        assertTrue(in.exists());
        String file=FileUtils.readFileToString(in, StandardCharsets.UTF_8);
        assertTrue(file.contains("SimpleBean"));
        assertTrue(file.contains("field1: string|null"));
        assertTrue(file.contains("field2: number"));
        assertTrue(file.contains("field3: boolean"));
    }

    private void writeString(File file, String string) throws IOException {
        try (Writer writer = new FileWriter(file)) {
            writer.write(string);
        }
    }
}