/*-
 * Copyright 2019-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package de.ingogriebsch.maven.sync.packagejson.version.plugin.check;

import static java.nio.charset.StandardCharsets.UTF_8;

import static org.apache.commons.io.FileUtils.writeStringToFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.File;
import java.io.IOException;

import de.ingogriebsch.maven.sync.packagejson.version.plugin.check.VersionValidator.ConstraintViolation;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class VersionValidatorTest {

    @Test
    void should_return_an_empty_optional_if_the_version_matches(@TempDir File tempDir) throws Exception {
        String version = "1.2.3-SNAPSHOT";
        File packageJson = new File(tempDir, "package.json");
        writeStringToFile(packageJson, "{\"version\": \"" + version + "\"}", UTF_8);

        VersionValidator validator = VersionValidator.of(tempDir, packageJson, UTF_8);
        assertThat(validator.validate(version)).isEmpty();
    }

    @Test
    void should_return_a_constraint_violation_if_the_version_does_not_match(@TempDir File tempDir) throws Exception {
        String pomVersion = "1.2.3-SNAPSHOT";
        File packageJson = new File(tempDir, "package.json");
        String packageJsonVersion = "1.2.4-SNAPSHOT";
        writeStringToFile(packageJson, "{\"version\": \"" + packageJsonVersion + "\"}", UTF_8);

        VersionValidator validator = VersionValidator.of(tempDir, packageJson, UTF_8);

        assertThat(validator.validate(pomVersion)).isNotEmpty();
    }

    @Test
    void should_fail_if_the_file_is_not_a_valid_package_json(@TempDir File tempDir) throws IOException {
        File packageJson = new File(tempDir, "package.json");
        writeStringToFile(packageJson, "some content", UTF_8);

        VersionValidator validator = VersionValidator.of(tempDir, packageJson, UTF_8);
        assertThatThrownBy(() -> validator.validate("1.2.3-SNAPSHOT")).isInstanceOf(IOException.class);
    }

    @Nested
    class ConstraintViolationTest {

        @Test
        void toString_should_return_a_string_containing_all_the_necessary_information() throws IOException {
            String packageJsonName = "package.json";
            String packageJsonVersion = "1.0.0-SNAPSHOT";
            String pomVersion = "1.2.0-SNAPSHOT";

            ConstraintViolation constraintViolation = ConstraintViolation.of(packageJsonName, packageJsonVersion, pomVersion);

            assertThat(constraintViolation.toString()) //
                .contains(packageJsonName) //
                .contains(packageJsonVersion) //
                .contains(pomVersion);
        }
    }
}
