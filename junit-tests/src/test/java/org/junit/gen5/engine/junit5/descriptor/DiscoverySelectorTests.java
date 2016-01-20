/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.engine.junit5.descriptor;

import static org.junit.gen5.api.Assertions.assertEquals;
import static org.junit.gen5.api.Assertions.assertSame;
import static org.junit.gen5.api.Assertions.assertThrows;
import static org.junit.gen5.api.Assertions.assertTrue;
import static org.junit.gen5.launcher.main.TestDiscoveryRequestBuilder.request;

import java.util.List;
import java.util.stream.Collectors;

import org.junit.gen5.api.Nested;
import org.junit.gen5.api.Test;
import org.junit.gen5.commons.util.PreconditionViolationException;
import org.junit.gen5.engine.EngineDiscoveryRequest;
import org.junit.gen5.engine.TestDescriptor;
import org.junit.gen5.engine.discovery.ClassSelector;
import org.junit.gen5.engine.discovery.MethodSelector;
import org.junit.gen5.engine.discovery.PackageSelector;
import org.junit.gen5.engine.discovery.UniqueIdSelector;
import org.junit.gen5.engine.support.hierarchical.DummyTestEngine;

public class DiscoverySelectorTests {

	private final JUnit5EngineDescriptor engineDescriptor = new JUnit5EngineDescriptor(
		new DummyTestEngine("ENGINE_ID"));
	private DiscoverySelectorResolver resolver = new DiscoverySelectorResolver(engineDescriptor);

	@Test
	public void testSingleClassResolution() {
		ClassSelector selector = ClassSelector.forClass(MyTestClass.class);

		resolver.resolveSelectors(request().select(selector).build());

		assertEquals(3, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass"));
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test1()"));
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test2()"));
	}

	@Test
	public void testTwoClassesResolution() {
		ClassSelector selector1 = ClassSelector.forClass(MyTestClass.class);
		ClassSelector selector2 = ClassSelector.forClass(YourTestClass.class);

		resolver.resolveSelectors(request().select(selector1, selector2).build());

		assertEquals(6, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = engineDescriptor.allDescendants().stream().map(d -> d.getUniqueId()).collect(
			Collectors.toList());
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass"));
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test1()"));
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test2()"));
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.YourTestClass"));
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.YourTestClass#test3()"));
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.YourTestClass#test4()"));
	}

	@Test
	public void testClassResolutionOfNestedClass() {
		ClassSelector selector = ClassSelector.forClass(OtherTestClass.NestedTestClass.class);

		resolver.resolveSelectors(request().select(selector).build());

		assertEquals(3, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = engineDescriptor.allDescendants().stream().map(d -> d.getUniqueId()).collect(
			Collectors.toList());
		assertTrue(
			uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.OtherTestClass$NestedTestClass"));
		assertTrue(uniqueIds.contains(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.OtherTestClass$NestedTestClass#test5()"));
		assertTrue(uniqueIds.contains(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.OtherTestClass$NestedTestClass#test6()"));
	}

	@Test
	public void testMethodResolution() throws NoSuchMethodException {
		MethodSelector selector = MethodSelector.forMethod(
			MyTestClass.class.getDeclaredMethod("test1").getDeclaringClass(),
			MyTestClass.class.getDeclaredMethod("test1"));

		resolver.resolveSelectors(request().select(selector).build());

		assertEquals(2, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass"));
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test1()"));
	}

	@Test
	public void testMethodResolutionFromInheritedMethod() throws NoSuchMethodException {
		MethodSelector selector = MethodSelector.forMethod(HerTestClass.class,
			MyTestClass.class.getDeclaredMethod("test1"));

		resolver.resolveSelectors(request().select(selector).build());

		assertEquals(2, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.HerTestClass"));
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.HerTestClass#test1()"));
	}

	@Test
	public void testResolutionOfNotTestMethod() throws NoSuchMethodException {
		MethodSelector selector = MethodSelector.forMethod(
			MyTestClass.class.getDeclaredMethod("notATest").getDeclaringClass(),
			MyTestClass.class.getDeclaredMethod("notATest"));
		EngineDiscoveryRequest request = request().select(selector).build();
		assertThrows(PreconditionViolationException.class, () -> resolver.resolveSelectors(request));
	}

	@Test
	public void testClassResolutionByUniqueId() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass");

		resolver.resolveSelectors(request().select(selector).build());

		assertEquals(3, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass"));
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test1()"));
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test2()"));
	}

	@Test
	public void testInnerClassResolutionByUniqueId() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.OtherTestClass$NestedTestClass");

		resolver.resolveSelectors(request().select(selector).build());

		assertEquals(3, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds();
		assertTrue(
			uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.OtherTestClass$NestedTestClass"));
		assertTrue(uniqueIds.contains(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.OtherTestClass$NestedTestClass#test5()"));
		assertTrue(uniqueIds.contains(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.OtherTestClass$NestedTestClass#test6()"));
	}

	@Test
	public void testMethodOfInnerClassByUniqueId() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.OtherTestClass$NestedTestClass#test5()");

		resolver.resolveSelectors(request().select(selector).build());

		assertEquals(2, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds();
		assertTrue(
			uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.OtherTestClass$NestedTestClass"));
		assertTrue(uniqueIds.contains(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.OtherTestClass$NestedTestClass#test5()"));
	}

	@Test
	public void testNonResolvableUniqueId() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId("ENGINE_ID:poops-machine");
		EngineDiscoveryRequest request = request().select(selector).build();

		assertThrows(PreconditionViolationException.class, () -> resolver.resolveSelectors(request));
	}

	@Test
	public void testUniqueIdOfNotTestMethod() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass#notATest()");
		EngineDiscoveryRequest request = request().select(selector).build();

		assertThrows(PreconditionViolationException.class, () -> resolver.resolveSelectors(request));
	}

	@Test
	public void testMethodResolutionByUniqueId() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test1()");

		resolver.resolveSelectors(request().select(selector).build());

		assertEquals(2, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass"));
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test1()"));
	}

	@Test
	public void testMethodResolutionByUniqueIdFromInheritedClass() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.HerTestClass#test1()");

		resolver.resolveSelectors(request().select(selector).build());

		assertEquals(2, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds();

		// System.out.println(uniqueIds);
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.HerTestClass"));
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.HerTestClass#test1()"));
	}

	@Test
	public void testMethodResolutionByUniqueIdWithParams() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.HerTestClass#test7(java.lang.String)");

		resolver.resolveSelectors(request().select(selector).build());

		assertEquals(2, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds();

		// System.out.println(uniqueIds);
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.HerTestClass"));
		assertTrue(uniqueIds.contains(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.HerTestClass#test7(java.lang.String)"));
	}

	@Test
	public void testMethodResolutionByUniqueIdWithWrongParams() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.HerTestClass#test7(java.math.BigDecimal)");
		EngineDiscoveryRequest request = request().select(selector).build();

		assertThrows(PreconditionViolationException.class, () -> resolver.resolveSelectors(request));
	}

	@Test
	public void testTwoMethodResolutionsByUniqueId() {
		UniqueIdSelector selector1 = UniqueIdSelector.forUniqueId(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test1()");
		UniqueIdSelector selector2 = UniqueIdSelector.forUniqueId(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test2()");

		// adding same selector twice should have no effect
		resolver.resolveSelectors(request().select(selector1, selector2, selector2).build());

		assertEquals(3, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds();
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass"));
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test1()"));
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test2()"));

		TestDescriptor classFromMethod1 = descriptorByUniqueId(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test1()").getParent().get();
		TestDescriptor classFromMethod2 = descriptorByUniqueId(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.MyTestClass#test2()").getParent().get();

		assertEquals(classFromMethod1, classFromMethod2);
		assertSame(classFromMethod1, classFromMethod2);
	}

	@Test
	public void testPackageResolution() {
		PackageSelector selector = PackageSelector.forPackageName("org.junit.gen5.engine.junit5.descriptor.subpackage");

		resolver.resolveSelectors(request().select(selector).build());

		assertEquals(6, engineDescriptor.allDescendants().size());
		List<String> uniqueIds = uniqueIds();
		assertTrue(
			uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.subpackage.Class1WithTestCases"));
		assertTrue(uniqueIds.contains(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.subpackage.Class1WithTestCases#test1()"));
		assertTrue(
			uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.subpackage.Class2WithTestCases"));
		assertTrue(uniqueIds.contains(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.subpackage.Class2WithTestCases#test2()"));
		assertTrue(uniqueIds.contains(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.subpackage.ClassWithStaticInnerTestCases$ShouldBeDiscovered#test1()"));
	}

	@Test
	public void testNestedTestResolutionFromBaseClass() {
		ClassSelector selector = ClassSelector.forClass(TestCaseWithNesting.class);

		resolver.resolveSelectors(request().select(selector).build());

		// engineDescriptor.allDescendants().stream().forEach(d -> System.out.println(d));

		List<String> uniqueIds = engineDescriptor.allDescendants().stream().map(d -> d.getUniqueId()).collect(
			Collectors.toList());
		assertEquals(6, uniqueIds.size());

		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting"));
		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting#testA()"));
		assertTrue(
			uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest"));
		assertTrue(uniqueIds.contains(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest#testB()"));
		assertTrue(uniqueIds.contains(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest@DoubleNestedTest"));
		assertTrue(uniqueIds.contains(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest@DoubleNestedTest#testC()"));
	}

	@Test
	public void testNestedTestResolutionFromNestedTestClass() {
		ClassSelector selector = ClassSelector.forClass(TestCaseWithNesting.NestedTest.class);

		resolver.resolveSelectors(request().select(selector).build());

		List<String> uniqueIds = engineDescriptor.allDescendants().stream().map(d -> d.getUniqueId()).collect(
			Collectors.toList());
		assertEquals(5, uniqueIds.size());

		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting"));
		assertTrue(
			uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest"));
		assertTrue(uniqueIds.contains(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest#testB()"));
		assertTrue(uniqueIds.contains(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest@DoubleNestedTest"));
		assertTrue(uniqueIds.contains(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest@DoubleNestedTest#testC()"));
	}

	@Test
	public void testNestedTestResolutionFromUniqueId() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest@DoubleNestedTest");

		resolver.resolveSelectors(request().select(selector).build());

		List<String> uniqueIds = engineDescriptor.allDescendants().stream().map(d -> d.getUniqueId()).collect(
			Collectors.toList());
		assertEquals(4, uniqueIds.size());

		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting"));
		assertTrue(
			uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest"));
		assertTrue(uniqueIds.contains(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest@DoubleNestedTest"));
		assertTrue(uniqueIds.contains(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest@DoubleNestedTest#testC()"));
	}

	@Test
	public void testNestedTestResolutionFromClass() {
		ClassSelector selector = ClassSelector.forClass(TestCaseWithNesting.NestedTest.DoubleNestedTest.class);

		resolver.resolveSelectors(request().select(selector).build());

		List<String> uniqueIds = engineDescriptor.allDescendants().stream().map(d -> d.getUniqueId()).collect(
			Collectors.toList());
		assertEquals(4, uniqueIds.size());

		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting"));
		assertTrue(
			uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest"));
		assertTrue(uniqueIds.contains(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest@DoubleNestedTest"));
		assertTrue(uniqueIds.contains(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest@DoubleNestedTest#testC()"));
	}

	@Test
	public void testNestedTestResolutionFromUniqueIdToMethod() {
		UniqueIdSelector selector = UniqueIdSelector.forUniqueId(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest#testB()");

		resolver.resolveSelectors(request().select(selector).build());

		List<String> uniqueIds = engineDescriptor.allDescendants().stream().map(d -> d.getUniqueId()).collect(
			Collectors.toList());
		assertEquals(3, uniqueIds.size());

		assertTrue(uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting"));
		assertTrue(
			uniqueIds.contains("ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest"));
		assertTrue(uniqueIds.contains(
			"ENGINE_ID:org.junit.gen5.engine.junit5.descriptor.TestCaseWithNesting@NestedTest#testB()"));
	}

	private TestDescriptor descriptorByUniqueId(String id) {
		return engineDescriptor.allDescendants().stream().filter(d -> d.getUniqueId().equals(id)).findFirst().get();
	}

	private List<String> uniqueIds() {
		return engineDescriptor.allDescendants().stream().map(d -> d.getUniqueId()).collect(Collectors.toList());
	}

}

class MyTestClass {

	@Test
	void test1() {

	}

	@Test
	void test2() {

	}

	void notATest() {

	}
}

class YourTestClass {

	@Test
	void test3() {

	}

	@Test
	void test4() {

	}

}

class HerTestClass extends MyTestClass {

	@Test
	void test7(String param) {

	}
}

class OtherTestClass {

	static class NestedTestClass {

		@Test
		void test5() {

		}

		@Test
		void test6() {

		}

	}
}

class TestCaseWithNesting {

	@Test
	void testA() {

	}

	@Nested
	class NestedTest {

		@Test
		void testB() {

		}

		@Nested
		class DoubleNestedTest {

			@Test
			void testC() {

			}

		}

	}
}
