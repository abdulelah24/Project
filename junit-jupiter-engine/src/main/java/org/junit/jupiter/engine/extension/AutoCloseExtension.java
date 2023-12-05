/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static org.junit.platform.commons.util.AnnotationUtils.findAnnotatedFields;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.function.Predicate;

import org.apiguardian.api.API;
import org.junit.jupiter.api.AutoClose;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ExtensionContext.Namespace;
import org.junit.jupiter.api.extension.ExtensionContext.Store;
import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;
import org.junit.platform.commons.util.ExceptionUtils;
import org.junit.platform.commons.util.ReflectionUtils;

/**
 * {@code AutoCloseExtension} is a JUnit Jupiter extension that closes resources if a field in a test class is annotated
 * with {@link AutoClose @AutoClose}.
 *
 * <p>Consult the Javadoc for {@link AutoClose} for details on the contract.
 *
 * @since 5.11
 * @see AutoClose
 * @see AutoCloseable
 */
@API(status = API.Status.EXPERIMENTAL, since = "5.11")
public class AutoCloseExtension implements AfterAllCallback, AfterEachCallback {

	private static final Logger logger = LoggerFactory.getLogger(AutoCloseExtension.class);
	static final Namespace NAMESPACE = Namespace.create(AutoClose.class);

	@Override
	public void afterAll(ExtensionContext context) {
		Store contextStore = context.getStore(NAMESPACE);
		Class<?> testClass = context.getRequiredTestClass();

		registerCloseables(contextStore, testClass, null);
	}

	@Override
	public void afterEach(ExtensionContext context) {
		Store contextStore = context.getStore(NAMESPACE);

		for (Object instance : context.getRequiredTestInstances().getAllInstances()) {
			registerCloseables(contextStore, instance.getClass(), instance);
		}
	}

	private void registerCloseables(Store contextStore, Class<?> testClass, /* @Nullable */ Object testInstance) {
		Predicate<Field> isStatic = testInstance == null ? ReflectionUtils::isStatic : ReflectionUtils::isNotStatic;
		findAnnotatedFields(testClass, AutoClose.class, isStatic).forEach(field -> {
			try {
				contextStore.put(field, asCloseableResource(testInstance, field));
			}
			catch (Throwable t) {
				ExceptionUtils.throwAsUncheckedException(t);
			}
		});
	}

	private static Store.CloseableResource asCloseableResource(/* @Nullable */ Object testInstance, Field field) {
		return () -> {
			Object toBeClosed = ReflectionUtils.tryToReadFieldValue(field, testInstance).get();
			if (toBeClosed == null) {
				logger.warn(() -> "@AutoClose: Field " + getQualifiedFieldName(field)
						+ " couldn't be closed because it was null.");
				return;
			}
			getAndDestroy(field, toBeClosed);
		};
	}

	private static void getAndDestroy(Field field, Object toBeClosed) {
		String methodName = field.getAnnotation(AutoClose.class).value();
		Method destroyMethod = ReflectionUtils.findMethod(toBeClosed.getClass(), methodName).orElseThrow(
			() -> new ExtensionConfigurationException("@AutoClose: Cannot resolve the destroy method " + methodName
					+ "() at " + getQualifiedFieldName(field) + ": " + field.getType().getSimpleName()));
		ReflectionUtils.invokeMethod(destroyMethod, toBeClosed);
	}

	private static String getQualifiedFieldName(Field field) {
		return field.getDeclaringClass().getSimpleName() + "." + field.getName();
	}

}
