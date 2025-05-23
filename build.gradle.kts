import org.jetbrains.intellij.platform.gradle.IntelliJPlatformType
import org.jetbrains.intellij.platform.gradle.models.ProductRelease
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    id("java")
    id("org.jetbrains.kotlin.jvm") version "2.1.0"
    id("org.jetbrains.intellij.platform") version "2.6.0"
}

group = "com.tseyler.livetemplates.sharing"
version = "1.2"

repositories {
    mavenCentral()
    intellijPlatform {
        defaultRepositories()
    }
}

dependencies {
    intellijPlatform {
        create("IC", "2025.1")
        pluginVerifier()
    }
}

intellijPlatform {
    pluginConfiguration {
        ideaVersion {
            sinceBuild = "242"
            untilBuild = "252.*"
        }
    }
    pluginVerification {
        ides {
            ProductReleasesValueSource {
                channels = listOf(ProductRelease.Channel.RELEASE)
                types = listOf(IntelliJPlatformType.IntellijIdeaCommunity)
            }
            select {
                sinceBuild = "242"
                untilBuild = "252.*"
            }
        }
    }
}

tasks {
    withType<JavaCompile> {
        sourceCompatibility = "17"
        targetCompatibility = "17"
    }
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        compilerOptions.jvmTarget = JvmTarget.JVM_17
    }
}
