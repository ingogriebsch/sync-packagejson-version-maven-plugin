package de.ingogriebsch.maven.sync.packagejson.version.plugin;

import org.apache.maven.project.MavenProject;

/**
 * A {@link PomVersionEvaluator} that returns the runtime version of the Maven project.
 * <p>
 * This means, that the implementation simply asks the given {@link MavenProject} instance about the version.
 * 
 * @since 1.1.0
 *
 */
class RuntimePomVersionEvaluator implements PomVersionEvaluator {

    @Override
    public String get(MavenProject mavenProject) {
        return mavenProject.getVersion();
    }
}
