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
package de.ingogriebsch.maven.sync.packagejson.version.plugin.sync;

import static java.io.File.separator;
import static java.util.Optional.empty;
import static java.util.regex.Pattern.DOTALL;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static org.apache.commons.io.FilenameUtils.separatorsToUnix;
import static org.apache.commons.lang3.StringUtils.substringAfter;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ingogriebsch.maven.sync.packagejson.version.plugin.Logger;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.Value;
import org.apache.commons.io.FileUtils;

/**
 * A component that overwrites the version of the <code>package.json</code> with the version of the <code>pom.xml</code>.
 * 
 * @since 1.0.0
 */
class VersionWriter {

    private static final Pattern pattern = Pattern.compile("^.*?\"version\"\\s?:\\s?\"(.+?)\".*$", DOTALL);
    private static final ObjectMapper objectMapper = objectMapper();
    private final Logger logger;

    VersionWriter(Logger logger) {
        this.logger = logger;
    }

    /**
     * Writes the version to the given file.
     * 
     * @param pomVersion the version that should be written to the file
     * @return an {@link Optional} that is either empty (if the version is already the same as the version in the pom.xml) or
     *         contains a {@link Protocol} (if the version needs to be synchronized).
     * @param baseDir the directory that is used as the root of the folder and file structure.
     * @param file the <code>package.json</code> like file that is validated.
     * @param encoding the encoding of the <code>package.json</code> like file.
     * @since 1.0.0
     */
    @SneakyThrows(IOException.class)
    Optional<Protocol> write(String pomVersion, File baseDir, File file, Charset encoding) {
        String name = relativeName(file, baseDir);

        String packageJsonVersion = extractVersion(file, encoding);
        if (packageJsonVersion.equals(pomVersion)) {
            logger.debug("Version of the package.json '%s' is the same as of the pom.xml, therefore returning.", name);
            return empty();
        }

        String packageJsonContent = FileUtils.readFileToString(file, encoding);
        Matcher matcher = pattern.matcher(packageJsonContent);
        if (!matcher.matches() || matcher.groupCount() != 1) {
            logger.debug("No version found in package.json '%s', therefore returning.", name);
            return empty();
        }

        logger.debug("Replacing the version in package.json '%s' with version '%s'.", name, pomVersion);
        packageJsonContent = new StringBuilder(packageJsonContent) //
            .replace(matcher.start(1), matcher.end(1), pomVersion) //
            .toString();

        FileUtils.write(file, packageJsonContent, encoding, false);
        return Optional.of(Protocol.of(name, pomVersion));
    }

    private static String relativeName(File file, File baseDir) {
        return separatorsToUnix(substringAfter(file.getAbsolutePath(), baseDir.getAbsolutePath() + separator));
    }

    @SneakyThrows(IOException.class)
    private static String extractVersion(File file, Charset encoding) {
        return objectMapper.readValue(file, PackageJson.class).getVersion();
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
    static class Protocol {

        String packageJsonName;
        String pomVersion;

        @Override
        public String toString() {
            return new StringBuilder("Set the version in '") //
                .append(packageJsonName) //
                .append("' to '") //
                .append(pomVersion) //
                .append("'.") //
                .toString();
        }
    }

    @Data
    private static class PackageJson {

        private String version;
    }
}
