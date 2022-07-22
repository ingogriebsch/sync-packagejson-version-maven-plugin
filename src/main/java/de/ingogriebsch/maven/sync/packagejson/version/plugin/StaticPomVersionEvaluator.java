package de.ingogriebsch.maven.sync.packagejson.version.plugin;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;

import java.io.File;
import java.io.IOException;

import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
class StaticPomVersionEvaluator implements PomVersionEvaluator {

    private static final XmlMapper xmlMapper = xmlMapper();
    private final Logger logger;

    @Override
    @SneakyThrows(IOException.class)
    public String get(MavenProject mavenProject) {
        File file = new File(mavenProject.getBasedir(), "pom.xml");
        logger.debug("Reading the version from pom file '%s'...", file.getAbsolutePath());
        Project project = xmlMapper.readValue(file, Project.class);

        String version = project.getVersion();
        if (version == null) {
            logger.debug("Version of the projects pom file is not given, therefore falling back to the version of the parent...");
            Project.Parent parent = project.getParent();
            version = parent != null ? parent.getVersion() : null;
        }

        logger.debug("Evaluated pom file version '%s' [in a static way].", version);
        return version;
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
