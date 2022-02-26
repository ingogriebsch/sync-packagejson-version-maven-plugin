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

import static java.lang.String.format;

import java.util.Arrays;
import java.util.Set;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

/**
 * An extension of {@link org.apache.maven.plugin.AbstractMojo} which acts as a base class for all concrete mojo's to ease the
 * implementation of them.
 * <p>
 * This class also handles some general cases like skipping the execution and checking if the packaging is supported. It also
 * provides some helper methods to ease the implementation of specific cases.
 * 
 * @since 1.0.0
 */
/**
 * @author Ingo
 *
 */
public abstract class AbstractMojo extends org.apache.maven.plugin.AbstractMojo {

    /**
     * The factory instance that should be used to evaluate the version of the pom.xml.
     * 
     * @since 1.0.0
     */
    private final PomVersionEvaluatorFactory pomVersionEvaluationFactory;

    /**
     * A logger that should be used instead of the log instance that is provided through Maven.
     * 
     * @since 1.0.0
     */
    protected final Logger logger;

    /**
     * The Maven project the mojo is executed on.
     * 
     * @since 1.0.0
     */
    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    /**
     * The default constructor which is responsible for initializing common members.
     * <p>
     * Needs to be called by the classes that extend this class.
     * 
     * @since 1.0.0
     */
    protected AbstractMojo() {
        this.logger = Logger.logger(this::getLog);
        this.pomVersionEvaluationFactory = new PomVersionEvaluatorFactory(logger);
    }

    /**
     * @see org.apache.maven.plugin.Mojo#execute()
     */
    @Override
    public final void execute() throws MojoExecutionException, MojoFailureException {
        if (isSkipped()) {
            logger.info("Execution is skipped on purpose!");
            return;
        }

        String packaging = project.getPackaging();
        if (!supportsPackaging(packaging)) {
            logger.info("Execution is skipped because the packaing of this project [%s] is not supported!", packaging);
            return;
        }

        try {
            validate();
        } catch (Exception e) {
            throw new MojoExecutionException("Caught exception while validating this mojo!", e);
        }

        try {
            doExecute();
        } catch (MojoExecutionException | MojoFailureException e) {
            throw e;
        } catch (Exception e) {
            throw new MojoExecutionException("Caught exception while execute this mojo!", e);
        }
    }

    /**
     * Explains if the execution of the mojo should be skipped or not!
     * 
     * @return {@code true} if the execution of the mojo should be skipped, otherwise {@code false}
     * @since 1.0.0
     */
    protected abstract boolean isSkipped();

    /**
     * Executes whatever process behavior this mojo implements.
     * <p>
     * Needs to be implemented by the mojo that extends this abstract and will be executed if all previous checks do not hinder
     * this method to be executed.
     * 
     * @throws MojoFailureException if the execution of the mojo fails.
     * @throws MojoExecutionException if the execution of the mojo breaks.
     * @since 1.0.0
     */
    protected abstract void doExecute() throws MojoExecutionException, MojoFailureException;

    /**
     * Returns the 'pomVersionEvaluation' property configured on the concrete mojo
     * 
     * @return the 'pomVersionEvaluation' property configured on the concrete mojo.
     * @since 1.1.0
     */
    protected abstract String getPomVersionEvaluation();

    /**
     * Allows the mojo to validate whatever is necessary to get validated before executing the mojo.
     * 
     * @throws Exception if the validation is not successful.
     * @since 1.0.0
     */
    protected void validate() throws Exception {
        String pomVersionEvaluation = getPomVersionEvaluation();
        Set<String> pomVersionEvaluations = pomVersionEvaluationFactory.getIds();

        if (!pomVersionEvaluations.contains(pomVersionEvaluation)) {
            throw new IllegalArgumentException(
                format("Property 'pomVersionEvaluation' must contain one of the following values '%s' but contains value '%s'!",
                    Arrays.toString(pomVersionEvaluations.toArray()), pomVersionEvaluation));
        }
    }

    /**
     * Explains if the packaging of the project the mojo is running on is supported by the mojo.
     * 
     * @param packaging the packaging of the project the mojo is running on
     * @return {@code true} if the packaging of the project is supported by the mojo, otherwise {@code false}
     * @since 1.0.0
     */
    protected boolean supportsPackaging(String packaging) {
        return true;
    }

    /**
     * Evaluates the version of the pom.xml based on the configuration made on the concrete mojo.
     * 
     * @param mavenProject the Maven project to be evaluated.
     * @return the evaluated version from the pom.xml
     * @since 1.1.0
     */
    protected String evaluatePomVersion(MavenProject mavenProject) {
        return pomVersionEvaluationFactory.create(getPomVersionEvaluation()).map(p -> p.get(project)).orElseThrow();
    }
}
