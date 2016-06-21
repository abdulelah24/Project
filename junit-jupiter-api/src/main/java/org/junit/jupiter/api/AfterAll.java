/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.api;

import static org.junit.platform.commons.meta.API.Usage.Maintained;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.junit.platform.commons.meta.API;

/**
 * {@code @AfterAll} is used to signal that the annotated method should be
 * executed <em>after</em> <strong>all</strong> tests in the current test
 * class or test class hierarchy.
 *
 * <p>{@code @AfterAll} may also be used as a meta-annotation in order to
 * create a custom <em>composed annotation</em> that inherits the semantics
 * of {@code @AfterAll}.
 *
 * <p>In contrast to {@link AfterEach @AfterEach} methods, {@code @AfterAll}
 * methods are only executed once for a given test class or test class
 * hierarchy.
 *
 * <p>{@code @AfterAll} methods must not be {@code private} and must be
 * {@code static}. Consequently, {@code @AfterAll} methods are not
 * supported in {@link Nested @Nested} test classes.
 *
 * @since 5.0
 * @see BeforeAll
 * @see BeforeEach
 * @see AfterEach
 * @see Test
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(Maintained)
public @interface AfterAll {
}
