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
import static java.util.stream.Collectors.toList;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static org.apache.commons.io.FilenameUtils.separatorsToUnix;
import static org.apache.commons.lang3.StringUtils.substringAfter;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.Value;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * A component that overwrites the version of the <code>package.json</code> with the version of the <code>pom.xml</code>.
 * 
 * @since 1.0.0
 */
@Value(staticConstructor = "of")
class VersionWriter {

    private static final Pattern pattern = Pattern.compile("^.*\"version\".*:.*\"(.+)\".*$");
    private static final ObjectMapper objectMapper = objectMapper();

    File baseDir;
    File file;
    Charset encoding;

    /**
     * Writes the version to the given file.
     * 
     * @param version the version that should be written to the file
     * @return an {@link Optional} that is either empty (if the version is already the same as the version in the pom.xml) or
     *         contains a {@link Protocol} (if the version needs to be synchronized).
     * @since 1.0.0
     */
    Optional<Protocol> write(String version) {
        if (version.equals(extractVersion(file, encoding))) {
            return empty();
        }

        List<String> lines = readLines(file, encoding);
        lines = replaceVersion(lines, version);
        writeLines(lines, file, encoding);

        return Optional.of(protocol(version));
    }

    private Protocol protocol(String version) {
        return Protocol.of( //
            separatorsToUnix(substringAfter(file.getAbsolutePath(), baseDir.getAbsolutePath() + File.separator)), //
            version //
        );
    }

    @SneakyThrows(IOException.class)
    private static String extractVersion(File file, Charset encoding) {
        return objectMapper.readValue(file, PackageJson.class).getVersion();
    }

    @SneakyThrows(IOException.class)
    private static List<String> readLines(File file, Charset encoding) {
        try (FileInputStream source = new FileInputStream(file)) {
            return IOUtils.readLines(source, encoding);
        }
    }

    private static List<String> replaceVersion(List<String> lines, String version) {
        return lines.stream().map(line -> replaceIf(line, version)).collect(toList());
    }

    private static String replaceIf(String line, String version) {
        Matcher matcher = pattern.matcher(line);
        if (!matcher.matches()) {
            return line;
        }

        return line.replace(matcher.group(1), version);
    }

    @SneakyThrows(IOException.class)
    private static void writeLines(List<String> lines, File file, Charset encoding) {
        FileUtils.writeLines(file, encoding.name(), lines);
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