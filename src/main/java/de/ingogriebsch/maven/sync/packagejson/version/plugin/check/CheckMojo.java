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

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

import static org.apache.maven.plugins.annotations.LifecyclePhase.VERIFY;

import java.io.File;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.google.inject.internal.util.Lists;
import de.ingogriebsch.maven.sync.packagejson.version.plugin.AbstractMojo;
import de.ingogriebsch.maven.sync.packagejson.version.plugin.PackageJsonCollector;
import de.ingogriebsch.maven.sync.packagejson.version.plugin.check.VersionValidator.ConstraintViolation;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Checks if the version of a <code>package.json</code> has the same version as the version declared in the <code>pom.xml</code>
 * and fails the build if not.
 * 
 * @since 1.0.0
 */
@Mojo(name = "check", defaultPhase = VERIFY, requiresProject = true, threadSafe = true)
class CheckMojo extends AbstractMojo {

    private static final String PROPERTY_PREFIX = "sync-packagejson-version.check.";

    /**
     * Flag to control if the execution of the goal should be skipped.
     * 
     * @since 1.0.0
     */
    @Parameter(property = PROPERTY_PREFIX + "skip", alias = "skipCheck", defaultValue = "false")
    private boolean skip = false;

    /**
     * Flag to control if the execution of the goal should fail if no package.json is found.
     * 
     * @since 1.0.0
     */
    @Parameter(property = PROPERTY_PREFIX + "failIfNoneFound", defaultValue = "true")
    private boolean failIfNoneFound = true;

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
     * @see AbstractMojo#isSkipped()
     */
    @Override
    protected boolean isSkipped() {
        return skip;
    }

    /**
     * @see AbstractMojo#doExecute()
     */
    @Override
    protected void doExecute() throws MojoExecutionException, MojoFailureException {
        File baseDir = project.getBasedir();
        List<File> packageJsons = PackageJsonCollector.of(baseDir, asList(includes), asList(excludes)).collect();

        if (packageJsons.isEmpty()) {
            String msg = "No package.json file found in this project!";
            if (failIfNoneFound) {
                throw new MojoFailureException(msg);
            }
            logger.warn(msg);
            return;
        }

        boolean singlePackageJson = packageJsons.size() == 1;
        logger.info(
            format("Checking if the version of the %d found package.json file%s %s in sync with the version of the pom.xml...",
                packageJsons.size(), singlePackageJson ? "" : "s", singlePackageJson ? "is" : "are"));

        String version = project.getVersion();
        List<ConstraintViolation> violations = packageJsons //
            .stream() //
            .map(pj -> validate(version, baseDir, pj, encoding)) //
            .filter(Optional::isPresent) //
            .map(Optional::get) //
            .collect(toList());

        if (!violations.isEmpty()) {
            output(violations);

            boolean singleViolation = violations.size() == 1;
            throw new MojoFailureException(
                format("%d package.json file%s found in this project %s not in sync with the version of the pom.xml!",
                    violations.size(), singleViolation ? "" : "s", singleViolation ? "is" : "are"));
        }

        logger.info("Looks fine! :)");
    }

    private void output(List<ConstraintViolation> violations) {
        violations.forEach(v -> logger.error(v.toString()));
    }

    private static Optional<ConstraintViolation> validate(String version, File baseDir, File packageJson, String encoding) {
        return VersionValidator.of(baseDir, packageJson, Charset.forName(encoding)).validate(version);
    }

    private static List<String> asList(String[] values) {
        return values != null ? Arrays.asList(values) : Lists.newArrayList();
    }
}
