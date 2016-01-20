/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5;

import static org.junit.gen5.engine.junit5.descriptor.TestDescriptorBuilder.*;
import static org.junit.gen5.launcher.main.TestDiscoveryRequestBuilder.request;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.gen5.api.Assertions;
import org.junit.gen5.api.Nested;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.EngineDiscoveryRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.discovery.ClassFilter;

/**
 * Microtests for {@link DiscoveryFilterApplier}
 */
class DiscoveryFilterApplierTests {

	DiscoveryFilterApplier applier = new DiscoveryFilterApplier();

	@Test
	void nonMatchingClassesAreExcluded() {

		EngineDiscoveryRequest request = request().filter(ClassFilter.byNamePattern(".*\\$MatchingClass")).build();

		// @formatter:off
		TestDescriptor engineDescriptor = engineDescriptor()
			.with(
				classTestDescriptor("matching", MatchingClass.class),
				classTestDescriptor("other", OtherClass.class)
			)
			.build();
		// @formatter:on

		applier.applyAllFilters(request, engineDescriptor);

		List<String> includedDescriptors = engineDescriptor.allDescendants().stream().map(
			TestDescriptor::getUniqueId).collect(Collectors.toList());
		Assertions.assertEquals(1, includedDescriptors.size());
		Assertions.assertTrue(includedDescriptors.contains("matching"));
	}

	@Test
	void nestedTestClassesAreAlwaysIncludedWhenTheirParentIs() {
		EngineDiscoveryRequest request = request().filter(ClassFilter.byNamePattern(".*\\$MatchingClass")).build();

		// @formatter:off
		TestDescriptor engineDescriptor = engineDescriptor()
			.with(
				classTestDescriptor("matching", MatchingClass.class)
					.with(nestedClassTestDescriptor("nested", MatchingClass.NestedClass.class))
			)
			.build();
		// @formatter:on

		applier.applyAllFilters(request, engineDescriptor);

		List<String> includedDescriptors = engineDescriptor.allDescendants().stream().map(
			TestDescriptor::getUniqueId).collect(Collectors.toList());
		Assertions.assertEquals(2, includedDescriptors.size());
		Assertions.assertTrue(includedDescriptors.contains("matching"));
		Assertions.assertTrue(includedDescriptors.contains("nested"));
	}

	private static class MatchingClass {
		@Nested
		class NestedClass {
		}
	}

	private static class OtherClass {
	}
}
