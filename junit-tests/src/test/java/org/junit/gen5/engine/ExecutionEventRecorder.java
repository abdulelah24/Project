/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import static java.util.function.Predicate.isEqual;
import static java.util.stream.Collectors.toList;
import static org.junit.gen5.commons.util.FunctionUtils.where;
import static org.junit.gen5.engine.ExecutionEvent.*;
import static org.junit.gen5.engine.ExecutionEvent.Type.*;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.junit.gen5.engine.ExecutionEvent.Type;
import org.junit.gen5.engine.TestExecutionResult.Status;

/**
 * {@link EngineExecutionListener} that records all events and makes them available to tests.
 *
 * @see ExecutionEvent
 */
public class ExecutionEventRecorder implements EngineExecutionListener {

	public static List<ExecutionEvent> execute(TestEngine testEngine, TestPlanSpecification testPlanSpecification) {
		TestDescriptor engineTestDescriptor = testEngine.discoverTests(testPlanSpecification);
		ExecutionEventRecorder listener = new ExecutionEventRecorder();
		testEngine.execute(new ExecutionRequest(engineTestDescriptor, listener));
		return listener.getExecutionEvents();
	}

	public final List<ExecutionEvent> executionEvents = new CopyOnWriteArrayList<>();

	@Override
	public void reportingEntryPublished(TestDescriptor testDescriptor, Map<String, String> entry) {
		addEvent(ExecutionEvent.reportingEntryPublished(testDescriptor, entry));
	}

	@Override
	public void dynamicTestRegistered(TestDescriptor testDescriptor) {
		addEvent(ExecutionEvent.dynamicTestRegistered(testDescriptor));
	}

	@Override
	public void executionSkipped(TestDescriptor testDescriptor, String reason) {
		addEvent(ExecutionEvent.executionSkipped(testDescriptor, reason));
	}

	@Override
	public void executionStarted(TestDescriptor testDescriptor) {
		addEvent(ExecutionEvent.executionStarted(testDescriptor));
	}

	@Override
	public void executionFinished(TestDescriptor testDescriptor, TestExecutionResult result) {
		addEvent(ExecutionEvent.executionFinished(testDescriptor, result));
	}

	public List<ExecutionEvent> getExecutionEvents() {
		return executionEvents;
	}

	public Stream<ExecutionEvent> eventStream() {
		return getExecutionEvents().stream();
	}

	public long getTestSkippedCount() {
		return testEventsByType(SKIPPED).count();
	}

	public long getTestStartedCount() {
		return testEventsByType(STARTED).count();
	}

	public long getReportingEntryPublishedCount() {
		return testEventsByType(REPORTING_ENTRY_PUBLISHED).count();
	}

	public long getTestFinishedCount() {
		return testEventsByType(FINISHED).count();
	}

	public long getTestSuccessfulCount() {
		return getTestFinishedCount(Status.SUCCESSFUL);
	}

	public long getTestAbortedCount() {
		return getTestFinishedCount(Status.ABORTED);
	}

	public long getTestFailedCount() {
		return getTestFinishedCount(Status.FAILED);
	}

	public long getContainerSkippedCount() {
		return containerEventsByType(SKIPPED).count();
	}

	public long getContainerStartedCount() {
		return containerEventsByType(STARTED).count();
	}

	public long getContainerFinishedCount() {
		return containerEventsByType(FINISHED).count();
	}

	public List<ExecutionEvent> getFailedTestFinishedEvents() {
		return testFinishedEvents(Status.FAILED).collect(toList());
	}

	private long getTestFinishedCount(Status status) {
		return testFinishedEvents(status).count();
	}

	private Stream<ExecutionEvent> testFinishedEvents(Status status) {
		return testEventsByType(FINISHED).filter(
			byPayload(TestExecutionResult.class, where(TestExecutionResult::getStatus, isEqual(status))));
	}

	private Stream<ExecutionEvent> testEventsByType(Type type) {
		return eventsByTypeAndTestDescriptor(type, TestDescriptor::isTest);
	}

	private Stream<ExecutionEvent> containerEventsByType(Type type) {
		return eventsByTypeAndTestDescriptor(type, TestDescriptor::isContainer);
	}

	private Stream<ExecutionEvent> eventsByTypeAndTestDescriptor(Type type,
			Predicate<? super TestDescriptor> predicate) {
		return eventStream().filter(byType(type).and(byTestDescriptor(predicate)));
	}

	private void addEvent(ExecutionEvent event) {
		executionEvents.add(event);
	}

}
