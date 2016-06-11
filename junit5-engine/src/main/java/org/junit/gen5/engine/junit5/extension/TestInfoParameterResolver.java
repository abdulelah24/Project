/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.extension;

import java.lang.reflect.Method;
import java.util.Optional;
import java.util.Set;

import org.junit.gen5.api.TestInfo;
import org.junit.gen5.api.extension.ExtensionContext;
import org.junit.gen5.api.extension.ParameterContext;
import org.junit.gen5.api.extension.ParameterResolver;
import org.junit.gen5.commons.util.ToStringBuilder;

/**
 * {@link ParameterResolver} that resolves the {@link TestInfo} for
 * the currently executing test.
 *
 * @since 5.0
 */
class TestInfoParameterResolver implements ParameterResolver {

	@Override
	public boolean supports(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return (parameterContext.getParameter().getType() == TestInfo.class);
	}

	@Override
	public TestInfo resolve(ParameterContext parameterContext, ExtensionContext extensionContext) {
		return new DefaultTestInfo(extensionContext);
	}

	private static class DefaultTestInfo implements TestInfo {

		private final String displayName;
		private final Set<String> tags;
		private final Optional<Class<?>> testClass;
		private final Optional<Method> testMethod;

		DefaultTestInfo(ExtensionContext extensionContext) {
			this.displayName = extensionContext.getDisplayName();
			this.tags = extensionContext.getTags();
			this.testClass = extensionContext.getTestClass();
			this.testMethod = extensionContext.getTestMethod();
		}

		@Override
		public String getDisplayName() {
			return this.displayName;
		}

		@Override
		public Set<String> getTags() {
			return tags;
		}

		@Override
		public Optional<Class<?>> getTestClass() {
			return this.testClass;
		}

		@Override
		public Optional<Method> getTestMethod() {
			return this.testMethod;
		}

		@Override
		public String toString() {
			// @formatter:off
			return new ToStringBuilder(this)
				.append("displayName", this.displayName)
				.append("tags", this.tags)
				.append("testClass", this.testClass)
				.append("testMethod", this.testMethod)
				.toString();
			// @formatter:on
		}

	}

}
