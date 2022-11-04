Library to generate [Flow](https://flow.org/) types from Java classes.

This library is for converting JavaBeans (data structures) to Flow types.
It can be useful when developing software with Java backend and JavaScript (Flow) frontend.

Library uses [Jackson](https://github.com/FasterXML/jackson) to processing JavaBeans, 
[JavaParser](https://github.com/javaparser/javaparser) for reading JavaDoc from source classes,
[ASM](https://asm.ow2.io/) for reading class retention annotations.


# Example conversion
Java code:
```java
/**
 * Dane o użytkowniku systemu.
 */
public class UserInfo {
    @NotNull
    public String id;
    @NotNull
    public String email;
    public String firstname;
    public String lastname;
}

/**
 * Klasa ze szczegółowymi informacjami o użytkowniku.
 */
public class UserData extends UserInfo {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String orgId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String org;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public String orgNip;
}
```
Flow types:
```flow js
/**
 * Dane o użytkowniku systemu.
 **/
export type UserInfo = {
	id: string;
	email: string;
	firstname: string|null;
	lastname: string|null;
};

/**
 * Klasa ze szczegółowymi informacjami o użytkowniku.
 **/
export type UserData = UserInfo & {
    orgId?: string|null;
    org?: string|null;
    orgNip?: string|null;
};
```

# Installation
For now this library is in alpha stage, and it isn't published therefor for use it needs to be downloaded
and installed locally to maven.

```shell
./gradlew publishToMavenLocal
```

# Gradle plugin usage (recommended)
Library can be used to generate Flow types from Gradle. First (because library is not published) it Maven local repository
needs to be added to plugin repositories (`settings.gradle`):
```groovy
pluginManagement {
    repositories {
        mavenLocal()
        gradlePluginPortal()
    }
}
```
and now in `build.gradle`:
```groovy
plugins {
    id 'java'
    id 'pl.rtprog.java2flow' version '0.0.2-SNAPSHOT'
}

java2flow {
    output = 'api.js'
    // provide classes to be transformed by name
    classes = [ 'pl.rtprog.pl.rtprog.java2flow.TestClass' ]
    // convert all classes is given packages
    packages = [ 'pl.rtprog.pl.rtprog.package2' ]  // 

}
```
Gradle plugin options:
- `output` - generated Flow types file name; default `${buildDir}/types.js`,
- `classes` - array of class names to be converted to Flow types,
- `packages` - array of packages to convert classes that are in them to Flow types.


# Runtime usage
Library can be used in runtime to generate Flow types. To do so it needs to be added to dependencies (`build.gradle`):
```groovy
repositories {
    mavenCentral()
    mavenLocal()    // this is needed
}

dependencies {
    implementation group: 'pl.rtprog', name: 'java2flow-core', version: '0.0.2-SNAPSHOT'
}
```
and then called from code:
```java
Java2Flow c=new Java2Flow();
c.addHeader();
c.export(ClassToExport.class);
return c.toString();
```
