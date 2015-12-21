/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.execution.injection.sample;

import java.lang.reflect.Parameter;

import org.junit.gen5.api.extension.ExtensionContext;
import org.junit.gen5.api.extension.MethodContext;
import org.junit.gen5.api.extension.MethodParameterResolver;

/**
 * @since 5.0
 */
public class CustomTypeParameterResolver implements MethodParameterResolver {

	@Override
	public boolean supports(Parameter parameter, MethodContext methodContext, ExtensionContext extensionContext) {
		return parameter.getType().equals(CustomType.class);
	}

}
