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

import static java.util.Optional.empty;
import static java.util.regex.Pattern.DOTALL;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ingogriebsch.maven.sync.packagejson.version.plugin.Logger;
import de.ingogriebsch.maven.sync.packagejson.version.plugin.PackageJson;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.Value;
import org.apache.commons.io.FileUtils;

/**
 * A component that overwrites the version of a <code>package.json</code> with the version of the <code>pom.xml</code>.
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
     * Writes the version to the given <code>package.json</code>.
     * 
     * @param pomVersion the version that should be written to the file
     * @param packageJson the <code>package.json</code> there the version is written.
     * @return an {@link Optional} that is either empty (if the version is already the same as the version in the pom.xml) or
     *         contains a {@link Protocol} (if the version needs to be synchronized).
     * @since 1.0.0
     */
    @SneakyThrows(IOException.class)
    Optional<Protocol> write(String pomVersion, PackageJson packageJson) {
        String name = packageJson.getName();

        String version = extractVersion(packageJson);
        if (version.equals(pomVersion)) {
            logger.debug("The version of '%s' is the same as of the pom.xml, therefore returning.", name);
            return empty();
        }

        File file = packageJson.getFile();
        Charset encoding = packageJson.getEncoding();
        String content = FileUtils.readFileToString(file, encoding);

        Matcher matcher = pattern.matcher(content);
        if (!matcher.matches() || matcher.groupCount() != 1) {
            logger.debug("No version found in '%s', therefore returning.", name);
            return empty();
        }

        logger.debug("Replacing the version in '%s' with version '%s'.", name, pomVersion);
        content = new StringBuilder(content) //
            .replace(matcher.start(1), matcher.end(1), pomVersion) //
            .toString();

        FileUtils.write(file, content, encoding, false);
        return Optional.of(Protocol.of(name, pomVersion));
    }

    @SneakyThrows(IOException.class)
    private static String extractVersion(PackageJson packageJson) {
        return objectMapper.readValue(packageJson.getFile(), PackageJsonContent.class).getVersion();
    }

    private static ObjectMapper objectMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.disable(FAIL_ON_UNKNOWN_PROPERTIES);
        return objectMapper;
    }

    /**
     * A pojo that describes that the version of the <code>package.json</code> is not the same as the version of the
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
    private static class PackageJsonContent {

        private String version;
    }
}
