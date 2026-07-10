package service;

import java.util.Objects;
import java.util.Optional;

/**
 * Generic result wrapper for service-layer operations.
 * <p>
 * Encapsulates either a successful value or a failure message,
 * avoiding exceptions for expected error cases (validation failures,
 * insufficient funds, etc.).
 */
public final class Result<T> {

    private final boolean success;
    private final T value;
    private final String error;

    private Result(boolean success, T value, String error) {
        this.success = success;
        this.value = value;
        this.error = error;
    }

    public static <T> Result<T> success(T value) {
        return new Result<>(true, Objects.requireNonNull(value), null);
    }

    public static <T> Result<T> failure(String error) {
        return new Result<>(false, null,
            Objects.requireNonNull(error, "Error message must not be null"));
    }

    public boolean isSuccess() { return success; }
    public boolean isFailure() { return !success; }
    public T getValue() { return value; }
    public Optional<String> getError() { return Optional.ofNullable(error); }
    public String getErrorMessage() { return error != null ? error : ""; }
}
