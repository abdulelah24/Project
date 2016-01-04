/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit4.discovery;

import static org.junit.gen5.commons.util.ReflectionUtils.MethodSortOrder.HierarchyDown;

import java.util.function.Predicate;

import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.runner.RunWith;

/**
 * @since 5.0
 */
class IsJUnit4TestClassWithTests implements Predicate<Class<?>> {

	private static final IsPotentialJUnit4TestMethod isTestMethod = new IsPotentialJUnit4TestMethod();

	private static final IsPotentialJUnit4TestClass isPotentialTestClass = new IsPotentialJUnit4TestClass();

	@Override
	public boolean test(Class<?> testClassCandidate) {
		if (!isPotentialTestClass.test(testClassCandidate))
			return false;
		// Do not use AnnotationUtils.hasAnnotation since JUnit4 does not support meta
		// annotations.
		return testClassCandidate.isAnnotationPresent(RunWith.class) || hasTestMethods(testClassCandidate);
	}

	private boolean hasTestMethods(Class<?> testClassCandidate) {
		return !ReflectionUtils.findMethods(testClassCandidate, isTestMethod, HierarchyDown).isEmpty();
	}

}
