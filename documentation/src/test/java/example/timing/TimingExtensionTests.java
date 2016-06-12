/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package example.timing;

import org.junit.gen5.api.Test;
import org.junit.gen5.api.extension.ExtendWith;

/**
 * Tests that demonstrate the example {@link TimingExtension}.
 *
 * @since 5.0
 */
// @formatter:off
// tag::user_guide[]
@ExtendWith(TimingExtension.class)
class TimingExtensionTests {

	@Test
	void sleep20ms() throws Exception {
		Thread.sleep(20);
	}

	@Test
	void sleep50ms() throws Exception {
		Thread.sleep(50);
	}
}
// end::user_guide[]
// @formatter:on
