/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.testdoubles;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestEngine;
import org.junit.gen5.engine.TestPlanSpecification;
import org.junit.gen5.engine.junit5.resolver.TestResolverRegistry;
import org.junit.gen5.engine.junit5.resolver.TestResolverResult;

public class TestResolverSpyWithTestsForRoot extends TestResolverSpy {
	private final TestDescriptor root;
	private final TestDescriptor resolvedTest;

	public TestResolverSpyWithTestsForRoot(TestDescriptor root) {
		this.root = root;
		this.resolvedTest = new TestDescriptorWithParentStub(root);
	}

	public TestDescriptor getResolvedTest() {
		return resolvedTest;
	}

	@Override
	public TestResolverResult resolveFor(TestDescriptor parent, TestPlanSpecification testPlanSpecification) {
		super.resolveFor(parent, testPlanSpecification);

		if (root.equals(parent)) {
			return TestResolverResult.proceedResolving(resolvedTest);
		}
		else {
			return TestResolverResult.empty();
		}
	}

	@Override
	public void setTestEngine(TestEngine testEngine) {
	}
}
