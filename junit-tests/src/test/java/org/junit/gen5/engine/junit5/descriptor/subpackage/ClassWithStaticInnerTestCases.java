/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor.subpackage;

import org.junit.gen5.api.Test;

/**
 * @since 5.0
 */
public class ClassWithStaticInnerTestCases {

	static class ShouldBeDiscovered {

		@Test
		void test1() {
		}
	}

	@SuppressWarnings("unused")
	private static class ShouldNotBeDiscovered {

		@Test
		void test2() {
		}
	}

}
