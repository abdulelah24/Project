/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.api.extension;

import java.util.Optional;

/**
 * The result of evaluating a condition e.g. in {@link TestLifecycleExtension}.
 */
public class ConditionEvaluationResult {

	/**
	 * Factory for creating <em>enabled</em> results.
	 */
	public static ConditionEvaluationResult enabled(String reason) {
		return new ConditionEvaluationResult(true, reason);
	}

	/**
	 * Factory for creating <em>disabled</em> results.
	 */
	public static ConditionEvaluationResult disabled(String reason) {
		return new ConditionEvaluationResult(false, reason);
	}

	private final boolean enabled;

	private final Optional<String> reason;

	private ConditionEvaluationResult(boolean enabled, String reason) {
		this.enabled = enabled;
		this.reason = Optional.ofNullable(reason);
	}

	public boolean isDisabled() {
		return !enabled;
	}

	public Optional<String> getReason() {
		return reason;
	}

	@Override
	public String toString() {
		// @formatter:off
		return new StringBuilder(getClass().getSimpleName()).append(" ")
			.append("[")
			.append("enabled = ").append(this.enabled).append(", ")
			.append("reason = ").append(this.reason)
			.append("]")
			.toString();
		// @formatter:on
	}

}
