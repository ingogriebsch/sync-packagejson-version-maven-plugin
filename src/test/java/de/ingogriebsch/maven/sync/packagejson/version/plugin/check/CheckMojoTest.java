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
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;

import lombok.SneakyThrows;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class CheckMojoTest {

    @Test
    void should_succeed_if_skip_is_set_to_true() {
        CheckMojo mojo = new CheckMojo();
        apply(mojo, "log", mock(Log.class));
        apply(mojo, "skip", true);

        assertThatNoException().isThrownBy(() -> mojo.execute());
    }

    @Test
    void should_fail_if_no_package_json_is_found(@TempDir File tempDir) {
        MavenProject mavenProject = mock(MavenProject.class);
        doReturn(tempDir).when(mavenProject).getBasedir();

        CheckMojo mojo = new CheckMojo();
        apply(mojo, "log", mock(Log.class));
        apply(mojo, "project", mavenProject);

        assertThatThrownBy(() -> mojo.execute()).isInstanceOf(MojoFailureException.class);
    }

    @Test
    void should_succeed_if_no_package_json_is_found_but_failIfNoneFound_is_set_to_false(@TempDir File tempDir) {
        MavenProject mavenProject = mock(MavenProject.class);
        doReturn(tempDir).when(mavenProject).getBasedir();

        CheckMojo mojo = new CheckMojo();
        apply(mojo, "log", mock(Log.class));
        apply(mojo, "project", mavenProject);
        apply(mojo, "failIfNoneFound", false);

        assertThatNoException().isThrownBy(() -> mojo.execute());
    }

    @Test
    void should_fail_if_version_of_package_json_does_not_match_version_of_pom(@TempDir File tempDir) throws IOException {
        new File(tempDir, "README.md").createNewFile();

        File packageJson = new File(tempDir, "package.json");
        writeStringToFile(packageJson, "{\"version\": \"1.0.0-SNAPSHOT\"}", UTF_8);

        MavenProject mavenProject = mock(MavenProject.class);
        doReturn(tempDir).when(mavenProject).getBasedir();
        doReturn("1.1.0-SNAPSHOT").when(mavenProject).getVersion();

        CheckMojo mojo = new CheckMojo();
        apply(mojo, "log", mock(Log.class));
        apply(mojo, "project", mavenProject);
        apply(mojo, "encoding", UTF_8.toString());
        apply(mojo, "includes", new String[] { "package.json" });

        assertThatThrownBy(() -> mojo.execute()).isInstanceOf(MojoFailureException.class);
    }

    @Test
    void should_succeed_if_version_of_package_json_matches_version_of_pom(@TempDir File tempDir) throws IOException {
        new File(tempDir, "README.md").createNewFile();

        String version = "1.1.0-SNAPSHOT";
        File packageJson = new File(tempDir, "package.json");
        writeStringToFile(packageJson, "{\"version\": \"" + version + "\"}", UTF_8);

        MavenProject mavenProject = mock(MavenProject.class);
        doReturn(tempDir).when(mavenProject).getBasedir();
        doReturn(version).when(mavenProject).getVersion();

        CheckMojo mojo = new CheckMojo();
        apply(mojo, "log", mock(Log.class));
        apply(mojo, "project", mavenProject);
        apply(mojo, "encoding", UTF_8.toString());
        apply(mojo, "includes", new String[] { "package.json" });

        assertThatNoException().isThrownBy(() -> mojo.execute());
    }

    @SneakyThrows
    private static <T> T apply(T instance, String name, Object value) {
        FieldUtils.writeField(instance, name, value, true);
        return instance;
    }
}
