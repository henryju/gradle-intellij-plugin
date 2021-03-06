package org.jetbrains.intellij

import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginConvention
import org.gradle.api.publish.ivy.internal.artifact.DefaultIvyArtifact
import org.gradle.api.tasks.SourceSet
import org.gradle.process.JavaForkOptions
import org.jetbrains.annotations.NotNull

class Utils {
    @NotNull
    public static SourceSet mainSourceSet(@NotNull Project project) {
        JavaPluginConvention javaConvention = project.convention.getPlugin(JavaPluginConvention);
        javaConvention.sourceSets.getByName(SourceSet.MAIN_SOURCE_SET_NAME)
    }
    
    @NotNull
    public static SourceSet testSourceSet(@NotNull Project project) {
        JavaPluginConvention javaConvention = project.convention.getPlugin(JavaPluginConvention);
        javaConvention.sourceSets.getByName(SourceSet.TEST_SOURCE_SET_NAME)
    }
    
    @NotNull
    public static DefaultIvyArtifact createDependency(File file, String configuration, File baseDir) {
        def relativePath = baseDir.toURI().relativize(file.toURI()).getPath()
        def artifact = new DefaultIvyArtifact(file, relativePath - ".jar", "jar", "jar", null)
        artifact.conf = configuration
        artifact
    }

    @NotNull
    public static Set<File> sourcePluginXmlFiles(@NotNull Project project) {
        pluginXmlFiles(mainSourceSet(project).resources.srcDirs)
    }

    @NotNull
    public static Set<File> outPluginXmlFiles(@NotNull Project project) {
        pluginXmlFiles(mainSourceSet(project).output.files)
    }

    @NotNull
    private static Set<File> pluginXmlFiles(@NotNull Set<File> roots) {
        Set<File> result = new HashSet<>()
        roots.each {
            def pluginXml = new File(it, "META-INF/plugin.xml")
            if (pluginXml.exists()) result += pluginXml
        }
        result
    }

    @NotNull
    public static Map<String, Object> getIdeaSystemProperties(@NotNull Project project,
                                                              @NotNull Map<String, Object> originalProperties,
                                                              @NotNull IntelliJPluginExtension extension,
                                                              boolean inTests) {
        def properties = new HashMap<String, Object>()
        properties.putAll(originalProperties)
        def suffix = inTests ? "-test" : ""
        properties.put("idea.config.path", "${extension.sandboxDirectory}/config${suffix}")
        properties.put("idea.system.path", "${extension.sandboxDirectory}/system${suffix}")
        properties.put("idea.plugins.path", "${extension.sandboxDirectory}/plugins")
        def pluginId = getPluginId(project)
        if (!properties.containsKey("idea.required.plugins.id") && pluginId != null) {
            properties.put("idea.required.plugins.id", pluginId)
        }
        return properties;
    }

    @NotNull
    public static List<String> getIdeaJvmArgs(@NotNull JavaForkOptions options,
                                              @NotNull List<String> originalArguments,
                                              @NotNull IntelliJPluginExtension extension) {
        if (options.maxHeapSize == null) options.maxHeapSize = "512m"
        if (options.minHeapSize == null) options.minHeapSize = "256m"
        boolean hasPermSizeArg = false
        def result = []
        for (String arg : originalArguments) {
            if (arg.startsWith("-XX:MaxPermSize")) {
                hasPermSizeArg = true
            }
            result += arg
        }

        result += "-Xbootclasspath/a:${extension.ideaDirectory.absolutePath}/lib/boot.jar"
        if (!hasPermSizeArg) result += "-XX:MaxPermSize=250m"
        return result
    }

    static def getPluginId(@NotNull Project project) {
        Set<String> ids = new HashSet<>()
        sourcePluginXmlFiles(project).each {
            def pluginXml = new XmlParser().parse(it)
            ids += pluginXml.id*.text()
        }
        return ids.size() == 1 ? ids.first() : null;
    }
}
