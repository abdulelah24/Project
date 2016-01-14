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

import org.junit.Assert;

public class PlainJUnit3TestCaseWithSingleTestWhichFails extends TestCase {

	public void test() {
		Assert.fail("this test should fail");
	}

}
