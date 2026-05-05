package com.keVend.backend.i18n;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.context.i18n.LocaleContextHolder;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import java.util.Locale;

/**
 * Thin facade over {@link MessageSource} that resolves keys against the
 * locale on the current request thread (set by Spring from the
 * {@code Accept-Language} header).
 *
 * <p>Use {@link #t(String, Object...)} for plain strings and
 * {@link #status(HttpStatusCode, String, Object...)} when throwing
 * {@link ResponseStatusException}.
 */
@Component
@RequiredArgsConstructor
public class I18n {

    private final MessageSource messageSource;

    public String t(String key, Object... args) {
        Locale locale = LocaleContextHolder.getLocale();
        return messageSource.getMessage(key, args, key, locale);
    }

    /**
     * Resolve a key in an explicit locale — used by background jobs (push/SMS
     * notifications) where there is no request-scoped {@code Accept-Language}
     * to read from. Falls back to English when {@code localeTag} is null/blank.
     */
    public String tFor(String localeTag, String key, Object... args) {
        Locale locale = (localeTag == null || localeTag.isBlank())
                ? Locale.ENGLISH
                : Locale.forLanguageTag(localeTag);
        return messageSource.getMessage(key, args, key, locale);
    }

    public ResponseStatusException status(HttpStatusCode code, String key, Object... args) {
        return new ResponseStatusException(code, t(key, args));
    }

    public ResponseStatusException badRequest(String key, Object... args) {
        return status(HttpStatus.BAD_REQUEST, key, args);
    }

    public ResponseStatusException notFound(String key, Object... args) {
        return status(HttpStatus.NOT_FOUND, key, args);
    }

    public ResponseStatusException conflict(String key, Object... args) {
        return status(HttpStatus.CONFLICT, key, args);
    }

    public ResponseStatusException forbidden(String key, Object... args) {
        return status(HttpStatus.FORBIDDEN, key, args);
    }
}
