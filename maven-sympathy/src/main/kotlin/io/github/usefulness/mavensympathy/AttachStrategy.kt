package io.github.usefulness.mavensympathy

import org.gradle.api.Incubating

public enum class AttachStrategy {
    /**
     * If `maven-publish` is present, the plugin will behave as [ExtractFromMavenPublishComponents],
     * if not it will fall back to [WatchAllResolvableConfigurations] behavior
     *
     * The behavior is subject to change, but the assumption is it should cover most common setups.
     */
    @Incubating
    Default,

    /**
     * The plugin will only watch configurations attached to SoftwareComponents
     *
     * The implementation relies on internal gradle APIs and may break in the future Gradle versions.
     */
    @Incubating
    ExtractFromMavenPublishComponents,

    /**
     * The plugin will check all resolvable configurations for versions mismatch
     */
    WatchAllResolvableConfigurations,
}
