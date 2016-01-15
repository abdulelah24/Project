/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import static java.util.stream.Collectors.toList;
import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.api.Assertions.assertNotNull;
import static org.junit.gen5.engine.specification.dsl.DiscoveryRequestBuilder.request;
import static org.junit.gen5.engine.specification.dsl.NameBasedSelectorBuilder.byName;
import static org.junit.gen5.engine.specification.dsl.UniqueIdSelectorBuilder.byUniqueId;

import java.util.Arrays;
import java.util.List;

import org.junit.gen5.api.Test;
import org.junit.gen5.engine.specification.ClassSelector;
import org.junit.gen5.engine.specification.MethodSelector;
import org.junit.gen5.engine.specification.PackageNameSelector;
import org.junit.gen5.engine.specification.UniqueIdSelector;

/**
 * Unit tests for {@link DiscoveryRequest}.
 *
 * @since 5.0
 */
public class DiscoveryRequestTests {
	@Test
	public void forUniqueIdForMethod() {
		DiscoverySelector element = byUniqueId("junit5:org.example.UserTests#fullname()");
		assertEquals(UniqueIdSelector.class, element.getClass());
	}

	@Test
	public void forNameWithClass() {
		DiscoverySelector element = byName(MyTestClass.class.getName());
		assertEquals(ClassSelector.class, element.getClass());
	}

	@Test
	public void forNameWithMethod() throws Exception {
		DiscoverySelector element = byName(fullyQualifiedMethodName());
		assertEquals(MethodSelector.class, element.getClass());
	}

	@Test
	public void forNameWithPackage() {
		DiscoverySelector element = byName("org.junit.gen5");
		assertEquals(PackageNameSelector.class, element.getClass());
	}

	@Test
	public void buildDiscoveryRequest() throws Exception {
		// @formatter:off
		DiscoveryRequest spec = request().select(
		byUniqueId("junit5:org.example.UserTests#fullname()"),
			byName(MyTestClass.class.getName()),
			byName("org.junit.gen5"),
			byName(fullyQualifiedMethodName())
		).build();
		// @formatter:on

		assertNotNull(spec);
		List<Class<? extends DiscoverySelector>> expected = Arrays.asList(UniqueIdSelector.class, ClassSelector.class,
			PackageNameSelector.class, MethodSelector.class);
		assertEquals(expected, spec.getSelectors().stream().map(Object::getClass).collect(toList()));
	}

	private String fullyQualifiedMethodName() throws Exception {
		return MyTestClass.class.getName() + "#" + MyTestClass.class.getDeclaredMethod("myTest").getName();
	}

	static class MyTestClass {

		void myTest() {
		}
	}
}
