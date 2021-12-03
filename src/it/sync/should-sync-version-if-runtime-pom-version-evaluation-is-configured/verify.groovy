import static org.assertj.core.api.Assertions.assertThat
import java.nio.charset.Charset
import java.nio.file.Files

def buildLog = new File(basedir, "build.log")
def buildLogLines = Files.readAllLines(buildLog.toPath(), Charset.defaultCharset())
assertThat(buildLogLines).containsOnlyOnce("[INFO] Done! :)")

def packageJson = new File(basedir, "package.json")
def packageJsonLines = Files.readAllLines(packageJson.toPath(), Charset.defaultCharset())
assertThat(packageJsonLines).containsOnlyOnce("    \"version\": \"1.2.0-SNAPSHOT\",")

// Need to return after all checks, otherwise the test will fail!
return
