/*
 * Copyright 2015-2017 the original author or authors.
 *
 * All rights reserved. This program and the accompanying materials are
 * made available under the terms of the Eclipse Public License v1.0 which
 * accompanies this distribution and is available at
 *
 * http://www.eclipse.org/legal/epl-v10.html
 */

package org.junit.jupiter.engine.extension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.RepetitionInfo;
import org.junit.jupiter.api.TestInfo;

/**
 * Integration tests for {@link RepeatedTest}.
 *
 * @since 5.0
 */
class RepeatedTestTests {

	private static int fortyTwo = 0;

	@BeforeEach
	@AfterEach
	void beforeAndAfterEach(TestInfo testInfo, RepetitionInfo repetitionInfo) {
		if (testInfo.getDisplayName().startsWith("repeatedOnce")) {
			assertThat(repetitionInfo.getCurrentRepetition()).isEqualTo(1);
			assertThat(repetitionInfo.getTotalRepetitions()).isEqualTo(1);
		}
		else if (testInfo.getDisplayName().startsWith("repeatedFortyTwoTimes")) {
			assertThat(repetitionInfo.getCurrentRepetition()).isBetween(1, 42);
			assertThat(repetitionInfo.getTotalRepetitions()).isEqualTo(42);
		}
	}

	@AfterAll
	static void afterAll() {
		assertEquals(42, fortyTwo);
	}

	@RepeatedTest(-99)
	void negativeRepeatCount(TestInfo testInfo) {
		assertThat(testInfo.getDisplayName()).isEqualTo("negativeRepeatCount(TestInfo) :: repetition 1 of 1");
	}

	@RepeatedTest(0)
	void zeroRepeatCount(TestInfo testInfo) {
		assertThat(testInfo.getDisplayName()).isEqualTo("zeroRepeatCount(TestInfo) :: repetition 1 of 1");
	}

	@RepeatedTest(1)
	void repeatedOnce(TestInfo testInfo) {
		assertThat(testInfo.getDisplayName()).isEqualTo("repeatedOnce(TestInfo) :: repetition 1 of 1");
	}

	@RepeatedTest(42)
	void repeatedFortyTwoTimes(TestInfo testInfo) {
		assertThat(testInfo.getDisplayName()).startsWith("repeatedFortyTwoTimes(TestInfo) :: repetition ");
		fortyTwo++;
	}

	@RepeatedTest(1)
	void defaultDisplayName(TestInfo testInfo) {
		assertThat(testInfo.getDisplayName()).isEqualTo("defaultDisplayName(TestInfo) :: repetition 1 of 1");
	}

	@RepeatedTest(value = 1, name = "")
	void defaultDisplayNameWithEmptyPattern(TestInfo testInfo) {
		assertThat(testInfo.getDisplayName()).isEqualTo(
			"defaultDisplayNameWithEmptyPattern(TestInfo) :: repetition 1 of 1");
	}

	@RepeatedTest(value = 1, name = " \t  ")
	void defaultDisplayNameWithBlankPattern(TestInfo testInfo) {
		assertThat(testInfo.getDisplayName()).isEqualTo(
			"defaultDisplayNameWithBlankPattern(TestInfo) :: repetition 1 of 1");
	}

	@RepeatedTest(1)
	@DisplayName("Repeat!")
	void customDisplayName(TestInfo testInfo) {
		assertThat(testInfo.getDisplayName()).isEqualTo("Repeat! :: repetition 1 of 1");
	}

	@RepeatedTest(1)
	@DisplayName("   \t ")
	void customDisplayNameWithBlankName(TestInfo testInfo) {
		assertThat(testInfo.getDisplayName()).isEqualTo(
			"customDisplayNameWithBlankName(TestInfo) :: repetition 1 of 1");
	}

	@RepeatedTest(value = 1, name = "{displayName}")
	@DisplayName("Repeat!")
	void customDisplayNameWithPatternIncludingDisplayName(TestInfo testInfo) {
		assertThat(testInfo.getDisplayName()).isEqualTo("Repeat!");
	}

	@RepeatedTest(value = 1, name = "#{currentRepetition}")
	@DisplayName("Repeat!")
	void customDisplayNameWithPatternIncludingCurrentRepetition(TestInfo testInfo) {
		assertThat(testInfo.getDisplayName()).isEqualTo("#1");
	}

	@RepeatedTest(value = 1, name = "Repetition #{currentRepetition} for {displayName}")
	@DisplayName("Repeat!")
	void customDisplayNameWithPatternIncludingDisplayNameAndCurrentRepetition(TestInfo testInfo) {
		assertThat(testInfo.getDisplayName()).isEqualTo("Repetition #1 for Repeat!");
	}

	@RepeatedTest(value = 1, name = "{displayName} {currentRepetition}/{totalRepetitions}")
	@DisplayName("Repeat!")
	void customDisplayNameWithPatternIncludingDisplayNameCurrentRepetitionAndTotalRepetitions(TestInfo testInfo) {
		assertThat(testInfo.getDisplayName()).isEqualTo("Repeat! 1/1");
	}

	@RepeatedTest(value = 1, name = "Repetition #{currentRepetition} for {displayName}")
	void defaultDisplayNameWithPatternIncludingDisplayNameAndCurrentRepetition(TestInfo testInfo) {
		assertThat(testInfo.getDisplayName()).isEqualTo(
			"Repetition #1 for defaultDisplayNameWithPatternIncludingDisplayNameAndCurrentRepetition(TestInfo)");
	}

	@RepeatedTest(value = 5, name = "{displayName}")
	void injectRepetitionInfo(TestInfo testInfo, RepetitionInfo repetitionInfo) {
		assertThat(testInfo.getDisplayName()).isEqualTo("injectRepetitionInfo(TestInfo, RepetitionInfo)");
		assertThat(repetitionInfo.getCurrentRepetition()).isBetween(1, 5);
		assertThat(repetitionInfo.getTotalRepetitions()).isEqualTo(5);
	}

}
