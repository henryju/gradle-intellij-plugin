# Overview
This plugin allows you to build plugins for IntelliJ platform using specific IntelliJ SDK and bundled plugins.

The plugin adds extra IntelliJ-specific dependencies, patches compile tasks in order to instrument code with 
nullability assertions and forms classes made with IntelliJ GUI Designer and provides some build steps which might be
helpful while developing plugins for IntelliJ platform.

# Usage

## Gradle >= 2.1

```groovy
plugins {
  id "org.jetbrains.intellij" version "0.0.20"
}
```

## Gradle < 2.1

```groovy
buildscript {
  repositories {
    maven {
      url "https://plugins.gradle.org/m2/"
    }
  }
  dependencies {
    classpath group: 'org.jetbrains', name: 'gradle-intellij-plugin', version: '0.0.20'
  }
}

apply plugin: 'org.jetbrains.intellij'
```

## Configuration

Plugin provides following options to configure target IntelliJ SDK and build archive

- `intellij.version` defines the version of IDEA distribution that should be used as a dependency. 
The option accepts build numbers, version numbers and two meta values `LATEST-EAP-SNAPSHOT`, `LATEST-TRUNK-SNAPSHOT`. 
**Default value**: `LATEST-EAP-SNAPSHOT`

- `intellij.plugins` defines the list of bundled IDEA plugins that should be used as dependencies. 
**Default value:** `<empty>`

- `intellij.pluginName` is used for naming target zip-archive and defines the name of plugin artifact. 
of bundled IDEA plugins that should be used as dependencies.
**Default value:** `${project.name}`

- `intellij.sandboxDirectory` defines path of sandbox directory that is used for running IDEA with developing plugin.
**Default value**: `${project.buildDir}/idea-sandbox`

- `intellij.instrumentCode` defines whether plugin should instrument java classes with nullability assertions. 
Also it might be required for compiling forms created by IntelliJ GUI designer.
**Default value**: `true`

- `intellij.updateSinceUntilBuild` defines whether plugin should patch `plugin.xml` with since and until build values, 
if true then `IntelliJIDEABuildNumber` will be used as a `since` value and `IntelliJIDEABranch.9999` will be used as until value.
**Default value**: `true`

#### Build steps

Plugin introduces following build steps

- `patchPluginXml` sets project version and since/until build numbers in output plugin.xml file
- `prepareSandbox` creates proper structure of plugin and fills sandbox directory with it
- `buildPlugin` assembles plugin and prepares zip archive for deployment
- `runIdea` executes IntelliJ IDEA instance with installed the plugin you're developing 

### build.gradle
```groovy
plugins {
  id "org.jetbrains.intellij" version "0.0.20"
}

apply plugin: 'org.jetbrains.intellij'

intellij {
  version '14.1.4'
  plugins 'coverage'
  pluginName 'MyPlugin' 
}

```

# License
This plugin is available under the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0).
