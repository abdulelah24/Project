/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4.execution;

import org.junit.runner.Request;
import org.junit.runner.Runner;

class RunnerRequest extends Request {

	private final Runner runner;

	RunnerRequest(Runner runner) {
		this.runner = runner;
	}

	@Override
	public Runner getRunner() {
		return runner;
	}
}
