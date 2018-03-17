/*
 * Copyright 2015-2018 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.jupiter.params.aggregator;

import java.util.List;

public interface ArgumentsAccessor {

	Object[] toArray();

	List<Object> toList();

	int getSize();

	Object get(int index);

	Object getAs(Class<?> clazz, int index);

	Character getChar(int index);

	Boolean getBoolean(int index);

	Byte getByte(int index);

	Short getShort(int index);

	Integer getInt(int index);

	Long getLong(int index);

	Float getFloat(int index);

	Double getDouble(int index);

	String getString(int index);
}
