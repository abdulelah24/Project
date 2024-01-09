/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.commons.util;

import static java.nio.file.FileVisitResult.CONTINUE;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.function.Consumer;

import org.junit.platform.commons.logging.Logger;
import org.junit.platform.commons.logging.LoggerFactory;

/**
 * @since 1.11
 */
class ResourceFileVisitor extends SimpleFileVisitor<Path> {

	private static final Logger logger = LoggerFactory.getLogger(ResourceFileVisitor.class);

	static final String CLASS_FILE_SUFFIX = ".class";

	private final Consumer<Path> classFileConsumer;

	ResourceFileVisitor(Consumer<Path> classFileConsumer) {
		this.classFileConsumer = classFileConsumer;
	}

	@Override
	public FileVisitResult visitFile(Path file, BasicFileAttributes attributes) {
		if (isNotClassFile(file)) {
			classFileConsumer.accept(file);
		}
		return CONTINUE;
	}

	@Override
	public FileVisitResult visitFileFailed(Path file, IOException ex) {
		logger.warn(ex, () -> "I/O error visiting file: " + file);
		return CONTINUE;
	}

	@Override
	public FileVisitResult postVisitDirectory(Path dir, IOException ex) {
		if (ex != null) {
			logger.warn(ex, () -> "I/O error visiting directory: " + dir);
		}
		return CONTINUE;
	}

	private static boolean isNotClassFile(Path file) {
		return !file.getFileName().toString().endsWith(CLASS_FILE_SUFFIX);
	}

}
