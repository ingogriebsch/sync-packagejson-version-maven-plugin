package de.ingogriebsch.maven.sync.packagejson.version.plugin;

import static java.util.Collections.unmodifiableSet;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import javax.inject.Singleton;

import com.google.common.collect.Maps;

/**
 * A factory that creates a {@link PomVersionEvaluator} based on a given identifier.
 * 
 * @since 1.1.0
 */
@Singleton
public class PomVersionEvaluatorFactory {

    private static final Map<String, PomVersionEvaluator> evaluators = providers();

    public Optional<PomVersionEvaluator> create(String id) {
        return Optional.ofNullable(evaluators.get(id));
    }

    public Set<String> getIds() {
        return unmodifiableSet(evaluators.keySet());
    }

    private static Map<String, PomVersionEvaluator> providers() {
        Map<String, PomVersionEvaluator> providers = Maps.newHashMap();
        providers.put("runtime", new RuntimePomVersionEvaluator());
        providers.put("static", new StaticPomVersionEvaluator());
        return providers;
    }
}
