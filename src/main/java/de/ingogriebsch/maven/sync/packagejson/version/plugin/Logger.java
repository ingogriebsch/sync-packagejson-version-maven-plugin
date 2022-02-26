package de.ingogriebsch.maven.sync.packagejson.version.plugin;

import static java.lang.String.format;

import java.util.function.Supplier;

import org.apache.maven.plugin.logging.Log;

/**
 * A Logger SPI that provides some convenience methods to ease the logging of messages.
 *
 * @since 1.0.0
 */
public class Logger {

    private final Supplier<Log> source;

    private Logger(Supplier<Log> source) {
        this.source = source;
    }

    public static Logger logger(Supplier<Log> source) {
        return new Logger(source);
    }

    public static Logger noOpLogger() {
        return logger(() -> new NoOpLog());
    }

    /**
     * Replaces the placeholders in the message based on the given arguments and logs the message on debug level (if enabled).
     *
     * @param message the message that will be logged if the log level is enabled
     * @param args the arguments that will be used as replacements for the placeholders in the message before logging the
     *        message
     * @since 1.0.0
     */
    public void debug(String message, Object... args) {
        Log log = source.get();
        if (log.isDebugEnabled()) {
            log.debug(format(message, args));
        }
    }

    /**
     * Replaces the placeholders in the message based on the given arguments and logs the message on info level (if enabled).
     *
     * @param message the message that will be logged if the log level is enabled
     * @param args the arguments that will be used as replacements for the placeholders in the message before logging the
     *        message
     * @since 1.0.0
     */
    public void info(String message, Object... args) {
        Log log = source.get();
        if (log.isInfoEnabled()) {
            log.info(format(message, args));
        }
    }

    /**
     * Replaces the placeholders in the message based on the given arguments and logs the message on warn level (if enabled).
     *
     * @param message the message that will be logged if the log level is enabled
     * @param args the arguments that will be used as replacements for the placeholders in the message before logging the
     *        message
     * @since 1.0.0
     */
    public void warn(String message, Object... args) {
        Log log = source.get();
        if (log.isWarnEnabled()) {
            log.warn(format(message, args));
        }
    }

    /**
     * Replaces the placeholders in the message based on the given arguments and logs the message on error level (if enabled).
     *
     * @param message the message that will be logged if the log level is enabled
     * @param args the arguments that will be used as replacements for the placeholders in the message before logging the
     *        message
     * @since 1.0.0
     */
    public void error(String message, Object... args) {
        Log log = source.get();
        if (log.isErrorEnabled()) {
            log.error(format(message, args));
        }
    }

    private static class NoOpLog implements Log {

        @Override
        public boolean isDebugEnabled() {
            return false;
        }

        @Override
        public void debug(CharSequence content) {
        }

        @Override
        public void debug(CharSequence content, Throwable error) {
        }

        @Override
        public void debug(Throwable error) {
        }

        @Override
        public boolean isInfoEnabled() {
            return false;
        }

        @Override
        public void info(CharSequence content) {
        }

        @Override
        public void info(CharSequence content, Throwable error) {
        }

        @Override
        public void info(Throwable error) {
        }

        @Override
        public boolean isWarnEnabled() {
            return false;
        }

        @Override
        public void warn(CharSequence content) {
        }

        @Override
        public void warn(CharSequence content, Throwable error) {
        }

        @Override
        public void warn(Throwable error) {
        }

        @Override
        public boolean isErrorEnabled() {
            return false;
        }

        @Override
        public void error(CharSequence content) {
        }

        @Override
        public void error(CharSequence content, Throwable error) {
        }

        @Override
        public void error(Throwable error) {
        }
    }
}
