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

import static java.lang.String.format;
import static java.nio.charset.Charset.forName;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import com.google.inject.internal.util.Lists;
import de.ingogriebsch.maven.sync.packagejson.version.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Synchronizes the <code>package.json</code> like files that are declared as included so that they have the same version as the
 * version of the <code>pom.xml</code>.
 * 
 * @since 1.0.0
 */
@Mojo(name = "sync", requiresProject = true, requiresDirectInvocation = true)
class SyncMojo extends AbstractMojo {

    private static final String PROPERTY_PREFIX = "sync-packagejson-version.sync.";

    /**
     * The encoding in which the files are interpreted while executing this mojo.
     * 
     * @since 1.0.0
     */
    @Parameter(property = PROPERTY_PREFIX + "encoding", defaultValue = "${project.build.sourceEncoding}")
    private String encoding;

    /**
     * List of files to include. Specified as file-set patterns which are relative to the projects root directory.
     * 
     * @since 1.0.0
     */
    @Parameter(property = PROPERTY_PREFIX + "includes", defaultValue = "package.json")
    private String[] includes;

    /**
     * List of files to exclude. Specified as file-set patterns which are relative to the projects root directory.
     * 
     * @since 1.0.0
     */
    @Parameter(property = PROPERTY_PREFIX + "excludes")
    private String[] excludes;

    /**
     * @see AbstractMojo#isSkipped()
     */
    @Override
    protected boolean isSkipped() {
        return false;
    }

    /**
     * @see AbstractMojo#doExecute()
     */
    @Override
    protected void doExecute() throws MojoExecutionException, MojoFailureException {
        List<File> packageJsons = PackageJsonCollector.of(project.getBasedir(), asList(includes), asList(excludes)).collect();

        if (packageJsons.isEmpty()) {
            throw new MojoFailureException("No package.json like file found in this project!");
        }

        boolean singlePackageJson = packageJsons.size() == 1;
        logger.info(
            format("Synchronizing the version of %s package.json like file%s with the version of the pom.xml of this project...",
                singlePackageJson ? "the" : Integer.toString(packageJsons.size()), singlePackageJson ? "" : "s"));

        String version = project.getVersion();
        packageJsons.forEach(pj -> synchronize(version, pj, encoding));

        logger.info("Done! :)");
    }

    private static void synchronize(String version, File packageJson, String encoding) {
        VersionWriter.of(packageJson, forName(encoding)).write(version);
    }

    private static List<String> asList(String[] values) {
        return values != null ? Arrays.asList(values) : Lists.newArrayList();
    }
}
