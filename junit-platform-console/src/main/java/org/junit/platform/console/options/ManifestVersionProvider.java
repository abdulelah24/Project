/*
 * Copyright 2015-2024 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v2.0 which
 * accompanies this distribution and is available at
 *
 * https://www.eclipse.org/legal/epl-v20.html
 */

package org.junit.platform.console.options;

import picocli.CommandLine;

class ManifestVersionProvider implements CommandLine.IVersionProvider {

	public static String getImplementationVersion() {
		return ManifestVersionProvider.class.getPackage().getImplementationVersion();
	}

	@Override
	public String[] getVersion() {
		return new String[] { //
				String.format("@|bold JUnit Platform Console Launcher %s|@", getImplementationVersion()), //
				"JVM: ${java.version} (${java.vendor} ${java.vm.name} ${java.vm.version})", //
				"OS: ${os.name} ${os.version} ${os.arch}" //
		};
	}

}
