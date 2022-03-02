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
import static java.nio.charset.Charset.forName;
import static java.util.stream.Collectors.toList;

import static org.apache.maven.plugins.annotations.LifecyclePhase.VERIFY;

import java.util.List;
import java.util.Optional;

import javax.inject.Singleton;

import de.ingogriebsch.maven.sync.packagejson.version.plugin.AbstractMojo;
import de.ingogriebsch.maven.sync.packagejson.version.plugin.PackageJson;
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
@Singleton
@Mojo(name = "check", defaultPhase = VERIFY, requiresProject = true, threadSafe = true)
class CheckMojo extends AbstractMojo {

    private static final String PROPERTY_PREFIX = "sync-packagejson-version.check.";

    /**
     * The validator that is used to validate if the version of a <code>package.json</code> matches against the version of the
     * <code>pom.xml</code>.
     * 
     * @since 1.2.0
     */
    private final VersionValidator versionValidator;

    /**
     * Flag to control if the execution of the goal should be skipped.
     * 
     * @since 1.0.0
     */
    @Parameter(property = PROPERTY_PREFIX + "skip", alias = "skipCheck", defaultValue = "false")
    private boolean skip = false;

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
     * Flag to control if the execution of the goal should fail if no package.json is found.
     * 
     * @since 1.0.0
     */
    @Parameter(property = PROPERTY_PREFIX + "failIfNoneFound", defaultValue = "true")
    private boolean failIfNoneFound = true;

    /**
     * The rule how the version of the pom.xml is evaluated. Permissible values are 'runtime' and 'static'.
     * 
     * @since 1.1.0
     */
    @Parameter(property = PROPERTY_PREFIX + "pomVersionEvaluation", defaultValue = "runtime")
    private String pomVersionEvaluation;

    CheckMojo() {
        versionValidator = new VersionValidator(logger);
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
        return skip;
    }

    /**
     * @see AbstractMojo#doExecute()
     */
    @Override
    protected void doExecute() throws MojoExecutionException, MojoFailureException {
        logger.info(format(
            "Checking if the version of the package.json's found in this project are in sync with the version of the pom.xml [using '%s' evaluation]...",
            pomVersionEvaluation));

        List<PackageJson> packageJsons = collectPackageJsons(includes, excludes);
        if (packageJsons.isEmpty()) {
            String msg = "No package.json's found in this project!";
            if (failIfNoneFound) {
                throw new MojoFailureException(msg);
            }
            logger.warn(msg);
            return;
        }

        String pomVersion = evaluatePomVersion(project);
        List<ConstraintViolation> violations = packageJsons //
            .stream() //
            .map(pj -> versionValidator.validate(pomVersion, pj, forName(encoding))) //
            .flatMap(Optional::stream) //
            .collect(toList());

        if (!violations.isEmpty()) {
            output(violations);

            boolean singleViolation = violations.size() == 1;
            throw new MojoFailureException(
                format("%d package.json%s found in this project %s not in sync with the version of the pom.xml!",
                    violations.size(), singleViolation ? "" : "'s", singleViolation ? "is" : "are"));
        }

        logger.info("Looks fine! :)");
    }

    private void output(List<ConstraintViolation> violations) {
        violations.forEach(v -> logger.error(v.toString()));
    }
}
