package com.beust.kobalt.plugin.retrolambda

import com.beust.kobalt.Plugins
import com.beust.kobalt.TaskResult
import com.beust.kobalt.api.*
import com.beust.kobalt.api.Kobalt.Companion.context
import com.beust.kobalt.api.annotation.Directive
import com.beust.kobalt.api.annotation.Task
import com.beust.kobalt.internal.JvmCompilerPlugin
import com.beust.kobalt.internal.TaskManager
import com.beust.kobalt.maven.DependencyManager
import com.beust.kobalt.maven.aether.Scope
import com.beust.kobalt.misc.RunCommand
import com.google.inject.Inject
import com.google.inject.Singleton
import java.io.File
import java.nio.charset.Charset

/**
 * Run Retrolambda on the classes right after "compile". This plug-in automatically downloads and uses
 * the most recent retrolambda.jar and it can be configured with the `retrolambda{}` directive.
 */
@Singleton
class RetrolambdaPlugin @Inject constructor(val dependencyManager: DependencyManager,
        val taskContributor : TaskContributor, override var taskManager: TaskManager)
    : ConfigActor<RetrolambdaConfig>(), IClasspathContributor, ITaskContributor, IPlugin {
    override val name = PLUGIN_NAME

    companion object {
        const val PLUGIN_NAME = "Retrolambda"
        // Note the use of the versionless id here (no version number specified, ends with ":") so that
        // we will always be using the latest version of the Retrolambda jar
        const val ID = "net.orfjackal.retrolambda:retrolambda:"
        val JAR = DependencyManager.create(ID)
    }

    override fun apply(project: Project, context: KobaltContext) {
        super.apply(project, context)
        taskContributor.addVariantTasks(this, project, context, "retrolambda", runTask = { taskRetrolambda(project) },
                runBefore = listOf("generateDex"), runAfter = listOf("compile"))
    }

    // IClasspathContributor
    // Only add the Retrolambda jar file if the user specified a `retrolambda{}` directive in their build file
    override fun classpathEntriesFor(project: Project?, context: KobaltContext) =
            if (project != null && configurationFor(project) != null) listOf(JAR)
            else emptyList()

    @Task(name = "retrolambda", description = "Run Retrolambda", runBefore = arrayOf("generateDex"),
            alwaysRunAfter = arrayOf(JvmCompilerPlugin.TASK_COMPILE))
    fun taskRetrolambda(project: Project): TaskResult {
        val config = configurationFor(project)
        val result =
                if (config != null) {
                    val classesDir = project.classesDir(context!!)
                    val projects = project.projectProperties.get("dependentProjects") as List<ProjectDescription>
                    val classpath = (dependencyManager.calculateDependencies(project, context!!,
                            scopes = listOf(Scope.COMPILE),
                            passedDependencies = project.compileDependencies)
                            .map {
                                it.jarFile.get()
                            } + classesDir).joinToString("\n")

                    // Use retrolambda.classpathFile instead of retrolambda.classpath to avoid problems
                    // with file path separators on Windows
                    val classpathFile = File.createTempFile("kobalt-", "")
                    classpathFile.writeText(classpath, Charset.defaultCharset())

                    val args = listOf(
                            "-Dretrolambda.inputDir=" + classesDir.replace("\\", "/"),
                            "-Dretrolambda.classpathFile=" + classpathFile,
                            "-Dretrolambda.bytecodeVersion=${config.byteCodeVersion}",
                            "-jar", JAR.jarFile.get().path)

                    val result = RunCommand("java").apply {
                        directory = File(project.directory)
                    }.run(args)
                    TaskResult(result == 0)
                } else {
                    TaskResult()
                }

        return result
    }

    // ITaskContributor
    override fun tasksFor(project: Project, context: KobaltContext) : List<DynamicTask> = taskContributor.dynamicTasks

    // IPlugin
    override fun accept(project: Project): Boolean {
        // Should return true only for Android projects
        return true
    }

    // IPlugin
    override fun shutdown() {
    }


}

class RetrolambdaConfig(var byteCodeVersion: Int = 50) {
}

@Directive
fun Project.retrolambda(init: RetrolambdaConfig.() -> Unit) = let {
    RetrolambdaConfig().apply {
        init()
        (Plugins.findPlugin(RetrolambdaPlugin.PLUGIN_NAME) as RetrolambdaPlugin).addConfiguration(it, this)
    }
}

//fun main(argv: Array<String>) {
//    // Need to depend on "com.beust:kobalt:0.356" instead of "com.beust:kobalt-plugin-api:0.356"
//    // for this to compile
//    //    com.beust.kobalt.main(argv)
//}
