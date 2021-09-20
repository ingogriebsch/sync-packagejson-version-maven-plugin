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

/**
 * Declares the set of properties that are used by the corresponding {@link CheckMojo} to control its execution.
 * 
 * @since 1.0.0
 */
interface CheckProperties {

    /**
     * Declares the encoding in which the files are interpreted while executing this mojo.
     * 
     * @return the encoding
     * @since 1.0.0
     */
    String getEncoding();

    /**
     * Declares the files which should be included in the check.
     * 
     * @return the list of files to include
     * @since 1.0.0
     */
    String[] getIncludes();

    /**
     * Declares the files which should be excluded from the check.
     * 
     * @return the list of files to exclude
     * @since 1.0.0
     */
    String[] getExcludes();

    /**
     * Declares if the execution should fail if no <code>package.json</code> like files are found.
     * 
     * @return {@code true} if the execution of the mojo should fail if no <code>package.json</code> like files are found,
     *         otherwise {@code false}
     * @since 1.0.0
     */
    boolean isFailIfNoneFound();
}
