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

import java.util.function.Supplier;

import lombok.RequiredArgsConstructor;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.Component;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.MavenProjectHelper;

/**
 * An extension of {@link org.apache.maven.plugin.AbstractMojo} which acts as a base class for all concrete mojo's to ease the
 * implementation of them.
 * <p>
 * This class also handles some general cases like skipping the execution and checking if the packaging is supported. It also
 * provides some helper methods to ease the implementation of specific cases.
 * 
 * @since 1.0.0
 */
public abstract class AbstractMojo extends org.apache.maven.plugin.AbstractMojo {

    protected final Logger logger;

    @Parameter(defaultValue = "${project}", readonly = true)
    protected MavenProject project;

    @Component
    protected MavenProjectHelper helper;

    protected AbstractMojo() {
        logger = new Logger(this::getLog);
    }

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

    protected abstract boolean isSkipped();

    protected void validate() throws Exception {
    }

    protected abstract void doExecute() throws MojoExecutionException, MojoFailureException;

    protected boolean supportsPackaging(String packaging) {
        return true;
    }

    @RequiredArgsConstructor
    protected static class Logger {

        private final Supplier<Log> source;

        public void debug(String message, Object... args) {
            Log log = source.get();
            if (log.isDebugEnabled()) {
                log.debug(format(message, args));
            }
        }

        public void info(String message, Object... args) {
            Log log = source.get();
            if (log.isInfoEnabled()) {
                log.info(format(message, args));
            }
        }

        public void warn(String message, Object... args) {
            Log log = source.get();
            if (log.isWarnEnabled()) {
                log.warn(format(message, args));
            }
        }

        public void error(String message, Object... args) {
            Log log = source.get();
            if (log.isErrorEnabled()) {
                log.error(format(message, args));
            }
        }
    }
}
