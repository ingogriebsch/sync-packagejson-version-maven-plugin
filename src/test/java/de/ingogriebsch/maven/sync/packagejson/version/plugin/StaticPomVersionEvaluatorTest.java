package de.ingogriebsch.maven.sync.packagejson.version.plugin;

import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;

import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;

class StaticPomVersionEvaluatorTest {

    @Test
    void should_return_version_from_pom_xml() throws Exception {
        MavenProject mavenProject = new MavenProject();
        mavenProject.setFile(new File(StaticPomVersionEvaluator.class.getResource("staticPomVersionEvaluator/pom.xml").toURI()));

        assertThat(new StaticPomVersionEvaluator().get(mavenProject)).isEqualTo("1.0.0-SNAPSHOT");
    }

    @Test
    void should_return_version_of_parent_from_pom_xml() throws Exception {
        MavenProject mavenProject = new MavenProject();
        mavenProject.setFile(
            new File(StaticPomVersionEvaluator.class.getResource("staticPomVersionEvaluator/pom-with-parent.xml").toURI()));

        assertThat(new StaticPomVersionEvaluator().get(mavenProject)).isEqualTo("1.0.0-SNAPSHOT");
    }
}
