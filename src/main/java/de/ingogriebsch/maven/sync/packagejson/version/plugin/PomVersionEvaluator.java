package de.ingogriebsch.maven.sync.packagejson.version.plugin;

import org.apache.maven.project.MavenProject;

/**
 * An SPI that allows to request the version of the pom.xml. The implementation dictates how to provide the version.
 * 
 * @since 1.1.0
 */
public interface PomVersionEvaluator {

    String get(MavenProject mavenProject);
}
