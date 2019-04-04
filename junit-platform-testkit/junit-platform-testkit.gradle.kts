plugins {
	`java-library-conventions`
	id("org.moditect.gradleplugin")
}

description = "JUnit Platform Test Kit"

javaLibrary {
	automaticModuleName = "org.junit.platform.testkit"
}

dependencies {
	api("org.apiguardian:apiguardian-api:${Versions.apiGuardian}")
	api("org.assertj:assertj-core:${Versions.assertJ}")
	api("org.opentest4j:opentest4j:${Versions.ota4j}")

	api(project(":junit-platform-launcher"))
}

moditect {
	addMainModuleInfo {
		overwriteExistingFiles.set(true)
		module {
			moduleInfo {
				name = "org." + project.name.replace('-', '.')
			}
		}
	}
}
