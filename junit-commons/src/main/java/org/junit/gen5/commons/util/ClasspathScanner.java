/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.commons.util;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * <h3>DISCLAIMER</h3>
 *
 * <p>These utilities are intended solely for usage within the JUnit framework
 * itself. <strong>Any usage by external parties is not supported.</strong>
 * Use at your own risk!
 *
 * @since 5.0
 */
class ClasspathScanner {

	private static final String CLASS_FILE_SUFFIX = ".class";

	private final Supplier<ClassLoader> classLoaderSupplier;

	private final BiFunction<String, ClassLoader, Optional<Class<?>>> loadClass;

	ClasspathScanner(Supplier<ClassLoader> classLoaderSupplier,
			BiFunction<String, ClassLoader, Optional<Class<?>>> loadClass) {
		this.classLoaderSupplier = classLoaderSupplier;
		this.loadClass = loadClass;
	}

	boolean isPackage(String packageName) {
		String path = packagePath(packageName);
		try {
			Enumeration<URL> resource = classLoaderSupplier.get().getResources(path);
			return resource.hasMoreElements();
		}
		catch (IOException e) {
			return false;
		}
	}

	List<String> scanForPackagesInPackage(String basePackageName) {
		Preconditions.notBlank(basePackageName, "basePackageName must not be blank");

		List<File> dirs = allSourceDirsForPackage(basePackageName);
		return allSubPackagesInSourceDirs(dirs, basePackageName);
	}

	private List<String> allSubPackagesInSourceDirs(List<File> dirs, String basePackageName) {
		// @formatter:off
		return dirs.stream()
				.flatMap(dir -> findPackagesInSourceDir(dir, basePackageName))
				.distinct()
                .filter(this::isPackage)
				.collect(Collectors.toList());
		// @formatter:on
	}

	private Stream<String> findPackagesInSourceDir(File dir, String basePackageName) {
		File[] files = dir.listFiles();
		if (files == null) {
			return Stream.empty();
		}

		// @formatter:off
		return Arrays.stream(files)
				.filter(File::isDirectory)
				.map(File::getName)
				.map(name -> appendPackageName(basePackageName, name));
		// @formatter:on
	}

	List<Class<?>> scanForClassesInPackageOnly(String basePackageName, Predicate<Class<?>> classFilter) {
		Preconditions.notBlank(basePackageName, "basePackageName must not be blank");

		List<File> dirs = allSourceDirsForPackage(basePackageName);
		return allClassesInSourceDirsOnly(dirs, basePackageName, classFilter);
	}

	private List<Class<?>> allClassesInSourceDirsOnly(List<File> sourceDirs, String basePackageName,
			Predicate<Class<?>> classFilter) {
		List<Class<?>> classes = new ArrayList<>();
		for (File aSourceDir : sourceDirs) {
			classes.addAll(findClassesInSourceDirOnly(aSourceDir, basePackageName, classFilter));
		}
		return classes;
	}

	List<Class<?>> scanForClassesInPackage(String basePackageName, Predicate<Class<?>> classFilter) {
		Preconditions.notBlank(basePackageName, "basePackageName must not be blank");

		List<File> dirs = allSourceDirsForPackage(basePackageName);
		return allClassesInSourceDirs(dirs, basePackageName, classFilter);
	}

	private List<Class<?>> allClassesInSourceDirs(List<File> sourceDirs, String basePackageName,
			Predicate<Class<?>> classFilter) {
		List<Class<?>> classes = new ArrayList<>();
		for (File aSourceDir : sourceDirs) {
			classes.addAll(findClassesInSourceDirRecursively(aSourceDir, basePackageName, classFilter));
		}
		return classes;
	}

	List<Class<?>> scanForClassesInClasspathRoot(File root, Predicate<Class<?>> classFilter) {
		Preconditions.notNull(root, () -> "root must not be null");
		Preconditions.condition(root.exists(),
			() -> "root must exist, but could not be found: " + root.getAbsolutePath());
		Preconditions.condition(root.isDirectory(), "root must be a directory, but is not: " + root.getAbsolutePath());

		return findClassesInSourceDirRecursively(root, "", classFilter);
	}

	List<Class<?>> scanForClassesInClasspathRoots(List<File> roots, Predicate<Class<?>> classTester) {
		Preconditions.notNull(roots, "roots must not be null");
		Preconditions.notEmpty(roots, "roots must not be empty");

		// @formatter:off
        return roots.stream()
                .flatMap(root -> scanForClassesInClasspathRoot(root, classTester).stream())
                .collect(Collectors.toList());
        // @formatter:on
	}

	private List<File> allSourceDirsForPackage(String basePackageName) {
		try {
			ClassLoader classLoader = classLoaderSupplier.get();
			String path = packagePath(basePackageName);
			Enumeration<URL> resources = classLoader.getResources(path);
			List<File> dirs = new ArrayList<>();
			while (resources.hasMoreElements()) {
				URL resource = resources.nextElement();
				dirs.add(new File(resource.getFile()));
			}
			return dirs;
		}
		catch (IOException e) {
			return Collections.emptyList();
		}
	}

	private String packagePath(String basePackageName) {
		return basePackageName.replace('.', '/');
	}

	private List<Class<?>> findClassesInSourceDirOnly(File sourceDir, String packageName,
			Predicate<Class<?>> classFilter) {
		List<Class<?>> classesCollector = new ArrayList<>();
		collectClassesOnly(sourceDir, packageName, classesCollector, classFilter);
		return classesCollector;
	}

	private void collectClassesOnly(File sourceDir, String packageName, List<Class<?>> classesCollector,
			Predicate<Class<?>> classFilter) {
		File[] files = sourceDir.listFiles();
		if (files == null) {
			return;
		}
		for (File file : files) {
			if (isClassFile(file)) {
				Optional<Class<?>> classForClassFile = loadClassForClassFile(file, packageName);
				classForClassFile.filter(classFilter).ifPresent(clazz -> classesCollector.add(clazz));
			}
		}
	}

	private List<Class<?>> findClassesInSourceDirRecursively(File sourceDir, String packageName,
			Predicate<Class<?>> classFilter) {
		List<Class<?>> classesCollector = new ArrayList<>();
		collectClassesRecursively(sourceDir, packageName, classesCollector, classFilter);
		return classesCollector;
	}

	private void collectClassesRecursively(File sourceDir, String packageName, List<Class<?>> classesCollector,
			Predicate<Class<?>> classFilter) {
		File[] files = sourceDir.listFiles();
		if (files == null) {
			return;
		}
		for (File file : files) {
			if (isClassFile(file)) {
				Optional<Class<?>> classForClassFile = loadClassForClassFile(file, packageName);
				classForClassFile.filter(classFilter).ifPresent(clazz -> classesCollector.add(clazz));
			}
			else if (file.isDirectory()) {
				collectClassesRecursively(file, appendPackageName(packageName, file.getName()), classesCollector,
					classFilter);
			}
		}
	}

	private String appendPackageName(String packageName, String subpackageName) {
		if (packageName.isEmpty())
			return subpackageName;
		else
			return packageName + "." + subpackageName;
	}

	private Optional<Class<?>> loadClassForClassFile(File file, String packageName) {
		String className = packageName + '.'
				+ file.getName().substring(0, file.getName().length() - CLASS_FILE_SUFFIX.length());
		return loadClass.apply(className, classLoaderSupplier.get());
	}

	private static boolean isClassFile(File file) {
		return file.isFile() && file.getName().endsWith(CLASS_FILE_SUFFIX);
	}
}
