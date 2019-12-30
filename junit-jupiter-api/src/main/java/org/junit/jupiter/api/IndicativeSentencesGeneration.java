/*
 * Copyright 2015-2019 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.api;

import static org.apiguardian.api.API.Status.EXPERIMENTAL;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.apiguardian.api.API;

/**
 * {@code @IndicativeSentencesGeneration} is used to declare a custom separator
 * by {@code IndicativeSentences}, if this notation was not declared
 * will be use ", " as separator by default.
 *
 * @since 5.6
 * @see DisplayName
 * @see DisplayNameGenerator
 */
@DisplayNameGeneration(DisplayNameGenerator.IndicativeSentences.class)
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@API(status = EXPERIMENTAL, since = "5.6")
public @interface IndicativeSentencesGeneration {

	String DEFAULT_SEPARATOR = ", ";
	/**
	 * Pre-defined display name generator instances.
	 */
	DisplayNameGenerator standardGenerator = new DisplayNameGenerator.Standard();
	DisplayNameGenerator replaceUnderscoresGenerator = new DisplayNameGenerator.ReplaceUnderscores();

	/**
	 * Custom separator for indicative sentences generator.
	 *
	 * @return custom separator for indicative sentences
	 */
	String separator() default "";

	/**
	 * Custom display name generator.
	 *
	 * @return custom display name generator class
	 */
	Class<? extends DisplayNameGenerator> value();
}
