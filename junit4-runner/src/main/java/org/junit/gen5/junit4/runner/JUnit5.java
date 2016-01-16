/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.junit4.runner;

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;
import static org.junit.gen5.engine.specification.dsl.ClassFilters.classNameMatches;
import static org.junit.gen5.engine.specification.dsl.ClassSelectorBuilder.forClass;
import static org.junit.gen5.engine.specification.dsl.DiscoveryRequestBuilder.request;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.gen5.engine.DiscoveryRequest;
import org.junit.gen5.engine.DiscoverySelector;
import org.junit.gen5.engine.PostDiscoveryFilter;
import org.junit.gen5.engine.specification.dsl.*;
import org.junit.gen5.launcher.Launcher;
import org.junit.gen5.launcher.TestIdentifier;
import org.junit.gen5.launcher.TestPlan;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.model.InitializationError;

/**
 * JUnit 4 based {@link Runner} which runs tests that use the JUnit 5 programming and extension models.
 *
 * <p>Annotating a class with {@code @RunWith(JUnit5.class)} allows it to be run with IDEs and build systems that
 * support JUnit 4 but do not yet support the JUnit 5 APIs directly.
 *
 * <p>Consult the various annotations in this package for configuration options.
 *
 * <p>If you don't use any annotations, you can simply us this runner on a JUnit 5 test class.
 * Contrary to standard JUnit 5 test classes, the test class must be {@code public} in order
 * to be picked up by IDEs and build tools.
 *
 * @since 5.0
 * @see Classes
 * @see ClassNamePattern
 * @see Packages
 * @see UniqueIds
 * @see OnlyIncludeTags
 * @see ExcludeTags
 * @see OnlyEngine
 */
public class JUnit5 extends Runner implements Filterable {

	private static final Class<?>[] EMPTY_CLASS_ARRAY = new Class<?>[0];
	private static final String[] EMPTY_STRING_ARRAY = new String[0];
	private static final String EMPTY_STRING = "";

	private final Launcher launcher = new Launcher();
	private final Class<?> testClass;
	private DiscoveryRequest specification;
	private JUnit5TestTree testTree;

	public JUnit5(Class<?> testClass) throws InitializationError {
		this.testClass = testClass;
		this.specification = createSpecification();
		this.testTree = generateTestTree();
	}

	@Override
	public Description getDescription() {
		return this.testTree.getSuiteDescription();
	}

	@Override
	public void run(RunNotifier notifier) {
		JUnit5RunnerListener listener = new JUnit5RunnerListener(this.testTree, notifier);
		this.launcher.registerTestExecutionListeners(listener);
		this.launcher.execute(this.specification);
	}

	private JUnit5TestTree generateTestTree() {
		Preconditions.notNull(this.specification, "DiscoveryRequest must not be null");
		TestPlan plan = this.launcher.discover(this.specification);
		return new JUnit5TestTree(plan, testClass);
	}

	private DiscoveryRequest createSpecification() {
		List<DiscoverySelector> selectors = getSpecElementsFromAnnotations();

		// Allows to simply add @RunWith(JUnit5.class) to any JUnit5 test case
		if (selectors.isEmpty()) {
			selectors.add(forClass(this.testClass));
		}

		DiscoveryRequest request = request().select(selectors).build();
		addFiltersFromAnnotations(request);
		return request;
	}

	private void addFiltersFromAnnotations(DiscoveryRequest request) {
		addClassNameMatchesFilter(request);
		addIncludeTagsFilter(request);
		addExcludeTagsFilter(request);
	}

	private List<DiscoverySelector> getSpecElementsFromAnnotations() {
		List<DiscoverySelector> selectors = new ArrayList<>();
		selectors.addAll(getClassSpecificationElements());
		selectors.addAll(getUniqueIdSpecificationElements());
		selectors.addAll(getPackageSpecificationElements());
		return selectors;
	}

	private List<DiscoverySelector> getClassSpecificationElements() {
		return stream(getTestClasses()).map(ClassSelectorBuilder::forClass).collect(toList());
	}

	private List<DiscoverySelector> getUniqueIdSpecificationElements() {
		return stream(getUniqueIds()).map(UniqueIdSelectorBuilder::byUniqueId).collect(toList());
	}

	private List<DiscoverySelector> getPackageSpecificationElements() {
		return stream(getPackageNames()).map(PackageSelectorBuilder::byPackageName).collect(toList());
	}

	private void addClassNameMatchesFilter(DiscoveryRequest plan) {
		String regex = getClassNameRegExPattern();
		if (!regex.isEmpty()) {
			plan.addFilter(classNameMatches(regex));
		}
	}

	private void addIncludeTagsFilter(DiscoveryRequest plan) {
		String[] includeTags = getIncludeTags();
		if (includeTags.length > 0) {
			PostDiscoveryFilter tagNamesFilter = TagFilterBuilder.includeTags(includeTags);
			plan.addPostFilter(tagNamesFilter);
		}
	}

	private void addExcludeTagsFilter(DiscoveryRequest plan) {
		String[] excludeTags = getExcludeTags();
		if (excludeTags.length > 0) {
			PostDiscoveryFilter excludeTagsFilter = TagFilterBuilder.excludeTags(excludeTags);
			plan.addPostFilter(excludeTagsFilter);
		}
	}

	private Class<?>[] getTestClasses() {
		return getValueFromAnnotation(Classes.class, Classes::value, EMPTY_CLASS_ARRAY);
	}

	private String[] getUniqueIds() {
		return getValueFromAnnotation(UniqueIds.class, UniqueIds::value, EMPTY_STRING_ARRAY);
	}

	private String[] getPackageNames() {
		return getValueFromAnnotation(Packages.class, Packages::value, EMPTY_STRING_ARRAY);
	}

	private String[] getIncludeTags() {
		return getValueFromAnnotation(OnlyIncludeTags.class, OnlyIncludeTags::value, EMPTY_STRING_ARRAY);
	}

	private String[] getExcludeTags() {
		return getValueFromAnnotation(ExcludeTags.class, ExcludeTags::value, EMPTY_STRING_ARRAY);
	}

	private String getExplicitEngineId() {
		return getValueFromAnnotation(OnlyEngine.class, OnlyEngine::value, EMPTY_STRING).trim();
	}

	private String getClassNameRegExPattern() {
		return getValueFromAnnotation(ClassNamePattern.class, ClassNamePattern::value, EMPTY_STRING).trim();
	}

	private <A extends Annotation, V> V getValueFromAnnotation(Class<A> annotationClass, Function<A, V> extractor,
			V defaultValue) {
		A annotation = this.testClass.getAnnotation(annotationClass);
		return (annotation != null ? extractor.apply(annotation) : defaultValue);
	}

	@Override
	public void filter(Filter filter) throws NoTestsRemainException {
		Set<TestIdentifier> filteredIdentifiers = testTree.getFilteredLeaves(filter);
		if (filteredIdentifiers.isEmpty()) {
			throw new NoTestsRemainException();
		}
		this.specification = createTestPlanSpecificationForUniqueIds(filteredIdentifiers);
		this.testTree = generateTestTree();
	}

	private DiscoveryRequest createTestPlanSpecificationForUniqueIds(Set<TestIdentifier> testIdentifiers) {
		// @formatter:off
		List<DiscoverySelector> elements = testIdentifiers.stream()
				.map(TestIdentifier::getUniqueId)
				.map(Object::toString)
				.map(UniqueIdSelectorBuilder::byUniqueId)
				.collect(toList());
		// @formatter:on
		return request().select(elements).build();
	}

}
