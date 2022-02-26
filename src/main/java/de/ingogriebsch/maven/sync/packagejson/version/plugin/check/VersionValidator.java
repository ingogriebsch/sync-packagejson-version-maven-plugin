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
import static org.apache.commons.io.FilenameUtils.separatorsToUnix;
import static org.apache.commons.lang3.StringUtils.substringAfter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ingogriebsch.maven.sync.packagejson.version.plugin.Logger;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.Value;

/**
 * A component that checks if the version of the given <code>package.json</code> like file is valid (means is the same as the
 * given version).
 * 
 * @since 1.0.0
 */
class VersionValidator {

    private static final ObjectMapper objectMapper = objectMapper();
    private final Logger logger;

    VersionValidator(Logger logger) {
        this.logger = logger;
    }

    /**
     * Checks if the version of the given <code>package.json</code> like file is valid (means is the same as the given version).
     * 
     * @param pomVersion the version of the <code>pom.xml</code>
     * @param baseDir the directory that is used as the root of the folder and file structure.
     * @param file the <code>package.json</code> like file that is validated.
     * @param encoding the encoding of the <code>package.json</code> like file.
     * @return an {@link Optional} that is either empty (if the version is valid) or contains a {@link ConstraintViolation} (if
     *         the version is not valid).
     * @since 1.0.0
     */
    Optional<ConstraintViolation> validate(String pomVersion, File baseDir, File file, Charset encoding) {
        String name = relativeName(file, baseDir);
        String packageJsonVersion = read(file).getVersion();
        logger.debug("Read version '%s' from package.json '%s'.", packageJsonVersion, name);

        if (!packageJsonVersion.equals(pomVersion)) {
            logger.debug("Version '%s' of the package.json does not match against version '%s' of the pom.xml.",
                packageJsonVersion, pomVersion);
            return Optional.of(ConstraintViolation.of(name, packageJsonVersion, pomVersion));
        }
        return Optional.empty();
    }

    private static String relativeName(File file, File baseDir) {
        return separatorsToUnix(substringAfter(file.getAbsolutePath(), baseDir.getAbsolutePath() + File.separator));
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

    /**
     * A pojo that describes that the version of the <code>package.json</code> like file is not the same as the version of the
     * <code>pom.xml</code>.
     * 
     * @since 1.0.0
     */
    @Value(staticConstructor = "of")
    static class ConstraintViolation {

        String packageJsonName;
        String packageJsonVersion;
        String pomVersion;

        @Override
        public String toString() {
            return new StringBuilder("Version '") //
                .append(packageJsonVersion) //
                .append("' of '") //
                .append(packageJsonName) //
                .append("' is not in sync with version '") //
                .append(pomVersion) //
                .append("' of the pom.xml!") //
                .toString();
        }
    }

    @Data
    private static class PackageJson {

        private String version;
    }
}
