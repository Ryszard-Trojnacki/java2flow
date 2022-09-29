package pl.rtprog.java2flow;

import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.tasks.TaskAction;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class GenerateTask extends DefaultTask {
    public String outputFile;
    public List<String> classes;
//    public List<String> classPatterns;

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
            if(classes!=null) {
                for(String cl: classes) {
                    Class<?> c=classLoader.loadClass(cl);
                    if(c==null) {
                        getLogger().warn("Missing class for name: {}", cl);
                        continue;
                    }
                    jf.export(c);

                }
            }

            final File output = outputFile != null
                    ? getProject().file(outputFile)
                    : new File(new File(getProject().getBuildDir(), "types.js"), getProject().getName());
            getLogger().debug("Generating output to: {}", output);
            try(FileOutputStream out=new FileOutputStream(output)) {
                out.write(jf.toString().getBytes(StandardCharsets.UTF_8));
            }
        }
    }

}
