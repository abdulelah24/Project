/*
 * Copyright 2015-2020 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.vintage.engine.samples.junit4;

import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @since 4.12
 */
public class PlainJUnit4TestCaseWithSingleTestWhichIsIgnored {

	@Test
	@Ignore("ignored test")
	public void ignoredTest() {
		Assert.fail("this should not be called");
	}

}
