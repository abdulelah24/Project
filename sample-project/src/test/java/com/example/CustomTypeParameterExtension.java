/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package com.example;

import java.lang.reflect.Parameter;

import org.junit.gen5.api.extension.ExtensionContext;
import org.junit.gen5.api.extension.ExtensionPointRegistry;
import org.junit.gen5.api.extension.MethodParameterResolver;
import org.junit.gen5.api.extension.TestExtension;

/**
 * @since 5.0
 */
public class CustomTypeParameterExtension implements TestExtension {

	@Override
	public void registerExtensionPoints(ExtensionPointRegistry registry) {
		registry.register(new CustomTypeParameterResolver(), MethodParameterResolver.class);
	}

	private class CustomTypeParameterResolver implements MethodParameterResolver {
		@Override
		public boolean supports(Parameter parameter, ExtensionContext testExecutionContext) {
			return parameter.getType().equals(CustomType.class);
		}
	}
}
