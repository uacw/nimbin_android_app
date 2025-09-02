import io.gitlab.arturbosch.detekt.Detekt

plugins {
    id("io.gitlab.arturbosch.detekt") version "1.23.4"
}

// Detekt configuration
detekt {
    buildUponDefaultConfig = true
    allRules = false
    config.setFrom("$projectDir/config/detekt/detekt.yml")
    baseline = file("$projectDir/config/detekt/baseline.xml")
}

tasks.withType<Detekt>().configureEach {
    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(true)
        sarif.required.set(true)
        md.required.set(true)
    }
}

// Dependency updates checking
tasks.register("dependencyUpdates") {
    doLast {
        println("Checking for dependency updates...")
        exec {
            commandLine("./gradlew", "dependencyUpdates")
        }
    }
}

// Custom task for generating release notes
tasks.register("generateReleaseNotes") {
    doLast {
        val version = project.version
        val releaseNotes = """
            # NimBin v$version Release Notes
            
            ## New Features
            - Enhanced security with biometric authentication
            - Improved performance with advanced caching
            - Better error handling and user feedback
            - Production-ready CI/CD pipeline
            
            ## Bug Fixes
            - Fixed memory leaks in API client
            - Resolved UI state management issues
            - Improved network error handling
            
            ## Security Improvements
            - Added certificate pinning
            - Enhanced data encryption
            - Root detection implementation
            - ProGuard obfuscation optimizations
        """.trimIndent()

        val releaseNotesFile = file("RELEASE_NOTES.md")
        releaseNotesFile.writeText(releaseNotes)
        println("Release notes generated: ${releaseNotesFile.absolutePath}")
    }
}
