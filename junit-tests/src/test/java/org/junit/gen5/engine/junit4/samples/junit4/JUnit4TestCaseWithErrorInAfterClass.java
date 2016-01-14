/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4.samples.junit4;

import static org.junit.Assert.fail;
import static org.junit.runners.MethodSorters.NAME_ASCENDING;

import org.junit.AfterClass;
import org.junit.FixMethodOrder;
import org.junit.Test;

@FixMethodOrder(NAME_ASCENDING)
public class JUnit4TestCaseWithErrorInAfterClass {

	@AfterClass
	public static void failingAfterClass() {
		fail("error in @AfterClass");
	}

	@Test
	public void failingTest() {
		fail("expected to fail");
	}

	@Test
	public void succeedingTest() {
		// no-op
	}

}
