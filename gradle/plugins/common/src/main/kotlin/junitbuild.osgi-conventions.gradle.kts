import aQute.bnd.gradle.BundleTaskExtension
import aQute.bnd.gradle.Resolve

plugins {
	`java-library`
}

val importAPIGuardian = "org.apiguardian.*;resolution:=\"optional\""

val projectDescription = objects.property<String>().convention(provider { project.description })

// This task enhances `jar` and `shadowJar` tasks with the bnd
// `BundleTaskExtension` extension which allows for generating OSGi
// metadata into the jar
tasks.withType<Jar>().matching { task: Jar ->
	task.name == "jar" || task.name == "shadowJar"
}.all { // configure tasks eagerly as workaround for https://github.com/bndtools/bnd/issues/5695
	extra["importAPIGuardian"] = importAPIGuardian

	extensions.create<BundleTaskExtension>(BundleTaskExtension.NAME, this).apply {
		properties.set(projectDescription.map {
			mapOf("project.description" to it)
		})
		// These are bnd instructions necessary for generating OSGi metadata.
		// We've generalized these so that they are widely applicable limiting
		// module configurations to special cases.
		setBnd(
			"""
				# Set the Bundle-SymbolicName to the archiveBaseName.
				# We don't use the archiveClassifier which Bnd will use
				# in the default Bundle-SymbolicName value.
				Bundle-SymbolicName: ${'$'}{task.archiveBaseName}

				# Set the Bundle-Name from the project description
				Bundle-Name: ${'$'}{project.description}

				# These are the general rules for package imports.
				Import-Package: \
					${importAPIGuardian},\
					org.junit.platform.commons.logging;status=INTERNAL,\
					kotlin.*;resolution:="optional",\
					*

				# This tells bnd not to complain if a module doesn't actually import
				# the kotlin and apiguardian packages, but enough modules do to make it a default.
				-fixupmessages.kotlin.import: "Unused Import-Package instructions: \\[kotlin.*\\]";is:=ignore
				-fixupmessages.apiguardian.import: "Unused Import-Package instructions: \\[org.apiguardian.*\\]";is:=ignore

				# This tells bnd to ignore classes it finds in `META-INF/versions/`
				# because bnd doesn't yet support multi-release jars.
				-fixupmessages.wrong.dir: "Classes found in the wrong directory: \\{META-INF/versions/...";is:=ignore

				# Don't scan for Class.forName package imports.
				# See https://bnd.bndtools.org/instructions/noclassforname.html
				-noclassforname: true

				# Don't add all the extra headers bnd normally adds.
				# See https://bnd.bndtools.org/instructions/noextraheaders.html
				-noextraheaders: true

				# Don't add the Private-Package header.
				# See https://bnd.bndtools.org/instructions/removeheaders.html
				-removeheaders: Private-Package

				# Instruct the APIGuardianAnnotations how to operate.
				# See https://bnd.bndtools.org/instructions/export-apiguardian.html
				-export-apiguardian: *;version=${'$'}{versionmask;===;${'$'}{version_cleanup;${'$'}{task.archiveVersion}}}
			"""
		)

		// Do the actual work putting OSGi stuff in the jar.
		doLast(buildAction())
	}
}

// Bnd's Resolve task uses a properties file for its configuration. This
// task writes out the properties necessary for it to verify the OSGi
// metadata.
val osgiProperties by tasks.registering(WriteProperties::class) {
	destinationFile.set(layout.buildDirectory.file("verifyOSGiProperties.bndrun"))
	property("-standalone", true)
	project.extensions.getByType(JavaLibraryExtension::class.java).let { javaLibrary ->
		property("-runee", "JavaSE-${javaLibrary.mainJavaVersion}")
	}
	property("-runrequires", "osgi.identity;filter:='(osgi.identity=${project.name})'")
	property("-runsystempackages", "jdk.internal.misc,jdk.jfr,sun.misc")
	// API Guardian should be optional -> instruct resolver to ignore it
	// during resolution. Resolve should still pass.
	property("-runblacklist", "org.apiguardian.api")
}

val osgiVerification by configurations.creatingResolvable {
	extendsFrom(configurations.runtimeClasspath.get())
}

// Bnd's Resolve task is what verifies that a jar can be used in OSGi and
// that its metadata is valid. If the metadata is invalid this task will
// fail.
val verifyOSGi by tasks.registering(Resolve::class) {
	bndrun.set(osgiProperties.flatMap { it.destinationFile })
	outputBndrun.set(layout.buildDirectory.file("resolvedOSGiProperties.bndrun"))
	isReportOptional = false
	// By default bnd will use jars found in:
	// 1. project.sourceSets.main.runtimeClasspath
	// 2. project.configurations.archives.artifacts.files
	// to validate the metadata.
	// This adds jars defined in `osgiVerification` also so that bnd
	// can use them to validate the metadata without causing those to
	// end up in the dependencies of those projects.
	bundles(osgiVerification)
	properties.empty()
	outputs.doNotCacheIf("https://github.com/bndtools/bnd/issues/5666") { true }
}

tasks.check {
	dependsOn(verifyOSGi)
}
