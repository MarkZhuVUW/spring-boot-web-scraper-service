package net.markz.webscraper.api.exceptions;

@FunctionalInterface
public interface ExceptionHandler<T> {
  T handle();
}
