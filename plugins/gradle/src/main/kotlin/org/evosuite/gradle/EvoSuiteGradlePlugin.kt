package org.evosuite.gradle

import org.evosuite.gradle.tasks.DownloadEvosuiteTask
import org.evosuite.gradle.tasks.GenerateEvoSuiteTestTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.SourceSet
import org.gradle.api.tasks.testing.Test
import org.gradle.kotlin.dsl.create
import org.gradle.kotlin.dsl.dependencies
import org.gradle.kotlin.dsl.getByType

class EvoSuiteGradlePlugin : Plugin<Project> {
    override fun apply(target: Project) {
        target.plugins.apply("java")
        val extension = target.extensions.create<EvoSuiteExtension>("evosuite", target)
        val java = target.extensions.getByType<JavaPluginExtension>()
        val downloadTask = target.tasks.create("downloadEvosuite", DownloadEvosuiteTask::class)
        val evosuiteConfiguration = target.configurations.create("evosuite")

        target.dependencies {
            evosuiteConfiguration("junit", "junit", "4.12")
            evosuiteConfiguration("org.evosuite", "evosuite-standalone-runtime", "1.0.6")
        }

        extension.sourceSets.forEach {
            handleSourceSet(target, java, downloadTask, it)
        }
    }

    private fun handleSourceSet(
        project: Project,
        java: JavaPluginExtension,
        downloadTask: DownloadEvosuiteTask,
        sourceSet: SourceSet
    ) {
        val testSourceSet = java.sourceSets.create("${sourceSet.name}ESTest")

        project.configurations.getByName(testSourceSet.implementationConfigurationName).apply {
            // Extends common evosuite configurations
            extendsFrom(project.configurations.getByName("evosuite"))

            // Extends source
            extendsFrom(project.configurations.getByName(sourceSet.runtimeClasspathConfigurationName))
        }

        project.dependencies.add(testSourceSet.implementationConfigurationName, sourceSet.output)

        // Define generate ESTest task
        val generateTask = project.tasks.create<GenerateEvoSuiteTestTask>(
            "generate${sourceSet.name.capitalize()}ESTest",
            GenerateEvoSuiteTestTask::class.java,
            sourceSet,
            testSourceSet,
            downloadTask.evosuiteExecutable
        ).apply {
            dependsOn(sourceSet.compileJavaTaskName)
            dependsOn(downloadTask)
        }

        project.tasks.getByName(testSourceSet.compileJavaTaskName).dependsOn(generateTask)

        project.tasks.create<Test>(
            "${sourceSet.name.decapitalize()}ESTest"
        ){
            dependsOn(generateTask)
            testClassesDirs = testSourceSet.output.classesDirs
            classpath = testSourceSet.runtimeClasspath
        }
    }
}