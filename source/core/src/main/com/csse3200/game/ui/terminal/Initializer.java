package com.csse3200.game.ui.terminal;

import java.lang.reflect.Field;

/**
 * This Class is meant to initialize the shell by running some predetermined scripts.
 * If you want to add you own snippets to be initialized, declare a `private static final String`
 * and assign it the code you want to run on shell startup.
 */
public class Initializer {
  static public Shell getInitializedShell() {
    Shell shell = new Shell(new Shell.Console() {
      @Override public void print(Object obj) { TerminalService.print(obj); }
      @Override public String next() { return null; }
      @Override public boolean hasNext() { return false; }
      @Override public void close() {}
    });
    for (Field field : Initializer.class.getDeclaredFields()) {
      field.setAccessible(true);
      try {
        Object fieldValue = field.get(null);
        if (fieldValue instanceof String) {
          shell.eval((String) fieldValue);
        }
      } catch (IllegalAccessException e) {
        // Ignore
      }
    }
    return shell;
  }

  /**
   * Setup basic shell functionality
   */
  private static final String init = """
    init = () {
      "This makes the function slightly faster as we only need to look at the top frame";
      globalThis = globalThis;
      setGlobal = globalThis.setGlobal;

      "
        exports setGlobal function
        setGlobal function is used to set global variables from inside of function scopes
      ";
      setGlobal("setGlobal", setGlobal);

      "getGlobal can be used to get global variables that have been locally overridden";
      setGlobal("getGlobal", globalThis.getGlobal);

      "print stuff to the console";
      setGlobal("print", globalThis.console.print);

      "--- Types ---";

      "Shell is the class of this shell object in java";
      setGlobal("Shell", globalThis.getClass());

      "Range constructor can be used in the forEach loop";
      setGlobal("Range", Shell.RangeClass);

      "
        `return` is a special class which causes a value to be returned from a function
        there is a limitation that return must be its own statement
        i.e `() { return(0); } ()` will return 0 but
        `() { discard = (v) {}; discard(return(0)); } ()` will return nothing
      ";
      setGlobal("return", Shell.ReturnValueClass);

      "--- Conditionals ---";

      "`if` is a function that executes the second function argument if the first argument evaluates to true";
      setGlobal("if", globalThis.ifThen);

      "
        `ifElse` executes the second function argument if the first argument evaluates to true
        `ifElse` executes the third function argument if the first argument evaluates to false
      ";
      setGlobal("ifElse", globalThis.ifElse);

      "
        `forEach` executes the second function for each iterated value in the first argument
      ";
      setGlobal("forEach", globalThis.forEach);

      "
        `while` executes the second function argument until the first argument evaluates to false
      ";
      setGlobal("while", globalThis.whileLoop);

      "
        `tryCatch` executes the first function argument and calls the second if an exception is thrown
      ";
      setGlobal("tryCatch", globalThis.tryCatch);

      "--- Airthematic ---";
      setGlobal("and", globalThis.and);
      setGlobal("or", globalThis.or);
      setGlobal("not", globalThis.not);
    };

    init();
  """;
}
