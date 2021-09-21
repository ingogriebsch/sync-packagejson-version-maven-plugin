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

import static java.util.stream.Collectors.toList;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.List;
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

    File file;
    Charset encoding;

    /**
     * Writes the version to the given file.
     * 
     * @param version the version that should be written to the file
     * @since 1.0.0
     */
    void write(String version) {
        if (version.equals(extractVersion(file, encoding))) {
            return;
        }

        List<String> lines = readLines(file, encoding);
        lines = replaceVersion(lines, version);
        writeLines(lines, file, encoding);
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

    @Data
    private static class PackageJson {

        private String version;
    }
}
