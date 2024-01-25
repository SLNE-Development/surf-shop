import java.util.*

plugins {
    `java-library`
    `maven-publish`

    id("dev.slne.gradle-properties-conventions")
}

publishing {
    publications {
        create<MavenPublication>("mavenJava") {
            from(components["java"])
        }
    }
    
    repositories {
        maven {
            name = "slne-space"
            url = uri(System.getenv("REPOSITORY_URL") ?: "https://packages.slne.dev/maven/p/surf/maven")
            credentials {
                username = System.getenv("JB_SPACE_CLIENT_ID")
                password = System.getenv("JB_SPACE_CLIENT_SECRET")
            }
        }
    }
}

repositories {
    mavenCentral()

    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://repo.codemc.io/repository/maven-snapshots/")
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots")
    maven("https://repo.codemc.io/repository/maven-releases/")
    maven("https://jitpack.io")
    maven("https://packages.slne.dev/maven/p/surf/maven") { name = "space-maven" }
}

group = "dev.slne"

java.sourceCompatibility = JavaVersion.VERSION_17

java {
    withSourcesJar()
}

tasks.withType<ProcessResources> {
    filesMatching(listOf("**/plugin.yml", "**/paper-plugin.yml", "**/velocity-plugin.json")) {
        val properties = Properties()
        properties["\${project.version}"] = project.version
        properties["\${project.name}"] = project.name
        properties["\${project.description}"] = project.description ?: "No description provided."
        properties["\${project.website}"] = project.findProperty("website") ?: "https://slne.dev"

        for (key in properties.keys) {
            filter {
                it.replace(key.toString(), properties[key].toString())
            }
        }
    }
}

tasks {
    compileJava {
        options.encoding = Charsets.UTF_8.name()
        options.compilerArgs.add("-parameters")
    }
    javadoc {
        options.encoding = Charsets.UTF_8.name()
    }
}
