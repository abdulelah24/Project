/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package example.testinterface;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.util.Arrays;
import java.util.Collection;

import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.TestFactory;

//tag::user_guide[]
public interface DynamicTests {

	@TestFactory
	default Collection<DynamicTest> dynamicTestsFromCollection() {
		return Arrays.asList(dynamicTest("1st dynamic test in test interface", () -> assertTrue(true)),
			dynamicTest("2nd dynamic test in test interface", () -> assertEquals(4, 2 * 2)));
	}

}
//end::user_guide[]
