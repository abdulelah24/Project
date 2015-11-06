/*
 * Copyright 2015 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.commons.util;

import java.io.IOException;
import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

public class FindClassesInPackageTest {

	@Test
	public void findAllClassesInThisPackage() throws IOException, ClassNotFoundException {
		Class[] classes = ReflectionUtils.findAllClassesInPackage("org.junit.gen5.commons");
		//		for(Class clazz : classes) {
		//			System.out.println(clazz.getName());
		//		}
		Assert.assertTrue("Should be at least 19 classes", classes.length >= 20);
		Assert.assertTrue(Arrays.asList(classes).contains(InnerClassToBeFound.class));
		Assert.assertTrue(Arrays.asList(classes).contains(MemberClassToBeFound.class));
	}

	class MemberClassToBeFound {

	}

	static class InnerClassToBeFound {

	}
}
