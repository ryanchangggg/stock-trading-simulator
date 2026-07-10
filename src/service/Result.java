package service;

import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

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

    /**
     * Creates a success result representing a void operation (no value).
     */
    public static Result<Void> success() {
        return new Result<>(true, null, null);
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

    /**
     * Transforms the value if this is a success result.
     *
     * @param mapper the mapping function to apply to the value
     * @param <U>    the new result type
     * @return a new success result with the mapped value, or the same failure
     */
    public <U> Result<U> map(Function<? super T, ? extends U> mapper) {
        if (success) {
            return Result.success(mapper.apply(value));
        }
        return Result.failure(error);
    }

    /**
     * Returns the value if this is a success result, otherwise the default.
     */
    public T orElse(T defaultValue) {
        return success ? value : defaultValue;
    }
}
