/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.fakes;

import org.junit.platform.engine.UniqueId;
import org.junit.platform.engine.support.descriptor.AbstractTestDescriptor;

/**
 * @since 1.4
 */
public class TestDescriptorStub extends AbstractTestDescriptor {

	public TestDescriptorStub(UniqueId uniqueId, String displayName) {
		super(uniqueId, displayName);
	}

	public TestDescriptorStub(UniqueId uniqueId, String displayName, String requirement) {
		super(uniqueId, displayName, requirement);
	}

	@Override
	public Type getType() {
		return getChildren().isEmpty() ? Type.TEST : Type.CONTAINER;
	}

}
