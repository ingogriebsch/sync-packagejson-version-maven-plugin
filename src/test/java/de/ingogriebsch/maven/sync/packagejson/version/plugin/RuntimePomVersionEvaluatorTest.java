package de.ingogriebsch.maven.sync.packagejson.version.plugin;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;

class RuntimePomVersionEvaluatorTest {

    @Test
    void should_return_version_from_maven_project() {
        MavenProject mavenProject = new MavenProject();
        mavenProject.setVersion("1.0.1-SNAPSHOT");

        assertThat(new RuntimePomVersionEvaluator().get(mavenProject)).isEqualTo(mavenProject.getVersion());
    }
}
