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
package de.ingogriebsch.maven.sync.packagejson.version.plugin;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.InstanceOfAssertFactories.FILE;

import java.io.File;
import java.io.IOException;
import java.util.List;

import de.ingogriebsch.maven.sync.packagejson.version.plugin.PackageJsonCollector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PackageJsonCollectorTest {

    @Test
    void should_return_an_empty_list_if_the_base_directory_is_empty(@TempDir File tempDir) {
        PackageJsonCollector collector = PackageJsonCollector.of(tempDir).withInclude("**/*.*");
        assertThat(collector.collect()).isEmpty();
    }

    @Test
    void should_return_a_matching_list_if_the_base_directory_contains_a_matching_file(@TempDir File tempDir) throws IOException {
        new File(tempDir, "package-lock.json").createNewFile();
        File packageJson = new File(tempDir, "package.json");
        packageJson.createNewFile();

        PackageJsonCollector collector = PackageJsonCollector.of(tempDir).withInclude("package.json");
        assertThat(collector.collect()).hasSize(1).first().isEqualTo(packageJson);
    }

    @Test
    void should_return_a_matching_list_if_the_sub_directory_contains_a_matching_file(@TempDir File tempDir) throws IOException {
        new File(tempDir, "package.json").createNewFile();

        File subDir = new File(tempDir, "dir");
        subDir.mkdir();

        File packageJson = new File(subDir, "package.json");
        packageJson.createNewFile();

        PackageJsonCollector collector = PackageJsonCollector.of(tempDir).withInclude("dir/package.json");
        assertThat(collector.collect()).hasSize(1).first(FILE).isEqualTo(packageJson);
    }

    @Test
    void should_return_an_empty_list_if_the_base_directory_does_not_contain_a_matching_file(@TempDir File tempDir)
        throws IOException {
        new File(tempDir, "package-lock.json").createNewFile();

        PackageJsonCollector collector = PackageJsonCollector.of(tempDir).withInclude("package.json");
        assertThat(collector.collect()).isEmpty();
    }

    @Test
    void should_return_a_matching_list_if_the_base_directory_and_sub_directories_contain_matching_files(@TempDir File tempDir)
        throws IOException {
        new File(tempDir, "package-lock.json").createNewFile();
        File packageJson1 = new File(tempDir, "package.json");
        packageJson1.createNewFile();

        File subDir = new File(tempDir, "dir");
        subDir.mkdir();

        new File(subDir, "package-lock.json").createNewFile();
        File packageJson2 = new File(subDir, "package.json");
        packageJson2.createNewFile();

        PackageJsonCollector collector = PackageJsonCollector.of(tempDir).withInclude("**/package.json");
        List<File> collected = collector.collect();
        assertThat(collected).containsExactly(packageJson2, packageJson1);
    }

    @Test
    void should_return_an_empty_list_if_the_base_directory_and_sub_directories_do_not_contain_matching_files(
        @TempDir File tempDir) throws IOException {
        new File(tempDir, "package-lock.json").createNewFile();

        File subDir = new File(tempDir, "dir");
        subDir.mkdir();

        new File(subDir, "package-lock.json").createNewFile();

        PackageJsonCollector collector = PackageJsonCollector.of(tempDir).withInclude("**/package.json");
        assertThat(collector.collect()).isEmpty();
    }
}
