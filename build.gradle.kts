@file:Suppress("UNCHECKED_CAST")

import com.matthewprenger.cursegradle.CurseExtension
import com.matthewprenger.cursegradle.CurseProject
import com.jfrog.bintray.gradle.BintrayExtension
import groovy.util.Node
import groovy.xml.QName
import net.minecraftforge.gradle.userdev.UserDevExtension
import org.jetbrains.dokka.gradle.DokkaTask
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import java.time.LocalDateTime
import kotlin.reflect.KProperty

inline fun <reified T : Any> ExtensionContainer.get(block: T.() -> Unit) = getByType(T::class).block()
operator fun <T : Task> T.invoke(action: Action<T>) = action.execute(this)
operator fun Map<String, Any?>.getValue(thisRef: Any?, property: KProperty<*>) = get(property.name)?.toString()
fun BintrayExtension.pkg(block: BintrayExtension.PackageConfig.() -> Unit) = pkg.block()
fun BintrayExtension.PackageConfig.version(block: BintrayExtension.VersionConfig.() -> Unit) = version.block()
fun CurseExtension.project(block: CurseProject.() -> Unit) = curseProjects.add(CurseProject().apply(block))
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

buildscript {
  repositories {
    maven("https://files.minecraftforge.net/maven")
  }
  dependencies {
    classpath(group = "net.minecraftforge.gradle", name = "ForgeGradle", version = "3.+") { isChanging = true }
  }
}

apply(plugin = "net.minecraftforge.gradle")

plugins {
  kotlin("jvm") version "1.3.41"
  id("org.jetbrains.dokka") version "0.10.0"
  id("com.jfrog.bintray") version "1.8.4"
  id("com.matthewprenger.cursegradle") version "1.4.0"
  `maven-publish`
  signing
  eclipse
}

version = "3.0.1"
group = "io.opencubes"

val githubUser by properties
val githubToken by properties
val githubRepo = "Boxlin"
val gitBranch = "v3"
val bintrayUser by properties
val bintrayToken by properties
val bintrayRepo = "minecraft"
val bintrayPackage = "Boxlin"
val curseforgeKey by properties

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
  mappings("snapshot", "20190719-1.14.3")

  runs {
    create("client") {
      workingDirectory(project.file("run"))
      properties["forge.logging.markers"] = "SCAN,REGISTRIES,REGISTRYDUMP"
      properties["forge.logging.console.level"] = "debug"
      mods {
        create(project.name) {
          source(sourceSets.main.get())
        }
      }
    }
    create("server") {
      workingDirectory(project.file("run"))
      properties["forge.logging.markers"] = "SCAN,REGISTRIES,REGISTRYDUMP"
      properties["forge.logging.console.level"] = "debug"
      mods {
        create(project.name) {
          source(sourceSets.main.get())
        }
      }
    }
    create("data") {
      workingDirectory(project.file("run"))
      properties["forge.logging.markers"] = "SCAN,REGISTRIES,REGISTRYDUMP"
      properties["forge.logging.console.level"] = "debug"
      args("--mod", project.name, "--all", "--output", file("src/generated/resources/"))
      mods {
        create(project.name) {
          source(sourceSets.main.get())
        }
      }
    }
  }
}

repositories {
  jcenter()
}

dependencies {
  val minecraft by configurations
  val compile by configurations

  minecraft("net.minecraftforge:forge:1.14.4-28.0.49")
  compile(kotlin("stdlib-jdk8"))
  compile(kotlin("reflect"))
}

val jar: Jar by tasks
val dokka: DokkaTask by tasks
val javadoc: Javadoc by tasks
val classes: Task by tasks

jar {
  manifest {
    attributes["FMLModType"] = "LANGPROVIDER"
    attributes["Specification-Title"] = project.name
    attributes["Specification-Vendor"] = githubUser
    attributes["Specification-Version"] = "3"
    attributes["Implementation-Title"] = project.name
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
  from(project.the<SourceSetContainer>()["main"].allSource)
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
    create<MavenPublication>(project.name) {
      from(components["java"])

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
    maven("https://maven.pkg.github.com/$githubUser/$githubRepo") {
      name = "GitHub"
      credentials {
        username = githubUser!!
        password = githubToken!!
      }
    }
    maven("https://api.bintray.com/maven/$bintrayUser/$bintrayRepo/$bintrayPackage") {
      name = "Bintray"
      credentials {
        username = bintrayUser!!
        password = bintrayToken!!
      }
    }
  }
}

signing {
  useGpgCmd()
  sign(publishing.publications[project.name])
}

extensions.get<CurseExtension> {
  project {
    apiKey = curseforgeKey!!
    id = "283350"
    changelog = file("changelog.md")
    releaseType = "release"
    changelogType = "markdown"
    mainArtifact(fullJar)
  }
}
