package org.evosuite.gradle.tasks

import org.apache.maven.repository.internal.MavenRepositorySystemUtils
import org.eclipse.aether.RepositorySystem
import org.eclipse.aether.artifact.DefaultArtifact
import org.eclipse.aether.collection.CollectRequest
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory
import org.eclipse.aether.graph.Dependency
import org.eclipse.aether.repository.LocalRepository
import org.eclipse.aether.repository.RemoteRepository
import org.eclipse.aether.repository.RepositoryPolicy
import org.eclipse.aether.resolution.DependencyRequest
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory
import org.eclipse.aether.spi.connector.transport.TransporterFactory
import org.eclipse.aether.transfer.AbstractTransferListener
import org.eclipse.aether.transfer.TransferEvent
import org.eclipse.aether.transport.http.HttpTransporterFactory
import org.evosuite.gradle.evosuiteVersion
import org.gradle.api.DefaultTask
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.TaskAction
import java.io.File

open class DownloadEvosuiteTask : DefaultTask() {
    @Internal
    override fun getGroup(): String = "evosuite"

    @get:OutputFile
    val evosuiteExecutable: File = File(project.buildDir, "evosuite-executable/evosuite.jar")

    @TaskAction
    fun downloadEvosuite() {
        val locator = MavenRepositorySystemUtils.newServiceLocator()
        locator.addService(RepositoryConnectorFactory::class.java, BasicRepositoryConnectorFactory::class.java)
        locator.addService(TransporterFactory::class.java, HttpTransporterFactory::class.java)

        val repository = locator.getService(RepositorySystem::class.java)
        val session = MavenRepositorySystemUtils.newSession()
        session.checksumPolicy = RepositoryPolicy.CHECKSUM_POLICY_FAIL
        session.localRepositoryManager =
            repository.newLocalRepositoryManager(session, LocalRepository(File(project.buildDir, "evosuite-resolver")))
        session.transferListener = object : AbstractTransferListener() {
            override fun transferStarted(event: TransferEvent) {
                logger.info("Downloading ${event.resource.resourceName} from ${event.resource.repositoryId}")
            }
        }
        session.setReadOnly()

        val repositories = repository.newResolutionRepositories(
            session,
            listOf(
                RemoteRepository.Builder("central", "default", "https://repo.maven.apache.org/maven2").build()
            )
        )

        val dependencies = listOf(
            Dependency(
                DefaultArtifact("org.evosuite", "evosuite-master", "jar", evosuiteVersion),
                null
            )
        )

        val resolved = repository.resolveDependencies(
            session,
            DependencyRequest(
                CollectRequest(
                    null as Dependency?,
                    dependencies,
                    repositories
                ),
                null
            )
        )

        // We have only one artifact
        val artifact = resolved.artifactResults[0].artifact.file
        evosuiteExecutable.delete()
        artifact.copyTo(evosuiteExecutable)
    }
}