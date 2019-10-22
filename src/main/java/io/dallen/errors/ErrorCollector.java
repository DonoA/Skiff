package io.dallen.errors;

import java.util.List;

/**
 * An error collecting barrier. Errors thrown during operations should be propagated upward until a top level error
 * collector is located. The top level error collector can then be refrenced for a list of errors thrown within an
 * operation
 * @param <T> The type of object the error is thrown on. Generally includes some information on where in the input
 *           code the error was thrown for printing
 */
public interface ErrorCollector<T> {

    void throwError(String msg, T on);

    List<String> getErrors();

}
