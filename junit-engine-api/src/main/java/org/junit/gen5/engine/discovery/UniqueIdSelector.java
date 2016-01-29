/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.discovery;

import java.util.Objects;

import org.junit.gen5.commons.util.ToStringBuilder;
import org.junit.gen5.engine.DiscoverySelector;

/**
 * @since 5.0
 */
public class UniqueIdSelector implements DiscoverySelector {

	public static UniqueIdSelector forUniqueId(String uniqueId) {
		return new UniqueIdSelector(uniqueId);
	}

	private final String uniqueId;

	private UniqueIdSelector(String uniqueId) {
		this.uniqueId = uniqueId;
	}

	public String getUniqueId() {
		return uniqueId;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o)
			return true;
		if (o == null || getClass() != o.getClass())
			return false;
		UniqueIdSelector that = (UniqueIdSelector) o;
		return Objects.equals(uniqueId, that.uniqueId);
	}

	@Override
	public int hashCode() {
		return Objects.hash(uniqueId);
	}

	@Override
	public String toString() {
		return new ToStringBuilder(this).append("uniqueId", uniqueId).toString();
	}
}
