Library to generate [Flow](https://flow.org/) types from Java classes.

This library is for converting JavaBeans (data structures) to Flow types.
It can be useful when developing software with Java backend and JavaScript (Flow) frontend.

Library uses [Jackson](https://github.com/FasterXML/jackson) to processing JavaBeans.

# Installation
For now this library is in alpha stage, and it isn't published therefor for use it needs to be downloaded
and installed locally to maven.

```shell
./gradlew publishToMavenLocal
```

# Runtime usage
Library can be used in runtime to generate Flow types. To do so it needs to be added to dependencies (`build.gradle`):
```groovy
dependencies {
    implementation group: 'pl.rtprog', name: 'java2flow-core', version: '0.0.1-SNAPSHOT'
}
```
and then called from code:
```java
Java2Flow c=new Java2Flow();
c.addHeader();
c.export(ClassToExport.class);
return c.toString();
```

# Gradle plugin usage
Library can be used to generate Flow types from Gradle:
```groovy
plugins {
    id 'pl.rtprog.java2flow' version '0.0.1-SNAPSHOT'
}

java2flow {
    output = 'api.js'
    classes = [ 'pl.rtprog.pl.rtprog.java2flow.TestClass' ]
}
```
Gradle plugin options:
- `output` - generated Flow types file name; default `${buildDir}/types.js`,
- `classes` - array of class names to be converted to Flow types.
