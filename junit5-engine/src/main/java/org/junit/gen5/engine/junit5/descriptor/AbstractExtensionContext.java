/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor;

import java.util.*;
import java.util.stream.Collectors;

import org.junit.gen5.api.extension.ExtensionContext;
import org.junit.gen5.engine.EngineExecutionListener;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.TestTag;
import org.junit.gen5.engine.junit5.execution.ExtensionValuesStore;
import org.junit.gen5.engine.junit5.execution.NamespaceAwareStore;
import org.junit.gen5.engine.reporting.ReportEntry;

/**
 * @since 5.0
 */
abstract class AbstractExtensionContext implements ExtensionContext {

	private final ExtensionContext parent;
	private final EngineExecutionListener engineExecutionListener;
	private final TestDescriptor testDescriptor;
	private final ExtensionValuesStore valuesStore;

	AbstractExtensionContext(ExtensionContext parent, EngineExecutionListener engineExecutionListener,
			TestDescriptor testDescriptor) {
		this.parent = parent;
		this.engineExecutionListener = engineExecutionListener;
		this.testDescriptor = testDescriptor;
		this.valuesStore = createStore(parent);
	}

	private ExtensionValuesStore createStore(ExtensionContext parent) {
		ExtensionValuesStore parentStore = null;
		if (parent != null) {
			parentStore = ((AbstractExtensionContext) parent).valuesStore;
		}
		return new ExtensionValuesStore(parentStore);
	}

	@Override
	public void publishReportEntry(Map<String, String> values) {
		engineExecutionListener.reportingEntryPublished(this.testDescriptor, ReportEntry.from(values));
	}

	@Override
	public Optional<ExtensionContext> getParent() {
		return Optional.ofNullable(parent);
	}

	protected TestDescriptor getTestDescriptor() {
		return testDescriptor;
	}

	@Override
	public Store getStore(Namespace namespace) {
		return new NamespaceAwareStore(valuesStore, namespace);
	}

	@Override
	public Set<String> getTags() {
		return testDescriptor.getTags().stream().map(TestTag::getName).collect(Collectors.toSet());
	}
}
