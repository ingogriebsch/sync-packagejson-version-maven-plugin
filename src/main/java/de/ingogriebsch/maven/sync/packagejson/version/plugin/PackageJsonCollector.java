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

import static com.google.common.collect.Lists.newArrayList;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import lombok.Value;
import org.apache.maven.shared.model.fileset.FileSet;
import org.apache.maven.shared.model.fileset.util.FileSetManager;

/**
 * A component that collects the <code>package.json</code> like files.
 * 
 * @since 1.0.0
 */
public class PackageJsonCollector {

    private final Logger logger;

    public PackageJsonCollector(Logger logger) {
        this.logger = logger;
    }

    /**
     * Returns the list of <code>package.json's</code> that are found based on the given includes and excludes.
     * 
     * @param params the parameters that are used to collect the relevant <code>package.json's</code>.
     * @return the list of <code>package.json's</code> that are found based on the given includes and excludes
     * @since 1.2.0
     */
    public List<PackageJson> collect(Params params) {
        FileSet fileSet = prepareFileSet(params);
        logger.debug("Using fileSet [%s] to collect the relevant package.json's.", asString(fileSet));

        String[] fileNames = new FileSetManager().getIncludedFiles(fileSet);
        List<PackageJson> files = stream(fileNames).map(n -> packageJson(n, params)).collect(toList());

        logger.debug("Collected the following package.json's: %s.", files);
        return files;
    }

    private static PackageJson packageJson(String packageJsonFilename, Params params) {
        return PackageJson.of(params.getBaseDir(), new File(params.getBaseDir(), packageJsonFilename));
    }

    private static FileSet prepareFileSet(Params params) {
        FileSet fileSet = new FileSet();
        fileSet.setFollowSymlinks(false);
        fileSet.setUseDefaultExcludes(false);
        fileSet.setDirectory(params.getBaseDir().getAbsolutePath());
        fileSet.setIncludes(asList(params.getIncludes()));
        fileSet.setExcludes(asList(params.getExcludes()));
        return fileSet;
    }

    private static List<String> asList(String[] elements) {
        return elements != null ? Arrays.asList(elements) : newArrayList();
    }

    @Value(staticConstructor = "of")
    public static class Params {

        File baseDir;
        String[] includes;
        String[] excludes;

        public static Params of(File baseDir, String[] includes) {
            return of(baseDir, includes, null);
        }
    }

    private static String asString(FileSet fileSet) {
        return new StringBuilder("FileSet(") //
            .append("directory=") //
            .append(fileSet.getDirectory()) //
            .append(", includes=") //
            .append(fileSet.getIncludes()) //
            .append(", excludes=") //
            .append(fileSet.getExcludes()) //
            .append(")") //
            .toString();
    }
}
