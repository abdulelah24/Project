/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.jmh;

import org.junit.jupiter.engine.JupiterTestEngine;
import org.junit.platform.engine.TestEngine;
import org.junit.platform.launcher.core.LauncherConfig;
import org.junit.platform.runner.BenchmarksJUnitPlatform;
import org.junit.runner.JUnitCore;
import org.junit.runner.notification.RunNotifier;
import org.junit.vintage.engine.VintageTestEngine;
import org.openjdk.jmh.annotations.Benchmark;

import static org.junit.platform.launcher.core.LauncherConfig.builder;
import static org.junit.platform.launcher.core.LauncherFactory.create;

/**
 * JMH benchmarks for platform.
 *
 * @since 5.1
 */
public class PlatformBenchmarks {
    @Benchmark
    public void junit4_platform_noTest() {
        runWithJUnit4(NoTest.class);
    }

    @Benchmark
    public void junit4_platform_emptyTest() {
        runWithJUnit4(JUnit4EmptyTest.class);
    }
    
    @Benchmark
    public void junitJupiter_platform_vintage_noTest() {
        runWithJUnitJupiter(NoTest.class, true);
    }

    @Benchmark
    public void junitJupiter_platform_noTest() {
        runWithJUnitJupiter(NoTest.class, false);
    }

    @Benchmark
    public void junitJupiter_platform_vintage_emptyTest() {
        runWithJUnitJupiter(JUnitJupiterEmptyTest.class, true);
    }

    @Benchmark
    public void junitJupiter_platform_emptyTest() {
        runWithJUnitJupiter(JUnitJupiterEmptyTest.class, false);
    }

    private void runWithJUnit4(Class<?> testClass) {
        new JUnitCore().run(testClass);
    }

    private void runWithJUnitJupiter(Class<?> testClass, boolean vintage) {
        TestEngine testEngine = vintage ? new VintageTestEngine() : new JupiterTestEngine();
        LauncherConfig config = builder().enableTestEngineAutoRegistration(false).addTestEngines(testEngine).build();
        RunNotifier notifier = new RunNotifier();
        new BenchmarksJUnitPlatform(testClass, create(config)).run(notifier);
    }

    public static class NoTest {
    }

    public static class JUnitJupiterEmptyTest {
        @org.junit.jupiter.api.Test
        public void emptyTest() {
        }
    }

    public static class JUnit4EmptyTest {
        @org.junit.Test
        public void emptyTest() {
        }
    }
}
