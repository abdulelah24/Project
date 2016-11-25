/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.migrationsupport.rules.adapter;

import static org.junit.platform.commons.meta.API.Usage.Internal;

import java.lang.reflect.Method;

import org.junit.jupiter.migrationsupport.rules.member.RuleAnnotatedMember;
import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.Preconditions;
import org.junit.platform.commons.util.ReflectionUtils;
import org.junit.rules.TestRule;

@API(Internal)
public abstract class AbstractTestRuleAdapter implements GenericBeforeAndAfterAdvice {

	private final TestRule target;

	public AbstractTestRuleAdapter(RuleAnnotatedMember annotatedMember, Class<? extends TestRule> adapteeClass) {
		this.target = annotatedMember.getTestRuleInstance();
		Preconditions.condition(adapteeClass.isAssignableFrom(this.target.getClass()),
			() -> adapteeClass + " is not assignable from " + this.target.getClass());
	}

	protected Object executeMethod(String name) {
		return executeMethod(name, new Class<?>[0]);
	}

	protected Object executeMethod(String name, Class<?>[] parameterTypes, Object... arguments) {
		try {
			Method method = this.target.getClass().getDeclaredMethod(name, parameterTypes);
			method.setAccessible(true);
			return ReflectionUtils.invokeMethod(method, target, arguments);
		}
		catch (NoSuchMethodException | SecurityException ex) {
			throw new JUnitException(
				"Error while looking up method to call via reflection for class " + target.getClass().getName(), ex);
		}
	}

}
