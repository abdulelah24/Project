/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.resolver.testpackage;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.gen5.api.Assertions;
import org.junit.gen5.api.Test;

public class SingleTestClass {
	@Test
	void test1() {
		assertThat(1 + 1).isEqualTo(2);
	}

	@Test
	void test2() {
		assertThat(1 + 1).isEqualTo(2);
	}

	void noTestBecauseOfMissingTestAnnotation() {
		Assertions.fail("This must not be executed!");
	}

	@Test
	private void noTestBecauseOfLimitedVisibility() {
		Assertions.fail("This must not be executed!");
	}

	@Test
	static void noTestBecauseOfStaticModifier() {
		Assertions.fail("This must not be executed!");
	}
}
