package net.markz.webscraper.api.exceptions;

/**
 * A functional interfaces that handles exceptions for the callback function.
 * @param <T>
 */
@FunctionalInterface
public interface Lazy<T> {
  T lazyDo();
}
