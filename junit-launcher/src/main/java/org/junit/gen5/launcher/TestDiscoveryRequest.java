/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.launcher;

import java.util.*;

import org.junit.gen5.engine.*;
import org.junit.gen5.launcher.main.JUnit5Launcher;
import org.junit.gen5.launcher.main.TestDiscoveryRequestBuilder;

/**
 * This class extends the {@link EngineDiscoveryRequest}
 * by providing access to filters which are applied by the
 * {@link JUnit5Launcher} itself
 *
 * <p>Moreover, the add*-methods can be used by external clients
 * that do not want to use the
 * {@link TestDiscoveryRequestBuilder}.
 *
 * @since 5.0
 */
public interface TestDiscoveryRequest extends EngineDiscoveryRequest {

	void addSelector(DiscoverySelector selector);

	void addSelectors(Collection<DiscoverySelector> selectors);

	void addEngineIdFilter(EngineIdFilter engineIdFilter);

	void addEngineIdFilters(Collection<EngineIdFilter> engineIdFilters);

	void addFilter(DiscoveryFilter<?> discoveryFilter);

	void addFilters(Collection<DiscoveryFilter<?>> discoveryFilters);

	void addPostFilter(PostDiscoveryFilter postDiscoveryFilter);

	void addPostFilters(Collection<PostDiscoveryFilter> postDiscoveryFilters);

	List<EngineIdFilter> getEngineIdFilters();

	List<PostDiscoveryFilter> getPostDiscoveryFilters();

}
