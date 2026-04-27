// Top-level build file where you can add configuration options common to all sub-projects/modules.
import org.gradle.api.artifacts.ProjectDependency

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.kotlin.ksp) apply false
    alias(libs.plugins.google.services) apply false
}

tasks.register("verifyArchitectureDependencies") {
    group = "verification"
    description = "Ensures feature/core modules do not depend on the app module."

    doLast {
        val forbiddenDependencies = subprojects
            .filter { project -> project.path.startsWith(":features:") || project.path.startsWith(":core:") }
            .flatMap { project ->
                listOf("api", "implementation", "compileOnly", "runtimeOnly")
                    .mapNotNull { configurationName -> project.configurations.findByName(configurationName) }
                    .flatMap { configuration ->
                        configuration.dependencies
                            .withType(ProjectDependency::class.java)
                            .filter { dependency -> dependency.path == ":app" }
                            .map { dependency -> "${project.path} -> ${dependency.path}" }
                    }
            }

        check(forbiddenDependencies.isEmpty()) {
            "Feature/core modules must not depend on :app:\n${forbiddenDependencies.joinToString("\n")}"
        }
    }
}

tasks.register("verifyNoHardcodedOpenWeatherSecrets") {
    group = "verification"
    description = "Fails when OpenWeather API keys are committed into source/config files."

    doLast {
        val ignoredDirectories = setOf(".git", ".gradle", ".idea", "build")
        val checkedExtensions = setOf("kt", "kts", "java", "xml", "md", "properties", "json")
        val hardcodedOpenWeatherKey = Regex("(?i)OPENWEATHERMAP_API_KEY[^\\n\\r]*[\"'][a-f0-9]{32}[\"']")

        val matches = rootDir
            .walkTopDown()
            .onEnter { file -> file.name !in ignoredDirectories }
            .filter { file -> file.isFile && file.extension in checkedExtensions }
            .filterNot { file -> file.name == "local.properties" }
            .flatMap { file ->
                file.readLines().mapIndexedNotNull { index, line ->
                    if (hardcodedOpenWeatherKey.containsMatchIn(line)) {
                        "${file.relativeTo(rootDir).path}:${index + 1}"
                    } else {
                        null
                    }
                }
            }
            .toList()

        check(matches.isEmpty()) {
            "Hardcoded OpenWeather API key found:\n${matches.joinToString("\n")}"
        }
    }
}

tasks.register("checkArchitecture") {
    group = "verification"
    description = "Runs architecture fitness functions."
    dependsOn("verifyArchitectureDependencies", "verifyNoHardcodedOpenWeatherSecrets")
}

tasks.matching { it.name == "check" }.configureEach {
    dependsOn("checkArchitecture")
}

subprojects {
    tasks.matching { it.name == "check" }.configureEach {
        dependsOn(rootProject.tasks.named("checkArchitecture"))
    }
}
