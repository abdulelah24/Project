/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.extension;

import java.lang.reflect.*;
import java.util.*;

import org.junit.gen5.api.*;
import org.junit.gen5.api.extension.*;

/**
 * {@link MethodParameterResolver} that injects a TestReporter.
 *
 * @since 5.0
 */
public class TestReporterParameterResolver implements MethodParameterResolver {

	@Override
	public boolean supports(Parameter parameter, MethodContext methodContext, ExtensionContext testContext) {
		return (parameter.getType() == TestReporter.class);
	}

	@Override
	public TestReporter resolve(Parameter parameter, MethodContext methodContext, ExtensionContext testContext) {
		return entry -> testContext.publishReportEntry(entry);
	}

}
