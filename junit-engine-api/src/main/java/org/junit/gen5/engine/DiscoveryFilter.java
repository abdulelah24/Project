/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine;

/**
 * {@code DiscoveryFilter} filters particular tests during test discovery.
 *
 * @since 5.0
 */
public interface DiscoveryFilter<T> extends GenericFilter<T> {
}
