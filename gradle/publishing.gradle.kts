apply(plugin = "maven-publish")
apply(plugin = "signing")

val isSnapshot = project.version.toString().contains("SNAPSHOT")
val isContinuousIntegrationEnvironment = System.getenv("CI")?.toBoolean() ?: false
val isJitPackEnvironment = System.getenv("JITPACK")?.toBoolean() ?: false

// ensure project is built successfully before publishing it
val build = tasks[LifecycleBasePlugin.BUILD_TASK_NAME]
tasks[PublishingPlugin.PUBLISH_LIFECYCLE_TASK_NAME].dependsOn(build)
tasks[MavenPublishPlugin.PUBLISH_LOCAL_LIFECYCLE_TASK_NAME].dependsOn(build)

configure<SigningExtension> {
	sign(the<PublishingExtension>().publications)
	setRequired(!(isSnapshot || isContinuousIntegrationEnvironment || isJitPackEnvironment))
}

configure<PublishingExtension> {
	publications {
		create<MavenPublication>("maven") {
			pom {
				name.set(provider {
					project.description ?: "${project.group}:${project.name}"
				})
				url.set("http://junit.org/junit5/")
				scm {
					connection.set("scm:git:git://github.com/junit-team/junit5.git")
					developerConnection.set("scm:git:git://github.com/junit-team/junit5.git")
					url.set("https://github.com/junit-team/junit5")
				}
				licenses {
					license {
						val license = rootProject.extra["license"] as Map<String, String>
						name.set(license["name"])
						url.set(license["url"])
					}
				}
				developers {
					developer {
						id.set("bechte")
						name.set("Stefan Bechtold")
						email.set("stefan.bechtold@me.com")
					}
					developer {
						id.set("jlink")
						name.set("Johannes Link")
						email.set("business@johanneslink.net")
					}
					developer {
						id.set("marcphilipp")
						name.set("Marc Philipp")
						email.set("mail@marcphilipp.de")
					}
					developer {
						id.set("mmerdes")
						name.set("Matthias Merdes")
						email.set("Matthias.Merdes@heidelberg-mobil.com")
					}
					developer {
						id.set("sbrannen")
						name.set("Sam Brannen")
						email.set("sam@sambrannen.com")
					}
					developer {
						id.set("sormuras")
						name.set("Christian Stein")
						email.set("sormuras@gmail.com")
					}
				}
			}
		}
	}
	repositories {
		maven {
			val stagingRepoUrl = uri("https://oss.sonatype.org/service/local/staging/deploy/maven2/")
			val snapshotRepoUrl = uri("https://oss.sonatype.org/content/repositories/snapshots/")
			url = if (isSnapshot) snapshotRepoUrl else stagingRepoUrl
			credentials {
				username = rootProject.findProperty("ossrhUsername") as String? ?: ""
				password = rootProject.findProperty("ossrhPassword") as String? ?: ""
			}
		}
	}
}
