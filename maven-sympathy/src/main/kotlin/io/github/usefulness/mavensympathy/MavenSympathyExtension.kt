package io.github.usefulness.mavensympathy

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import javax.inject.Inject

public open class MavenSympathyExtension @Inject constructor(objectFactory: ObjectFactory) {

    /**
     * Defines how the plugin will hook up with the project to listen for version mismatches
     */
    public val attachStrategy: Property<AttachStrategy> = objectFactory.property(AttachStrategy::class.java)
        .apply { set(AttachStrategy.Default) }
}
