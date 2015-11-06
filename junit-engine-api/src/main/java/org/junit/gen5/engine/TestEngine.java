/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

import java.util.Collection;

public interface TestEngine {

	default String getId() {
		return getClass().getCanonicalName();
	}

	Collection<TestDescriptor> discoverTests(TestPlanSpecification specification);

	default boolean supports(TestDescriptor testDescriptor) {
		return testDescriptor.getUniqueId().startsWith(getId());
	}

	void execute(EngineExecutionContext context);
}
