/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.descriptor;

import static org.apiguardian.api.API.Status.INTERNAL;
import static org.junit.jupiter.engine.descriptor.ExtensionUtils.populateNewExtensionRegistryFromExtendWithAnnotation;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;

import org.apiguardian.api.API;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.BeforeTestExecutionCallback;
import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.junit.jupiter.engine.execution.AfterEachMethodAdapter;
import org.junit.jupiter.engine.execution.BeforeEachMethodAdapter;
import org.junit.jupiter.engine.execution.ExecutableInvoker;
import org.junit.jupiter.engine.execution.JupiterEngineExecutionContext;
import org.junit.jupiter.engine.extension.ExtensionRegistry;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.engine.TestDescriptor;
import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.hierarchical.OpenTest4JAwareThrowableCollector;
import org.junit.platform.engine.support.hierarchical.ThrowableCollector;
import org.junit.platform.engine.support.hierarchical.ThrowableCollector.Executable;

/**
 * {@link TestDescriptor} for tests based on Java methods.
 *
 * <h3>Default Display Names</h3>
 *
 * <p>The default display name for a test method is the name of the method
 * concatenated with a comma-separated list of parameter types in parentheses.
 * The names of parameter types are retrieved using {@link Class#getSimpleName()}.
 * For example, the default display name for the following test method is
 * {@code testUser(TestInfo, User)}.
 *
 * <pre class="code">
 *   {@literal @}Test
 *   void testUser(TestInfo testInfo, {@literal @}Mock User user) { ... }
 * </pre>
 *
 * @since 5.0
 */
@API(status = INTERNAL, since = "5.0")
public class TestMethodTestDescriptor extends MethodBasedTestDescriptor {

	private static final ExecutableInvoker executableInvoker = new ExecutableInvoker();

	public TestMethodTestDescriptor(UniqueId uniqueId, Class<?> testClass, Method testMethod) {
		super(uniqueId, testClass, testMethod);
	}

	TestMethodTestDescriptor(UniqueId uniqueId, String displayName, Class<?> testClass, Method testMethod) {
		super(uniqueId, displayName, testClass, testMethod);
	}

	@Override
	public Type getType() {
		return Type.TEST;
	}

	// --- Node ----------------------------------------------------------------

	@Override
	public JupiterEngineExecutionContext prepare(JupiterEngineExecutionContext context) throws Exception {
		ExtensionRegistry registry = populateNewExtensionRegistry(context);
		Object testInstance = context.getTestInstanceProvider().getTestInstance(Optional.of(registry));

		ThrowableCollector throwableCollector = new OpenTest4JAwareThrowableCollector();
		ExtensionContext extensionContext = new MethodExtensionContext(context.getExtensionContext(),
			context.getExecutionListener(), this, context.getConfigurationParameters(), testInstance,
			throwableCollector);

		// @formatter:off
		return context.extend()
				.withExtensionRegistry(registry)
				.withExtensionContext(extensionContext)
				.withThrowableCollector(throwableCollector)
				.build();
		// @formatter:on
	}

	protected ExtensionRegistry populateNewExtensionRegistry(JupiterEngineExecutionContext context) {
		return populateNewExtensionRegistryFromExtendWithAnnotation(context.getExtensionRegistry(), getTestMethod());
	}

	@Override
	public JupiterEngineExecutionContext execute(JupiterEngineExecutionContext context,
			DynamicTestExecutor dynamicTestExecutor) throws Exception {
		ThrowableCollector throwableCollector = context.getThrowableCollector();

		// @formatter:off
		invokeBeforeEachCallbacks(context);
			if (throwableCollector.isEmpty()) {
				invokeBeforeEachMethods(context);
				if (throwableCollector.isEmpty()) {
					invokeBeforeTestExecutionCallbacks(context);
					if (throwableCollector.isEmpty()) {
						invokeTestMethod(context, dynamicTestExecutor);
					}
					invokeAfterTestExecutionCallbacks(context);
				}
				invokeAfterEachMethods(context);
			}
		invokeAfterEachCallbacks(context);
		// @formatter:on

		throwableCollector.assertEmpty();

		return context;
	}

	private void invokeBeforeEachCallbacks(JupiterEngineExecutionContext context) {
		invokeBeforeMethodsOrCallbacksUntilExceptionOccurs(context,
			((extensionContext, callback) -> () -> callback.beforeEach(extensionContext)), BeforeEachCallback.class);
	}

	private void invokeBeforeEachMethods(JupiterEngineExecutionContext context) {
		ExtensionRegistry registry = context.getExtensionRegistry();
		invokeBeforeMethodsOrCallbacksUntilExceptionOccurs(context,
			((extensionContext, adapter) -> () -> {
				try {
					adapter.invokeBeforeEachMethod(extensionContext, registry);
				}
				catch (Throwable throwable) {
					invokeTestExecutionExceptionHandlers(throwable, registry,
						((ex, handler) -> () -> handler.handleExceptionInBeforeEachMethod(extensionContext, ex)));
				}
			}),
			BeforeEachMethodAdapter.class);
	}

	private void invokeBeforeTestExecutionCallbacks(JupiterEngineExecutionContext context) {
		invokeBeforeMethodsOrCallbacksUntilExceptionOccurs(context,
			((extensionContext, callback) -> () -> callback.beforeTestExecution(extensionContext)),
			BeforeTestExecutionCallback.class);
	}

	private <T extends Extension> void invokeBeforeMethodsOrCallbacksUntilExceptionOccurs(
			JupiterEngineExecutionContext context, BiFunction<ExtensionContext, T, Executable> generator,
			Class<T> type) {

		ExtensionRegistry registry = context.getExtensionRegistry();
		ExtensionContext extensionContext = context.getExtensionContext();
		ThrowableCollector throwableCollector = context.getThrowableCollector();

		for (T callback : registry.getExtensions(type)) {
			Executable executable = generator.apply(extensionContext, callback);
			throwableCollector.execute(executable);
			if (throwableCollector.isNotEmpty()) {
				break;
			}
		}
	}

	protected void invokeTestMethod(JupiterEngineExecutionContext context, DynamicTestExecutor dynamicTestExecutor) {
		ExtensionContext extensionContext = context.getExtensionContext();
		ThrowableCollector throwableCollector = context.getThrowableCollector();

		throwableCollector.execute(() -> {
			try {
				Method testMethod = getTestMethod();
				Object instance = extensionContext.getRequiredTestInstance();
				executableInvoker.invoke(testMethod, instance, extensionContext, context.getExtensionRegistry());
			}
			catch (Throwable throwable) {
				invokeTestExecutionExceptionHandlers(throwable, context.getExtensionRegistry(),
					((ex, handler) -> () -> handler.handleTestExecutionException(extensionContext, ex)));
			}
		});
	}

	private void invokeTestExecutionExceptionHandlers(Throwable ex, ExtensionRegistry registry,
			BiFunction<Throwable, TestExecutionExceptionHandler, Executable> generator) {

		invokeTestExecutionExceptionHandlers(ex, registry.getReversedExtensions(TestExecutionExceptionHandler.class),
				generator);
	}

	private void invokeTestExecutionExceptionHandlers(Throwable ex, List<TestExecutionExceptionHandler> handlers,
			BiFunction<Throwable, TestExecutionExceptionHandler, Executable> generator) {

		// No handlers left?
		if (handlers.isEmpty()) {
			ExceptionUtils.throwAsUncheckedException(ex);
		}

		try {
			// Invoke next available handler
			Executable executable = generator.apply(ex, handlers.remove(0));
			executable.execute();
		}
		catch (Throwable t) {
			invokeTestExecutionExceptionHandlers(t, handlers, generator);
		}
	}

	private void invokeAfterTestExecutionCallbacks(JupiterEngineExecutionContext context) {
		invokeAllAfterMethodsOrCallbacks(context,
			((extensionContext, callback) -> () -> callback.afterTestExecution(extensionContext)),
			AfterTestExecutionCallback.class);
	}

	private void invokeAfterEachMethods(JupiterEngineExecutionContext context) {
		ExtensionRegistry registry = context.getExtensionRegistry();
		invokeAllAfterMethodsOrCallbacks(context,
			((extensionContext, adapter) -> () -> {
				try {
					adapter.invokeAfterEachMethod(extensionContext, registry);
				}
				catch (Throwable throwable) {
					invokeTestExecutionExceptionHandlers(throwable, registry,
						((ex, handler) -> () -> handler.handleExceptionInAfterEachMethod(extensionContext, ex)));
				}
			}),
			AfterEachMethodAdapter.class);
	}

	private void invokeAfterEachCallbacks(JupiterEngineExecutionContext context) {
		invokeAllAfterMethodsOrCallbacks(context,
			((extensionContext, callback) -> () -> callback.afterEach(extensionContext)), AfterEachCallback.class);
	}

	private <T extends Extension> void invokeAllAfterMethodsOrCallbacks(JupiterEngineExecutionContext context,
			BiFunction<ExtensionContext, T, Executable> generator, Class<T> type) {

		ExtensionRegistry registry = context.getExtensionRegistry();
		ExtensionContext extensionContext = context.getExtensionContext();
		ThrowableCollector throwableCollector = context.getThrowableCollector();

		registry.getReversedExtensions(type).forEach(callback -> {
			Executable executable = generator.apply(extensionContext, callback);
			throwableCollector.execute(executable);
		});
	}

}
