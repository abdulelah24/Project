/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.testers;

import static org.junit.gen5.commons.util.ReflectionUtils.MethodSortOrder.HierarchyDown;

import java.util.function.Predicate;

import org.junit.gen5.commons.util.ReflectionUtils;

/**
 * Test if a class really is a Junit5 test class with executable tests or nested tests.
 *
 * @since 5.0
 */
public class IsTestClassWithTests implements Predicate<Class<?>> {

	private static final IsTestMethod isTestMethod = new IsTestMethod();

	private static final IsPotentialTestContainer isPotentialTestContainer = new IsPotentialTestContainer();

	private static final IsNestedTestClass isNestedTestClass = new IsNestedTestClass();

	@Override
	public boolean test(Class<?> candidate) {
		//please do not collapse into single return
		if (!isPotentialTestContainer.test(candidate))
			return false;
		return hasTestMethods(candidate) || hasNestedTests(candidate);
	}

	private boolean hasTestMethods(Class<?> candidate) {
		return !ReflectionUtils.findMethods(candidate, isTestMethod, HierarchyDown).isEmpty();
	}

	private boolean hasNestedTests(Class<?> candidate) {
		return !ReflectionUtils.findNestedClasses(candidate, isNestedTestClass).isEmpty();
	}

}
