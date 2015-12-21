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

import java.util.*;

import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.TestExecutionResult;
import org.junit.gen5.engine.TestExecutionResult.Status;

/**
 * Register an instance of this class with a {@link Launcher} to be notified of
 * events that occur during test execution.
 *
 * <p>All methods in this class have empty <em>default</em> implementations.
 * Concrete implementations may therefore override one or more of these
 * methods to be notified of the selected events.
 *
 * <p>JUnit provides two example implementations:
 * <ul>
 * <li>{@link org.junit.gen5.launcher.listeners.LoggingListener}</li>
 * <li>{@link org.junit.gen5.launcher.listeners.SummaryGeneratingListener}</li>
 * </ul>
 *
 * <p>Contrary to JUnit 4, {@linkplain org.junit.gen5.engine.TestEngine test engines}
 * are supposed to report events not only for {@linkplain TestIdentifier identifiers}
 * that represent executable leaves in the {@linkplain TestPlan test plan} but also
 * for all intermediate containers. However, while both the JUnit 4 and JUnit 5 engines
 * comply with this contract, there is no way to guarantee this for third-party
 * engines.
 *
 * @since 5.0
 * @see Launcher
 * @see TestPlan
 * @see TestIdentifier
 */
public interface TestExecutionListener {

	/**
	 * Called when a {@link TestIdentifier} is publishing addition test
	 * reporting data
	 * <em>before</em> any test has been executed.
	 *
	 * @param testIdentifier describes the tree of tests about to be executed
	 */
	default void reportingEntryPublished(TestIdentifier testIdentifier, Map<String, String> entry) {

	}

	/**
	 * Called when the execution of the {@link TestPlan} has started,
	 * <em>before</em> any test has been executed.
	 *
	 * @param testPlan describes the tree of tests about to be executed
	 */
	default void testPlanExecutionStarted(TestPlan testPlan) {
	}

	/**
	 * Called when the execution of the {@link TestPlan} has finished,
	 * <em>after</em> all tests have been executed.
	 *
	 * @param testPlan describes the tree of tests that have been executed
	 */
	default void testPlanExecutionFinished(TestPlan testPlan) {
	}

	/**
	 * Called when a new, dynamic {@link TestIdentifier} has been registered.
	 *
	 * <p>A <em>dynamic test</em> is a test that is not known a-priori and
	 * therefore not contained in the original {@link TestPlan}.
	 *
	 * @param testIdentifier the identifier of the newly registered test
	 * or container
	 */
	default void dynamicTestRegistered(TestIdentifier testIdentifier) {
	}

	/**
	 * Called when the execution of a leaf or subtree of the {@link TestPlan}
	 * has been skipped.
	 *
	 * <p>The {@link TestIdentifier} may represent a test or a container.
	 *
	 * <p>A skipped test or subtree of tests will never be reported as
	 * {@linkplain #executionStarted started} or
	 * {@linkplain #executionFinished finished}.
	 *
	 * @param testIdentifier the identifier of the skipped test or container
	 * @param reason a human-readable message describing why the execution
	 * has been skipped
	 */
	default void executionSkipped(TestIdentifier testIdentifier, String reason) {
	}

	/**
	 * Called when the execution of a leaf or subtree of the {@link TestPlan}
	 * is about to be started.
	 *
	 * <p>The {@link TestIdentifier} may represent a test or a container.
	 *
	 * <p>This method will only be called if the test or container has not
	 * been {@linkplain #executionSkipped skipped}.
	 *
	 * <p>This method will be called for a container {@code TestIdentifier}
	 * <em>before</em> {@linkplain #executionStarted starting} or
	 * {@linkplain #executionSkipped skipping} any of its children.
	 *
	 * @param testIdentifier the identifier of the started test or container
	 */
	default void executionStarted(TestIdentifier testIdentifier) {
	}

	/**
	 * Called when the execution of a leaf or subtree of the {@link TestPlan}
	 * has finished, regardless of the outcome.
	 *
	 * <p>The {@link TestIdentifier} may represent a test or a container.
	 *
	 * <p>This method will only be called if the test or container has not
	 * been {@linkplain #executionSkipped skipped}.
	 *
	 * <p>This method will be called for a container {@code TestIdentifier}
	 * <em>after</em> all of its children have been
	 * {@linkplain #executionSkipped skipped} or have
	 * {@linkplain #executionFinished finished}.
	 *
	 * <p>The {@link TestExecutionResult} describes the result of the execution
	 * for the supplied {@code testIdentifier}. The result does not include or
	 * aggregate the results of its children. For example, a container with a
	 * failing test will be reported as {@link Status#SUCCESSFUL SUCCESSFUL} even
	 * if one or more of its children are reported as {@link Status#FAILED FAILED}.
	 *
	 * @param testIdentifier the identifier of the finished test or container
	 * @param testExecutionResult the (unaggregated) result of the execution for
	 * the supplied {@code TestIdentifier}
	 *
	 * @see TestExecutionResult
	 */
	default void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
	}

}
