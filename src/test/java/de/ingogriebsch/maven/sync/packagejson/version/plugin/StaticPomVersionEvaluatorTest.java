package de.ingogriebsch.maven.sync.packagejson.version.plugin;

import static de.ingogriebsch.maven.sync.packagejson.version.plugin.Logger.noOpLogger;
import static org.assertj.core.api.Assertions.assertThat;

import java.io.File;
import java.net.URI;

import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;

class StaticPomVersionEvaluatorTest {

    @Test
    void should_return_version_from_pom_xml() throws Exception {
        File pomFile = new File(StaticPomVersionEvaluator.class.getResource("staticPomVersionEvaluator/pom.xml").toURI());
        MavenProject mavenProject = new MavenProject();
        mavenProject.setFile(pomFile);

        StaticPomVersionEvaluator evaluator = new StaticPomVersionEvaluator(noOpLogger());
        String version = evaluator.get(mavenProject);

        assertThat(version).isEqualTo("1.0.0-SNAPSHOT");
    }

    @Test
    void should_return_version_of_parent_from_pom_xml() throws Exception {
        URI pomFile = StaticPomVersionEvaluator.class.getResource("staticPomVersionEvaluator/pom-with-parent.xml").toURI();
        MavenProject mavenProject = new MavenProject();
        mavenProject.setFile(new File(pomFile));

        StaticPomVersionEvaluator evaluator = new StaticPomVersionEvaluator(noOpLogger());
        String version = evaluator.get(mavenProject);

        assertThat(version).isEqualTo("1.0.0-SNAPSHOT");
    }
}
