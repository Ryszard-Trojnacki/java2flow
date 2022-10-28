package pl.rtprog.java2flow;

import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Input;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.OutputFile;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public abstract class Java2FlowTask extends DefaultTask {
    @Input @Optional
    abstract public ListProperty<String> getClasses();
    @Input @Optional
    abstract public Property<Boolean> getGenerateEmpty();
    @OutputFile @Optional
    abstract public Property<String> getOutput();
    @Input @Optional
    abstract public ListProperty<String> getPackages();


    private List<URL> getFilesFromConfiguration(String configuration) {
        try {
            final List<URL> urls = new ArrayList<>();
            for (File file : getProject().getConfigurations().getAt(configuration).getFiles()) {
                urls.add(file.toURI().toURL());
            }
            return urls;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    @TaskAction
    public void generate() throws Exception {
        final Set<URL> urls = new LinkedHashSet<>();
        for (Task task : getProject().getTasks()) {
            if (task.getName().startsWith("compile") && !task.getName().startsWith("compileTest")) {
                for (File file : task.getOutputs().getFiles()) {
                    urls.add(file.toURI().toURL());
                }
            }
        }
        urls.addAll(getFilesFromConfiguration("compileClasspath"));

        try (URLClassLoader classLoader = URLClassLoader.newInstance(urls.toArray(new URL[0]), Thread.currentThread().getContextClassLoader())) {
            Java2Flow jf=new Java2Flow();
            jf.addHeader();
            List<String> classes=this.getClasses().getOrNull();
            boolean generated=false;
            if(classes!=null) {
                for(String cl: classes) {
                    Class<?> c=classLoader.loadClass(cl);
                    if(c==null) {
                        getLogger().warn("Missing class for name: {}", cl);
                        continue;
                    }
                    generated=true;
                    jf.export(c);

                }
            }
            List<String> packages=this.getPackages().getOrNull();
            if(packages!=null) {
                for(String pkg: packages) {
                    // TODO: Scan packages for Java classes.
                }
            }

            if(generated || this.getGenerateEmpty().getOrElse(Boolean.FALSE)) {
                File output;
                if(this.getOutput().isPresent()) {
                    output=new File(this.getOutput().get());
                    if(output.getParentFile()==null) output=new File(getProject().getProjectDir(), this.getOutput().get());
                }
                else output = new File(getProject().getBuildDir(), "types.js");

                output.getParentFile().mkdirs();
                getLogger().debug("Generating output to: {}", output);
                try (FileOutputStream out = new FileOutputStream(output)) {
                    out.write(jf.toString().getBytes(StandardCharsets.UTF_8));
                }
            }
        }
    }

}
