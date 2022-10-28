package pl.rtprog.java2flow;

import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.testfixtures.ProjectBuilder;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertNotNull;

public class Java2flowPluginTest {
    @Test
    void pluginRegistersATask() {
        Project project= ProjectBuilder.builder().build();
        project.getPlugins().apply("pl.rtprog.java2flow");
        Task task= project.getTasks().findByName("java2flow");
        assertNotNull(task);
    }
}
