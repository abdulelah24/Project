/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.migrationsupport.rules;

import static org.junit.platform.commons.meta.API.Usage.Experimental;
import static org.junit.platform.commons.util.ReflectionUtils.MethodSortOrder.HierarchyDown;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.jupiter.api.extension.TestExtensionContext;
import org.junit.jupiter.migrationsupport.rules.adapter.AbstractTestRuleAdapter;
import org.junit.jupiter.migrationsupport.rules.member.RuleAnnotatedMember;
import org.junit.jupiter.migrationsupport.rules.member.RuleAnnotatedMethod;
import org.junit.platform.commons.meta.API;
import org.junit.platform.commons.util.AnnotationUtils;
import org.junit.rules.TestRule;

@API(Experimental)
class TestRuleMethodSupport extends AbstractTestRuleSupport {

	TestRuleMethodSupport(Function<RuleAnnotatedMember, AbstractTestRuleAdapter> adapterGenerator,
			Class<? extends TestRule> ruleType) {
		super(adapterGenerator, ruleType);
	}

	@Override
	protected RuleAnnotatedMember createRuleAnnotatedMember(TestExtensionContext context, Member member) {
		return new RuleAnnotatedMethod(context.getTestInstance(), (Method) member);
	}

	@Override
	protected List<Member> findRuleAnnotatedMembers(Object testInstance) {
		List<Method> annotatedMethods = AnnotationUtils.findAnnotatedMethods(testInstance.getClass(),
			super.getAnnotationType(), HierarchyDown);

		Predicate<Method> methodsWithCorrectReturnType = method -> method.getReturnType().isAssignableFrom(
			super.getRuleType());

		// @formatter:off
        return annotatedMethods.stream()
                .filter(methodsWithCorrectReturnType)
                .collect(Collectors.toList());
		// @formatter:on

	}

}
