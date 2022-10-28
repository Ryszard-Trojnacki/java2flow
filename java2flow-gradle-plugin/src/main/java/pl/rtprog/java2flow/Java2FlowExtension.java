package pl.rtprog.java2flow;

import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

import javax.inject.Inject;
import java.io.File;

/**
 * Gradle plugin extension.
 *
 * @author Ryszard Trojnacki
 */
public interface Java2FlowExtension {
    /**
     * Configuration for package names or class names for which
     * there should be generated Flow file.
     */
    ListProperty<String> getClasses();
    /**
     * JavaScript Flow output file
     */
    Property<String> getOutput();

    /**
     * Packaged to scan for classes and generate Flow types.
     */
    ListProperty<String> getPackages();

    /**
     * Should empty file be generated?
     */
    Property<Boolean> getGenerateEmpty();
}
