import static org.assertj.core.api.Assertions.assertThat
import java.nio.charset.Charset
import java.nio.file.Files

def buildLog = new File(basedir, "build.log")
def lines = Files.readAllLines(buildLog.toPath(), Charset.defaultCharset())

// FIXME assertThat(lines).containsOnlyOnce("[ERROR] Failed to execute goal de.ingogriebsch.maven:sync-packagejson-version-maven-plugin:1.1.0-SNAPSHOT:check (check) on project some.fancy.artifact: Caught exception while validating this mojo! Property 'pomVersionEvaluation' must contain one of the following values '[static, runtime]' but contains value 'unknown'! -> [Help 1]")

// Need to return after all checks, otherwise the test will fail!
return
