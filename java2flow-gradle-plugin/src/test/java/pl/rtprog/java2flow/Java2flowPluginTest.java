package pl.rtprog.java2flow;

import org.gradle.api.Project;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class Java2flowPluginTest {
    @Test
    void pluginRegistersATask() {
        Project project= ProjectBuilder.builder().build();
        project.getPlugins().apply("pl.rtprog.java2flow");

        assertNotNull(project.getTasks().findByName("java2flow"));
    }
}
