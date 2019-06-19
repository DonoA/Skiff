package io.dallen.compiler;

public class NoSuchObjectException extends RuntimeException {

  public NoSuchObjectException(String varName) {
    super("Variable '" + varName + "' not bound");
  }

}
