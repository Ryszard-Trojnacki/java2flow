package pl.rtprog.java2flow;

import org.gradle.api.DefaultTask;
import org.gradle.api.Task;
import org.gradle.api.plugins.JavaPluginExtension;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;
import org.gradle.api.tasks.Optional;
import org.gradle.api.tasks.*;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Implementation of Gradle task for generating types.
 *
 * @author Ryszard Trojnacki
 */
public abstract class Java2FlowTask extends DefaultTask {
    /**
     * Configuration for package names or class names for which
     * there should be generated Flow file.
     * @return list of classes for which should be generated output
     */
    @Input @Optional
    abstract public ListProperty<String> getClasses();

    /**
     * Should empty file be generated?
     * @return true if empty file should be also generated
     */
    @Input @Optional
    abstract public Property<Boolean> getGenerateEmpty();

    /**
     * JavaScript Flow output file
     * @return output filename
     */
    @OutputFile @Optional
    abstract public Property<String> getOutput();

    /**
     * Packaged to scan for classes and generate Flow types.
     * @return list of packages for generate flow types
     * @see Java2FlowExtension#getPackages()
     */
    @Input @Optional
    abstract public ListProperty<String> getPackages();

    /**
     * Should Flow types be generated
     * @return true if Flow types should be generated
     */
    @Input @Optional
    abstract public Property<Boolean> getGenerateFlow();

    /**
     * Should JSDoc types be generated
     * @return true if JSDoc types should be generated
     */
    @Input @Optional
    abstract public Property<Boolean> getGenerateJSDoc();


    private List<URL> getFilesFromConfiguration(String configuration) {
        try {
            final List<URL> urls = new ArrayList<>();
            for (File file : getProject().getConfigurations().getByName(configuration).getFiles()) {
                urls.add(file.toURI().toURL());
            }
            return urls;
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }

    private static String packageToPath(String packageName) {
        return packageName.replace('.', File.separatorChar);
    }

    private static String fixInnerClassName(String cn) {
        int digits=0;
        while (digits<cn.length() && Character.isDigit(cn.charAt(digits))) ++digits;
        return cn.substring(digits);
    }

    private static List<String> listClasses(String packageName, File dir) {
        File[] classes=dir.listFiles(pathname -> pathname.getName().endsWith(".class") || pathname.isDirectory());
        if(classes==null || classes.length==0) return Collections.emptyList();
        if(!packageName.isEmpty()) packageName+=".";

        List<String> res=new ArrayList<>();
        for(File f: classes) {
            if(f.isDirectory()) continue;   // TODO: Subdirectories
            String cn=f.getName();
            cn=cn.substring(0, cn.length()-6);  // remove extension `.class`.
            String[] parts=cn.split("\\$");
            StringBuilder name= new StringBuilder(parts[0]);
            for(int i=1;i<parts.length;++i) {
                String p=fixInnerClassName(parts[i]);
                if(p.isEmpty()) {
                    name = null;    // anonymous class, skip
                    break;
                }
                name.append("$").append(p);
            }
            if(name!=null) res.add(packageName+name);
        }
        return res;
    }

    /**
     * Main method that generates output file with types.
     * @throws Exception exception if something goes wrong
     */
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
//        System.out.println("StartURLs: "+urls);

        urls.addAll(getFilesFromConfiguration("compileClasspath"));

        try (URLClassLoader classLoader = URLClassLoader.newInstance(urls.toArray(new URL[0]), Thread.currentThread().getContextClassLoader())) {
            JavaPluginExtension jpe=getProject().getExtensions().getByType(JavaPluginExtension.class);
            SourceSetContainer ssc=jpe.getSourceSets();
            SourceSet main=ssc.getByName(SourceSet.MAIN_SOURCE_SET_NAME);
            File srcDir=main.getJava().getSourceDirectories().getSingleFile();

            Java2Flow jf=new Java2Flow(
                    new JavadocProcessor(srcDir.toPath()),
                    new ClassAnnotationReader(classLoader),
                    this.getGenerateFlow().getOrElse(true),
                    this.getGenerateJSDoc().getOrElse(false)
            );
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
                for(File f: main.getOutput().getClassesDirs().getFiles()) {
                    for(String pkg: packages) {
                        File dir=new File(f, packageToPath(pkg));
                        for(String cl: listClasses(pkg, dir)) {
//                            System.out.println("Generating types for: "+cl);
                            Class<?> c=classLoader.loadClass(cl);
                            generated=true;
                            jf.export(c);
                        }
                    }
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
