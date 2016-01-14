/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package example;

// tag::user_guide[]
import static org.junit.gen5.api.Assertions.assertEquals;

import org.junit.gen5.api.Test;

class FirstJUnit5Tests {

	@Test
	void myFirstTest() {
		assertEquals(2, 1 + 1);
	}

}
// end::user_guide[]
