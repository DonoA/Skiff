package io.dallen.errors;

import java.util.List;

public interface ErrorCollector<T> {

    void throwError(String msg, T on);

    List<String> getErrors();

}
