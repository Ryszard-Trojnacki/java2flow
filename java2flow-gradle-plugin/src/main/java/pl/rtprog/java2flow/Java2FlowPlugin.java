package pl.rtprog.java2flow;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;

import java.lang.module.Configuration;

/**
 * Java2Flow Gradle plugin.
 *
 * @author Ryszard Trojnacki
 */
public class Java2FlowPlugin implements Plugin<Project> {
    @Override
    public void apply(Project project) {
        final Java2FlowExtension ext=project.getExtensions().create("java2flow", Java2FlowExtension.class);

        Task task=project.getTasks().create("java2flow", Java2FlowTask.class, t -> {
            t.classes().set(ext.classes());
            t.output().set(ext.output());
        });
//
//        for(Task t: project.getTasks()) {
//            if (t.getName().startsWith("compile")) {
//                task.dependsOn(t.getName());
//                task.getInputs().files(t);
//            }
//        }
    }
}
