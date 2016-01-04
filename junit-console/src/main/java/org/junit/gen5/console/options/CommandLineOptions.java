/*
 * Copyright 2015-2016 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.gen5.console.options;

import static java.util.Collections.emptyList;

import java.util.List;
import java.util.Optional;

public class CommandLineOptions {

	private boolean displayHelp;
	private boolean exitCodeEnabled;
	private boolean ansiColorOutputDisabled;
	private boolean runAllTests;
	private boolean hideDetails;
	private String classnameFilter;
	private List<String> tagsFilter = emptyList();
	private List<String> additionalClasspathEntries = emptyList();
	private List<String> arguments = emptyList();
	private List<String> excludeTags = emptyList();

	public boolean isDisplayHelp() {
		return displayHelp;
	}

	public void setDisplayHelp(boolean displayHelp) {
		this.displayHelp = displayHelp;
	}

	public boolean isExitCodeEnabled() {
		return exitCodeEnabled;
	}

	public void setExitCodeEnabled(boolean exitCodeEnabled) {
		this.exitCodeEnabled = exitCodeEnabled;
	}

	public boolean isAnsiColorOutputDisabled() {
		return ansiColorOutputDisabled;
	}

	public void setAnsiColorOutputDisabled(boolean ansiColorOutputDisabled) {
		this.ansiColorOutputDisabled = ansiColorOutputDisabled;
	}

	public boolean isRunAllTests() {
		return runAllTests;
	}

	public void setRunAllTests(boolean runAllTests) {
		this.runAllTests = runAllTests;
	}

	public boolean isHideDetails() {
		return hideDetails;
	}

	public void setHideDetails(boolean hideDetails) {
		this.hideDetails = hideDetails;
	}

	public Optional<String> getClassnameFilter() {
		return Optional.ofNullable(classnameFilter);
	}

	public void setClassnameFilter(String classnameFilter) {
		this.classnameFilter = classnameFilter;
	}

	public List<String> getTagsFilter() {
		return tagsFilter;
	}

	public void setTagsFilter(List<String> tagsFilter) {
		this.tagsFilter = tagsFilter;
	}

	public List<String> getExcludeTags() {
		return excludeTags;
	}

	public void setExcludeTags(List<String> excludeTags) {
		this.excludeTags = excludeTags;
	}

	public List<String> getAdditionalClasspathEntries() {
		return additionalClasspathEntries;
	}

	public void setAdditionalClasspathEntries(List<String> additionalClasspathEntries) {
		this.additionalClasspathEntries = additionalClasspathEntries;
	}

	public List<String> getArguments() {
		return arguments;
	}

	public void setArguments(List<String> arguments) {
		this.arguments = arguments;
	}

}
