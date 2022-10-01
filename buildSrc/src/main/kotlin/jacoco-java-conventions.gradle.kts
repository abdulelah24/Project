import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.attributes.LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE
import org.junit.gradle.jacoco.JacocoConventions.COVERAGE_CLASSES

plugins {
	java
	id("jacoco-conventions")
}

val mavenizedProjects: List<Project> by rootProject.extra
val enableJaCoCo = project.hasProperty("enableJaCoCo")

tasks.withType<Test>().configureEach {
	configure<JacocoTaskExtension> {
		isEnabled = enableJaCoCo
	}
}

val codeCoverageClassesJar by tasks.registering(Jar::class) {
	from(tasks.jar.map { zipTree(it.archiveFile) })
	archiveClassifier.set("jacoco")
	enabled = project in mavenizedProjects
	duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

configurations.create("codeCoverageReportClasses") {
	isCanBeResolved = false
	isCanBeConsumed = true
	isTransitive = false
	attributes.attribute(LIBRARY_ELEMENTS_ATTRIBUTE, objects.named(LibraryElements::class, COVERAGE_CLASSES))
	outgoing.artifact(codeCoverageClassesJar)
}
