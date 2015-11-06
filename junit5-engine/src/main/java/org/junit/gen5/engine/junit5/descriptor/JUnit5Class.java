/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor;

import lombok.Value;

@Value
public class JUnit5Class extends JUnit5Testable {

	private final Class<?> javaClass;

	public JUnit5Class(String uniqueId, Class<?> javaClass) {
		super(uniqueId);
		this.javaClass = javaClass;
	}

	public void accept(Visitor visitor) {
		visitor.visitClass(getUniqueId(), javaClass);
	}

}
