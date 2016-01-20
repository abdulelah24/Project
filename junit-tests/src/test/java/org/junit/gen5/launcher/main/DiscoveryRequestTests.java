/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher.main;

import static java.util.stream.Collectors.toList;
import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.api.Assertions.assertNotNull;
import static org.junit.gen5.engine.discovery.NameBasedSelector.forName;
import static org.junit.gen5.engine.discovery.UniqueIdSelector.forUniqueId;
import static org.junit.gen5.launcher.main.TestDiscoveryRequestBuilder.request;

import java.util.Arrays;
import java.util.List;

import org.junit.gen5.api.Test;
import org.junit.gen5.engine.DiscoverySelector;
import org.junit.gen5.engine.EngineDiscoveryRequest;
import org.junit.gen5.engine.discovery.ClassSelector;
import org.junit.gen5.engine.discovery.MethodSelector;
import org.junit.gen5.engine.discovery.PackageSelector;
import org.junit.gen5.engine.discovery.UniqueIdSelector;

/**
 * Unit tests for {@link DiscoveryRequest}.
 *
 * @since 5.0
 */
public class DiscoveryRequestTests {
	@Test
	public void forUniqueIdForMethod() {
		DiscoverySelector element = forUniqueId("junit5:org.example.UserTests#fullname()");
		assertEquals(UniqueIdSelector.class, element.getClass());
	}

	@Test
	public void forNameWithClass() {
		DiscoverySelector element = forName(MyTestClass.class.getName());
		assertEquals(ClassSelector.class, element.getClass());
	}

	@Test
	public void forNameWithMethod() throws Exception {
		DiscoverySelector element = forName(fullyQualifiedMethodName());
		assertEquals(MethodSelector.class, element.getClass());
	}

	@Test
	public void forNameWithPackage() {
		DiscoverySelector element = forName("org.junit.gen5");
		assertEquals(PackageSelector.class, element.getClass());
	}

	@Test
	public void buildDiscoveryRequest() throws Exception {
		// @formatter:off
		EngineDiscoveryRequest spec = request().select(
		forUniqueId("junit5:org.example.UserTests#fullname()"),
			forName(MyTestClass.class.getName()),
			forName("org.junit.gen5"),
			forName(fullyQualifiedMethodName())
		).build();
		// @formatter:on

		assertNotNull(spec);
		List<Class<? extends DiscoverySelector>> expected = Arrays.asList(UniqueIdSelector.class, ClassSelector.class,
			PackageSelector.class, MethodSelector.class);
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
