package de.ingogriebsch.maven.sync.packagejson.version.plugin;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

import java.io.IOException;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.SneakyThrows;
import lombok.Value;
import org.apache.maven.project.MavenProject;

/**
 * A {@link PomVersionEvaluator} that returns the static version of the Maven project.
 * <p>
 * This means that the implementation checks the content of the pom.xml file and extracts the value of the 'version' element.
 * 
 * @since 1.1.0
 *
 */
class StaticPomVersionEvaluator implements PomVersionEvaluator {

    private static final XmlMapper xmlMapper = xmlMapper();

    @Override
    @SneakyThrows(IOException.class)
    public String get(MavenProject mavenProject) {
        Project project = xmlMapper.readValue(mavenProject.getFile(), Project.class);

        String version = project.getVersion();
        if (version != null) {
            return version;
        }

        Project.Parent parent = project.getParent();
        return parent != null ? parent.getVersion() : null;
    }

    private static XmlMapper xmlMapper() {
        return XmlMapper.builder().disable(FAIL_ON_UNKNOWN_PROPERTIES).build();
    }

    @Value
    private static class Project {

        Parent parent;
        String version;

        @Value
        private static class Parent {

            String version;

        }
    }
}
