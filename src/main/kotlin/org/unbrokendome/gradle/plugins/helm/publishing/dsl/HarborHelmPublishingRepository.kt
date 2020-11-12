package org.unbrokendome.gradle.plugins.helm.publishing.dsl

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.unbrokendome.gradle.plugins.helm.dsl.credentials.SerializableCredentials
import org.unbrokendome.gradle.plugins.helm.dsl.credentials.toSerializable
import org.unbrokendome.gradle.plugins.helm.publishing.publishers.AbstractHttpHelmChartPublisher
import org.unbrokendome.gradle.plugins.helm.publishing.publishers.HelmChartPublisher
import org.unbrokendome.gradle.plugins.helm.publishing.publishers.PublisherParams
import org.unbrokendome.gradle.plugins.helm.util.property
import java.net.URI
import javax.inject.Inject


interface HarborHelmPublishingRepository : HelmPublishingRepository {

    /**
     * The name of the Harbor project.
     */
    val projectName: Property<String>
}


private open class DefaultHarborHelmPublishingRepository
@Inject constructor(
    name: String,
    objects: ObjectFactory
) : AbstractHelmPublishingRepository(objects, name), HarborHelmPublishingRepository {


    override val projectName: Property<String> =
        objects.property<String>()
            .convention("library")


    override val publisherParams: PublisherParams
        get() = HarborPublisherParams(
            url = requireNotNull(url.orNull) { "url is required for Harbor publishing repository" },
            projectName = requireNotNull(projectName.orNull) { "projectName is required for Harbor publishing repository" },
            credentials = configuredCredentials.orNull?.toSerializable()
        )


    private class HarborPublisherParams(
        private val url: URI,
        private val projectName: String,
        private val credentials: SerializableCredentials?
    ) : PublisherParams {

        override fun createPublisher(): HelmChartPublisher =
            HarborPublisher(url, projectName, credentials)
    }


    private class HarborPublisher(
        url: URI,
        private val projectName: String,
        credentials: SerializableCredentials?
    ) : AbstractHttpHelmChartPublisher(url, credentials) {

        override val uploadMethod: String
            get() = "POST"

        override fun uploadPath(chartName: String, chartVersion: String): String =
            "/api/chartrepo/$projectName/charts"
    }
}


internal fun ObjectFactory.newHarborHelmPublishingRepository(name: String): HarborHelmPublishingRepository =
    newInstance(DefaultHarborHelmPublishingRepository::class.java, name)