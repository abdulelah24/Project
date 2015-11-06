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

import static org.junit.gen5.launcher.TestEngineRegistry.lookupAllTestEngines;

import java.util.Collection;
import java.util.List;

import org.junit.gen5.engine.EngineDescriptor;
import org.junit.gen5.engine.EngineExecutionContext;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.TestExecutionListener;
import org.junit.gen5.engine.TestPlanSpecification;

/**
 * @author Stefan Bechtold
 * @author Sam Brannen
 * @since 5.0
 */
public class Launcher {

	private final TestListenerRegistry listenerRegistry = new TestListenerRegistry();

	public void registerTestPlanExecutionListeners(TestExecutionListener... testListeners) {
		listenerRegistry.registerListener(testListeners);
	}

	public TestPlan discover(TestPlanSpecification specification) {
		TestPlan testPlan = new TestPlan();
		for (TestEngine testEngine : lookupAllTestEngines()) {
			Collection<TestDescriptor> testDescriptors = testEngine.discoverTests(specification);
			if (!testDescriptors.isEmpty()) {
				testPlan.addTestDescriptors(testDescriptors);
			}
		}
		return testPlan;
	}

	public void execute(TestPlanSpecification specification) {
		execute(discover(specification));
	}

	public void execute(TestPlan testPlan) {
		TestPlanExecutionListener testPlanExecutionListener = listenerRegistry.getCompositeTestPlanExecutionListener();
		TestExecutionListener testExecutionListener = listenerRegistry.getCompositeTestExecutionListener();

		testPlanExecutionListener.testPlanExecutionStarted(testPlan);
		for (TestEngine testEngine : lookupAllTestEngines()) {
			testPlanExecutionListener.testPlanExecutionStartedOnEngine(testPlan, testEngine);
			List<TestDescriptor> testDescriptors = testPlan.getAllTestDescriptorsForTestEngine(testEngine);
			testEngine.execute(new EngineExecutionContext(testDescriptors, testExecutionListener));
			testPlanExecutionListener.testPlanExecutionFinishedOnEngine(testPlan, testEngine);
		}
		testPlanExecutionListener.testPlanExecutionFinished(testPlan);
	}
}
