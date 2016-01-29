/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.discovery;

import org.junit.gen5.commons.util.PreconditionViolationException;
import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.gen5.commons.util.ToStringBuilder;
import org.junit.gen5.engine.DiscoverySelector;

/**
 * @since 5.0
 */
public class ClassSelector implements DiscoverySelector {

	public static ClassSelector forClass(Class<?> testClass) {
		return new ClassSelector(testClass);
	}

	public static ClassSelector forClassName(String className) {
		return forClass(ReflectionUtils.loadClass(className).orElseThrow(
			() -> new PreconditionViolationException("Could not resolve class with name: " + className)));
	}

	private final Class<?> testClass;

	private ClassSelector(Class<?> testClass) {
		this.testClass = testClass;
	}

	public Class<?> getTestClass() {
		return testClass;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;

		ClassSelector that = (ClassSelector) o;
		return testClass.equals(that.testClass);
	}

	@Override
	public int hashCode() {
		return testClass.hashCode();
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("testClass", testClass).toString();
	}

}
