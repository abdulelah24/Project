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
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import org.junit.gen5.commons.util.ReflectionUtils;
import org.junit.internal.builders.AllDefaultPossibilitiesBuilder;
import org.junit.internal.builders.AnnotatedBuilder;
import org.junit.internal.builders.JUnit4Builder;
import org.junit.runner.Runner;
import org.junit.runners.model.RunnerBuilder;

/**
 * Customization of {@link AllDefaultPossibilitiesBuilder} from JUnit 4 to
 * ignore certain classes that would otherwise be reported as errors or cause
 * infinite recursion.
 *
 * @see DefensiveAnnotatedBuilder
 * @see DefensiveJUnit4Builder
 */
class DefensiveAllDefaultPossibilitiesBuilder extends AllDefaultPossibilitiesBuilder {

	private final AnnotatedBuilder annotatedBuilder;
	private final DefensiveJUnit4Builder defensiveJUnit4Builder;

	DefensiveAllDefaultPossibilitiesBuilder() {
		super(true);
		annotatedBuilder = createAnnotatedBuilder();
		defensiveJUnit4Builder = new DefensiveJUnit4Builder();
	}

	private AnnotatedBuilder createAnnotatedBuilder() {
		// Load via reflection because it might not be available at runtime.
		Optional<Class<?>> junit5RunnerClass = ReflectionUtils.loadClass("org.junit.gen5.junit4.runner.JUnit5");
		if (junit5RunnerClass.isPresent()) {
			return new DefensiveAnnotatedBuilder(this, junit5RunnerClass.get());
		}
		return new AnnotatedBuilder(this);
	}

	@Override
	protected AnnotatedBuilder annotatedBuilder() {
		return annotatedBuilder;
	}

	@Override
	protected JUnit4Builder junit4Builder() {
		return defensiveJUnit4Builder;
	}

	/**
	 * Customization of {@link AnnotatedBuilder} that ignores classes annotated
	 * with {@code @RunWith(JUnit5.class)} to avoid infinite recursion.
	 */
	private static class DefensiveAnnotatedBuilder extends AnnotatedBuilder {

		private final Class<?> junit5RunnerClass;

		public DefensiveAnnotatedBuilder(RunnerBuilder suiteBuilder, Class<?> junit5RunnerClass) {
			super(suiteBuilder);
			this.junit5RunnerClass = junit5RunnerClass;
		}

		@Override
		public Runner buildRunner(Class<? extends Runner> runnerClass, Class<?> testClass) throws Exception {
			if (junit5RunnerClass.isAssignableFrom(runnerClass)) {
				return null;
			}
			return super.buildRunner(runnerClass, testClass);
		}
	}

	/**
	 * Customization of {@link JUnit4Builder} that ignores classes that do not
	 * contain any test methods in order not to report errors for them.
	 */
	private static class DefensiveJUnit4Builder extends JUnit4Builder {

		private final Predicate<Method> hasTestAnnotation = new IsPotentialJUnit4TestMethod();

		@Override
		public Runner runnerForClass(Class<?> testClass) throws Throwable {
			if (containsTestMethods(testClass)) {
				return super.runnerForClass(testClass);
			}
			return null;
		}

		private boolean containsTestMethods(Class<?> testClass) {
			List<Method> testMethods = ReflectionUtils.findMethods(testClass, hasTestAnnotation);
			return !testMethods.isEmpty();
		}
	}

}
