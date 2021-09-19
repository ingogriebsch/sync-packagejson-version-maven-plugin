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

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.Value;

@Value(staticConstructor = "of")
class VersionValidator {

    private static final ObjectMapper objectMapper = objectMapper();

    private final File file;

    Optional<ConstraintViolation> validate(String version) {
        PackageJson packageJson = read(file);

        return matches(packageJson.getVersion(), version) ? Optional.empty()
            : Optional.of(ConstraintViolation.of(file, packageJson.getVersion(), version));
    }

    private static boolean matches(String packageJsonVersion, String pomVersion) {
        return pomVersion.equals(packageJsonVersion);
    }

    @SneakyThrows(IOException.class)
    private static PackageJson read(File file) {
        return objectMapper.readValue(file, PackageJson.class);
    }

    private static ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(FAIL_ON_UNKNOWN_PROPERTIES);
        return objectMapper;
    }

    @Value(staticConstructor = "of")
    static class ConstraintViolation {

        File packageJson;
        String packageJsonVersion;
        String pomVersion;

        @Override
        public String toString() {
            return new StringBuilder("Version '") //
                .append(packageJsonVersion) //
                .append("' of package.json '") //
                .append(packageJson.getAbsolutePath()) //
                .append("' is not in sync with the version '") //
                .append(pomVersion) //
                .append("' of this project.") //
                .toString();
        }
    }

    @Data
    private static class PackageJson {

        private String version;
    }
}
