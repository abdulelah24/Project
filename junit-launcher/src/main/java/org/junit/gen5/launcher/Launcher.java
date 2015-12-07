/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher;

import static java.util.stream.Collectors.toSet;
import static java.util.stream.StreamSupport.stream;

import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;

import org.junit.gen5.engine.ExecutionRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.TestExecutionListener;
import org.junit.gen5.engine.TestPlanSpecification;

/**
 * @since 5.0
 */
public class Launcher {

	private static final Logger LOG = Logger.getLogger(Launcher.class.getName());

	private final TestListenerRegistry listenerRegistry = new TestListenerRegistry();
	private final TestEngineRegistry testEngineRegistry;

	public Launcher() {
		this(new ServiceLoaderTestEngineRegistry());
	}

	// for tests only
	Launcher(TestEngineRegistry testEngineRegistry) {
		this.testEngineRegistry = testEngineRegistry;
	}

	public void registerTestPlanExecutionListeners(TestExecutionListener... testListeners) {
		listenerRegistry.registerListener(testListeners);
	}

	public TestPlan discover(TestPlanSpecification specification) {
		return TestPlan.from(discoverRootDescriptor(specification));
	}

	private RootTestDescriptor discoverRootDescriptor(TestPlanSpecification specification) {
		RootTestDescriptor root = new RootTestDescriptor();
		for (TestEngine testEngine : testEngineRegistry.lookupAllTestEngines()) {
			LOG.info("Discovering tests in engine " + testEngine.getId());
			TestDescriptor engineRoot = testEngine.discoverTests(specification);
			root.addTestDescriptorForEngine(testEngine, engineRoot);
		}
		root.applyFilters(specification);
		root.prune();
		return root;
	}

	public void execute(TestPlanSpecification specification) {
		execute(discoverRootDescriptor(specification));
	}

	private void execute(RootTestDescriptor root) {
		TestPlanExecutionListener testPlanExecutionListener = listenerRegistry.getCompositeTestPlanExecutionListener();
		TestExecutionListener testExecutionListener = listenerRegistry.getCompositeTestExecutionListener();

		TestPlan testPlan = TestPlan.from(root);
		testPlanExecutionListener.testPlanExecutionStarted(testPlan);
		for (TestEngine testEngine : getAvailableEngines()) {
			Optional<TestDescriptor> testDescriptorOptional = root.getTestDescriptorFor(testEngine);
			testDescriptorOptional.ifPresent(testDescriptor -> {
				testPlanExecutionListener.testPlanExecutionStartedOnEngine(testPlan, testEngine);
				testEngine.execute(new ExecutionRequest(testDescriptor, testExecutionListener));
				testPlanExecutionListener.testPlanExecutionFinishedOnEngine(testPlan, testEngine);
			});
		}
		testPlanExecutionListener.testPlanExecutionFinished(testPlan);
	}

	public Set<TestEngine> getAvailableEngines() {
		return stream(testEngineRegistry.lookupAllTestEngines().spliterator(), false).collect(toSet());
	}

}
