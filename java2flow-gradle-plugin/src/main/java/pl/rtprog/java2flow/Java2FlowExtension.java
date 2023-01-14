package pl.rtprog.java2flow;

import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Property;

/**
 * Gradle plugin extension.
 *
 * @author Ryszard Trojnacki
 */
public interface Java2FlowExtension {
    /**
     * Configuration for package names or class names for which
     * there should be generated Flow file.
     * @return list of classes for which should be generated output
     */
    ListProperty<String> getClasses();
    /**
     * JavaScript Flow output file
     * @return output filename
     */
    Property<String> getOutput();

    /**
     * Packaged to scan for classes and generate Flow types.
     * @return list of packages for generate flow types
     */
    ListProperty<String> getPackages();

    /**
     * Should empty file be generated?
     * @return boolean true if empty file should be also generated
     */
    Property<Boolean> getGenerateEmpty();

    /**
     * Should Flow types be generated
     * @return boolean true if Flow types should be generated
     */
    Property<Boolean> getFlow();

    /**
     * Should JSDoc types be generated
     * @return boolean true if JSDoc types should be generated
     */
    Property<Boolean> getJsdoc();
}
