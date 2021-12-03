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

import static org.apache.commons.io.FileUtils.writeStringToFile;
import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

import java.io.File;
import java.io.IOException;

import de.ingogriebsch.maven.sync.packagejson.version.plugin.PomVersionEvaluatorFactory;
import lombok.SneakyThrows;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class SyncMojoTest {

    @Test
    void should_fail_if_no_package_json_is_found(@TempDir File tempDir) {
        MavenProject mavenProject = mock(MavenProject.class);
        doReturn(tempDir).when(mavenProject).getBasedir();

        SyncMojo mojo = new SyncMojo(new PomVersionEvaluatorFactory());
        apply(mojo, "log", mock(Log.class));
        apply(mojo, "project", mavenProject);
        apply(mojo, "pomVersionEvaluation", "runtime");

        assertThatThrownBy(() -> mojo.execute()).isInstanceOf(MojoFailureException.class);
    }

    @Test
    void should_succeed_if_package_json_is_found(@TempDir File tempDir) throws IOException {
        new File(tempDir, "README.md").createNewFile();

        String version = "1.1.0-SNAPSHOT";
        File packageJson = new File(tempDir, "package.json");
        writeStringToFile(packageJson, "{\"version\": \"" + version + "\"}", UTF_8);

        MavenProject mavenProject = mock(MavenProject.class);
        doReturn(tempDir).when(mavenProject).getBasedir();
        doReturn(version).when(mavenProject).getVersion();

        SyncMojo mojo = new SyncMojo(new PomVersionEvaluatorFactory());
        apply(mojo, "log", mock(Log.class));
        apply(mojo, "project", mavenProject);
        apply(mojo, "encoding", UTF_8.toString());
        apply(mojo, "includes", new String[] { "package.json" });
        apply(mojo, "pomVersionEvaluation", "runtime");

        assertThatNoException().isThrownBy(() -> mojo.execute());
    }

    @SneakyThrows
    private static <T> T apply(T instance, String name, Object value) {
        FieldUtils.writeField(instance, name, value, true);
        return instance;
    }
}
