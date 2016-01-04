/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.execution;

import java.util.*;

import org.junit.gen5.api.extension.ExtensionContext;
import org.junit.gen5.engine.*;
import org.junit.gen5.engine.EngineExecutionContext;

public class JUnit5EngineExecutionContext implements EngineExecutionContext {

	private final State state;

	public JUnit5EngineExecutionContext() {
		this(new State());
	}

	private JUnit5EngineExecutionContext(State state) {
		this.state = state;
	}

	public TestInstanceProvider getTestInstanceProvider() {
		return state.testInstanceProvider;
	}

	public TestExtensionRegistry getTestExtensionRegistry() {
		return state.testExtensionRegistry;
	}

	public ExtensionContext getExtensionContext() {
		return state.extensionContext;
	}

	public Builder extend() {
		return builder(this);
	}

	public static Builder builder() {
		return new Builder(null, new State());
	}

	public static Builder builder(JUnit5EngineExecutionContext context) {
		return new Builder(context.state, null);
	}

	@Override
	public void registerPublishHandler(PublishHandler handler) {
		state.publishHandler = handler;
	}

	public void publishReportEntry(TestDescriptor descriptor, Map<String, String> entry) {
		state.publishHandler.publishReportEntry(descriptor, entry);
	}

	private static final class State implements Cloneable {

		TestInstanceProvider testInstanceProvider;
		TestExtensionRegistry testExtensionRegistry;
		ExtensionContext extensionContext;
		PublishHandler publishHandler = (testDescriptor, entry) -> {
		};

		@Override
		public State clone() {
			try {
				return (State) super.clone();
			}
			catch (CloneNotSupportedException e) {
				throw new RuntimeException("State could not be cloned", e);
			}
		}

	}

	public static class Builder {

		private State originalState;
		private State newState;

		private Builder(State originalState, State state) {
			this.originalState = originalState;
			this.newState = state;
		}

		public Builder withTestInstanceProvider(TestInstanceProvider testInstanceProvider) {
			newState().testInstanceProvider = testInstanceProvider;
			return this;
		}

		public Builder withTestExtensionRegistry(TestExtensionRegistry testExtensionRegistry) {
			newState().testExtensionRegistry = testExtensionRegistry;
			return this;
		}

		public Builder withExtensionContext(ExtensionContext extensionContext) {
			newState().extensionContext = extensionContext;
			return this;
		}

		public JUnit5EngineExecutionContext build() {
			if (newState != null) {
				originalState = newState;
				newState = null;
			}
			return new JUnit5EngineExecutionContext(originalState);
		}

		private State newState() {
			if (newState == null) {
				this.newState = originalState.clone();
			}
			return newState;
		}

	}

}
