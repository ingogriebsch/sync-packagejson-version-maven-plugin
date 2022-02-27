package de.ingogriebsch.maven.sync.packagejson.version.plugin;

import static java.io.File.separator;

import static org.apache.commons.io.FilenameUtils.separatorsToUnix;
import static org.apache.commons.lang3.StringUtils.substringAfter;

import java.io.File;
import java.nio.charset.Charset;

import lombok.Value;

/**
 * An abstraction of the <code>package.json</code> to ease the implementation of the different use cases.
 * 
 * @since 1.2.0
 */
@Value(staticConstructor = "of")
public class PackageJson {

    File baseDir;
    File file;
    Charset encoding;

    @Override
    public String toString() {
        return getName();
    }

    public String getName() {
        return separatorsToUnix(substringAfter(file.getAbsolutePath(), baseDir.getAbsolutePath() + separator));
    }
}
