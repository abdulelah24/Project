/*
 * Copyright 2015-2021 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.engine.extension;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.junit.jupiter.api.extension.TestInstanceFactory;
import org.junit.jupiter.api.extension.TestInstanceFactoryContext;
import org.junit.jupiter.api.extension.TestInstancePreConstructCallback;
import org.junit.jupiter.engine.AbstractJupiterTestEngineTests;

/**
 * Integration tests that verify support for {@link TestInstancePreConstructCallback}.
 *
 * @since 5.9
 */
class TestInstancePreConstructCallbackTests extends AbstractJupiterTestEngineTests {
	private static final List<String> callSequence = new ArrayList<>();

	@BeforeEach
	void resetCallSequence() {
		callSequence.clear();
	}

	static class CallSequenceRecordingTest {
		protected static void record(String event) {
			callSequence.add(event);
		}
	}

	@ExtendWith(InstancePreConstructCallbackRecordingFoo.class)
	static class InstancePreConstruct extends CallSequenceRecordingTest {
		public InstancePreConstruct() {
			record("constructor");
		}

		@BeforeAll
		static void beforeAll() {
			record("beforeAll");
		}

		@BeforeEach
		void beforeEach() {
			record("beforeEach");
		}

		@Test
		void test1() {
			record("test1");
		}

		@Test
		void test2() {
			record("test2");
		}

		@AfterEach
		void afterEach() {
			record("afterEach");
		}

		@AfterAll
		static void afterAll() {
			record("afterAll");
		}
	}

	@Test
	void instancePreConstruct() {
		executeTestsForClass(InstancePreConstruct.class).testEvents().assertStatistics(
			stats -> stats.started(2).succeeded(2));

		// @formatter:off
		assertThat(callSequence).containsExactly(
				"beforeAll",

				"PreConstructCallback: name=foo, testClass=InstancePreConstruct, outerInstance: null",
				"constructor",
				"beforeEach",
				"test1",
				"afterEach",

				"PreConstructCallback: name=foo, testClass=InstancePreConstruct, outerInstance: null",
				"constructor",
				"beforeEach",
				"test2",
				"afterEach",

				"afterAll"
		);
		// @formatter:on
	}

	@ExtendWith(InstancePreConstructCallbackRecordingFoo.class)
	static class FactoryPreConstruct extends CallSequenceRecordingTest {
		@RegisterExtension
		static final TestInstanceFactory factory = (factoryContext, extensionContext) -> {
			record("testInstanceFactory");
			return new FactoryPreConstruct();
		};

		public FactoryPreConstruct() {
			record("constructor");
		}

		@BeforeAll
		static void beforeAll() {
			record("beforeAll");
		}

		@BeforeEach
		void beforeEach() {
			record("beforeEach");
		}

		@Test
		void test1() {
			record("test1");
		}

		@Test
		void test2() {
			record("test2");
		}

		@AfterEach
		void afterEach() {
			record("afterEach");
		}

		@AfterAll
		static void afterAll() {
			record("afterAll");
		}
	}

	@Test
	void factoryPreConstruct() {
		executeTestsForClass(FactoryPreConstruct.class).testEvents().assertStatistics(
			stats -> stats.started(2).succeeded(2));

		// @formatter:off
		assertThat(callSequence).containsExactly(
				"beforeAll",

				"PreConstructCallback: name=foo, testClass=FactoryPreConstruct, outerInstance: null",
				"testInstanceFactory",
				"constructor",
				"beforeEach",
				"test1",
				"afterEach",

				"PreConstructCallback: name=foo, testClass=FactoryPreConstruct, outerInstance: null",
				"testInstanceFactory",
				"constructor",
				"beforeEach",
				"test2",
				"afterEach",

				"afterAll"
		);
		// @formatter:on
	}

	@ExtendWith(InstancePreConstructCallbackRecordingFoo.class)
	static class PreConstructInNested extends CallSequenceRecordingTest {
		static AtomicInteger instanceCounter = new AtomicInteger();
		private final String instanceId;

		public PreConstructInNested() {
			record("constructor");
			instanceId = "#" + instanceCounter.incrementAndGet();
		}

		@BeforeAll
		static void beforeAll() {
			instanceCounter.set(0);
			record("beforeAll");
		}

		@BeforeEach
		void beforeEach() {
			record("beforeEach");
		}

		@Test
		void outerTest1() {
			record("outerTest1");
		}

		@Test
		void outerTest2() {
			record("outerTest2");
		}

		@AfterEach
		void afterEach() {
			record("afterEach");
		}

		@AfterAll
		static void afterAll() {
			record("afterAll");
		}

		@Override
		public String toString() {
			return instanceId;
		}

		@ExtendWith(InstancePreConstructCallbackRecordingBar.class)
		abstract class InnerParent extends CallSequenceRecordingTest {
		}

		@Nested
		@ExtendWith(InstancePreConstructCallbackRecordingBaz.class)
		class Inner extends InnerParent {
			Inner() {
				record("constructorInner");
			}

			@BeforeEach
			void beforeEachInner() {
				record("beforeEachInner");
			}

			@Test
			void innerTest1() {
				record("innerTest1");
			}

			@AfterEach
			void afterEachInner() {
				record("afterEachInner");
			}
		}
	}

	@Test
	void preConstructInNested() {
		executeTestsForClass(PreConstructInNested.class).testEvents().assertStatistics(
			stats -> stats.started(3).succeeded(3));

		// @formatter:off
		assertThat(callSequence).containsExactly(
				"beforeAll",

				"PreConstructCallback: name=foo, testClass=PreConstructInNested, outerInstance: null",
				"constructor",
				"beforeEach",
				"outerTest1",
				"afterEach",

				"PreConstructCallback: name=foo, testClass=PreConstructInNested, outerInstance: null",
				"constructor",
				"beforeEach",
				"outerTest2",
				"afterEach",

				"PreConstructCallback: name=foo, testClass=PreConstructInNested, outerInstance: null",
				"constructor",

				"PreConstructCallback: name=foo, testClass=Inner, outerInstance: #3",
				"PreConstructCallback: name=bar, testClass=Inner, outerInstance: #3",
				"PreConstructCallback: name=baz, testClass=Inner, outerInstance: #3",
				"constructorInner",
				"beforeEach",
				"beforeEachInner",
				"innerTest1",
				"afterEachInner",
				"afterEach",

				"afterAll"
		);
		// @formatter:on
	}

	static class PreConstructOnMethod extends CallSequenceRecordingTest {
		PreConstructOnMethod() {
			record("constructor");
		}

		@BeforeEach
		void beforeEach() {
			record("beforeEach");
		}

		@ExtendWith(InstancePreConstructCallbackRecordingFoo.class)
		@Test
		void test1() {
			record("test1");
		}

		@Test
		void test2() {
			record("test2");
		}

		@AfterEach
		void afterEach() {
			record("afterEach");
		}
	}

	@Test
	void preConstructOnMethod() {
		executeTestsForClass(PreConstructOnMethod.class).testEvents().assertStatistics(
			stats -> stats.started(2).succeeded(2));

		// @formatter:off
		assertThat(callSequence).containsExactly(
				"PreConstructCallback: name=foo, testClass=PreConstructOnMethod, outerInstance: null",
				"constructor",
				"beforeEach",
				"test1",
				"afterEach",

				"constructor",
				"beforeEach",
				"test2",
				"afterEach"
		);
		// @formatter:on
	}

	@TestInstance(TestInstance.Lifecycle.PER_CLASS)
	@ExtendWith(InstancePreConstructCallbackRecordingFoo.class)
	@ExtendWith(InstancePreConstructCallbackRecordingBar.class)
	static class PreConstructWithClassLifecycle extends CallSequenceRecordingTest {
		PreConstructWithClassLifecycle() {
			record("constructor");
		}

		@BeforeEach
		void beforeEach() {
			record("beforeEach");
		}

		@Test
		void test1() {
			callSequence.add("test1");
		}

		@Test
		void test2() {
			callSequence.add("test2");
		}
	}

	@Test
	void preConstructWithClassLifecycle() {
		executeTestsForClass(PreConstructWithClassLifecycle.class).testEvents().assertStatistics(
			stats -> stats.started(2).succeeded(2));

		// @formatter:off
		assertThat(callSequence).containsExactly(
				"PreConstructCallback: name=foo, testClass=PreConstructWithClassLifecycle, outerInstance: null",
				"PreConstructCallback: name=bar, testClass=PreConstructWithClassLifecycle, outerInstance: null",
				"constructor",
				"beforeEach",
				"test1",
				"beforeEach",
				"test2"
		);
		// @formatter:on
	}

	static abstract class AbstractTestInstancePreConstructCallback implements TestInstancePreConstructCallback {
		private final String name;

		AbstractTestInstancePreConstructCallback(String name) {
			this.name = name;
		}

		@Override
		public void preConstructTestInstance(TestInstanceFactoryContext factoryContext, ExtensionContext context) {
			assertThat(context.getTestInstance()).isNotPresent();
			assertThat(context.getTestClass()).isPresent();
			assertThat(factoryContext.getTestClass()).isSameAs(context.getTestClass().get());
			callSequence.add(
				"PreConstructCallback: name=" + name + ", testClass=" + factoryContext.getTestClass().getSimpleName()
						+ ", outerInstance: " + factoryContext.getOuterInstance().orElse(null));
		}
	}

	static class InstancePreConstructCallbackRecordingFoo extends AbstractTestInstancePreConstructCallback {
		InstancePreConstructCallbackRecordingFoo() {
			super("foo");
		}
	}

	static class InstancePreConstructCallbackRecordingBar extends AbstractTestInstancePreConstructCallback {
		InstancePreConstructCallbackRecordingBar() {
			super("bar");
		}
	}

	static class InstancePreConstructCallbackRecordingBaz extends AbstractTestInstancePreConstructCallback {
		InstancePreConstructCallbackRecordingBaz() {
			super("baz");
		}
	}
}
