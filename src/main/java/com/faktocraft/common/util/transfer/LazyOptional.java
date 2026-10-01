package com.faktocraft.common.util.transfer;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;

public class LazyOptional<T> {
  @FunctionalInterface
  public interface NonNullSupplier<T> {
    @NotNull
    T get();
  }

  private static final LazyOptional<Void> EMPTY = new LazyOptional<>(null);

  @Nullable
  private NonNullSupplier<T> supplier;
  private T resolved;
  private boolean isValid = true;
  private final List<Consumer<LazyOptional<T>>> listeners = new ArrayList<>();

  private LazyOptional(@Nullable NonNullSupplier<T> supplier) {
    this.supplier = supplier;
  }

  public static <T> LazyOptional<T> of(@Nullable NonNullSupplier<T> supplier) {
    return supplier == null ? empty() : new LazyOptional<>(supplier);
  }

  @SuppressWarnings("unchecked")
  public static <T> LazyOptional<T> empty() {
    return (LazyOptional<T>) EMPTY;
  }

  @SuppressWarnings("unchecked")
  public <X> LazyOptional<X> cast() {
    return (LazyOptional<X>) this;
  }

  @Nullable
  private T getValue() {
    if (!isValid || supplier == null) {
      return null;
    }
    if (resolved == null) {
      resolved = supplier.get();
    }
    return resolved;
  }

  public boolean isPresent() {
    return supplier != null && isValid;
  }

  public void ifPresent(Consumer<? super T> consumer) {
    T value = getValue();
    if (value != null) {
      consumer.accept(value);
    }
  }

  public <U> LazyOptional<U> map(Function<? super T, ? extends U> mapper) {
    return isPresent() ? of(() -> mapper.apply(getValue())) : empty();
  }

  public LazyOptional<T> filter(Predicate<? super T> predicate) {
    T value = getValue();
    return value != null && predicate.test(value) ? this : empty();
  }

  public Optional<T> resolve() {
    return Optional.ofNullable(getValue());
  }

  public T orElse(T other) {
    T value = getValue();
    return value != null ? value : other;
  }

  public T orElseGet(Supplier<? extends T> other) {
    T value = getValue();
    return value != null ? value : other.get();
  }

  public <X extends Throwable> T orElseThrow(Supplier<? extends X> exceptionSupplier) throws X {
    T value = getValue();
    if (value != null) {
      return value;
    }
    throw exceptionSupplier.get();
  }

  public void addListener(Consumer<LazyOptional<T>> listener) {
    if (isPresent()) {
      listeners.add(listener);
    } else {
      listener.accept(this);
    }
  }

  public void invalidate() {
    if (isValid) {
      isValid = false;
      supplier = null;
      resolved = null;
      listeners.forEach(l -> l.accept(this));
      listeners.clear();
    }
  }
}
