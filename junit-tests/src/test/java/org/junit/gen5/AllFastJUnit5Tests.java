/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5;

import org.junit.gen5.engine.junit5.JUnit5TestEngine;
import org.junit.gen5.junit4runner.JUnit5;
import org.junit.gen5.junit4runner.JUnit5.ClassNameMatches;
import org.junit.gen5.junit4runner.JUnit5.ExcludeTags;
import org.junit.gen5.junit4runner.JUnit5.OnlyEngine;
import org.junit.gen5.junit4runner.JUnit5.Packages;
import org.junit.runner.RunWith;

@RunWith(JUnit5.class)
@Packages("org.junit.gen5")
@ClassNameMatches(".*Test[s]")
@ExcludeTags("slow")
@OnlyEngine(JUnit5TestEngine.ENGINE_ID)
public class AllFastJUnit5Tests {
}
