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

import static org.assertj.core.api.Assertions.assertThatNoException;

import org.junit.jupiter.api.Test;

class CheckPropertiesValidatorTest {

    @Test
    void should_succeed_if_includes_is_not_given() {
        CheckPropertiesValidator validator =
            CheckPropertiesValidator.of(checkProperties(UTF_8.toString(), null, new String[] { "*.*" }, true));
        assertThatNoException().isThrownBy(() -> validator.validate());
    }

    @Test
    void should_succeed_if_excludes_is_not_given() {
        CheckPropertiesValidator validator =
            CheckPropertiesValidator.of(checkProperties(UTF_8.toString(), new String[] { "*.*" }, null, true));
        assertThatNoException().isThrownBy(() -> validator.validate());
    }

    @Test
    void should_succeed_if_all_properties_are_valid() {
        CheckPropertiesValidator validator =
            CheckPropertiesValidator.of(checkProperties(UTF_8.toString(), new String[] { "*.*" }, new String[] { "*.*" }, true));
        assertThatNoException().isThrownBy(() -> validator.validate());
    }

    private static CheckProperties checkProperties(String encoding, String[] includes, String[] excludes,
        boolean failIfNoneFound) {
        return new CheckProperties() {

            @Override
            public String getEncoding() {
                return encoding;
            }

            @Override
            public String[] getIncludes() {
                return includes;
            }

            @Override
            public String[] getExcludes() {
                return excludes;
            }

            @Override
            public boolean isFailIfNoneFound() {
                return failIfNoneFound;
            }
        };
    }
}
