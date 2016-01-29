/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.resolver;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.gen5.engine.discovery.ClassSelector.forClass;
import static org.junit.gen5.engine.junit5.resolver.ClassResolver.resolveClass;
import static org.junit.gen5.engine.junit5.resolver.EngineResolver.resolveEngine;
import static org.junit.gen5.engine.junit5.resolver.PackageResolver.resolvePackage;
import static org.junit.gen5.launcher.main.TestDiscoveryRequestBuilder.request;

import java.util.Optional;

import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.api.Test;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.discovery.ClassSelector;
import org.junit.gen5.engine.junit5.descriptor.PackageTestDescriptor;
import org.junit.gen5.engine.junit5.resolver.testpackage.NestingTestClass;
import org.junit.gen5.engine.junit5.resolver.testpackage.SingleTestClass;
import org.junit.gen5.engine.junit5.resolver.testpackage.TestsWithParametersTestClass;
import org.junit.gen5.engine.junit5.stubs.TestEngineStub;
import org.junit.gen5.engine.junit5.stubs.TestResolverRegistryMock;
import org.junit.gen5.engine.support.descriptor.EngineDescriptor;

public class ClassResolverTests {
	private String testPackageName;
	private EngineDescriptor engineDescriptor;
	private TestResolverRegistryMock testResolverRegistryMock;
	private ClassResolver resolver;

	@BeforeEach
	void setUp() {
		testPackageName = SingleTestClass.class.getPackage().getName();
		testResolverRegistryMock = new TestResolverRegistryMock();

		TestEngineStub testEngine = new TestEngineStub();
		engineDescriptor = resolveEngine(testEngine);

		resolver = new ClassResolver();
		resolver.bindTestResolveryRegistry(testResolverRegistryMock);
	}

	@Test
	void withAnEmptyDiscoveryRequest_doesNotResolveAnything() throws Exception {
		resolver.resolveAllFrom(engineDescriptor, request().build());
		assertThat(testResolverRegistryMock.testDescriptors).isEmpty();
	}

	@Test
	void givenAClassSelector_resolvesTheClass() {
		PackageTestDescriptor testPackage = resolvePackage(engineDescriptor, testPackageName);
		engineDescriptor.addChild(testPackage);

		testResolverRegistryMock.fetchParentFunction = (selector, root) -> testPackage;

		resolver.resolveAllFrom(engineDescriptor, request().select(forClass(SingleTestClass.class)).build());

		// @formatter:off
        assertThat(testResolverRegistryMock.testDescriptors)
                .containsOnly(
                        resolveClass(testPackage, SingleTestClass.class)
                )
                .doesNotHaveDuplicates();
        // @formatter:on
	}

	@Test
	void givenAPackageAndAClassSelector_resolvesTheClass_AndAttachesItToTheExistingTree() {
		PackageTestDescriptor testPackage = resolvePackage(engineDescriptor, testPackageName);
		engineDescriptor.addChild(testPackage);
		testResolverRegistryMock.fetchParentFunction = (selector, root) -> testPackage;

		resolver.resolveAllFrom(engineDescriptor, request().select(forClass(SingleTestClass.class)).build());

		assertThat(engineDescriptor.getChildren()).containsOnly(testPackage);
		assertThat(testPackage.getChildren()).containsOnly(resolveClass(testPackage, SingleTestClass.class));
	}

	@Test
	void whenNotifiedWithAPackageTestDescriptor_resolvesAllTopLevelAndNestedStaticClassesInThePackage()
			throws Exception {
		PackageTestDescriptor testPackage = resolvePackage(engineDescriptor, testPackageName);
		engineDescriptor.addChild(testPackage);

		resolver.resolveAllFrom(testPackage, request().build());

		// @formatter:off
		assertThat(testResolverRegistryMock.testDescriptors)
                .containsOnly(
                        resolveClass(testPackage, SingleTestClass.class),
                        resolveClass(testPackage, TestsWithParametersTestClass.class),
                        resolveClass(testPackage, NestingTestClass.class),
                        resolveClass(testPackage, NestingTestClass.NestedStaticClass.class)
                )
                .doesNotHaveDuplicates();
        // @formatter:on
	}

	@Test
	void givenAClassSelectorToANestedStaticClass_resolvesTheClassLikeATopLevelClass() {
		PackageTestDescriptor testPackage = resolvePackage(engineDescriptor, testPackageName);
		engineDescriptor.addChild(testPackage);

		testResolverRegistryMock.fetchParentFunction = (selector, root) -> testPackage;

		resolver.resolveAllFrom(engineDescriptor,
			request().select(forClass(NestingTestClass.NestedStaticClass.class)).build());

		// @formatter:off
        assertThat(testResolverRegistryMock.testDescriptors)
                .containsOnly(resolveClass(testPackage, NestingTestClass.NestedStaticClass.class))
                .doesNotHaveDuplicates();
        // @formatter:on
	}

	@Test
	void givenAPackageSelector_fetchesTheTreeUpToTheRoot() {
		PackageTestDescriptor testPackage = resolvePackage(engineDescriptor, testPackageName);
		engineDescriptor.addChild(testPackage);

		testResolverRegistryMock.fetchParentFunction = (selector, root) -> testPackage;

		ClassSelector selector = forClass(SingleTestClass.class);
		Optional<TestDescriptor> testDescriptor = resolver.fetchBySelector(selector, engineDescriptor);

		assertThat(testResolverRegistryMock.testDescriptors).isEmpty();
		assertThat(testDescriptor.isPresent()).isTrue();
		assertThat(testDescriptor.get().getParent().isPresent()).isTrue();
		assertThat(testDescriptor.get().getParent().get()).isEqualTo(testPackage);
	}
}
