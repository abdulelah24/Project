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

import static java.util.stream.Collectors.toList;
import static java.util.stream.StreamSupport.stream;

import java.util.ServiceLoader;
import java.util.logging.Logger;

import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.engine.TestEngine;

/**
 * @since 5.0
 */
class ServiceLoaderTestEngineRegistry implements TestEngineRegistry {
	private static final Logger LOG = Logger.getLogger(ServiceLoaderTestEngineRegistry.class.getName());
	private static Iterable<TestEngine> testEngines;

	@Override
	public synchronized Iterable<TestEngine> getTestEngines() {
		if (testEngines == null) {
			testEngines = ServiceLoader.load(TestEngine.class, ReflectionUtils.getDefaultClassLoader());
			for (TestEngine testEngine : testEngines) {
				LOG.info(() -> String.format("Discovered test engine with id: '%s'", testEngine.getId()));
				testEngine.initialize();
				LOG.info(
					() -> String.format("Initialization finished for test engine with id: '%s'", testEngine.getId()));
			}
		}
		return testEngines;
	}
}
