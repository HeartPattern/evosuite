package org.evosuite.gradle.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.tasks.*
import org.gradle.kotlin.dsl.getByType
import org.gradle.work.Incremental
import java.io.File

abstract class EvosuiteTask(
    @get: Internal protected val sourceSet: SourceSet,
    @get: Internal protected val targetSet: SourceSet,
    @get: InputFile protected val evosuiteExecutable: File
) : DefaultTask() {
    @Internal
    override fun getGroup(): String = "evosuite"

    @get: Internal
    protected val java = project.extensions.getByType<JavaPluginExtension>()

    @get: Incremental
    @get: PathSensitive(PathSensitivity.RELATIVE)
    @get:InputDirectory
    val inputDirectory: DirectoryProperty = project.objects.directoryProperty().fileValue(File(sourceSet.output.classesDirs.asPath))

    @get: OutputDirectory
    val outputDirectory: DirectoryProperty = project.objects.directoryProperty().fileValue(File(targetSet.java.sourceDirectories.asPath))
}