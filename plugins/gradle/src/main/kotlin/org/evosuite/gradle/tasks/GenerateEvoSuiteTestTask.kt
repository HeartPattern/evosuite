package org.evosuite.gradle.tasks

import org.evosuite.runtime.util.JavaExecCmdUtil
import org.gradle.api.file.FileType
import org.gradle.api.tasks.*
import org.gradle.work.InputChanges
import org.zeroturnaround.exec.ProcessExecutor
import org.zeroturnaround.exec.stream.slf4j.Slf4jStream
import java.io.File
import javax.inject.Inject

abstract class GenerateEvoSuiteTestTask @Inject constructor(
    sourceSet: SourceSet,
    targetSet: SourceSet,
    evosuiteExecutable: File
) : EvosuiteTask(sourceSet, targetSet, evosuiteExecutable) {
    @get: Input
    val memoryInDB: Int = 4000

    @get: Input
    val numberOfCores: Int = 8

    @get: Input
    @get: Optional
    val cuts: String? = null

    @get: Input
    @get: Optional
    val cutsFile: String? = null

    @get: Input
    val timeInMinutesPerClass: Int = 2

    @get: Input
    val timeInMinutesPerProject: Int = 0

    @get: Input
    val criterion: Collection<Criterion> = listOf(
        Criterion.LINE,
        Criterion.BRANCH,
        Criterion.EXCEPTION,
        Criterion.WEAKMUTATION,
        Criterion.OUTPUT,
        Criterion.METHOD,
        Criterion.METHODNOEXCEPTION,
        Criterion.CBRANCH
    )

    @get: Input
    @get: Optional
    val spawnManagerPort: Int? = null

    @get: Input
    val extraProperty: Map<String, String> = hashMapOf()

    @get: Input
    val extraArgs: List<String> = mutableListOf()

    @get: Input
    val schedule: Schedule = Schedule.BUDGET

    @OptIn(ExperimentalStdlibApi::class)
    @TaskAction
    fun generateEvoSuiteTest(inputChanges: InputChanges) {
        logger.info("Going to generate tests with EvoSuite")
        logger.info("Total memory: ${memoryInDB}mb")
        logger.info("Time per class: $timeInMinutesPerClass minutes")
        logger.info("Number of used cores: $numberOfCores")

        if (cuts != null)
            logger.info("Specified classes under test: $cuts")

//        val target = inputChanges.getFileChanges(inputDirectory)
//            .filter { it.fileType == FileType.FILE }
//            .filter { it.file.extension != "jar" }
//            .joinToString(File.pathSeparator) { it.file.absolutePath }

        val classpath = sourceSet.compileClasspath.map { it.absolutePath }.toSet().joinToString(File.pathSeparator)

        val commandLine = buildList<String> {
            add("java")
            add("-jar")
            add(evosuiteExecutable.absolutePath)
            add("-continuous")
            add("execute")
//            add("-generateSuite")
            add("-target")
            add(inputDirectory.get().asFile.absolutePath)
            add("-Dtest_dir=${outputDirectory.get().asFile.absolutePath}")
            add("-Dcriterion=${criterion.joinToString(":") { it.name }}")
            add("-Dctg_schedule=${schedule.name}")
            add("-Dctg_memory=${memoryInDB}")
            add("-Dctg_cores=${numberOfCores}")
            if (timeInMinutesPerProject != 0) {
                add("-Dctg_time=${timeInMinutesPerProject}")
                add("-Dctg_min_time_per_job=${timeInMinutesPerClass}")
            } else {
                add("-Dctg_time_per_class=${timeInMinutesPerClass}")
            }
            if (cuts != null)
                add("-Dctg_selected_cuts=${cuts}")
            if (cutsFile != null)
                add("-Dctg_selected_cuts_file_location=${cutsFile}")

            extraProperty.forEach { (key, value) ->
                add("-D${key}=${value}")
            }

            extraArgs.forEach { arg ->
                add(arg)
            }

            if(classpath.isNotEmpty()){
                add("-projectCP")
                add(classpath)
            }
        }

        val result = ProcessExecutor()
            .command(commandLine)
            .directory(project.projectDir)
            .redirectOutput(Slf4jStream.of(logger).asInfo())
            .redirectError(Slf4jStream.of(logger).asError())
            .start()
            .future
            .get()

        if(result.exitValue != 0)
            throw Exception("EvoSuite shutdown unexpectedly")

        File(project.projectDir, ".evosuite/best-tests").copyRecursively(outputDirectory.asFile.get())
    }

    enum class Criterion {
        EXCEPTION, DEFUSE, ALLDEFS, BRANCH, CBRANCH, STRONGMUTATION, WEAKMUTATION, MUTATION, STATEMENT, RHO, AMBIGUITY, IBRANCH, READABILITY, ONLYBRANCH, ONLYMUTATION, METHODTRACE, METHOD, METHODNOEXCEPTION, LINE, ONLYLINE, OUTPUT, INPUT, TRYCATCH
    }

    enum class Schedule {
        SIMPLE, BUDGET, SEEDING, BUDGET_AND_SEEDING, HISTORY
    }
}