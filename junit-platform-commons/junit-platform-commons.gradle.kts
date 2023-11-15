plugins {
	id("junitbuild.java-library-conventions")
	id("junitbuild.java-multi-release-sources")
	`java-test-fixtures`
}

description = "JUnit Platform Commons"

dependencies {
	api(platform(projects.junitBom))

	compileOnlyApi(libs.apiguardian)
}

tasks.jar {
	val release9ClassesDir = sourceSets.mainRelease9.get().output.classesDirs.singleFile
	inputs.dir(release9ClassesDir).withPathSensitivity(PathSensitivity.RELATIVE)
	doLast(objects.newInstance(junitbuild.java.UpdateJarAction::class).apply {
		javaLauncher = javaToolchains.launcherFor(java.toolchain)
		args.addAll(
			"--release", "9",
			"-C", release9ClassesDir.absolutePath, "."
		)
	})
}

tasks.codeCoverageClassesJar {
	exclude("org/junit/platform/commons/util/ModuleUtils.class")
}

eclipse {
	classpath {
		sourceSets -= project.sourceSets.mainRelease9.get()
	}
}
