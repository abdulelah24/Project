import org.asciidoctor.gradle.jvm.AbstractAsciidoctorTask
import org.gradle.api.tasks.PathSensitivity.RELATIVE
import org.gradle.internal.os.OperatingSystem
import org.jetbrains.kotlin.gradle.plugin.KotlinSourceSet
import org.junit.gradle.exec.ClasspathSystemPropertyProvider
import org.junit.gradle.javadoc.ModuleSpecificJavadocFileOption
import java.io.ByteArrayOutputStream
import java.nio.file.Files

plugins {
	id("org.asciidoctor.jvm.convert")
	id("org.asciidoctor.jvm.pdf")
	id("org.ajoberstar.git-publish")
	`kotlin-library-conventions`
}

val modularProjects: List<Project> by rootProject

// Because we need to set up Javadoc aggregation
modularProjects.forEach { evaluationDependsOn(it.path) }

javaLibrary {
	mainJavaVersion = JavaVersion.VERSION_1_8
	testJavaVersion = JavaVersion.VERSION_1_8
}

val apiReport by configurations.creating

dependencies {
	implementation(projects.junitJupiterApi) {
		because("Jupiter API is used in src/main/java")
	}

	// Pull in all "modular projects" to ensure that they are included
	// in reports generated by the ApiReportGenerator.
	modularProjects.forEach { apiReport(it) }

	testImplementation(projects.junitJupiter)
	testImplementation(projects.junitJupiterMigrationsupport)
	testImplementation(projects.junitPlatformConsole)
	testImplementation(projects.junitPlatformRunner)
	testImplementation(projects.junitPlatformSuite)
	testImplementation(projects.junitPlatformTestkit)
	testImplementation(kotlin("stdlib"))

	testImplementation(projects.junitVintageEngine)
	testRuntimeOnly(libs.bundles.log4j)
	testRuntimeOnly(libs.apiguardian) {
		because("it's required to generate API tables")
	}

	testImplementation(libs.classgraph) {
		because("ApiReportGenerator needs it")
	}
}

asciidoctorj {
	modules {
		diagram.use()
		pdf.version(libs.versions.asciidoctor.pdf)
	}
}

val snapshot = rootProject.version.toString().contains("SNAPSHOT")
val docsVersion = if (snapshot) "snapshot" else rootProject.version
val releaseBranch = if (snapshot) "HEAD" else "r${rootProject.version}"
val docsDir = file("$buildDir/ghpages-docs")
val replaceCurrentDocs = project.hasProperty("replaceCurrentDocs")
val uploadPdfs = !snapshot
val userGuidePdfFileName = "junit-user-guide-${rootProject.version}.pdf"
val ota4jDocVersion = if (libs.versions.opentest4j.get().contains("SNAPSHOT")) "snapshot" else libs.versions.opentest4j.get()
val apiGuardianDocVersion = if (libs.versions.apiguardian.get().contains("SNAPSHOT")) "snapshot" else libs.versions.apiguardian.get()

gitPublish {
	repoUri.set("https://github.com/junit-team/junit5.git")
	branch.set("gh-pages")
	sign.set(false)

	contents {
		from(docsDir)
		into("docs")
	}

	preserve {
		include("**/*")
		exclude("docs/$docsVersion/**")
		if (replaceCurrentDocs) {
			exclude("docs/current/**")
		}
	}
}

val generatedAsciiDocPath = file("$buildDir/generated/asciidoc")
val consoleLauncherOptionsFile = File(generatedAsciiDocPath, "console-launcher-options.txt")
val experimentalApisTableFile = File(generatedAsciiDocPath, "experimental-apis-table.txt")
val deprecatedApisTableFile = File(generatedAsciiDocPath, "deprecated-apis-table.txt")

val jdkJavadocBaseUrl = "https://docs.oracle.com/en/java/javase/11/docs/api"
val elementListsDir = file("$buildDir/elementLists")
val externalModulesWithoutModularJavadoc = mapOf(
		"org.apiguardian.api" to "https://apiguardian-team.github.io/apiguardian/docs/$apiGuardianDocVersion/api/",
		"org.assertj.core" to "https://javadoc.io/doc/org.assertj/assertj-core/${libs.versions.assertj.get()}/",
		"org.opentest4j" to "https://ota4j-team.github.io/opentest4j/docs/$ota4jDocVersion/api/"
)
require(externalModulesWithoutModularJavadoc.values.all { it.endsWith("/") }) {
	"all base URLs must end with a trailing slash: $externalModulesWithoutModularJavadoc"
}

tasks {

	val consoleLauncherTest by registering {
		val runtimeClasspath = sourceSets["test"].runtimeClasspath
		inputs.files(runtimeClasspath).withNormalizer(ClasspathNormalizer::class)
		val reportsDir = file("$buildDir/test-results")
		outputs.dir(reportsDir)
		outputs.cacheIf { true }

		// Track OS as input so that tests are executed on all configured operating systems on CI
		trackOperationSystemAsInput()

		doFirst {
			val debugging = findProperty("consoleLauncherTestDebug")?.toString()?.toBoolean() ?: false
			val output = ByteArrayOutputStream()
			val result = javaexec {
				classpath = runtimeClasspath
				mainClass.set("org.junit.platform.console.ConsoleLauncher")
				args("--scan-classpath")
				args("--config", "enableHttpServer=true")
				args("--include-classname", ".*Tests")
				args("--include-classname", ".*Demo")
				args("--exclude-tag", "exclude")
				args("--reports-dir", reportsDir)
				systemProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager")
				debug = debugging
				if (!debugging) {
					standardOutput = output
					errorOutput = output
				}
				isIgnoreExitValue = true
			}
			if (result.exitValue != 0 && !debugging) {
				System.out.write(output.toByteArray())
				System.out.flush()
			}
			result.rethrowFailure().assertNormalExitValue()
		}
	}

	register<JavaExec>("consoleLauncher") {
		val reportsDir = file("$buildDir/console-launcher")
		outputs.dir(reportsDir)
		outputs.upToDateWhen { false }
		classpath = sourceSets["test"].runtimeClasspath
		mainClass.set("org.junit.platform.console.ConsoleLauncher")
		args("--scan-classpath")
		args("--config", "enableHttpServer=true")
		args("--include-classname", ".*Tests")
		args("--include-classname", ".*Demo")
		args("--exclude-tag", "exclude")
		args("--reports-dir", reportsDir)
		systemProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager")
	}

	test {
		dependsOn(consoleLauncherTest)
		exclude("**/*")
	}

	val generateConsoleLauncherOptions by registering(JavaExec::class) {
		classpath = sourceSets["test"].runtimeClasspath
		mainClass.set("org.junit.platform.console.ConsoleLauncher")
		args("--help", "--disable-banner")
		redirectOutput(consoleLauncherOptionsFile)
	}

	val generateExperimentalApisTable by registering(JavaExec::class) {
		classpath = sourceSets["test"].runtimeClasspath
		mainClass.set("org.junit.api.tools.ApiReportGenerator")
		jvmArgumentProviders += ClasspathSystemPropertyProvider("api.classpath", apiReport)
		args("EXPERIMENTAL")
		redirectOutput(experimentalApisTableFile)
	}

	val generateDeprecatedApisTable by registering(JavaExec::class) {
		classpath = sourceSets["test"].runtimeClasspath
		mainClass.set("org.junit.api.tools.ApiReportGenerator")
		jvmArgumentProviders += ClasspathSystemPropertyProvider("api.classpath", apiReport)
		args("DEPRECATED")
		redirectOutput(deprecatedApisTableFile)
	}

	withType<AbstractAsciidoctorTask>().configureEach {
		dependsOn(generateConsoleLauncherOptions, generateExperimentalApisTable, generateDeprecatedApisTable)
		inputs.files(consoleLauncherOptionsFile, experimentalApisTableFile, deprecatedApisTableFile)

		resources {
			from(sourceDir) {
				include("**/images/**/*.png")
				include("**/images/**/*.svg")
			}
		}

		// Temporary workaround for https://github.com/asciidoctor/asciidoctor-gradle-plugin/issues/599
		inputs.dir(sourceDir).withPropertyName("sourceDir").withPathSensitivity(RELATIVE)

		attributes(mapOf(
				"jupiter-version" to version,
				"platform-version" to project.property("platformVersion"),
				"vintage-version" to project.property("vintageVersion"),
				"bom-version" to version,
				"junit4-version" to libs.versions.junit4.get(),
				"apiguardian-version" to libs.versions.apiguardian.get(),
				"ota4j-version" to libs.versions.opentest4j.get(),
				"surefire-version" to libs.versions.surefire.get(),
				"release-branch" to releaseBranch,
				"docs-version" to docsVersion,
				"revnumber" to version,
				"consoleLauncherOptionsFile" to consoleLauncherOptionsFile,
				"experimentalApisTableFile" to experimentalApisTableFile,
				"deprecatedApisTableFile" to deprecatedApisTableFile,
				"outdir" to outputDir.absolutePath,
				"source-highlighter" to "rouge",
				"tabsize" to "4",
				"toc" to "left",
				"icons" to "font",
				"sectanchors" to true,
				"idprefix" to "",
				"idseparator" to "-",
				"jdk-javadoc-base-url" to jdkJavadocBaseUrl
		))

		sourceSets["test"].apply {
			attributes(mapOf(
					"testDir" to java.srcDirs.first(),
					"testResourcesDir" to resources.srcDirs.first()
			))
			inputs.dir(java.srcDirs.first())
			inputs.dir(resources.srcDirs.first())
			withConvention(KotlinSourceSet::class) {
				attributes(mapOf("kotlinTestDir" to kotlin.srcDirs.first()))
				inputs.dir(kotlin.srcDirs.first())
			}
		}

		forkOptions {
			// To avoid warning, see https://github.com/asciidoctor/asciidoctor-gradle-plugin/issues/597
			jvmArgs(
				"--add-opens", "java.base/sun.nio.ch=ALL-UNNAMED",
				"--add-opens", "java.base/java.io=ALL-UNNAMED"
			)
		}
	}

	asciidoctor {
		sources {
			include("**/index.adoc")
		}
		resources {
			from(sourceDir) {
				include("tocbot-*/**")
			}
		}
		attributes(mapOf(
				"linkToPdf" to uploadPdfs,
				"userGuidePdfFileName" to userGuidePdfFileName,
				"releaseNotesUrl" to "../release-notes/index.html#release-notes"
		))
	}

	asciidoctorPdf {
		sources {
			include("user-guide/index.adoc")
		}
		copyAllResources()
		attributes(mapOf("releaseNotesUrl" to "https://junit.org/junit5/docs/$docsVersion/release-notes/"))
	}

	val downloadJavadocElementLists by registering {
		outputs.cacheIf { true }
		outputs.dir(elementListsDir).withPropertyName("elementListsDir")
		inputs.property("externalModulesWithoutModularJavadoc", externalModulesWithoutModularJavadoc)
		doFirst {
			externalModulesWithoutModularJavadoc.forEach { (moduleName, baseUrl) ->
				val resource = resources.text.fromUri("${baseUrl}element-list")
				elementListsDir.resolve(moduleName).apply {
					mkdir()
					resolve("element-list").writeText("module:$moduleName\n${resource.asString()}")
				}
			}
		}
	}

	val aggregateJavadocs by registering(Javadoc::class) {
		dependsOn(modularProjects.map { it.tasks.jar })
		dependsOn(downloadJavadocElementLists)
		group = "Documentation"
		description = "Generates aggregated Javadocs"

		title = "JUnit $version API"

		val additionalStylesheetFile = "src/javadoc/junit-stylesheet.css"
		inputs.file(additionalStylesheetFile)
		val overviewFile = "src/javadoc/junit-overview.html"
		inputs.file(overviewFile)

		options {

			memberLevel = JavadocMemberLevel.PROTECTED
			header = rootProject.description
			encoding = "UTF-8"
			locale = "en"
			overview = overviewFile
			jFlags("-Xmx1g")

			this as StandardJavadocDocletOptions
			splitIndex(true)
			addBooleanOption("Xdoclint:all,-accessibility,-missing", true)
			addBooleanOption("html5", true)
			addMultilineStringsOption("tag").value = listOf(
					"apiNote:a:API Note:",
					"implNote:a:Implementation Note:"
			)

			links(jdkJavadocBaseUrl)
			links("https://junit.org/junit4/javadoc/${libs.versions.junit4.get()}/")
			externalModulesWithoutModularJavadoc.forEach { (moduleName, baseUrl) ->
				linksOffline(baseUrl, "$elementListsDir/$moduleName")
			}

			groups = mapOf(
					"Jupiter" to listOf("org.junit.jupiter*"),
					"Vintage" to listOf("org.junit.vintage*"),
					"Platform" to listOf("org.junit.platform*")
			)
			addStringOption("-add-stylesheet", additionalStylesheetFile)
			use(true)
			noTimestamp(true)

			addStringsOption("-module", ",").value = modularProjects.map { it.javaModuleName }
			val moduleSourcePathOption = addPathOption("-module-source-path")
			moduleSourcePathOption.value = modularProjects.map { it.file("src/module") }
			moduleSourcePathOption.value.forEach { inputs.dir(it) }
			addOption(ModuleSpecificJavadocFileOption("-patch-module", modularProjects.associate {
				it.javaModuleName to files(it.sourceSets.matching { it.name.startsWith("main") }.map { it.allJava.srcDirs }).asPath
			}))
			addStringOption("-add-modules", "info.picocli")
			addOption(ModuleSpecificJavadocFileOption("-add-reads", mapOf(
					"org.junit.platform.console" to "info.picocli",
					"org.junit.jupiter.params" to "univocity.parsers"
			)))
		}

		source(modularProjects.map { files(it.sourceSets.matching { it.name.startsWith("main") }.map { it.allJava }) })
		classpath = files(modularProjects.map { it.sourceSets.main.get().compileClasspath })

		maxMemory = "1024m"
		destinationDir = file("$buildDir/docs/javadoc")

		doFirst {
			(options as CoreJavadocOptions).modulePath = classpath.files.toList()
		}
	}

	val fixJavadoc by registering(Copy::class) {
		dependsOn(aggregateJavadocs)
		group = "Documentation"
		description = "Fix links to external API specs in the locally aggregated Javadoc HTML files"

		val inputDir = aggregateJavadocs.map { it.destinationDir!! }
		inputs.property("externalModulesWithoutModularJavadoc", externalModulesWithoutModularJavadoc)
		from(inputDir.map { File(it, "element-list") }) {
			// For compatibility with pre JDK 10 versions of the Javadoc tool
			rename { "package-list" }
		}
		from(inputDir) {
			filesMatching("**/*.html") {
				val favicon = "<link rel=\"icon\" type=\"image/png\" href=\"https://junit.org/junit5/assets/img/junit5-logo.png\">"
				filter { line ->
					var result = if (line.startsWith("<head>")) line.replace("<head>", "<head>$favicon") else line
					externalModulesWithoutModularJavadoc.forEach { (moduleName, baseUrl) ->
						result = result.replace("${baseUrl}$moduleName/", baseUrl)
					}
					return@filter result
				}
			}
		}
		into("$buildDir/docs/fixedJavadoc")
	}

	val prepareDocsForUploadToGhPages by registering(Copy::class) {
		dependsOn(fixJavadoc, asciidoctor, asciidoctorPdf)
		outputs.dir(docsDir)

		from("$buildDir/checksum") {
			include("published-checksum.txt")
		}
		from(asciidoctor.map { it.outputDir }) {
			include("user-guide/**")
			include("release-notes/**")
			include("tocbot-*/**")
		}
		if (uploadPdfs) {
			from(asciidoctorPdf.map { it.outputDir }) {
				include("**/*.pdf")
				rename { userGuidePdfFileName }
			}
		}
		from(fixJavadoc.map { it.destinationDir }) {
			into("api")
		}
		into("$docsDir/$docsVersion")
		includeEmptyDirs = false
	}

	val createCurrentDocsFolder by registering(Copy::class) {
		dependsOn(prepareDocsForUploadToGhPages)
		outputs.dir("$docsDir/current")
		onlyIf { replaceCurrentDocs }

		from("$docsDir/$docsVersion")
		into("$docsDir/current")
	}

	val configureGitAuthor by registering {
		dependsOn(gitPublishReset)
		doFirst {
			File(gitPublish.repoDir.get().asFile, ".git/config").appendText("""
				[user]
					name = JUnit Team
					email = team@junit.org
			""".trimIndent())
		}
	}

	gitPublishCopy {
		dependsOn(prepareDocsForUploadToGhPages, createCurrentDocsFolder)
	}

	gitPublishCommit {
		dependsOn(configureGitAuthor)
	}
}

fun JavaExec.redirectOutput(outputFile: File) {
	outputs.file(outputFile)
	val byteStream = ByteArrayOutputStream()
	standardOutput = byteStream
	doLast {
		Files.createDirectories(outputFile.parentFile.toPath())
		Files.write(outputFile.toPath(), byteStream.toByteArray())
	}
}

eclipse {
	classpath {
		plusConfigurations.add(projects.junitPlatformConsole.dependencyProject.configurations["shadowed"])
		plusConfigurations.add(projects.junitJupiterParams.dependencyProject.configurations["shadowed"])
	}
}

idea {
	module {
		scopes["PROVIDED"]!!["plus"]!!.add(projects.junitPlatformConsole.dependencyProject.configurations["shadowed"])
		scopes["PROVIDED"]!!["plus"]!!.add(projects.junitJupiterParams.dependencyProject.configurations["shadowed"])
	}
}
