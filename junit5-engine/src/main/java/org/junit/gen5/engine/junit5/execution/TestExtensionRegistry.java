/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.execution;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.junit.gen5.api.extension.ExtensionPoint;
import org.junit.gen5.api.extension.ExtensionPoint.Position;
import org.junit.gen5.api.extension.ExtensionRegistrar;
import org.junit.gen5.api.extension.ExtensionRegistry;
import org.junit.gen5.api.extension.TestExtension;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.junit5.extension.*;

/**
 * A {@code TestExtensionRegistry registry} serves to hold all registered extensions (i.e. instances of
 * {@linkplain ExtensionPoint}) for a given {@linkplain org.junit.gen5.engine.Container} or
 * {@linkplain org.junit.gen5.engine.Leaf}. A registry has a reference to a parent registry and all lookups are done in
 * itself and in its parent and thereby all its ancestors.
 *
 * @since 5.0
 */
public class TestExtensionRegistry {

	/**
	 * Used to create and populate a new registry from a list of extensions and a parent.
	 *
	 * @param parentRegistry
	 * @param extensionClasses The extensions to be registered in the new registry
	 * @return a new TestExtensionRegistry
	 */
	public static TestExtensionRegistry newRegistryFrom(TestExtensionRegistry parentRegistry,
			List<Class<? extends TestExtension>> extensionClasses) {
		TestExtensionRegistry newTestExtensionRegistry = new TestExtensionRegistry(parentRegistry);
		extensionClasses.forEach(newTestExtensionRegistry::addExtension);
		return newTestExtensionRegistry;
	}

	private static final List<Class<? extends TestExtension>> defaultExtensionClasses = Collections.unmodifiableList(
		Arrays.asList(DisabledCondition.class, TestNameParameterResolver.class, TestReporterParameterResolver.class));

	/**
	 * @return all extension classes that are added by default to all root registries
	 */
	public static List<Class<? extends TestExtension>> getDefaultExtensionClasses() {
		return defaultExtensionClasses;
	}

	private final Set<Class<? extends TestExtension>> registeredExtensionClasses = new LinkedHashSet<>();

	private final List<RegisteredExtensionPoint<?>> registeredExtensionPoints = new ArrayList<>();

	private final Optional<TestExtensionRegistry> parent;

	public TestExtensionRegistry() {
		this(null);
	}

	public TestExtensionRegistry(TestExtensionRegistry parent) {
		this.parent = Optional.ofNullable(parent);
		if (!this.parent.isPresent()) {
			addDefaultExtensions();
		}
	}

	private void addDefaultExtensions() {
		getDefaultExtensionClasses().stream().forEach(this::addExtension);
	}

	/**
	 * @return all extension classes registered in this registry or one of its ancestors
	 */
	public Set<Class<? extends TestExtension>> getRegisteredExtensionClasses() {
		Set<Class<? extends TestExtension>> allRegisteredExtensionClasses = new LinkedHashSet<>();
		this.parent.ifPresent(
			parentRegistry -> allRegisteredExtensionClasses.addAll(parentRegistry.getRegisteredExtensionClasses()));
		allRegisteredExtensionClasses.addAll(this.registeredExtensionClasses);
		return Collections.unmodifiableSet(allRegisteredExtensionClasses);
	}

	@SuppressWarnings("unchecked")
	private <T extends ExtensionPoint> List<RegisteredExtensionPoint<T>> getRegisteredExtensionPoints(
			Class<T> extensionClass) {

		List<RegisteredExtensionPoint<T>> allExtensionPoints = new ArrayList<>();
		this.parent.ifPresent(
			parentRegistry -> allExtensionPoints.addAll(parentRegistry.getRegisteredExtensionPoints(extensionClass)));

		//@formatter:off
		registeredExtensionPoints.stream()
				.filter(registeredExtensionPoint -> extensionClass.isAssignableFrom(registeredExtensionPoint.extensionPoint.getClass()))
				.forEach(extensionPoint -> allExtensionPoints.add((RegisteredExtensionPoint<T>) extensionPoint));
		//@formatter:on

		//TODO: Reorder by using the position
		return allExtensionPoints;
	}

	/**
	 * Find all extension points for a given type in the right order.
	 *
	 * @param extensionClass
	 * @param <T> The exact {@linkplain ExtensionPoint} for which to find all extensions
	 * @return a list of all fitting extensions in the correct order considering all Position values
	 */
	public <T extends ExtensionPoint> List<T> getExtensionPoints(Class<T> extensionClass) {
		return getRegisteredExtensionPoints(extensionClass).stream().map(
			registeredExtensionPoint -> registeredExtensionPoint.extensionPoint).collect(Collectors.toList());
	}

	/**
	 * Register an extension class which can be either an {@linkplain ExtensionPoint} implementatio or an
	 * {@linkplain ExtensionRegistrar}
	 *
	 * @param extensionClass The test extension class to be registered
	 */
	public void addExtension(Class<? extends TestExtension> extensionClass) {
		boolean extensionExists = getRegisteredExtensionClasses().stream().anyMatch(
			registeredClass -> registeredClass.equals(extensionClass));
		if (!extensionExists) {
			TestExtension testExtension = ReflectionUtils.newInstance(extensionClass);
			registerExtensionPointImplementors(testExtension);
			registerFromExtensionRegistrar(testExtension);
			this.registeredExtensionClasses.add(extensionClass);
		}
	}

	private void registerFromExtensionRegistrar(TestExtension testExtension) {
		if (testExtension instanceof ExtensionRegistrar) {
			ExtensionRegistrar extensionRegistrar = (ExtensionRegistrar) testExtension;
			ExtensionRegistry extensionRegistry = new TestExtensionRegistry.LocalExtensionRegistry();
			extensionRegistrar.registerExtensions(extensionRegistry);
		}
	}

	private void registerExtensionPointImplementors(TestExtension testExtension) {
		if (testExtension instanceof ExtensionPoint) {
			ExtensionPoint extension = (ExtensionPoint) testExtension;
			registerExtension(extension, Position.DEFAULT);
		}
	}

	private <E extends ExtensionPoint> void registerExtension(E extension, Position position) {
		RegisteredExtensionPoint<E> registeredExtensionPoint = new RegisteredExtensionPoint<>(extension, position);
		registeredExtensionPoints.add(registeredExtensionPoint);
	}

	@SuppressWarnings("unused")
	private static class RegisteredExtensionPoint<T extends ExtensionPoint> {
		private final T extensionPoint;
		private final Position position;

		private RegisteredExtensionPoint(T extensionPoint, Position position) {
			this.extensionPoint = extensionPoint;
			this.position = position;
		}
	}

	/**
	 * Public for testing purposes only
	 */
	public class LocalExtensionRegistry implements ExtensionRegistry {
		@Override
		public <E extends ExtensionPoint> void register(E extension, Class<E> extensionPointType, Position position) {
			registerExtension(extension, position);
		}
	}
}
