package pl.rtprog.java2flow;

import org.gradle.internal.impldep.org.apache.commons.io.FileUtils;
import org.gradle.testkit.runner.BuildResult;
import org.gradle.testkit.runner.GradleRunner;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;

/**
 * Base class for testing plugin
 */
public abstract class Java2FlowTestBase {
    @TempDir
    protected File projectDir;

    protected File getBuildFile() {
        return new File(projectDir, "build.gradle");
    }

    protected File getSettingsFile() {
        return new File(projectDir, "settings.gradle");
    }

    /**
     * Helper method to write string to file.
     * @param file file to write string to
     * @param string string to store in file
     * @throws IOException when something is wrong
     */
    protected void writeString(File file, String string) throws IOException {
        try (Writer writer = new FileWriter(file)) {
            writer.write(string);
        }
    }

    /**
     * Write default, simple <code>build.gradle</code> file with java2flow configuration.
     * @param clazz name of class file to be processed
     * @throws IOException when something happens when writing file
     */
    protected void writeBuildFile(String clazz) throws IOException {
        writeString(getBuildFile(), "plugins {\n" +
                "  id 'java'\n" +
                "  id('pl.rtprog.java2flow')\n" +
                "}\n\n" +
                "java2flow {\n" +
                " classes = [ '"+clazz+"' ] \n" +
                "}\n");
    }

    /**
     * Helper method to write Java source file
     * @param clazz full class name
     * @param code source code
     * @return source code file
     * @throws IOException when something happens
     */
    protected File writeClassFile(String clazz, String code) throws IOException {
        File srcFile=new File(projectDir, String.join(File.separator, "src", "main", "java",
                clazz.replace(".", File.separator)+".java")
        );
        srcFile.getParentFile().mkdirs();
        if(code!=null) {
            writeString(srcFile, code);
        }
        return srcFile;
    }

    /**
     * Execute gradle build with <code>--info</code> and task <code>java2flow</code> on
     * {@link #projectDir}.
     * @return gradle execution result
     */
    protected BuildResult runGradle() {
        GradleRunner runner = GradleRunner.create();
        runner.forwardOutput();
        runner.withPluginClasspath();
        runner.withArguments("--info", "java2flow");
        runner.withProjectDir(projectDir);
        return runner.build();
    }

    /**
     * Read file to string
     * @param file file to be read
     * @return content of file or null
     * @throws IOException when cannot read
     */
    protected String readFile(File file) throws IOException{
        if(!file.exists() || !file.canRead() || !file.isFile()) return null;
        return FileUtils.readFileToString(file, StandardCharsets.UTF_8);
    }

    /**
     * Read file from temporary project directory
     * @param file file path parts
     * @return content of file or null if file cannot read
     * @throws IOException when cannot read
     */
    protected String readProjectFile(String... file) throws IOException {
        return readFile(new File(projectDir, String.join(File.separator, file)));
    }

    /**
     * Wrapper for {@link #readProjectFile(String...)} for file <code>build/types.js</code>.
     * @return content of <code>build/types.js</code> file
     * @throws IOException throw when occurred problem while reading file
     */
    protected String readTypes() throws IOException {
        return readProjectFile("build", "types.js");
    }

    protected void log(String test, String content) {
        System.out.println("["+test+"] Content:");
        System.out.println(content);
    }
}
