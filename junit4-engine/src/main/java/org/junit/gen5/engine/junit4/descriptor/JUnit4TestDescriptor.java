/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4.descriptor;

import static java.util.Arrays.stream;
import static java.util.Optional.ofNullable;
import static java.util.function.Predicate.isEqual;
import static org.junit.gen5.commons.meta.API.Usage.Internal;
import static org.junit.gen5.commons.util.CollectionUtils.getOnlyElement;
import static org.junit.gen5.commons.util.FunctionUtils.where;
import static org.junit.gen5.commons.util.ReflectionUtils.findMethods;

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.experimental.categories.Category;
import org.junit.gen5.commons.meta.API;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestSource;
import org.junit.gen5.engine.TestTag;
import org.junit.gen5.engine.support.descriptor.AbstractTestDescriptor;
import org.junit.gen5.engine.support.descriptor.JavaSource;
import org.junit.runner.Description;

/**
 * @since 5.0
 */
@API(Internal)
public class JUnit4TestDescriptor extends AbstractTestDescriptor {

	public static final String ENGINE_ID = "junit4";
	public static final char DEFAULT_SEPARATOR = '/';

	private final Description description;

	public JUnit4TestDescriptor(TestDescriptor parent, char separator, String uniqueIdSuffix, Description description) {
		this(parent, separator, uniqueIdSuffix, description, toJavaSource(description));
	}

	JUnit4TestDescriptor(TestDescriptor parent, char separator, String uniqueIdSuffix, Description description,
			Optional<? extends TestSource> source) {
		super(parent.getUniqueId() + separator + uniqueIdSuffix);
		this.description = description;
		source.ifPresent(this::setSource);
	}

	public Description getDescription() {
		return description;
	}

	@Override
	public String getName() {
		return description.getDisplayName();
	}

	@Override
	public String getDisplayName() {
		return ofNullable(description.getMethodName()).orElse(description.getDisplayName());
	}

	@Override
	public boolean isTest() {
		return description.isTest();
	}

	@Override
	public boolean isContainer() {
		return description.isSuite();
	}

	@Override
	public Set<TestTag> getTags() {
		Set<TestTag> result = new LinkedHashSet<>();
		getParent().ifPresent(parent -> result.addAll(parent.getTags()));
		// @formatter:off
		getDeclaredCategories().ifPresent(categoryClasses ->
			stream(categoryClasses)
				.map(ReflectionUtils::getAllAssignmentCompatibleClasses)
				.flatMap(Collection::stream)
				.distinct()
				.map(Class::getName)
				.map(TestTag::new)
				.forEachOrdered(result::add)
		);
		// @formatter:on
		return result;
	}

	private Optional<Class<?>[]> getDeclaredCategories() {
		Category annotation = description.getAnnotation(Category.class);
		return ofNullable(annotation).map(Category::value);
	}

	private static Optional<JavaSource> toJavaSource(Description description) {
		Class<?> testClass = description.getTestClass();
		if (testClass != null) {
			String methodName = description.getMethodName();
			if (methodName != null) {
				return Optional.of(toJavaMethodSource(testClass, methodName));
			}
			return Optional.of(new JavaSource(testClass));
		}
		return Optional.empty();
	}

	private static JavaSource toJavaMethodSource(Class<?> testClass, String methodName) {
		List<Method> methods = findMethods(testClass, where(Method::getName, isEqual(methodName)));
		if (methods.size() == 1) {
			return new JavaSource(getOnlyElement(methods));
		}
		return new JavaSource(testClass);
	}

}
