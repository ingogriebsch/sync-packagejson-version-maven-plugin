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

import static java.util.Arrays.stream;
import static java.util.stream.Collectors.toList;

import static com.google.inject.internal.util.Lists.newArrayList;

import java.io.File;
import java.util.List;

import lombok.Value;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

/**
 * A component that collects the <code>package.json</code> like files.
 * 
 * @since 1.0.0
 */
@Value(staticConstructor = "of")
public class PackageJsonCollector {

    File baseDir;
    List<String> includes;
    List<String> excludes;

    /**
     * Returns the list of <code>package.json</code> like files that are found based on the given includes and excludes.
     * 
     * @return the list of <code>package.json</code> like files that are found based on the given includes and excludes
     * @since 1.0.0
     */
    public List<File> collect() {
        return stream(new FileSetManager().getIncludedFiles(prepareFileSet())).map(this::file).collect(toList());
    }

    /**
     * A factory method that allows to create an instance of this class with the given base directory.
     * 
     * @param baseDir the base directory used to collect the pacakge.json like files
     * @return An instance of type {@link PackageJsonCollector}
     * @since 1.0.0
     */
    public static PackageJsonCollector of(File baseDir) {
        return of(baseDir, (List<String>) null, (List<String>) null);
    }

    /**
     * A factory method that allows to create an instance of this class with the given base directory, include and exclude.
     * 
     * @param baseDir the base directory used to collect the pacakge.json like files
     * @param include a file-set patterns that is interpreted relative to the given base directory
     * @param exclude a file-set patterns that is interpreted relative to the given base directory
     * @return An instance of type {@link PackageJsonCollector}
     * @since 1.0.0
     */
    public static PackageJsonCollector of(File baseDir, String include, String exclude) {
        return of(baseDir, newArrayList(include), newArrayList(exclude));
    }

    /**
     * A factory method that allows to create an instance of this class based on this instance and the given include.
     * 
     * @param include a file-set patterns that is interpreted relative to the given base directory
     * @return An instance of type {@link PackageJsonCollector}
     * @since 1.0.0
     */
    public PackageJsonCollector withInclude(String include) {
        return of(baseDir, newArrayList(include), excludes);
    }

    /**
     * A factory method that allows to create an instance of this class based on this instance and the given exclude.
     * 
     * @param exclude a file-set patterns that is interpreted relative to the given base directory
     * @return An instance of type {@link PackageJsonCollector}
     * @since 1.0.0
     */
    public PackageJsonCollector withExclude(String exclude) {
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
