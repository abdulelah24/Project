/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.tests.process;

public class WatchedProcess {

	private final Process process;
	private final WatchedOutput out;
	private final WatchedOutput err;

	WatchedProcess(Process process, WatchedOutput out, WatchedOutput err) {
		this.process = process;
		this.out = out;
		this.err = err;
	}

	ProcessResult waitFor() throws InterruptedException {
		try {
			int exitCode;
			try {
				try {
					exitCode = process.waitFor();
				}
				catch (InterruptedException e) {
					process.destroyForcibly();
					throw e;
				}
			}
			finally {
				try {
					out.thread().join();
				}
				finally {
					err.thread().join();
				}
			}
			return new ProcessResult(exitCode, out.streamAsString(), err.streamAsString());
		}
		finally {
			process.destroyForcibly();
		}
	}
}
