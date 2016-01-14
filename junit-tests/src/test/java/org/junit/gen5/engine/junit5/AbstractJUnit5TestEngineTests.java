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

import static org.junit.gen5.engine.TestPlanSpecification.*;

import org.junit.gen5.api.BeforeEach;
import org.junit.gen5.engine.EngineDescriptor;
import org.junit.gen5.engine.ExecutionEventRecorder;
import org.junit.gen5.engine.ExecutionRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestPlanSpecification;

/**
 * Abstract base class for tests involving the {@link JUnit5TestEngine}.
 *
 * @since 5.0
 */
abstract class AbstractJUnit5TestEngineTests {

	private final JUnit5TestEngine engine = new JUnit5TestEngine();

	@BeforeEach
	void initListeners() {
	}

	protected ExecutionEventRecorder executeTestsForClass(Class<?> testClass) {
		return executeTests(build(forClass(testClass)));
	}

	protected ExecutionEventRecorder executeTests(TestPlanSpecification spec) {
		TestDescriptor testDescriptor = discoverTests(spec);
		ExecutionEventRecorder eventRecorder = new ExecutionEventRecorder();
		engine.execute(new ExecutionRequest(testDescriptor, eventRecorder));
		return eventRecorder;
	}

	protected EngineDescriptor discoverTests(TestPlanSpecification spec) {
		return engine.discoverTests(spec);
	}

}
