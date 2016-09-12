/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.platform.console.options;

import static java.util.Collections.emptyList;
import static org.junit.platform.commons.meta.API.Usage.Internal;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.platform.commons.meta.API;

/**
 * @since 1.0
 */
@API(Internal)
public class CommandLineOptions {

	private boolean displayHelp;
	private boolean ansiColorOutputDisabled;
	private boolean hideDetails;

	private boolean scanClasspath;
	private List<String> arguments = emptyList();

	private String includeClassNamePattern = "^.*Tests?$";
	private List<String> includedEngines = emptyList();
	private List<String> excludedEngines = emptyList();
	private List<String> includedTags = emptyList();
	private List<String> excludedTags = emptyList();

	private List<Path> additionalClasspathEntries = emptyList();

	private Path reportsDir;

	public boolean isDisplayHelp() {
		return this.displayHelp;
	}

	public void setDisplayHelp(boolean displayHelp) {
		this.displayHelp = displayHelp;
	}

	public boolean isAnsiColorOutputDisabled() {
		return this.ansiColorOutputDisabled;
	}

	public void setAnsiColorOutputDisabled(boolean ansiColorOutputDisabled) {
		this.ansiColorOutputDisabled = ansiColorOutputDisabled;
	}

	public boolean isScanClasspath() {
		return this.scanClasspath;
	}

	public void setScanClasspath(boolean scanClasspath) {
		this.scanClasspath = scanClasspath;
	}

	public boolean isHideDetails() {
		return this.hideDetails;
	}

	public void setHideDetails(boolean hideDetails) {
		this.hideDetails = hideDetails;
	}

	public String getIncludeClassNamePattern() {
		return this.includeClassNamePattern;
	}

	public void setIncludeClassNamePattern(String includeClassNamePattern) {
		this.includeClassNamePattern = includeClassNamePattern;
	}

	public List<String> getIncludedEngines() {
		return this.includedEngines;
	}

	public void setIncludedEngines(List<String> includedEngines) {
		this.includedEngines = includedEngines;
	}

	public List<String> getExcludedEngines() {
		return this.excludedEngines;
	}

	public void setExcludedEngines(List<String> excludedEngines) {
		this.excludedEngines = excludedEngines;
	}

	public List<String> getIncludedTags() {
		return this.includedTags;
	}

	public void setIncludedTags(List<String> includedTags) {
		this.includedTags = includedTags;
	}

	public List<String> getExcludedTags() {
		return this.excludedTags;
	}

	public void setExcludedTags(List<String> excludedTags) {
		this.excludedTags = excludedTags;
	}

	public List<Path> getAdditionalClasspathEntries() {
		return this.additionalClasspathEntries;
	}

	public void setAdditionalClasspathEntries(List<Path> additionalClasspathEntries) {
		// Create a modifiable copy
		this.additionalClasspathEntries = new ArrayList<>(additionalClasspathEntries);
		this.additionalClasspathEntries.removeIf(path -> !Files.exists(path));
	}

	public Optional<Path> getReportsDir() {
		return Optional.ofNullable(this.reportsDir);
	}

	public void setReportsDir(Path reportsDir) {
		this.reportsDir = reportsDir;
	}

	public List<String> getArguments() {
		return this.arguments;
	}

	public void setArguments(List<String> arguments) {
		this.arguments = arguments;
	}

}
