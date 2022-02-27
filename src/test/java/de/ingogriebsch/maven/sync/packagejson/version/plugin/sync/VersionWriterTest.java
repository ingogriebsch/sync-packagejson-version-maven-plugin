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

import static java.nio.charset.StandardCharsets.UTF_8;

import static de.ingogriebsch.maven.sync.packagejson.version.plugin.Logger.noOpLogger;
import static org.apache.commons.io.FileUtils.copyFile;
import static org.apache.commons.io.FileUtils.readFileToString;
import static org.apache.commons.io.FileUtils.writeStringToFile;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.InstanceOfAssertFactories.map;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.ingogriebsch.maven.sync.packagejson.version.plugin.PackageJson;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class VersionWriterTest {

    @Test
    void should_replace_version_in_file(@TempDir File tempDir) throws Exception {
        String version = "1.2.3-SNAPSHOT";
        PackageJson packageJson = PackageJson.of(tempDir, new File(tempDir, "package.json"));
        writeStringToFile(packageJson.getFile(), "{\"version\": \"1.0.0\"}", UTF_8);

        VersionWriter validator = new VersionWriter(noOpLogger());
        assertThatNoException().isThrownBy(() -> validator.write(version, packageJson, UTF_8));

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> json = objectMapper.readValue(packageJson.getFile(), objectMapper.constructType(Map.class));
        assertThat(json).containsEntry("version", version);
    }

    @Test
    void should_preserve_the_structure_of_the_file(@TempDir File tempDir) throws Exception {
        PackageJson packageJson = PackageJson.of(tempDir, new File(tempDir, "package.json"));
        copyFile(new File(VersionWriterTest.class.getResource("versionWriter/package.json").toURI()), packageJson.getFile());
        String contentBefore = readFileToString(packageJson.getFile(), UTF_8);

        VersionWriter validator = new VersionWriter(noOpLogger());
        assertThatNoException().isThrownBy(() -> validator.write("0.2.0", packageJson, UTF_8));

        String contentAfter = readFileToString(packageJson.getFile(), UTF_8);
        assertThat(contentBefore).isEqualTo(contentAfter);
    }

    @Test
    void should_fail_if_the_file_is_not_a_valid_package_json(@TempDir File tempDir) throws IOException {
        PackageJson packageJson = PackageJson.of(tempDir, new File(tempDir, "package.json"));
        writeStringToFile(packageJson.getFile(), "some content", UTF_8);

        VersionWriter validator = new VersionWriter(noOpLogger());
        assertThatThrownBy(() -> validator.write("1.2.3-SNAPSHOT", packageJson, UTF_8)).isInstanceOf(IOException.class);
    }

    @Test
    void should_only_write_top_level_version_if_single_line_content(@TempDir File tempDir) throws Exception {
        String content = "{\"version\": \"1.0.0\", \"dependencies\": {\"version\": \"2.0.0\"}}";
        PackageJson packageJson = PackageJson.of(tempDir, new File(tempDir, "package.json"));
        writeStringToFile(packageJson.getFile(), content, UTF_8);

        String version = "1.2.3-SNAPSHOT";
        VersionWriter validator = new VersionWriter(noOpLogger());
        assertThatNoException().isThrownBy(() -> validator.write(version, packageJson, UTF_8));

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> json = objectMapper.readValue(packageJson.getFile(), objectMapper.constructType(Map.class));

        assertThat(json).containsEntry("version", version);

        assertThat(json.get("dependencies")) //
            .asInstanceOf(map(String.class, Object.class)) //
            .containsEntry("version", "2.0.0");
    }

    @Test
    void should_only_write_top_level_version_if_multi_line_content(@TempDir File tempDir) throws Exception {
        // @formatter:off
        String content = "{\r\n"
                + "    \"version\": \"1.0.0\",\r\n"
                + "    \"dependencies\": {\r\n"
                + "        \"version\": \"2.0.0\"\r\n"
                + "    }\r\n"
                + "}\r\n";
        // @formatter:on
        PackageJson packageJson = PackageJson.of(tempDir, new File(tempDir, "package.json"));
        writeStringToFile(packageJson.getFile(), content, UTF_8);

        String version = "1.2.3-SNAPSHOT";
        VersionWriter validator = new VersionWriter(noOpLogger());
        assertThatNoException().isThrownBy(() -> validator.write(version, packageJson, UTF_8));

        ObjectMapper objectMapper = new ObjectMapper();
        Map<String, Object> json = objectMapper.readValue(packageJson.getFile(), objectMapper.constructType(Map.class));

        assertThat(json).containsEntry("version", version);

        assertThat(json.get("dependencies")) //
            .asInstanceOf(map(String.class, Object.class)) //
            .containsEntry("version", "2.0.0");
    }
}
