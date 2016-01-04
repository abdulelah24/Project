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

import java.lang.reflect.Method;
import java.util.function.Predicate;

import org.junit.Test;

/**
 * @since 5.0
 */
class IsPotentialJUnit4TestMethod implements Predicate<Method> {

	@Override
	public boolean test(Method method) {
		// Don't use AnnotationUtils.hasAnnotation since JUnit 4 does not support
		// meta-annotations
		return method.isAnnotationPresent(Test.class);
	}

}
