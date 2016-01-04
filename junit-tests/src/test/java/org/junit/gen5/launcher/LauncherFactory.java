/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher;

import static java.util.Arrays.asList;

import org.junit.gen5.engine.TestEngine;

public class LauncherFactory {

	public static Launcher createLauncher(TestEngine... engines) {
		return createLauncher(asList(engines));
	}

	public static Launcher createLauncher(Iterable<TestEngine> engines) {
		return new Launcher(() -> engines);
	}

}
