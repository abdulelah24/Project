/*
 * Copyright 2015-2023 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.suite.api;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.TYPE })

public @interface SelectMethod {

	/**
	 * The name of the method to select.
	 */
	String name();

	/**
	 * The parameter types of the method to select, in the form "int,String".
	 */
	String parameterTypes() default "";

	/**
	 * The return type of the method to select.
	 */
	Class<?> returnType() default void.class;

}
