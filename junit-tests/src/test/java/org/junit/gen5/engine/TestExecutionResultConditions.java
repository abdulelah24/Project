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
import static org.junit.gen5.commons.util.FunctionUtils.where;

import org.assertj.core.api.Condition;
import org.junit.gen5.engine.TestExecutionResult.Status;

/**
 * Collection of AssertJ conditions for {@link TestExecutionResult}.
 */
public class TestExecutionResultConditions {

	public static Condition<TestExecutionResult> status(Status expectedStatus) {
		return new Condition<>(where(TestExecutionResult::getStatus, isEqual(expectedStatus)), "status is %s",
			expectedStatus);
	}

	public static Condition<Throwable> message(String expectedMessage) {
		return new Condition<>(where(Throwable::getMessage, isEqual(expectedMessage)), "message is \"%s\"",
			expectedMessage);
	}

	public static Condition<Throwable> isA(Class<? extends Throwable> expectedClass) {
		return new Condition<>(expectedClass::isInstance, "instance of %s", expectedClass.getName());
	}

	public static Condition<Throwable> suppressed(int index, Condition<Throwable> checked) {
		return new Condition<>(throwable -> checked.matches(throwable.getSuppressed()[index]),
			"suppressed at index %d matches %s", index, checked);

	}

	public static Condition<TestExecutionResult> cause(Condition<? super Throwable> condition) {
		return new Condition<TestExecutionResult>(where(TestExecutionResult::getThrowable, throwable -> {
			return throwable.isPresent() && condition.matches(throwable.get());
		}), "cause where %s", condition);
	}

}
