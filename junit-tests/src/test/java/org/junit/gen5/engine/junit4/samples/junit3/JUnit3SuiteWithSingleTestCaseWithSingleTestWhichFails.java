/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4.samples.junit3;

import junit.framework.TestCase;
import junit.framework.TestSuite;

public class JUnit3SuiteWithSingleTestCaseWithSingleTestWhichFails extends TestCase {

	public static junit.framework.Test suite() {
		TestSuite suite = new TestSuite();
		suite.addTestSuite(PlainJUnit3TestCaseWithSingleTestWhichFails.class);
		return suite;
	}

}
