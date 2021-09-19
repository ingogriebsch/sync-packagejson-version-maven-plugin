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

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import static com.google.inject.internal.util.Lists.newArrayList;

import java.io.File;
import java.util.List;

import lombok.Value;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

@Value(staticConstructor = "of")
class PackageJsonCollector {

    File baseDir;
    List<String> includes;
    List<String> excludes;

    List<File> collect() {
        return stream(new FileSetManager().getIncludedFiles(prepareFileSet())).map(this::file).collect(toList());
    }

    static PackageJsonCollector of(File baseDir) {
        return of(baseDir, (List<String>) null, (List<String>) null);
    }

    static PackageJsonCollector of(File baseDir, String include, String exclude) {
        return of(baseDir, newArrayList(include), newArrayList(exclude));
    }

    PackageJsonCollector withInclude(String include) {
        return of(baseDir, newArrayList(include), excludes);
    }

    PackageJsonCollector withExclude(String exclude) {
        return of(baseDir, includes, newArrayList(exclude));
    }

    private FileSet prepareFileSet() {
        FileSet fileSet = new FileSet();
        fileSet.setDirectory(baseDir.getAbsolutePath());
        fileSet.setFollowSymlinks(false);
        fileSet.setUseDefaultExcludes(false);
        fileSet.setIncludes(includes);
        fileSet.setExcludes(excludes);
        return fileSet;
    }

    private File file(String name) {
        return new File(baseDir, name);
    }
}
