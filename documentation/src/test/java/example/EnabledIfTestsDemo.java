/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package example;

// tag::user_guide[]
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.jupiter.api.EnabledIf;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

class EnabledIfTestsDemo {

	@Test // Static JavaScript expression
	@EnabledIf("1 == 1")
	void testWillBeExecuted() {
		assertTrue(1 == 1);
	}

	@RepeatedTest(10) // Dynamic JavaScript expression
	@EnabledIf("Math.random() >= 0.314159")
	void testWillNeverOrSometimesBeExecuted() {
	}

	@Test // Regular expression testing bound system property.
	@EnabledIf("/64/.test(systemProperties.get('os.arch'))")
	void testWillBeExecutedIfOsArchitectureContains64() {
		assertTrue(System.getProperty("os.arch").contains("64"));
	}

	@Test // Multi-line script and import Java package names.
	@EnabledIf(imports = "java.nio.file", //
			value = { //
					"path = Files.createTempFile('volatile-', '.temp')", //
					"systemProperties.put('volatile', path)", //
					"Files.exists(path)" })
	void importJavaPackages() {
		assertTrue(Files.exists((Path) System.getProperties().get("volatile")));
	}

	@Test // Select Groovy as ScriptEngine.
	@EnabledIf(engine = "groovy", //
			imports = { "java.nio.file.Files", "java.nio.file.Paths" }, //
			value = "Files.isWritable(Paths.get(System.properties['user.home']))")
	void groovy() {
		assertTrue(Files.isWritable(Paths.get(System.getProperty("user.home"))));
	}
}
// end::user_guide[]
