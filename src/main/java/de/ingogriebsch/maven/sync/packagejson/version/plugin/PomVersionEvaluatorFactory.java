package de.ingogriebsch.maven.sync.packagejson.version.plugin;

import static java.util.Collections.unmodifiableSet;

import static com.google.common.collect.Maps.newHashMap;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * A factory that creates a {@link PomVersionEvaluator} based on a given identifier.
 * 
 * @since 1.1.0
 */
public class PomVersionEvaluatorFactory {

    private final Map<String, PomVersionEvaluator> evaluators;
    private final Logger logger;

    PomVersionEvaluatorFactory(Logger logger) {
        this.logger = logger;
        this.evaluators = initEvaluators();
    }

    public Optional<PomVersionEvaluator> create(String id) {
        PomVersionEvaluator evaluator = evaluators.get(id);
        logger.debug("Created an evaluator instance based on id '%s' [type: '%s].", id,
            evaluator != null ? evaluator.getClass().getName() : null);
        return Optional.ofNullable(evaluator);
    }

    public Set<String> getIds() {
        Set<String> ids = unmodifiableSet(evaluators.keySet());
        logger.debug("Returning ids %s to identify the available evaluator instances.", ids);
        return ids;
    }

    private Map<String, PomVersionEvaluator> initEvaluators() {
        Map<String, PomVersionEvaluator> evaluators = newHashMap();
        evaluators.put("runtime", new RuntimePomVersionEvaluator(logger));
        evaluators.put("static", new StaticPomVersionEvaluator(logger));
        return evaluators;
    }
}
