/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.rat.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

class CasedStringTests {

    @MethodSource("testSegmentationData")
    @ParameterizedTest
    void testSegmentation(String pattern, CasedString.StringCase stringCase, String[] expected) {
        CasedString casedString = new CasedString(stringCase, pattern);
        assertThat(casedString.getSegments()).isEqualTo(expected);
    }

    static Stream<Arguments> testSegmentationData() {
        List<Arguments> lst = new ArrayList<>();
        lst.add(Arguments.of("CamelCase", CasedString.StringCase.CAMEL, new String[]{"Camel", "Case"}));
        lst.add(Arguments.of("CamelPMDCase", CasedString.StringCase.CAMEL,
                new String[]{"Camel", "P", "M", "D", "Case"}));
        lst.add(Arguments.of("camelCase", CasedString.StringCase.CAMEL, new String[]{"camel", "Case"}));
        lst.add(Arguments.of("camelPMDCase", CasedString.StringCase.CAMEL,
                new String[]{"camel", "P", "M", "D", "Case"}));
        lst.add(Arguments.of("PascalCase", CasedString.StringCase.PASCAL, new String[]{"Pascal", "Case"}));
        lst.add(Arguments.of("PascalPMDCase", CasedString.StringCase.PASCAL,
                new String[]{"Pascal", "P", "M", "D", "Case"}));
        lst.add(Arguments.of("pascalCase", CasedString.StringCase.PASCAL, new String[]{"pascal", "Case"}));
        lst.add(Arguments.of("pascalPMDCase", CasedString.StringCase.PASCAL,
                new String[]{"pascal", "P", "M", "D", "Case"}));
        lst.add(Arguments.of("snake_case", CasedString.StringCase.SNAKE, new String[]{"snake", "case"}));
        lst.add(Arguments.of("snake_Case", CasedString.StringCase.SNAKE, new String[]{"snake", "Case"}));
        lst.add(Arguments.of("snake__Case", CasedString.StringCase.SNAKE, new String[]{"snake", "", "Case"}));
        lst.add(Arguments.of("kebab-case", CasedString.StringCase.KEBAB, new String[]{"kebab", "case"}));
        lst.add(Arguments.of("kebab-Case", CasedString.StringCase.KEBAB, new String[]{"kebab", "Case"}));
        lst.add(Arguments.of("kebab--case", CasedString.StringCase.KEBAB, new String[]{"kebab", "", "case"}));
        lst.add(Arguments.of("phrase case", CasedString.StringCase.PHRASE, new String[]{"phrase", "case"}));
        lst.add(Arguments.of("phrase Case", CasedString.StringCase.PHRASE, new String[]{"phrase", "Case"}));
        lst.add(Arguments.of("phrase  case", CasedString.StringCase.PHRASE, new String[]{"phrase", "", "case"}));
        lst.add(Arguments.of("dot.case", CasedString.StringCase.DOT, new String[]{"dot", "case"}));
        lst.add(Arguments.of("dot..case", CasedString.StringCase.DOT, new String[]{"dot", "", "case"}));
        lst.add(Arguments.of("dot.Case", CasedString.StringCase.DOT, new String[]{"dot", "Case"}));
        return lst.stream();
    }

    @MethodSource("testToCaseData")
    @ParameterizedTest(name = "{index} {0} {1}")
    void testToCase(CasedString casedString, CasedString.StringCase stringCase, String expected) {
        assertThat(casedString.toCase(stringCase)).isEqualTo(expected);
    }

    static Stream<Arguments> testToCaseData() {
        List<Arguments> lst = new ArrayList<>();

        CasedString underTest = new CasedString(CasedString.StringCase.PASCAL, "camelCase");
        lst.add(Arguments.of(underTest, CasedString.StringCase.PASCAL, "camelCase"));
        lst.add(Arguments.of(underTest, CasedString.StringCase.SNAKE, "camel_Case"));
        lst.add(Arguments.of(underTest, CasedString.StringCase.KEBAB, "camel-Case"));
        lst.add(Arguments.of(underTest, CasedString.StringCase.PHRASE, "camel Case"));
        lst.add(Arguments.of(underTest, CasedString.StringCase.DOT, "camel.Case"));
        lst.add(Arguments.of(underTest, CasedString.StringCase.CAMEL, "CamelCase"));

        underTest = new CasedString(CasedString.StringCase.SNAKE, "snake_case");
        lst.add(Arguments.of(underTest, CasedString.StringCase.PASCAL, "snakeCase"));
        lst.add(Arguments.of(underTest, CasedString.StringCase.SNAKE, "snake_case"));
        lst.add(Arguments.of(underTest, CasedString.StringCase.KEBAB, "snake-case"));
        lst.add(Arguments.of(underTest, CasedString.StringCase.PHRASE, "snake case"));
        lst.add(Arguments.of(underTest, CasedString.StringCase.DOT, "snake.case"));
        lst.add(Arguments.of(underTest, CasedString.StringCase.CAMEL, "SnakeCase"));

        underTest = new CasedString(CasedString.StringCase.KEBAB, "kebab-case");
        lst.add(Arguments.of(underTest, CasedString.StringCase.PASCAL, "kebabCase"));
        lst.add(Arguments.of(underTest, CasedString.StringCase.SNAKE, "kebab_case"));
        lst.add(Arguments.of(underTest, CasedString.StringCase.KEBAB, "kebab-case"));
        lst.add(Arguments.of(underTest, CasedString.StringCase.PHRASE, "kebab case"));
        lst.add(Arguments.of(underTest, CasedString.StringCase.DOT, "kebab.case"));
        lst.add(Arguments.of(underTest, CasedString.StringCase.CAMEL, "KebabCase"));

        underTest = new CasedString(CasedString.StringCase.PHRASE, "phrase case");
        lst.add(Arguments.of(underTest, CasedString.StringCase.PASCAL, "phraseCase"));
        lst.add(Arguments.of(underTest, CasedString.StringCase.SNAKE, "phrase_case"));
        lst.add(Arguments.of(underTest, CasedString.StringCase.KEBAB, "phrase-case"));
        lst.add(Arguments.of(underTest, CasedString.StringCase.PHRASE, "phrase case"));
        lst.add(Arguments.of(underTest, CasedString.StringCase.DOT, "phrase.case"));
        lst.add(Arguments.of(underTest, CasedString.StringCase.CAMEL, "PhraseCase"));

        underTest = new CasedString(CasedString.StringCase.DOT, "dot.case");
        lst.add(Arguments.of(underTest, CasedString.StringCase.PASCAL, "dotCase"));
        lst.add(Arguments.of(underTest, CasedString.StringCase.SNAKE, "dot_case"));
        lst.add(Arguments.of(underTest, CasedString.StringCase.KEBAB, "dot-case"));
        lst.add(Arguments.of(underTest, CasedString.StringCase.PHRASE, "dot case"));
        lst.add(Arguments.of(underTest, CasedString.StringCase.DOT, "dot.case"));
        lst.add(Arguments.of(underTest, CasedString.StringCase.CAMEL, "DotCase"));

        underTest = new CasedString(CasedString.StringCase.PASCAL, "pascalCase");
        lst.add(Arguments.of(underTest, CasedString.StringCase.PASCAL, "pascalCase"));
        lst.add(Arguments.of(underTest, CasedString.StringCase.SNAKE, "pascal_Case"));
        lst.add(Arguments.of(underTest, CasedString.StringCase.KEBAB, "pascal-Case"));
        lst.add(Arguments.of(underTest, CasedString.StringCase.PHRASE, "pascal Case"));
        lst.add(Arguments.of(underTest, CasedString.StringCase.DOT, "pascal.Case"));
        lst.add(Arguments.of(underTest, CasedString.StringCase.CAMEL, "PascalCase"));

        underTest = new CasedString(CasedString.StringCase.DOT, "one..two");
        lst.add(Arguments.of(underTest, CasedString.StringCase.PASCAL, "oneTwo"));
        lst.add(Arguments.of(underTest, CasedString.StringCase.SNAKE, "one__two"));
        lst.add(Arguments.of(underTest, CasedString.StringCase.KEBAB, "one--two"));
        lst.add(Arguments.of(underTest, CasedString.StringCase.PHRASE, "one  two"));
        lst.add(Arguments.of(underTest, CasedString.StringCase.DOT, "one..two"));
        lst.add(Arguments.of(underTest, CasedString.StringCase.CAMEL, "OneTwo"));

        return lst.stream();
    }

    @MethodSource("testAssembleData")
    @ParameterizedTest(name = "{index} {0} {1}")
    void testAssemble(CasedString.StringCase underTest, String[] data, String expected) {
        assertThat(underTest.assemble(data)).isEqualTo(expected);
    }

    static Stream<Arguments> testAssembleData() {
        List<Arguments> lst = new ArrayList<>();
        String[] emptyFirst = {"", "one", "two"};
        String[] emptyMiddle = {"one", "", "two"};
        String[] emptyEnd = {"one", "two", ""};
        String[] nullFirst = {null, "one", "two"};
        String[] nullMiddle = {"one", null, "two"};
        String[] nullEnd = {"one", "two", null};
        String[] doubleEmpty = {"one", "", "", "two"};
        String[] doubleNull = {"one", null, null, "two"};

        CasedString.StringCase underTest = CasedString.StringCase.PASCAL;
        lst.add(Arguments.of(underTest, emptyFirst, "oneTwo"));
        lst.add(Arguments.of(underTest, emptyMiddle, "oneTwo"));
        lst.add(Arguments.of(underTest, emptyEnd, "oneTwo"));
        lst.add(Arguments.of(underTest, nullFirst, "oneTwo"));
        lst.add(Arguments.of(underTest, nullMiddle, "oneTwo"));
        lst.add(Arguments.of(underTest, nullEnd, "oneTwo"));
        lst.add(Arguments.of(underTest, doubleEmpty, "oneTwo"));
        lst.add(Arguments.of(underTest, doubleNull, "oneTwo"));

        underTest = CasedString.StringCase.CAMEL;
        lst.add(Arguments.of(underTest, emptyFirst, "OneTwo"));
        lst.add(Arguments.of(underTest, emptyMiddle, "OneTwo"));
        lst.add(Arguments.of(underTest, emptyEnd, "OneTwo"));
        lst.add(Arguments.of(underTest, nullFirst, "OneTwo"));
        lst.add(Arguments.of(underTest, nullMiddle, "OneTwo"));
        lst.add(Arguments.of(underTest, nullEnd, "OneTwo"));
        lst.add(Arguments.of(underTest, doubleEmpty, "OneTwo"));
        lst.add(Arguments.of(underTest, doubleNull, "OneTwo"));

        underTest = CasedString.StringCase.SNAKE;
        lst.add(Arguments.of(underTest, emptyFirst, "_one_two"));
        lst.add(Arguments.of(underTest, emptyMiddle, "one__two"));
        lst.add(Arguments.of(underTest, emptyEnd, "one_two_"));
        lst.add(Arguments.of(underTest, nullFirst, "one_two"));
        lst.add(Arguments.of(underTest, nullMiddle, "one_two"));
        lst.add(Arguments.of(underTest, nullEnd, "one_two"));
        lst.add(Arguments.of(underTest, doubleEmpty, "one___two"));
        lst.add(Arguments.of(underTest, doubleNull, "one_two"));

        underTest = CasedString.StringCase.KEBAB;
        lst.add(Arguments.of(underTest, emptyFirst, "-one-two"));
        lst.add(Arguments.of(underTest, emptyMiddle, "one--two"));
        lst.add(Arguments.of(underTest, emptyEnd, "one-two-"));
        lst.add(Arguments.of(underTest, nullFirst, "one-two"));
        lst.add(Arguments.of(underTest, nullMiddle, "one-two"));
        lst.add(Arguments.of(underTest, nullEnd, "one-two"));
        lst.add(Arguments.of(underTest, doubleEmpty, "one---two"));
        lst.add(Arguments.of(underTest, doubleNull, "one-two"));

        underTest = CasedString.StringCase.PHRASE;
        lst.add(Arguments.of(underTest, emptyFirst, " one two"));
        lst.add(Arguments.of(underTest, emptyMiddle, "one  two"));
        lst.add(Arguments.of(underTest, emptyEnd, "one two "));
        lst.add(Arguments.of(underTest, nullFirst, "one two"));
        lst.add(Arguments.of(underTest, nullMiddle, "one two"));
        lst.add(Arguments.of(underTest, nullEnd, "one two"));
        lst.add(Arguments.of(underTest, doubleEmpty, "one   two"));
        lst.add(Arguments.of(underTest, doubleNull, "one two"));

        underTest = CasedString.StringCase.DOT;
        lst.add(Arguments.of(underTest, emptyFirst, ".one.two"));
        lst.add(Arguments.of(underTest, emptyMiddle, "one..two"));
        lst.add(Arguments.of(underTest, emptyEnd, "one.two."));
        lst.add(Arguments.of(underTest, nullFirst, "one.two"));
        lst.add(Arguments.of(underTest, nullMiddle, "one.two"));
        lst.add(Arguments.of(underTest, nullEnd, "one.two"));
        lst.add(Arguments.of(underTest, doubleEmpty, "one...two"));
        lst.add(Arguments.of(underTest, doubleNull, "one.two"));

        underTest = CasedString.StringCase.SLASH;
        lst.add(Arguments.of(underTest, emptyFirst, "/one/two"));
        lst.add(Arguments.of(underTest, emptyMiddle, "one//two"));
        lst.add(Arguments.of(underTest, emptyEnd, "one/two/"));
        lst.add(Arguments.of(underTest, nullFirst, "one/two"));
        lst.add(Arguments.of(underTest, nullMiddle, "one/two"));
        lst.add(Arguments.of(underTest, nullEnd, "one/two"));
        lst.add(Arguments.of(underTest, doubleEmpty, "one///two"));
        lst.add(Arguments.of(underTest, doubleNull, "one/two"));

        return lst.stream();
    }

    @Test
    void asTest() {
        CasedString underTest = new CasedString(CasedString.StringCase.CAMEL, "camelCase");
        assertThat(underTest.as(CasedString.StringCase.CAMEL)).isEqualTo(underTest);
        CasedString expected = new CasedString(CasedString.StringCase.KEBAB, "camel-Case");
        assertThat(underTest.as(CasedString.StringCase.KEBAB)).isEqualTo(expected);
        underTest.getSegments()[0] = "Cow";
        assertThat(expected.getSegments()[0]).as("verify segments not shared").isEqualTo("camel");
    }

    @Test
    void testHashCode() {
        CasedString s1 = new CasedString(CasedString.StringCase.KEBAB, "the-test-string");
        CasedString s2 = new CasedString(CasedString.StringCase.DOT, "the.test.string");
        CasedString s3 = new CasedString(CasedString.StringCase.SLASH, "the/test/String");
        assertThat(s1).hasSameHashCodeAs(s2);
        assertThat(s1.hashCode()).isNotEqualTo(s3.hashCode());
    }

    @Test
    void testSegmentConstructor() {
        CasedString s1 = new CasedString(CasedString.StringCase.KEBAB, new String[]{"the", "test", "string"});
        CasedString s2 = new CasedString(CasedString.StringCase.KEBAB, "the-test-string");
        assertThat(s1).isEqualTo(s2);
        CasedString s3 = new CasedString(CasedString.StringCase.DOT, new String[]{"the", "test", "string"});
        assertThat(s1).isNotEqualTo(s3);
    }

    @Test
    void testNullValue() {
        CasedString nullValue = new CasedString(CasedString.StringCase.CAMEL, (String) null);
        assertThat(nullValue.getSegments()).isEqualTo(CasedString.StringCase.NULL_SEGMENT);
        assertThat(nullValue.toString()).isNull();
        assertThat(nullValue.as(CasedString.StringCase.DOT).getSegments()).isEqualTo(CasedString.StringCase.NULL_SEGMENT);
        assertThat(nullValue.toCase(CasedString.StringCase.DOT)).isNull();
        assertThat(nullValue).isEqualTo(new CasedString(CasedString.StringCase.CAMEL, (String) null))
                        .isNotEqualTo(new CasedString(CasedString.StringCase.CAMEL, ""));
    }

    @Test
    void testEmptyString() {
        CasedString emptyValue = new CasedString(CasedString.StringCase.CAMEL, "");
        assertThat(emptyValue.getSegments()).isEqualTo(CasedString.StringCase.EMPTY_SEGMENT);
        assertThat(emptyValue.toString()).isEmpty();
        assertThat(emptyValue.as(CasedString.StringCase.DOT).getSegments()).isEqualTo(CasedString.StringCase.EMPTY_SEGMENT);
        assertThat(emptyValue.toCase(CasedString.StringCase.DOT)).isEmpty();
        assertThat(emptyValue).isEqualTo(new CasedString(CasedString.StringCase.CAMEL, ""))
                .isNotEqualTo(new CasedString(CasedString.StringCase.CAMEL, (String)null));
    }

    @Test
    void testCamel() {
        CasedString underTest = new CasedString(CasedString.StringCase.CAMEL, "camelCase");
        assertThat(underTest).hasToString("CamelCase");
        assertThat(underTest.getSegments()).isEqualTo(new String[]{"camel", "Case"});
        underTest = new CasedString(CasedString.StringCase.CAMEL, new String[]{"camel", "case"});
        assertThat(underTest).hasToString("CamelCase");

    }

    @Test
    void testPascal() {
        CasedString underTest = new CasedString(CasedString.StringCase.PASCAL, "PascalCase");
        assertThat(underTest).hasToString("pascalCase");
        assertThat(underTest.getSegments()).isEqualTo(new String[]{"Pascal", "Case"});
        underTest = new CasedString(CasedString.StringCase.PASCAL, new String[]{"pascal", "case"});
        assertThat(underTest).hasToString("pascalCase");
    }
}
