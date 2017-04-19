/*
 * Copyright 2015-2017 the original author or authors.
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
 * {@code @BeforeAll} is used to signal that the annotated method should be
 * executed <em>before</em> <strong>all</strong> tests in the current test class.
 *
 * <p>In contrast to {@link BeforeEach @BeforeEach} methods, {@code @BeforeAll}
 * methods are only executed once for a given test class or test class
 * hierarchy.
 *
 * <h3>Method Signatures</h3>
 *
 * <p>{@code @BeforeAll} methods must not be {@code private} and must be
 * {@code static}. Consequently, {@code @BeforeAll} methods are not supported
 * in {@link Nested @Nested} test classes or as <em>interface default methods</em>.
 * {@code @BeforeAll} methods may optionally declare parameters to be resolved by
 * {@link org.junit.jupiter.api.extension.ParameterResolver ParameterResolvers}.
 *
 * <h3>Inheritance</h3>
 *
 * <p>{@code @BeforeAll} methods are inherited from superclasses as long as
 * they are not shadowed. Furthermore, {@code @BeforeAll} methods from
 * superclasses will be executed before {@code @BeforeAll} methods in subclasses.
 *
 * <h3>Composition</h3>
 *
 * <p>{@code @BeforeAll} may be used as a meta-annotation in order to create
 * a custom <em>composed annotation</em> that inherits the semantics of
 * {@code @BeforeAll}.
 *
 * @since 5.0
 * @see AfterAll
 * @see BeforeEach
 * @see AfterEach
 * @see Test
 */
@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@Documented
@API(Maintained)
public @interface BeforeAll {
}
