package pl.rtprog.java2flow;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.util.Collections;

/**
 * Java2Flow Gradle plugin.
 *
 * @author Ryszard Trojnacki
 */
public class Java2FlowPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        Task generateTask=project.task(Collections.singletonMap(Task.TASK_TYPE, GenerateTask.class), "generateFlow");
        for(Task task: project.getTasks()) {
            if (task.getName().startsWith("compile") && !task.getName().startsWith("compileTest")) {
                generateTask.dependsOn(task.getName());
                generateTask.getInputs().files(task);
            }
        }
    }
}
