package org.jetbrains.intellij

import org.gradle.api.DefaultTask
import org.gradle.api.tasks.TaskAction

import java.util.regex.Pattern

class PatchPluginXmlTask extends DefaultTask {
    public static final String NAME = "patchPluginXml"
    private static final Pattern VERSION_PATTERN = Pattern.compile('^[A-Z]{2}-([0-9.A-z]+)\\s*$')

    PatchPluginXmlTask() {
        name = NAME
        description = "Set plugin version, since-build and until-build values in plugin.xml."
        group = IntelliJPlugin.GROUP_NAME
    }

    @SuppressWarnings("GroovyUnusedDeclaration")
    @TaskAction
    public def patchPluginXml() {
        def (since, until) = sinceUntilBuild
        Utils.outPluginXmlFiles(project).each { file ->
            def pluginXml = new XmlParser().parse(file)
            def version = pluginXml.version
            if (version.isEmpty()) {
                pluginXml.appendNode("version", project.version)
            } else {
                version*.value = project.version
            }

            if (since != null && until != null) {
                def ideaVersionTag = pluginXml.'idea-version'
                if (ideaVersionTag.isEmpty()) {
                    pluginXml.appendNode("idea-version", ['since-build': since, 'until-build': until])
                }
                else {
                    ideaVersionTag*.'@since-build' = since
                    ideaVersionTag*.'@until-build' = until
                }
            }

            def printer = new XmlNodePrinter(new PrintWriter(new FileWriter(file)))
            printer.preserveWhitespace = true
            printer.print(pluginXml)
        }
    }

    def getSinceUntilBuild() {
        def extension = project.extensions.findByName(IntelliJPlugin.EXTENSION_NAME) as IntelliJPluginExtension
        if (extension != null && extension.updateSinceUntilBuild) {
            try {
                def matcher = VERSION_PATTERN.matcher(new File(extension.ideaDirectory, "build.txt").getText('UTF-8'))
                if (matcher.find()) {
                    def since = matcher.group(1)
                    def dotPosition = since.indexOf('.')
                    if (dotPosition > 0) {
                        return new Tuple(since, since.substring(0, dotPosition) + ".9999")
                    }
                }
            } catch (IOException e) {
                IntelliJPlugin.LOG.warn("Cannot read build.txt file", e)
            }
        }
        return new Tuple(null, null)
    }
}
