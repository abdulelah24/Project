/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.commons.util;

import static java.util.Arrays.asList;
import static java.util.Collections.emptySet;
import static java.util.Collections.singleton;
import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.api.Assertions.assertSame;
import static org.junit.gen5.api.Assertions.expectThrows;

import org.junit.gen5.api.Test;

class CollectionUtilsTests {

	@Test
	void getOnlyElementWithNullCollection() {
		PreconditionViolationException exception = expectThrows(PreconditionViolationException.class, () -> {
			CollectionUtils.getOnlyElement(null);
		});
		assertEquals("collection must not be null", exception.getMessage());
	}

	@Test
	void getOnlyElementWithEmptyCollection() {
		PreconditionViolationException exception = expectThrows(PreconditionViolationException.class, () -> {
			CollectionUtils.getOnlyElement(emptySet());
		});
		assertEquals("collection must contain exactly one element: []", exception.getMessage());
	}

	@Test
	void getOnlyElementWithSingleElementCollection() {
		Object expected = new Object();
		Object actual = CollectionUtils.getOnlyElement(singleton(expected));
		assertSame(expected, actual);
	}

	@Test
	void getOnlyElementWithMultiElementCollection() {
		PreconditionViolationException exception = expectThrows(PreconditionViolationException.class, () -> {
			CollectionUtils.getOnlyElement(asList("foo", "bar"));
		});
		assertEquals("collection must contain exactly one element: [foo, bar]", exception.getMessage());
	}

}
