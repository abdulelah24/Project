import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	`kotlin-dsl`
}

repositories {
	mavenCentral()
	gradlePluginPortal()
}

dependencies {
	implementation(kotlin("gradle-plugin"))
	implementation("biz.aQute.bnd:biz.aQute.bnd.gradle:5.2.0")
	implementation("com.github.jengelman.gradle.plugins:shadow:6.1.0")
	implementation("org.gradle:test-retry-gradle-plugin:1.2.0")
}

tasks.withType<KotlinCompile>().configureEach {
	kotlinOptions {
		allWarningsAsErrors = true
	}
}
