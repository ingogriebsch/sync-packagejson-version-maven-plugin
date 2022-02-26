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

import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class AbstractMojoTest {

    @Nested
    class ExecuteTest {

        @Test
        void should_throw_mojo_execution_exception_if_generic_exception_is_thrown_by_validate() throws Exception {
            final IllegalArgumentException exception =
                new IllegalArgumentException("Must be wrapped into a MojoExecutionException!");
            try {
                AbstractMojo mojo = new AbstractMojo() {

                    @Override
                    protected boolean isSkipped() {
                        return false;
                    }

                    @Override
                    protected void validate() throws Exception {
                        throw exception;
                    }

                    @Override
                    protected void doExecute() throws MojoExecutionException, MojoFailureException {
                    }

                    @Override
                    protected String getPomVersionEvaluation() {
                        return "runtime";
                    }
                };

                mojo.setLog(mock(Log.class));
                mojo.project = mock(MavenProject.class);
                mojo.execute();
            } catch (Exception e) {
                if (e.getClass().equals(MojoExecutionException.class) && e.getCause().equals(exception)) {
                    return;
                }
            }
            fail("Exception thrown by validate was not wrapped!");
        }

        @Test
        void should_throw_mojo_execution_exception_if_generic_exception_is_thrown_by_doExecute() throws Exception {
            final IllegalArgumentException exception =
                new IllegalArgumentException("Must be wrapped into a MojoExecutionException!");
            try {
                AbstractMojo mojo = new AbstractMojo() {

                    @Override
                    protected boolean isSkipped() {
                        return false;
                    }

                    @Override
                    protected void doExecute() throws MojoExecutionException, MojoFailureException {
                        throw exception;
                    }

                    @Override
                    protected String getPomVersionEvaluation() {
                        return "runtime";
                    }
                };

                mojo.setLog(mock(Log.class));
                mojo.project = mock(MavenProject.class);
                mojo.execute();
            } catch (Exception e) {
                if (e.getClass().equals(MojoExecutionException.class) && e.getCause().equals(exception)) {
                    return;
                }
            }
            fail("Exception thrown by doExecute was not wrapped!");
        }

        @Test
        void should_rethrow_exception_if_mojo_execution_exception_is_thrown_by_doExecute() throws Exception {
            final MojoExecutionException exception = new MojoExecutionException("Must be wrapped rethrown!");
            try {
                AbstractMojo mojo = new AbstractMojo() {

                    @Override
                    protected boolean isSkipped() {
                        return false;
                    }

                    @Override
                    protected void doExecute() throws MojoExecutionException, MojoFailureException {
                        throw exception;
                    }

                    @Override
                    protected String getPomVersionEvaluation() {
                        return "runtime";
                    }
                };

                mojo.setLog(mock(Log.class));
                mojo.project = mock(MavenProject.class);
                mojo.execute();
            } catch (Exception e) {
                if (e.equals(exception)) {
                    return;
                }
            }
            fail("Exception thrown by doExecute was not wrapped!");
        }

        @Test
        void should_rethrow_exception_if_mojo_failure_exception_is_thrown_by_doExecute() throws Exception {
            final MojoFailureException exception = new MojoFailureException("Must be wrapped rethrown!");
            try {
                AbstractMojo mojo = new AbstractMojo() {

                    @Override
                    protected boolean isSkipped() {
                        return false;
                    }

                    @Override
                    protected void doExecute() throws MojoExecutionException, MojoFailureException {
                        throw exception;
                    }

                    @Override
                    protected String getPomVersionEvaluation() {
                        return "runtime";
                    }
                };

                mojo.setLog(mock(Log.class));
                mojo.project = mock(MavenProject.class);
                mojo.execute();
            } catch (Exception e) {
                if (e.equals(exception)) {
                    return;
                }
            }
            fail("Exception thrown by doExecute was not wrapped!");
        }

        @Test
        void should_be_skipped_if_configured_explicitely() throws Exception {
            AbstractMojo mojo = new AbstractMojo() {

                @Override
                protected boolean isSkipped() {
                    return true;
                }

                @Override
                protected void doExecute() throws MojoExecutionException, MojoFailureException {
                    fail("doExecute should never be called because the execution was marked as skipped!");
                }

                @Override
                protected String getPomVersionEvaluation() {
                    return "runtime";
                }
            };

            mojo.setLog(mock(Log.class));
            mojo.project = mock(MavenProject.class);
            mojo.execute();
        }

        @Test
        void should_be_skipped_if_packaging_is_not_supported() throws Exception {
            AbstractMojo mojo = new AbstractMojo() {

                @Override
                protected boolean isSkipped() {
                    return false;
                }

                @Override
                protected boolean supportsPackaging(String packaging) {
                    return false;
                }

                @Override
                protected void doExecute() throws MojoExecutionException, MojoFailureException {
                    fail("doExecute should never be called because the packaging is not supported!");
                }

                @Override
                protected String getPomVersionEvaluation() {
                    return "runtime";
                }
            };

            mojo.setLog(mock(Log.class));
            mojo.project = mock(MavenProject.class);
            mojo.execute();
        }

        @Test
        void should_trigger_doExecute_if_not_skipped_and_packaging_is_supported() throws Exception {
            final String testMessage = "Some test message!";

            AbstractMojo mojo = new AbstractMojo() {

                @Override
                protected boolean isSkipped() {
                    return false;
                }

                @Override
                protected void doExecute() throws MojoExecutionException, MojoFailureException {
                    logger.warn(testMessage);
                }

                @Override
                protected String getPomVersionEvaluation() {
                    return "runtime";
                }
            };

            Log log = mock(Log.class);
            doReturn(true).when(log).isWarnEnabled();

            mojo.setLog(log);
            mojo.project = mock(MavenProject.class);
            mojo.execute();

            verify(log).warn(testMessage);
        }
    }

    @Nested
    class LoggerTest {

        @Test
        void should_trigger_logger_if_debug_is_enabled() {
            Log log = mock(Log.class);
            doReturn(true).when(log).isDebugEnabled();

            AbstractMojo mojo = mojo();
            mojo.setLog(log);
            mojo.logger.debug("some message...");

            verify(log).isDebugEnabled();
            verify(log).debug(anyString());
            verifyNoMoreInteractions(log);
        }

        @Test
        void should_not_trigger_logger_if_debug_is_not_enabled() {
            Log log = mock(Log.class);
            doReturn(false).when(log).isDebugEnabled();

            AbstractMojo mojo = mojo();
            mojo.setLog(log);
            mojo.logger.debug("some message...");

            verify(log).isDebugEnabled();
            verifyNoMoreInteractions(log);
        }

        @Test
        void should_trigger_logger_if_info_is_enabled() {
            Log log = mock(Log.class);
            doReturn(true).when(log).isInfoEnabled();

            AbstractMojo mojo = mojo();
            mojo.setLog(log);
            mojo.logger.info("some message...");

            verify(log).isInfoEnabled();
            verify(log).info(anyString());
            verifyNoMoreInteractions(log);
        }

        @Test
        void should_not_trigger_logger_if_info_is_not_enabled() {
            Log log = mock(Log.class);
            doReturn(false).when(log).isInfoEnabled();

            AbstractMojo mojo = mojo();
            mojo.setLog(log);
            mojo.logger.info("some message...");

            verify(log).isInfoEnabled();
            verifyNoMoreInteractions(log);
        }

        private AbstractMojo mojo() {
            AbstractMojo mojo = new AbstractMojo() {

                @Override
                protected boolean isSkipped() {
                    return false;
                }

                @Override
                protected void doExecute() throws MojoExecutionException, MojoFailureException {
                }

                @Override
                protected String getPomVersionEvaluation() {
                    return "runtime";
                }
            };
            return mojo;
        }
    }
}
