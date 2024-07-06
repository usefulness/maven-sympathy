# maven-sympathy

[![Build Project](https://github.com/usefulness/maven-sympathy/actions/workflows/default.yml/badge.svg?branch=master&event=push)](https://github.com/usefulness/maven-sympathy/actions/workflows/default.yml)
[![Latest Version](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/io/github/usefulness/maven-sympathy/maven-metadata.xml?label=gradle)](https://plugins.gradle.org/plugin/io.github.usefulness.maven-sympathy)
![Maven Central](https://img.shields.io/maven-central/v/io.github.usefulness/maven-sympathy)

https://jakewharton.com/nonsensical-maven-is-still-a-gradle-problem/


### Usage:
```groovy
plugins {
    id("io.github.usefulness.maven-sympathy") version "{{version}}"
}
```

<details>
<summary>Version Catalog</summary>

```toml
usefulness-maven-sympathy = { id = "io.github.usefulness.maven-sympathy", version = "{{version}}" }
```
</details>

From now on, the `sympathyForMrMaven` will run on every `check` task invocation. 

```
[compileClasspath] dependency org.jetbrains.kotlin:kotlin-stdlib:1.9.22 version changed: 1.9.22 → 1.9.23
[runtimeClasspath] dependency org.jetbrains.kotlin:kotlin-stdlib:1.9.22 version changed: 1.9.22 → 1.9.23
> Task :sympathyForMrMaven FAILED

FAILURE: Build failed with an exception.

* What went wrong:

Execution failed for task ':sympathyForMrMaven'.
> Declared dependencies were upgraded transitively. See task output above. Please update their versions.
```

#### Advanced configuration

###### Customize plugin behavior

Configurable via `io.github.usefulness.mavensympathy.MavenSympathyExtension` extension.

<details open>
<summary>Groovy</summary>

```groovy
mavenSympathy {
    attachStrategy = io.github.usefulness.mavensympathy.AttachStrategy.Default
}
```
</details>

<details>
<summary>Kotlin</summary>

```kotlin
mavenSympathy {
    attachStrategy = io.github.usefulness.mavensympathy.AttachStrategy.Default
}
```
</details>

- `attachStrategy` - Defines how the plugin will hook up with the project to listen for version mismatches. Has to be one of:  
   - `WatchAllResolvableConfigurations` - the plugin will check all resolvable configurations for versions mismatch 
   - `ExtractFromMavenPublishComponents` - the plugin will only watch configurations attached to SoftwareComponents  
     The implementation relies on internal gradle APIs and may break in the future Gradle versions.
   - `Default` - if `maven-publish` is present, the plugin will behave as `ExtractFromMavenPublishComponents`, if not it will fall back to `WatchAllResolvableConfigurations` behavior.   
      The behavior is subject to change, but the assumption is it should cover most common setups. 


###### Customize task behavior
<details open>
<summary>Groovy</summary>

```groovy
tasks.named("sympathyForMrMaven") {
    behaviorOnMismatch = BehaviorOnMismatch.Fail
}
```
</details>

<details>
<summary>Kotlin</summary>

```kotlin
tasks.named<io.github.usefulness.mavensympathy.SympathyForMrMavenTask>("sympathyForMrMaven") {
    behaviorOnMismatch = BehaviorOnMismatch.Fail
}
```
</details>

`behaviorOnMismatch` - one of `Fail` (prints error logs + fails the build) or `Warn` (only prints error logs)
