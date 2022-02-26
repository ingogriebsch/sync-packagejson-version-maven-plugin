package de.ingogriebsch.maven.sync.packagejson.version.plugin;

import lombok.RequiredArgsConstructor;
import org.apache.maven.project.MavenProject;

/**
 * A {@link PomVersionEvaluator} that returns the runtime version of the Maven project.
 * <p>
 * This means, that the implementation simply asks the given {@link MavenProject} instance about the version.
 * 
 * @since 1.1.0
 *
 */
@RequiredArgsConstructor
class RuntimePomVersionEvaluator implements PomVersionEvaluator {

    private final Logger logger;

    @Override
    public String get(MavenProject mavenProject) {
        String version = mavenProject.getVersion();
        logger.debug("Evaluated [during runtime] version '%s'.", version);
        return version;
    }
}
