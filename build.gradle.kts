import org.apache.tools.ant.types.resources.comparators.Date
import org.gradle.internal.os.OperatingSystem
import org.gradle.jvm.tasks.Jar

plugins {
    `java-library`
    `maven-publish`

    id("com.github.ben-manes.versions") version "0.21.0"
    id("com.jfrog.bintray") version "1.8.4"
}

group = "com.github.am4dr.javafx"
version = "0.4.3-SNAPSHOT"

repositories {
    jcenter()
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
    targetCompatibility = JavaVersion.VERSION_11
}

sourceSets {
    register("uiSample")
}

val javafxClassifier = when(OperatingSystem.current()) {
    OperatingSystem.WINDOWS -> "win"
    OperatingSystem.MAC_OS -> "mac"
    OperatingSystem.LINUX -> "linux"
    else -> throw RuntimeException("Mavenレポジトリ上で対応するJavaFXの実装が存在しないOS")
}
dependencies {
    api("org.openjfx:javafx-base:11.0.2")
    api("org.openjfx:javafx-controls:11.0.2")
    api("org.openjfx:javafx-graphics:11.0.2")
    compileOnly("org.openjfx:javafx-base:11.0.2:$javafxClassifier")
    compileOnly("org.openjfx:javafx-controls:11.0.2:$javafxClassifier")
    compileOnly("org.openjfx:javafx-graphics:11.0.2:$javafxClassifier")

    testImplementation("com.google.jimfs:jimfs:1.1")
    testImplementation("org.junit.jupiter:junit-jupiter:5.4.1")

    "uiSampleImplementation"(sourceSets.main.get().compileClasspath)
    "uiSampleImplementation"(sourceSets.main.get().output)
}


tasks {
    test {
        useJUnitPlatform()
    }
    javadoc {
        doFirst {
            val modulePaths = classpath.asPath
            classpath = files()
            // Expose StandardJavadocDocletOptions in Javadocs for better compatibility with Kotlin DSL · Issue #7038 · gradle/gradle
            // https://github.com/gradle/gradle/issues/7038
            options { this as StandardJavadocDocletOptions
                links("https://docs.oracle.com/en/java/javase/11/docs/api")
                locale = "en_US"
                addStringOption("-module-path", modulePaths)
            }
        }
    }
    compileJava {
        options.compilerArgs.addAll(listOf("--module-path", classpath.asPath))
        classpath = files()
    }

    register("runUiSamples", JavaExec::class.java) {
        classpath = sourceSets["uiSample"].runtimeClasspath
        main = "com.github.am4dr.javafx.sample_viewer.ui.Launcher"
        args = listOf("com.github.am4dr.javafx.sample_viewer.example.ControlSamples")
    }

    register("sourceJar", Jar::class.java) {
        dependsOn(classes)
        from(sourceSets.main.get().allSource)
    }
    register("javadocJar", Jar::class.java) {
        dependsOn(javadoc)
        from(javadoc.get().destinationDir)
        doFirst {
            println(javadoc.get().destinationDir)
        }
    }
}


publishing {
    publications {
        register("jar", MavenPublication::class.java) {
            from(components.getByName("java"))
        }
        register("javadoc", MavenPublication::class.java) {
            artifact(tasks["javadocJar"]) {
                classifier = "javadoc"
            }
        }
        register("source", MavenPublication::class.java) {
            artifact(tasks["sourceJar"]) {
                classifier = "sources"
            }
        }
    }
}

// Kotlin DSL support · Issue #258 · bintray/gradle-bintray-plugin
// https://github.com/bintray/gradle-bintray-plugin/issues/258
bintray {
    val bintrayUsername: String by project
    val bintrayApiKey: String by project
    user = bintrayUsername
    key = bintrayApiKey
    with(pkg) {
        repo = "maven"
        name = "javafx-gui-sample-viewer"
        setLicenses("MIT")
        vcsUrl = "https://github.com/am4dr/javafx-gui-sample-viewer.git"
        websiteUrl = "https://github.com/am4dr/javafx-gui-sample-viewer"
        githubRepo = "am4dr/javafx-gui-sample-viewer"
        with(version) {
            name = project.version.toString()
            vcsTag = project.version.toString()
            released  = Date().toString()
        }
    }
    setPublications("jar", "javadoc", "source")
}


val sampleProjects = listOf("colorful-buttons", "colorful-buttons_with-sourceset-separation").map { project(it) }
configure(sampleProjects) {
    apply(plugin = "java-library")

    repositories {
        jcenter()
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_11
    }

    dependencies {
        implementation(project(":"))
        implementation("org.openjfx:javafx-base:11.0.2:$javafxClassifier")
        implementation("org.openjfx:javafx-controls:11.0.2:$javafxClassifier")
        implementation("org.openjfx:javafx-graphics:11.0.2:$javafxClassifier")
    }
}
