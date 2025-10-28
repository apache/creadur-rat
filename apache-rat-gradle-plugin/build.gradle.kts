/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

import groovy.util.Node
import groovy.util.NodeList
import groovy.xml.XmlParser
import org.jetbrains.gradle.ext.copyright
import org.jetbrains.gradle.ext.encodings
import org.jetbrains.gradle.ext.settings

plugins {
  idea
  eclipse
  id("com.diffplug.spotless") version "8.0.0"
  id("org.jetbrains.gradle.plugin.idea-ext") version "1.3"

  `java-gradle-plugin`
  `jvm-test-suite`
}

// ////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Maven integration helper functions

fun xmlFirstChild(node: Node, element: String): Node = (node.get(element) as NodeList)[0] as Node

val rootPom: Node = XmlParser().parse(file("../pom.xml"))
val projectPom: Node = XmlParser().parse(file("pom.xml"))

val rootPomDependencyManagement = xmlFirstChild(rootPom, "dependencyManagement")
val rootPomDependencies = xmlFirstChild(rootPomDependencyManagement, "dependencies")

/** Get a `<dependency>` node by groupId and artifactId. */
fun pomDependency(pomDependencies: Node, groupId: String, artifactId: String): Node =
  pomDependencies
    .children()
    .map { it as Node }
    .first { dependency ->
      xmlFirstChild(dependency, "groupId").text() == groupId
      xmlFirstChild(dependency, "artifactId").text() == artifactId
    }

/** Get the managed dependency version from the root-pom by groupId and artifactId. */
fun rootPomDependencyVersion(groupId: String, artifactId: String): String =
  if (groupId == project.group.toString()) project.version.toString()
  else xmlFirstChild(pomDependency(rootPomDependencies, groupId, artifactId), "version").text()

/** Get the managed dependency as a GAV string from the root-pom by groupId and artifactId. */
fun rootPomDependency(groupId: String, artifactId: String): String =
  "$groupId:$artifactId:${rootPomDependencyVersion(groupId, artifactId)}"

// ////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Plugin, dependencies, et al

dependencies {
  // Gradle plugin production dependencies.
  // Declared in the `pom.xml` file to a have a single source of truth.
  xmlFirstChild(projectPom, "dependencies")
    .children()
    .map { it as Node }
    .forEach { dependency ->
      val groupId = xmlFirstChild(dependency, "groupId").text()
      val artifactId = xmlFirstChild(dependency, "artifactId").text()
      val version = rootPomDependencyVersion(groupId, artifactId)
      implementation("$groupId:$artifactId:$version")
    }
}

gradlePlugin {
  plugins {
    register("ratPlugin") {
      id = project.group.toString()
      implementationClass = "org.apache.rat.gradle.RatPlugin"
      // The following information is only relevant for the Gradle Plugin portal
      displayName = "Apache RAT Gradle Plugin"
      description = "Apache Release Audit Tool Gradle Plugin"
      tags.addAll("apache", "release", "audit", "RAT")
    }
  }
}

// Build the javadoc and sources jars when assembling (assemble / build tasks).
java {
  withJavadocJar()
  withSourcesJar()
}

// ////////////////////////////////////////////////////////////////////////////////////////////////////////////
// RAT options code generation

val generateGradleOptions by configurations.creating

dependencies {
  // Need apache-rat-tools jar to execute the contained `GradleGenerator` class.
  generateGradleOptions("org.apache.rat:apache-rat-tools:$version")
}

val generatedOptionsDir = project.layout.buildDirectory.dir("generated/sources/rat")

sourceSets { main { java { srcDir(generatedOptionsDir) } } }

val generateGradleOptionsSource by
  tasks.registering(JavaExec::class) {
    classpath(generateGradleOptions)
    mainClass = "org.apache.rat.tools.GradleGenerator"
    args =
      listOf(
        "org.apache.rat.gradle",
        "RatOptions",
        generatedOptionsDir.get().asFile.relativeTo(project.projectDir).toString(),
      )
    inputs.file("../apache-rat-tools/src/main/resources/GradleConfiguration.tpl")
    inputs.file("../apache-rat-tools/src/main/resources/GradleOptions.tpl")
    inputs.file("../apache-rat-tools/src/main/resources/GradleTaskBase.tpl")
    outputs.cacheIf { true }
    outputs.dir(generatedOptionsDir)
    actions.addFirst { generatedOptionsDir.get().asFile.deleteRecursively() }
  }

tasks.named<JavaCompile>("compileJava") { dependsOn(generateGradleOptionsSource) }

tasks.withType(JavaCompile::class.java).configureEach { options.release = 21 }

tasks.named<Jar>("sourcesJar") { dependsOn(generateGradleOptionsSource) }

testing {
  suites {
    withType<JvmTestSuite> {
      useJUnitJupiter(rootPomDependencyVersion("org.junit", "junit-bom"))

      dependencies {
        implementation(project())

        implementation(platform(rootPomDependency("org.junit", "junit-bom")))
        implementation("org.junit.jupiter:junit-jupiter")
        implementation(rootPomDependency("org.assertj", "assertj-core"))
      }

      targets.configureEach {
        testTask.configure {
          systemProperty("file.encoding", "UTF-8")
          systemProperty("user.language", "en")
          systemProperty("user.country", "US")
          systemProperty("user.variant", "")
        }
      }
    }
  }
}

// ////////////////////////////////////////////////////////////////////////////////////////////////////////////
// IDE helpers

val ideName = "${project.name} ${rootProject.version.toString().replace("^([0-9.]+).*", "\\1")}"

if (System.getProperty("idea.sync.active").toBoolean()) {
  // There's no proper way to set the name of the IDEA project (when "just importing" or
  // syncing the Gradle project)
  val ideaDir = rootProject.layout.projectDirectory.dir(".idea")
  ideaDir.asFile.mkdirs()
  ideaDir.file(".name").asFile.writeText(ideName)
}

eclipse { project { name = ideName } }

if (System.getProperty("idea.sync.active").toBoolean()) {
  idea {
    module {
      isDownloadJavadoc = false // was 'true', but didn't work
      isDownloadSources = false // was 'true', but didn't work
      inheritOutputDirs = true

      excludeDirs =
        excludeDirs +
          setOf(
            projectDir.resolve("build-logic/.kotlin"),
            projectDir.resolve("target"),
            projectDir.resolve(".gradle"),
            projectDir.resolve(".idea"),
          ) +
          allprojects.map { prj -> prj.layout.buildDirectory.asFile.get() }
    }

    project.settings {
      copyright {
        useDefault = "ApacheLicense-v2"
        profiles.create("ApacheLicense-v2") {
          // strip trailing LF
          val copyrightText = rootProject.file("codestyle/copyright-header.txt").readText()
          notice = copyrightText
        }
      }

      encodings.encoding = "UTF-8"
      encodings.properties.encoding = "UTF-8"
    }
  }
}

// ////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Code style/formatting

spotless {
  java {
    target("src/*/java/**/*.java")
    googleJavaFormat()
    importOrder("java", "javax", "org").apply {
      forbidWildcardImports()
      semanticSort()
      // No equivalent for static imports, always at the top.
    }
    licenseHeaderFile(rootProject.file("codestyle/copyright-header-java.txt"))
    endWithNewline()
    toggleOffOn()
  }
  kotlinGradle {
    ktfmt().googleStyle()
    // licenseHeaderFile(rootProject.file("codestyle/copyright-header-java.txt"), "$")
    target("*.gradle.kts")
  }
  format("xml") {
    target("src/**/*.xml", "src/**/*.xsd")
    targetExclude("codestyle/copyright-header.xml")
    eclipseWtp(com.diffplug.spotless.extra.wtp.EclipseWtpFormatterStep.XML)
      .configFile(rootProject.file("codestyle/org.eclipse.wst.xml.core.prefs"))
    // getting the license-header delimiter right is a bit tricky.
    // licenseHeaderFile(rootProject.file("codestyle/copyright-header.xml"), '<^[!?].*$')
  }
}

// ////////////////////////////////////////////////////////////////////////////////////////////////////////////
// Misc build stuff

// ensure jars conform to reproducible builds
// (https://docs.gradle.org/current/userguide/working_with_files.html#sec:reproducible_archives)
tasks.withType<AbstractArchiveTask>().configureEach {
  isPreserveFileTimestamps = false
  isReproducibleFileOrder = true

  dirPermissions { unix("755") }
  filePermissions {
    // do not force the "execute" bit in case the file _is_ executable
    user.read = true
    user.write = true
    group.read = true
    group.write = false
    other.read = true
    other.write = false
  }
}

tasks.withType<Jar>().configureEach {
  manifest {
    attributes(
      // Do not add any (more or less) dynamic information to jars, because that makes Gradle's
      // caching way less efficient. Note that version and Git information are already added to jar
      // manifests for release(-like) builds.
      "Implementation-Title" to "Apache Creadur RAT™",
      "Implementation-Vendor" to "Apache Software Foundation",
      "Implementation-URL" to "https://creadur.apache.org/rat/",
    )
  }

  from("..") {
    include("LICENSE", "NOTICE")
    into("META-INF")
  }
}

// Silence some (not so useful) javadoc warnings
tasks.withType<Javadoc>().configureEach {
  val opt = options as CoreJavadocOptions
  // don't spam log w/ "warning: no @param/@return"
  opt.addStringOption("Xdoclint:-reference", "-quiet")
}
