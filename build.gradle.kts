@file:Suppress("UNCHECKED_CAST")

import com.matthewprenger.cursegradle.CurseExtension
import com.matthewprenger.cursegradle.CurseProject
import com.jfrog.bintray.gradle.BintrayExtension
import groovy.util.Node
import groovy.xml.QName
import net.minecraftforge.gradle.userdev.UserDevExtension
import java.time.LocalDateTime

inline fun <reified T : Any> ExtensionContainer.get(block: T.() -> Unit) = getByType(T::class).block()
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
    maven { setUrl("https://files.minecraftforge.net/maven") }
  }
  dependencies {
    classpath(group = "net.minecraftforge.gradle", name = "ForgeGradle", version = "3.+") { isChanging = true }
  }
}

apply {
  plugin("net.minecraftforge.gradle")
  plugin("eclipse")
}

plugins {
  kotlin("jvm") version "1.3.41"
  `maven-publish`
  signing
  id("org.jetbrains.dokka") version "0.10.0"
  id("com.jfrog.bintray") version "1.8.4"
  id("com.matthewprenger.cursegradle") version "1.4.0"
}

version = "3.0.1"
group = "io.opencubes"
base.archivesBaseName = "boxlin"

java {
  sourceCompatibility = JavaVersion.VERSION_1_8
  targetCompatibility = JavaVersion.VERSION_1_8
}

tasks {
  compileJava {
    sourceCompatibility = "1.8"
    targetCompatibility = "1.8"
  }
  compileKotlin {
    kotlinOptions.jvmTarget = "1.8"
  }
  compileTestKotlin {
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
      property("forge.logging.markers", "SCAN,REGISTRIES,REGISTRYDUMP")
      property("forge.logging.console.level", "debug")
      args("--mod", "examplemod", "--all", "--output", file("src/generated/resources/"))
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
  "minecraft"("net.minecraftforge:forge:1.14.4-28.0.49")
  compile(kotlin("stdlib-jdk8"))
  compile(kotlin("reflect"))
}

tasks.jar {
  manifest {
    attributes["FMLModType"] = "LANGPROVIDER"
    attributes["Specification-Title"] = project.name
    attributes["Specification-Vendor"] = "ocpu"
    attributes["Specification-Version"] = "3"
    attributes["Implementation-Title"] = project.name
    attributes["Implementation-Vendor"] = "ocpu"
    attributes["Implementation-Version"] = archiveVersion.get()
    attributes["Implementation-Timestamp"] = LocalDateTime.now()
  }
}

val fullJar by tasks.creating(Jar::class) {
  group = JavaBasePlugin.BUILD_TASK_NAME
  description = "Generate a jar file with dependencies and code"
  archiveClassifier.set("mod")
  manifest = tasks.jar.get().manifest
  afterEvaluate {
    from(
      configurations.compile.get()
        .filter { it.path.contains("org.jetbrains.kotlin") }
        .map { if (it.isDirectory) it else zipTree(it) }
    )
  }
  with(tasks["jar"] as CopySpec)
}

val sourcesJar by tasks.creating(Jar::class) {
  group = JavaBasePlugin.BUILD_TASK_NAME
  description = "Assembles all source code"
  archiveClassifier.set("sources")
  dependsOn(tasks.classes)
  from(sourceSets["main"].allSource)
}

val javadocJar by tasks.creating(Jar::class) {
  group = JavaBasePlugin.DOCUMENTATION_GROUP
  description = "Assembles Kotlin docs with Dokka"
  archiveClassifier.set("javadoc")
  from(tasks.dokka)
}

publishing {
  publications {
    create<MavenPublication>(project.name) {
      from(components["java"])

      artifact(sourcesJar)
      artifact(javadocJar)

      pom {
        withXml {
          val root = asNode()
          root.appendNode("name", "Boxlin")
          root.appendNode("description", "A language provider for Minecraft Forge. This provider will load Kotlin based mods.")
          root.appendNode("url", "https://github.com/ocpu/Boxlin")
          for (dependencies in root.childrenByName("dependencies")) {
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
            url.set("http://www.opensource.org/licenses/mit-license.php")
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
          url.set("https://github.com/ocpu/Boxlin")
          connection.set("scm:git:git://github.com/ocpu/Boxlin.git")
          developerConnection.set("scm:git:ssh://github.com/ocpu/Boxlin.git")
        }
      }
    }
  }
  repositories {
    maven {
      name = "GitHub"
      setUrl("https://maven.pkg.github.com/${properties["githubUser"]}/${project.name}")
      credentials {
        username = properties["githubUser"] as String
        password = properties["githubToken"] as String
      }
    }
  }
}

signing {
  useGpgCmd()
  sign(publishing.publications["boxlin"])
}

bintray {
  user = properties["bintrayUser"] as String
  key = properties["bintrayKey"] as String
  setPublications("boxlin")
  pkg {
    repo = "minecraft"
    name = "Boxlin"
    websiteUrl = "https://github.com/ocpu/Boxlin"
    vcsUrl = "https://github.com/ocpu/Boxlin.git"
    setLicenses("MIT")
    version {
      name = project.version as String
      vcsTag = project.version as String
    }
  }
}

extensions.get<CurseExtension> {
  project {
    apiKey = properties["curseforgeKey"] as String
    id = "283350"
    changelog = file("changelog.md")
    releaseType = "release"
    changelogType = "markdown"
    mainArtifact(fullJar)
  }
}
