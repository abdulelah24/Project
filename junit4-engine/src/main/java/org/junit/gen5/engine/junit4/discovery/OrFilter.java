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

import static java.util.stream.Collectors.joining;

import java.util.Collection;

import org.junit.gen5.commons.util.Preconditions;
import org.junit.runner.Description;
import org.junit.runner.manipulation.Filter;

class OrFilter extends Filter {

	private final Collection<? extends Filter> filters;

	OrFilter(Collection<? extends Filter> filters) {
		this.filters = Preconditions.notEmpty(filters, "filters must not be empty");
	}

	@Override
	public boolean shouldRun(Description description) {
		return filters.stream().anyMatch(filter -> filter.shouldRun(description));
	}

	@Override
	public String describe() {
		return filters.stream().map(Filter::describe).collect(joining(" OR "));
	}

}
