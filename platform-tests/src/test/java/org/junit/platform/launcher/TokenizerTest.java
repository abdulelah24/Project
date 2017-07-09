package org.junit.platform.launcher;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class TokenizerTest {

    private static final String illegalCharacter = "|";

    @Test
    void removeLeadingAndTrailingSpaces() {
        assertThat(tokensExtractedFrom(" tag ")).containsExactly("tag");
    }

    @Test
    void upperAndLowerChaseCharactersAreAllowed() {
        assertThat(tokensExtractedFrom("AbyZ")).containsExactly("AbyZ");
    }

    @Test
    void tagsCanHaveSpaceCharacters() {
        assertThat(tokensExtractedFrom("tag with  spaces")).containsExactly("tag with  spaces");
        assertThat(tokensExtractedFrom("(  tag with spaces )  ")).containsExactly("(", "tag with spaces", ")");
    }

    @Test
    void andIsAReservedKeyword() {
        assertThat(tokensExtractedFrom("one and two")).containsExactly("one", "and", "two");
        assertThat(tokensExtractedFrom("andtag")).containsExactly("andtag");
        assertThat(tokensExtractedFrom("oneand two")).containsExactly("oneand two");
        assertThat(tokensExtractedFrom("one andtwo")).containsExactly("one andtwo");
    }

    @Test
    void orIsAReservedKeyword() {
        assertThat(tokensExtractedFrom("one or two")).containsExactly("one", "or", "two");
        assertThat(tokensExtractedFrom("ortag")).containsExactly("ortag");
        assertThat(tokensExtractedFrom("oneor two")).containsExactly("oneor two");
        assertThat(tokensExtractedFrom("one ortwo")).containsExactly("one ortwo");
    }

    @Test
    void notIsAReservedKeyword() {
        assertThat(tokensExtractedFrom("not tag")).containsExactly("not", "tag");
        assertThat(tokensExtractedFrom("nottag")).containsExactly("nottag");
    }

    @Test
    void discoverBrackets() {
        assertThat(tokensExtractedFrom("()")).containsExactly("(", ")");
        assertThat(tokensExtractedFrom("( a and b) or ( c and d )")).containsExactly("(", "a", "and", "b", ")", "or", "(", "c", "and", "d", ")");
    }

    @Test
    void reportOnNotParsableExpressions() {
        Assertions.assertThatThrownBy(() -> tokensExtractedFrom(illegalCharacter)).isInstanceOf(Tokenizer.IllegalTagExpression.class);
    }

    @ParameterizedTest
    @MethodSource("data")
    void acceptanceTests(String tagExpression, List<String> tokens) {
        assertThat(tokensExtractedFrom(tagExpression)).containsExactly(tokens.toArray(new String[0]));
    }

    public static Stream<Arguments> data() {
        return Stream.of(
                Arguments.of("a and b", asList("a", "and", "b")),
                Arguments.of("a or b", asList("a", "or", "b")),
                Arguments.of("not a", asList("not", "a")),
                Arguments.of("( a and b ) or ( c and d )", asList("(", "a", "and", "b", ")", "or", "(", "c", "and", "d", ")")),
                Arguments.of("not a or b and not c or not d or e and f", asList("not", "a", "or", "b", "and", "not", "c", "or", "not", "d", "or", "e", "and", "f"))
        );
    }

    private List<String> tokensExtractedFrom(String expression) {
        return new Tokenizer().tokenize(expression);
    }
}
