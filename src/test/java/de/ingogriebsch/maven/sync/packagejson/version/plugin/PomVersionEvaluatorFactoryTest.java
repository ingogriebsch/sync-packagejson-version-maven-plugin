package de.ingogriebsch.maven.sync.packagejson.version.plugin;

import static de.ingogriebsch.maven.sync.packagejson.version.plugin.Logger.noOpLogger;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class PomVersionEvaluatorFactoryTest {

    @Test
    void should_return_optional_containing_evaluator_if_id_is_known() {
        PomVersionEvaluatorFactory factory = new PomVersionEvaluatorFactory(noOpLogger());
        String id = factory.getIds().iterator().next();

        assertThat(factory.create(id)).isPresent();
    }

    @Test
    void should_return_empty_optional_if_id_is_unknown() {
        PomVersionEvaluatorFactory factory = new PomVersionEvaluatorFactory(noOpLogger());
        String id = "unkown";

        assertThat(factory.create(id)).isEmpty();
    }
}
