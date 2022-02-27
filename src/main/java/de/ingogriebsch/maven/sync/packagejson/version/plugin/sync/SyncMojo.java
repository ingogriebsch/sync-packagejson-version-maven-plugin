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
import java.util.List;

import javax.inject.Singleton;

import de.ingogriebsch.maven.sync.packagejson.version.plugin.AbstractMojo;
import de.ingogriebsch.maven.sync.packagejson.version.plugin.PackageJson;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Synchronizes the version of a <code>package.json</code> with the version of the <code>pom.xml</code>.
 * 
 * @since 1.0.0
 */
@Singleton
@Mojo(name = "sync", requiresProject = true, requiresDirectInvocation = true)
class SyncMojo extends AbstractMojo {

    private static final String PROPERTY_PREFIX = "sync-packagejson-version.sync.";

    /**
     * The writer that is used to write the version of a <code>package.json</code>.
     * 
     * @since 1.2.0
     */
    private final VersionWriter versionWriter;

    /**
     * The encoding in which the package.json file is interpreted while executing this mojo.
     * 
     * @since 1.0.0
     */
    @Parameter(property = PROPERTY_PREFIX + "encoding", defaultValue = "${project.build.sourceEncoding}")
    private String encoding;

    /**
     * The list of files to include. Specified as file-set patterns which are relative to the projects root directory.
     * 
     * @since 1.0.0
     */
    @Parameter(property = PROPERTY_PREFIX + "includes", defaultValue = "package.json,package-lock.json")
    private String[] includes;

    /**
     * The list of files to exclude. Specified as file-set patterns which are relative to the projects root directory.
     * 
     * @since 1.0.0
     */
    @Parameter(property = PROPERTY_PREFIX + "excludes")
    private String[] excludes;

    /**
     * The rule how the version of the pom.xml is evaluated. Legal values are 'runtime' and 'static'.
     * 
     * @since 1.1.0
     */
    @Parameter(property = PROPERTY_PREFIX + "pomVersionEvaluation", defaultValue = "runtime")
    private String pomVersionEvaluation;

    SyncMojo() {
        versionWriter = new VersionWriter(logger);
    }

    /**
     * @see AbstractMojo#getPomVersionEvaluation()
     */
    @Override
    protected String getPomVersionEvaluation() {
        return pomVersionEvaluation;
    }

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
        List<File> packageJsons = collectPackageJsons(includes, excludes);
        if (packageJsons.isEmpty()) {
            throw new MojoFailureException("No package.json found in this project!");
        }

        boolean singlePackageJson = packageJsons.size() == 1;
        logger.info(format(
            "Synchronizing the version of the %d found package.json%s with the version of the pom.xml [using '%s' evaluation]...",
            packageJsons.size(), singlePackageJson ? "" : "'s", pomVersionEvaluation));

        File baseDir = project.getBasedir();
        String pomVersion = evaluatePomVersion(project);
        packageJsons //
            .stream() //
            .map(pj -> PackageJson.of(baseDir, pj, forName(encoding))) //
            .forEach(pj -> synchronize(pomVersion, pj));

        logger.info("Done! :)");
    }

    private void synchronize(String pomVersion, PackageJson packageJson) {
        versionWriter.write(pomVersion, packageJson).ifPresent(p -> logger.info("  " + p.toString()));
    }
}
