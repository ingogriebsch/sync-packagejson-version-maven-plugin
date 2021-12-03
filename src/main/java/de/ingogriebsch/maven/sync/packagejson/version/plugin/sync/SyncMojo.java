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
import java.util.Set;

import javax.inject.Inject;

import com.google.inject.internal.util.Lists;
import de.ingogriebsch.maven.sync.packagejson.version.plugin.AbstractMojo;
import de.ingogriebsch.maven.sync.packagejson.version.plugin.PackageJsonCollector;
import de.ingogriebsch.maven.sync.packagejson.version.plugin.PomVersionEvaluatorFactory;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Synchronizes the version of a <code>package.json</code> file with the version of the <code>pom.xml</code>.
 * 
 * @since 1.0.0
 */
@Mojo(name = "sync", requiresProject = true, requiresDirectInvocation = true)
class SyncMojo extends AbstractMojo {

    private static final String PROPERTY_PREFIX = "sync-packagejson-version.sync.";

    private final PomVersionEvaluatorFactory pomVersionEvaluationFactory;

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

    /**
     * @see AbstractMojo#isSkipped()
     */
    @Override
    protected boolean isSkipped() {
        return false;
    }

    @Inject
    SyncMojo(PomVersionEvaluatorFactory pomVersionEvaluationFactory) {
        this.pomVersionEvaluationFactory = pomVersionEvaluationFactory;
    }

    /**
     * @see AbstractMojo#validate()
     */
    @Override
    protected void validate() throws Exception {
        Set<String> pomVersionEvaluations = pomVersionEvaluationFactory.getIds();
        if (!pomVersionEvaluations.contains(pomVersionEvaluation)) {
            throw new IllegalArgumentException(
                format("Property 'pomVersionEvaluation' must contain one of the following values '%s' but contains value '%s'!",
                    Arrays.toString(pomVersionEvaluations.toArray()), pomVersionEvaluation));
        }
    }

    /**
     * @see AbstractMojo#doExecute()
     */
    @Override
    protected void doExecute() throws MojoExecutionException, MojoFailureException {
        File baseDir = project.getBasedir();
        List<File> packageJsons = PackageJsonCollector.of(baseDir, asList(includes), asList(excludes)).collect();

        if (packageJsons.isEmpty()) {
            throw new MojoFailureException("No package.json file found in this project!");
        }

        boolean singlePackageJson = packageJsons.size() == 1;
        logger.info(format("Synchronizing the version of the %d found package.json file%s with the version of the pom.xml...",
            packageJsons.size(), singlePackageJson ? "" : "s"));

        String pomVersion = pomVersionEvaluationFactory.create(pomVersionEvaluation).map(p -> p.get(project)).orElseThrow();
        packageJsons.forEach(packageJson -> synchronize(pomVersion, baseDir, packageJson, encoding));

        logger.info("Done! :)");
    }

    private void synchronize(String pomVersion, File baseDir, File packageJson, String encoding) {
        VersionWriter.of(baseDir, packageJson, forName(encoding)).write(pomVersion)
            .ifPresent(p -> logger.info("  " + p.toString()));
    }

    private static List<String> asList(String[] values) {
        return values != null ? Arrays.asList(values) : Lists.newArrayList();
    }
}
