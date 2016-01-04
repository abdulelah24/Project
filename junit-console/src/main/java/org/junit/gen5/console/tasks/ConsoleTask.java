/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.console.tasks;

import java.io.PrintWriter;

/**
 * A task to be executed from the console.
 */
public interface ConsoleTask {

	/**
	 * Exit code indicating successful execution
	 */
	int SUCCESS = 0;

	/**
	 * Execute this task and return an exit code.
	 *
	 * @param out writer for console output
	 * @return exit code indicating success ({@code 0}) or failure ({@code != 0})
	 * @see ConsoleTask#SUCCESS
	 */
	int execute(PrintWriter out) throws Exception;

}
