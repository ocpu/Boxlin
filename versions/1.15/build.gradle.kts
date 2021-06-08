@file:Suppress("UNCHECKED_CAST")

import com.matthewprenger.cursegradle.CurseExtension
import com.matthewprenger.cursegradle.CurseProject
import org.jetbrains.dokka.gradle.DokkaTask
import groovy.util.Node
import groovy.xml.QName
import net.minecraftforge.gradle.userdev.UserDevExtension
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import kotlin.reflect.KProperty
import java.time.LocalDateTime

// Define useful extension functions
fun CurseExtension.project(block: CurseProject.() -> Unit) = curseProjects.add(CurseProject().apply(block))
operator fun <T : Task> T.invoke(action: Action<T>) = action.execute(this)
operator fun Map<String, Any?>.getValue(thisRef: Any?, property: KProperty<*>) = get(property.name)?.toString()
inline fun <reified T : Any> ExtensionContainer.get(block: T.() -> Unit) = getByType(T::class).block()
val Node.name: String
  get() = when (val res = name()) {
    is QName -> res.qualifiedName
    else -> res as String
  }
val Node.text: String get() = text()
val Node.children: List<Node> get() = children() as List<Node>
fun Node.childrenByName(name: String): List<Node> = children.filter { it.name == name }
fun Node.childByName(name: String): Node? = children.find { it.name == name }
val Node.parent: Node get() = parent()


// Define plugins and plugin dependencies
buildscript {
    repositories {
        maven("https://files.minecraftforge.net/maven")
        mavenCentral()
    }
    dependencies {
        classpath(group="net.minecraftforge.gradle", name="ForgeGradle", version="4.1.+") { isChanging = true }
    }
}

plugins {
  kotlin("jvm") version File("../../VERSION_KOTLIN").readText()
  id("org.jetbrains.dokka") version "0.10.0"
  id("com.matthewprenger.cursegradle") version "1.4.0"
  `maven-publish`
  signing
  eclipse
  java
}

apply(plugin = "net.minecraftforge.gradle")

// Constants
val projectName = "boxlin"
val libraryVersion = file("../../VERSION").readText()
val githubUser by properties
val githubToken by properties
val githubRepo = "Boxlin"
val gitBranch = "v3"
val myRemoteUser by properties
val myRemoteToken by properties
val curseforgeKey by properties
val curseforgeProjectId = "283350"

// Current MC and Forge constants
var minecraftVersion = properties["minecraftVersion"] as String? ?: "1.15.2"
var forgeVersion = properties["forgeVersion"] as String? ?: "31.2.0"
var mappingsChannel = properties["mappingsChannel"] as String? ?: "snapshot"
var mappingsVersion = properties["mappingsVersion"] as String? ?: "20190719-1.14.3"


// Build script start

version = "$libraryVersion-$minecraftVersion"
group = "io.opencubes"

sourceSets["main"].java {
  srcDir("./base/main/java")
  srcDir("./base/main/kotlin")
  srcDir("./src/main/java")
  srcDir("./src/main/kotlin")
}

sourceSets["main"].resources {
  srcDir("./base/main/resources")
  srcDir("./src/main/resources")
}

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

tasks {
  "compileJava"(JavaCompile::class) {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
  }
  "compileKotlin"(KotlinCompile::class) {
    kotlinOptions.jvmTarget = "1.8"
  }
  "compileTestKotlin"(KotlinCompile::class) {
    kotlinOptions.jvmTarget = "1.8"
  }
}

extensions.get<UserDevExtension> {
  mappings(mappingsChannel, mappingsVersion)

  runs {
    create("client") {
      workingDirectory(project.file("run"))
      properties["forge.logging.markers"] = "SCAN,REGISTRIES,REGISTRYDUMP"
      properties["forge.logging.console.level"] = "debug"
      mods {
        create(projectName) {
          source(sourceSets.main.get())
        }
      }
    }
    create("server") {
      workingDirectory(project.file("run"))
      properties["forge.logging.markers"] = "SCAN,REGISTRIES,REGISTRYDUMP"
      properties["forge.logging.console.level"] = "debug"
      mods {
        create(projectName) {
          source(sourceSets.main.get())
        }
      }
    }
    create("data") {
      workingDirectory(project.file("run"))
      properties["forge.logging.markers"] = "SCAN,REGISTRIES,REGISTRYDUMP"
      properties["forge.logging.console.level"] = "debug"
      args("--mod", projectName, "--all", "--output", file("src/generated/resources/"))
      mods {
        create(projectName) {
          source(sourceSets.main.get())
        }
      }
    }
  }
}

repositories {
  maven("https://maven.ocpu.me")
  mavenCentral()
}

dependencies {
  val minecraft by configurations
  val implementation by configurations

  minecraft("net.minecraftforge:forge:$minecraftVersion-$forgeVersion")
  implementation(kotlin("stdlib-jdk8"))
  implementation(kotlin("reflect"))
}

val jar: Jar by tasks
val dokka: DokkaTask by tasks
val javadoc: Javadoc by tasks
val classes: Task by tasks

jar {
  manifest {
    attributes["FMLModType"] = "LANGPROVIDER"
    attributes["Specification-Title"] = projectName
    attributes["Specification-Vendor"] = githubUser
    attributes["Specification-Version"] = "3.1"
    attributes["Implementation-Title"] = projectName
    attributes["Implementation-Vendor"] = githubUser
    attributes["Implementation-Version"] = archiveVersion.get()
    attributes["Implementation-Timestamp"] = LocalDateTime.now()
  }
}

val fullJar by tasks.creating(Jar::class) {
  group = JavaBasePlugin.BUILD_TASK_NAME
  description = "Generate a jar file with dependencies and code"
  archiveClassifier.set("mod")
  manifest = jar.manifest
  afterEvaluate {
    from(
      configurations.compile.get()
        .filter { it.path.contains("org.jetbrains.kotlin") }
        .map { if (it.isDirectory) it else zipTree(it) }
    )
  }
  with(jar)
}

val sourcesJar by tasks.creating(Jar::class) {
  group = JavaBasePlugin.BUILD_TASK_NAME
  description = "Assembles all source code"
  archiveClassifier.set("sources")
  dependsOn(classes)
  from((project.the<SourceSetContainer>()["main"] as SourceSet).allSource)
}

val javadocJar by tasks.creating(Jar::class) {
  group = JavaBasePlugin.DOCUMENTATION_GROUP
  description = "Assembles Kotlin docs with Dokka"
  archiveClassifier.set("javadoc")
  dependsOn(dokka)
  from(javadoc.destinationDir)
}

dokka {
  outputDirectory = javadoc.destinationDir.toString()
}

publishing {
  publications {
    create<MavenPublication>(projectName) {
      from(components["java"] as SoftwareComponent)

      artifact(sourcesJar)
      artifact(javadocJar)

      pom {
        name.set(githubRepo)
        description.set("A language provider for Minecraft Forge. This provider will load Kotlin based mods.")
        url.set("https://github.com/$githubUser/$githubRepo")

        withXml {
          for (dependencies in asNode().childrenByName("dependencies")) {
            for (dependency in dependencies.childrenByName("dependency")) {
              val artifactId = dependency.childByName("artifactId")?.text ?: continue
              if ("forge" in artifactId)
                dependencies.remove(dependency)
            }
          }
        }
        licenses {
          license {
            name.set("MIT License")
            url.set("https://github.com/$githubUser/$githubRepo/blob/$gitBranch/LICENSE")
            distribution.set("repo")
          }
        }
        developers {
          developer {
            id.set("ocpu")
            name.set("Martin Hövre")
            email.set("martin.hovre@opencubes.io")
          }
        }
        scm {
          url.set("https://github.com/$githubUser/$githubRepo")
          connection.set("scm:git:git://github.com/$githubUser/$githubRepo.git")
          developerConnection.set("scm:git:ssh://github.com/$githubUser/$githubRepo.git")
        }
      }
    }
  }
  repositories {
    maven("https://maven.ocpu.me") {
      name = "MyRemote"
      credentials {
        username = myRemoteUser!!
        password = myRemoteToken!!
      }
    }
  }
}

signing {
  useGpgCmd()
  sign(publishing.publications[projectName] as Publication)
}

extensions.get<CurseExtension> {
  project {
    apiKey = curseforgeKey!!
    id = curseforgeProjectId
    changelog = file("changelog.md")
    releaseType = "release"
    changelogType = "markdown"
    addGameVersion(minecraftVersion)
    mainArtifact(fullJar)
  }
}

