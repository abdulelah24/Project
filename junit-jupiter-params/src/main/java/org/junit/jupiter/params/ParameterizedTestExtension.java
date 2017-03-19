/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.params;

import static org.junit.platform.commons.util.AnnotationUtils.findAnnotation;
import static org.junit.platform.commons.util.AnnotationUtils.findRepeatableAnnotations;
import static org.junit.platform.commons.util.AnnotationUtils.isAnnotated;

import java.lang.reflect.Method;
import java.util.stream.Stream;

import org.junit.jupiter.api.extension.ContainerExtensionContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;
import org.junit.jupiter.api.extension.TestTemplateInvocationContextProvider;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.AnnotationInitializer;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * @since 5.0
 */
class ParameterizedTestExtension implements TestTemplateInvocationContextProvider {

	@Override
	public boolean supports(ContainerExtensionContext context) {
		return isAnnotated(context.getTestMethod(), ParameterizedTest.class);
	}

	@Override
	public Stream<TestTemplateInvocationContext> provide(ContainerExtensionContext context) {
		// TODO #14 Test that Streams returned by providers are closed
		Method templateMethod = Preconditions.notNull(context.getTestMethod().orElse(null),
			"test method must not be null");
		ParameterizedTestNameFormatter formatter = createNameFormatter(templateMethod);
		// @formatter:off
		return findRepeatableAnnotations(templateMethod, ArgumentsSource.class)
				.stream()
				.map(ArgumentsSource::value)
				.map(ReflectionUtils::newInstance)
				.map(provider -> AnnotationInitializer.initialize(templateMethod, provider))
				.flatMap(provider -> arguments(provider, context))
				.map(Arguments::get)
				.map(arguments -> new ParameterizedTestInvocationContext(formatter, arguments));
		// @formatter:on
	}

	private ParameterizedTestNameFormatter createNameFormatter(Method templateMethod) {
		String name = findAnnotation(templateMethod, ParameterizedTest.class).get().name();
		return new ParameterizedTestNameFormatter(name);
	}

	private static Stream<? extends Arguments> arguments(ArgumentsProvider provider,
			ContainerExtensionContext context) {
		try {
			return provider.arguments(context);
		}
		catch (Exception e) {
			// TODO #14 Test
			throw ExceptionUtils.throwAsUncheckedException(e);
		}
	}

}
