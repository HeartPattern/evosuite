package org.evosuite.gradle

import org.gradle.api.Project
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.SourceSet
import org.gradle.kotlin.dsl.get
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.listProperty
import org.gradle.kotlin.dsl.property

abstract class EvoSuiteExtension(val project: Project) {
    private val javaPluginExtension = project.extensions.getByType<JavaPluginExtension>()
    private val versionProperty: Property<String> = project.objects.property<String>().convention("1.0.6")
    private val sourceSetsProperty: ListProperty<SourceSet> =
        project.objects.listProperty<SourceSet>().convention(listOf(javaPluginExtension.sourceSets["main"]))


    var version: String
        get() = versionProperty.get()
        set(value) {
            versionProperty.set(value)
        }

    var sourceSets: List<SourceSet>
        get() = sourceSetsProperty.get()
        set(value) {
            sourceSetsProperty.set(value)
        }

    fun sourceSets(vararg sourceSet: SourceSet) {
        sourceSetsProperty.addAll(*sourceSet)
    }

    fun sourceSets(vararg sourceSet: String) {
        sourceSetsProperty.addAll(sourceSet.map(javaPluginExtension.sourceSets::getByName))
    }
}