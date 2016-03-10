/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4.discovery;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toCollection;
import static org.junit.gen5.engine.junit4.descriptor.JUnit4TestDescriptor.DEFAULT_SEPARATOR;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.function.IntFunction;
import java.util.logging.Logger;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.junit4.descriptor.JUnit4TestDescriptor;
import org.junit.gen5.engine.junit4.descriptor.RunnerTestDescriptor;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runners.model.RunnerBuilder;

class TestClassRequestResolver {

	private final TestDescriptor engineDescriptor;
	private final Logger logger;

	private final UniqueIdReader uniqueIdReader;
	private final UniqueIdStringifier uniqueIdStringifier = new UniqueIdStringifier();

	TestClassRequestResolver(TestDescriptor engineDescriptor, Logger logger) {
		this.engineDescriptor = engineDescriptor;
		this.logger = logger;
		this.uniqueIdReader = new UniqueIdReader(logger);
	}

	void populateEngineDescriptorFrom(Set<TestClassRequest> requests) {
		RunnerBuilder runnerBuilder = new DefensiveAllDefaultPossibilitiesBuilder();
		for (TestClassRequest request : requests) {
			Class<?> testClass = request.getTestClass();
			Runner runner = runnerBuilder.safeRunnerForClass(testClass);
			if (runner != null) {
				addRunnerTestDescriptor(request, testClass, runner);
			}
		}
	}

	private void addRunnerTestDescriptor(TestClassRequest request, Class<?> testClass, Runner runner) {
		RunnerTestDescriptor runnerTestDescriptor = determineRunnerTestDescriptor(testClass, runner,
			request.getFilters());
		engineDescriptor.addChild(runnerTestDescriptor);
	}

	private RunnerTestDescriptor determineRunnerTestDescriptor(Class<?> testClass, Runner runner,
			List<RunnerTestDescriptorAwareFilter> filters) {
		RunnerTestDescriptor runnerTestDescriptor = createCompleteRunnerTestDescriptor(testClass, runner);
		if (!filters.isEmpty()) {
			if (runner instanceof Filterable) {
				Filter filter = createOrFilter(filters, runnerTestDescriptor);
				Runner filteredRunner = runnerTestDescriptor.toRequest().filterWith(filter).getRunner();
				runnerTestDescriptor = createCompleteRunnerTestDescriptor(testClass, filteredRunner);
			}
			else {
				logger.warning(() -> "Runner " + runner.getClass().getName() //
						+ " (used on " + testClass.getName() + ") does not support filtering" //
						+ " and will therefore be run completely.");
			}
		}
		return runnerTestDescriptor;
	}

	private Filter createOrFilter(List<RunnerTestDescriptorAwareFilter> filters,
			RunnerTestDescriptor runnerTestDescriptor) {
		filters.stream().forEach(filter -> filter.initialize(runnerTestDescriptor));
		return new OrFilter(filters);
	}

	private RunnerTestDescriptor createCompleteRunnerTestDescriptor(Class<?> testClass, Runner runner) {
		RunnerTestDescriptor runnerTestDescriptor = new RunnerTestDescriptor(engineDescriptor, testClass, runner);
		addChildrenRecursively(runnerTestDescriptor);
		return runnerTestDescriptor;
	}

	private void addChildrenRecursively(JUnit4TestDescriptor parent) {
		List<Description> children = parent.getDescription().getChildren();
		// Use LinkedHashMap to preserve order, ArrayList for fast access by index
		Map<String, List<Description>> childrenByUniqueId = children.stream().collect(
			groupingBy(uniqueIdReader.andThen(uniqueIdStringifier), LinkedHashMap::new, toCollection(ArrayList::new)));
		for (Entry<String, List<Description>> entry : childrenByUniqueId.entrySet()) {
			String uniqueId = entry.getKey();
			List<Description> childrenWithSameUniqueId = entry.getValue();
			IntFunction<String> uniqueIdGenerator = determineUniqueIdGenerator(uniqueId, childrenWithSameUniqueId);
			for (int index = 0, size = childrenWithSameUniqueId.size(); index < size; index++) {
				String reallyUniqueId = uniqueIdGenerator.apply(index);
				Description description = childrenWithSameUniqueId.get(index);
				JUnit4TestDescriptor child = new JUnit4TestDescriptor(parent, DEFAULT_SEPARATOR, reallyUniqueId,
					description);
				parent.addChild(child);
				addChildrenRecursively(child);
			}
		}
	}

	private IntFunction<String> determineUniqueIdGenerator(String uniqueId,
			List<Description> childrenWithSameUniqueId) {
		if (childrenWithSameUniqueId.size() == 1) {
			return index -> uniqueId;
		}
		return index -> uniqueId + "[" + index + "]";
	}
}
