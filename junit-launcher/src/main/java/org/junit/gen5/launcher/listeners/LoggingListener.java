/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher.listeners;

import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.gen5.engine.TestExecutionResult;
import org.junit.gen5.launcher.TestExecutionListener;
import org.junit.gen5.launcher.TestIdentifier;
import org.junit.gen5.launcher.TestPlan;

/**
 * Simple {@link TestExecutionListener} for logging informational messages
 * for all events via a {@link BiConsumer} that consumes {@code Throwable}
 * and {@code Supplier<String>}.
 *
 * @since 5.0
 * @see #forJavaUtilLogging()
 * @see #forJavaUtilLogging(Level)
 * @see LoggingListener#LoggingListener(BiConsumer)
 */
public class LoggingListener implements TestExecutionListener {

	/**
	 * Create a {@code LoggingListener} which delegates to a
	 * {@link java.util.logging.Logger} using a log level of
	 * {@link Level#FINE FINE}.
	 * @see #forJavaUtilLogging(Level)
	 */
	public static LoggingListener forJavaUtilLogging() {
		return forJavaUtilLogging(Level.FINE);
	}

	/**
	 * Create a {@code LoggingListener} which delegates to a
	 * {@link java.util.logging.Logger} using the supplied
	 * {@linkplain Level log level}.
	 *
	 * @param logLevel the log level to use
	 * @see #forJavaUtilLogging()
	 */
	public static LoggingListener forJavaUtilLogging(Level logLevel) {
		Logger logger = Logger.getLogger(LoggingListener.class.getName());
		return new LoggingListener((t, messageSupplier) -> logger.log(logLevel, t, messageSupplier));
	}

	private final BiConsumer<Throwable, Supplier<String>> logger;

	/**
	 * Create a {@code LoggingListener} which delegates to the supplied
	 * {@link BiConsumer} for consumption of logging messages.
	 * @param logger a logger implemented as a {@code BiConsumer}
	 *
	 * @see #forJavaUtilLogging()
	 * @see #forJavaUtilLogging(Level)
	 */
	public LoggingListener(BiConsumer<Throwable, Supplier<String>> logger) {
		this.logger = logger;
	}

	@Override
	public void testPlanExecutionStarted(TestPlan testPlan) {
		log("TestPlan Execution Started: %s", testPlan);
	}

	@Override
	public void testPlanExecutionFinished(TestPlan testPlan) {
		log("TestPlan Execution Finished: %s", testPlan);
	}

	@Override
	public void dynamicTestRegistered(TestIdentifier testIdentifier) {
		log("Dynamic Test Registered: %s - %s", testIdentifier.getDisplayName(), testIdentifier.getUniqueId());
	}

	@Override
	public void executionStarted(TestIdentifier testIdentifier) {
		log("Execution Started: %s - %s", testIdentifier.getDisplayName(), testIdentifier.getUniqueId());
	}

	@Override
	public void executionSkipped(TestIdentifier testIdentifier, String reason) {
		log("Execution Skipped: %s - %s - %s", testIdentifier.getDisplayName(), testIdentifier.getUniqueId(), reason);
	}

	@Override
	public void executionFinished(TestIdentifier testIdentifier, TestExecutionResult testExecutionResult) {
		logWithThrowable("Execution Finished: %s - %s - %s", testExecutionResult.getThrowable().orElse(null),
			testIdentifier.getDisplayName(), testIdentifier.getUniqueId(), testExecutionResult);
	}

	private void log(String message, Object... args) {
		logWithThrowable(message, null, args);
	}

	private void logWithThrowable(String message, Throwable t, Object... args) {
		logger.accept(t, () -> String.format(message, args));
	}

}
