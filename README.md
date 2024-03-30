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

#### Advanced configuration
<details open>
<summary>Groovy</summary>

```groovy
tasks.named("sympathyForMrMaven") {
    behaviorOnFailure = BehaviorOnFailure.Fail
}
```
</details>

<details>
<summary>Kotlin</summary>

```kotlin
tasks.named<io.github.usefulness.mavensympathy.SympathyForMrMavenTask>("sympathyForMrMaven") {
    behaviorOnFailure = BehaviorOnFailure.Fail
}
```
</details>

`behaviorOnFailure` - one of `Fail` (prints error logs + fails the build) or `Warn` (only prints error logs)  
