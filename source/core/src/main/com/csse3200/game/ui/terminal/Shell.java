package com.csse3200.game.ui.terminal;

import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Scanner;
import java.util.Collection;
import java.lang.reflect.Array;

/**
 * Shell: A simple, single-file, dependency-free scripting language interpreter
 * written in vanilla Java.
 */
public class Shell {
  /** A reference to the Range class for internal use. */
  final public static Class<?> RangeClass = Range.class;
  /** A reference to the ShellMap class */
  final public static Class<?> ShellMapClass = ShellMap.class;
  /** A reference to the ReturnValue class for internal use. */
  final public static Class<?> ReturnValueClass = ReturnValue.class;

  /**
   * An interface for abstracting read/write operations, allowing the Shell to
   * work with different input/output sources, such as a standard console or a
   * network socket.
   */
  static public interface Console {
    /**
     * Prints any generic object to the output.
     *
     * @param obj The object to print.
     * NOTE: It is assumed that calls to the print method are cheap,
     *   Therefore, this should probably be buffered
     */
    void print(Object obj);

    /**
     * Reads a line / block to be Interpreter from the input.
     *
     * @return The line read from the input source.
     */
    String next();

    /**
     * Checks if there is another chunk to be read.
     *
     * @return true if there is a next line, false otherwise.
     */
    boolean hasNext();

    /**
     * Closes the console's underlying resources.
     */
    void close();
  }

  /** The console used for input and output operations. */
  final private Console console;
  /** The execution environment, holding global variables and the call stack. */
  public Environment env;

  /**
   * Constructs a new Shell with a given console and a new default environment.
   *
   * @param console The console interface for I/O.
   */
  public Shell(Console console) {
    this(console, new Environment());
  }

  /**
   * Constructs a new Shell with a specified console and a pre-existing
   * environment.
   *
   * @param console The console interface for I/O.
   * @param env     The execution environment to use.
   */
  public Shell(Console console, Environment env) {
    this.console = console;
    this.env = env;
    this.env.put("globalThis", this);
  }

  /**
   * Runs the shell's read-evaluation loop.
   */
  public void run() {
    while (console.hasNext()) {
      try {
        final Object result = eval(console.next());
        if (result != null) {
          console.print(result);
          console.print("\n");
        }
      } catch (ShellException e) {
        console.print("Error: ");
        console.print(e.getMessage());
        console.print("\n");
      } catch (Exception e) {
        console.print("Runtime Error: ");
        console.print(e.getClass().getSimpleName());
        console.print(" - ");
        console.print(e.getMessage());
        console.print("\n");
      }
    }
  }

  /**
   * Cleanup the resources used by the shell.
   */
  public void close() {
    console.close();
  }

  /**
   * Evaluates a given string of source code.
   *
   * @param source The source code to evaluate.
   * @return The result of the last evaluated statement.
   */
  public Object eval(String source) {
    if (source.trim().isEmpty()) return null;

    Parser parser = new Parser(source);
    ArrayList<Evaluable> statements = new ArrayList<>();

    // Parse all statements
    while (!parser.isAtEnd()) statements.add(parser.parseStatement());

    Object lastResult = null;
    // Execute all statements
    for (Evaluable s : statements) lastResult = s.evaluate(env);

    return lastResult;
  }

  /**
   * The main function.
   * This is for testing purposes only.
   */
  public static void main(String[] args) {
    new Shell(new Console() {
      final Scanner scanner = new Scanner(System.in);
      final PrintStream out = System.out;

      @Override
      public void print(Object obj) {
        out.print(obj);
      }

      @Override
      public String next() {
        out.print("> ");
        StringBuilder inputBuffer = new StringBuilder();

        while (true) {
          final String line = scanner.nextLine();
          inputBuffer.append(line);
          if (!line.trim().endsWith("!") && scanner.hasNextLine()) {
            inputBuffer.append("\n");
            continue;
          }

          String source = inputBuffer.toString().trim();
          source = source.substring(0, source.length() - 1);
          inputBuffer.setLength(0);

          return source;
        }
      }

      @Override
      public boolean hasNext() {
        return scanner.hasNextLine();
      }

      @Override
      public void close() {
        scanner.close();
      }
    }).run();
  }

  /**
   * Provides a string representation of the Shell instance, including its
   * environment.
   *
   * @return A string representation of the shell.
   */
  @Override
  public String toString() {
    return "Shell{.env = " + env + "}";
  }

  /**
   * Performs a logical AND operation.
   *
   * @param l The left-hand side operand.
   * @param r The right-hand side operand.
   * @return The result of the logical AND.
   */
  public static Object and(Object l, Object r) {
    return isTruthy(l) && isTruthy(r);
  }

  /**
   * Performs a logical OR operation.
   *
   * @param l The left-hand side operand.
   * @param r The right-hand side operand.
   * @return The result of the logical OR.
   */
  public static Object or(Object l, Object r) {
    return isTruthy(l) || isTruthy(r);
  }

  /**
   * Performs a logical NOT operation.
   *
   * @param x The operand.
   * @return The result of the logical NOT.
   */
  public static Object not(Object x) {
    return !isTruthy(x);
  }

  /**
   * Determines the "truthiness" of an object, similar to languages like
   * JavaScript or Python. Used to coerce objects to booleans.
   *
   * @param obj The object to evaluate.
   * @return false if the object is null, a zero number, an empty string/collection,
   *         or Boolean false. Returns true otherwise.
   */
  public static boolean isTruthy(Object obj) {
    switch (obj) {
      case null -> {
        return false;
      }
      case Boolean b -> {
        return b;
      }
      case Number number -> {
        return number.doubleValue() != 0.0;
      }
      case String s -> {
        return !s.isEmpty();
      }
      case Character c -> {
        return c != '\0';
      }
      case Collection<?> collection -> {
        return !collection.isEmpty();
      }
      case Map<?, ?> map -> {
        return !map.isEmpty();
      }
      default -> {
      }
    }
    if (obj.getClass().isArray()) return Array.getLength(obj) != 0;
    return true;
  }

  /**
   * Implements an if-then construct. Executes the function if the condition is
   * truthy.
   *
   * @param condition The condition to check.
   * @param function The function to execute if the condition is true.
   * @return The result of the function, or null if the condition was false.
   */
  public Object ifThen(Object condition, EvaluableFunction function) {
    if (isTruthy(condition)) {
      return function.evaluate(env, new ArrayList<>());
    }
    return null;
  }

  /**
   * Implements an if-else construct.
   *
   * @param condition The condition to check.
   * @param ifFunction The function to execute if the condition is true.
   * @param elseFunction The function to execute if the condition is false.
   * @return The result of the executed function.
   */
  public Object ifElse(Object condition, EvaluableFunction ifFunction, EvaluableFunction elseFunction) {
    if (isTruthy(condition)) {
      return ifFunction.evaluate(env, new ArrayList<>());
    }
    return elseFunction.evaluate(env, new ArrayList<>());
  }

  /**
   * Represents a numerical range that can be iterated over.
   * This is intended for shell use only
   */
  public static class Range implements Iterator<Long> {
    private long start;
    private final long end;
    private final long step;

    /**
     * Creates a range with a step of 1.
     *
     * @param start The starting value (inclusive).
     * @param end The ending value (inclusive).
     */
    public Range(long start, long end) {
      this(start, end, 1);
    }

    /**
     * Creates a range with a specified step.
     *
     * @param start The starting value (inclusive).
     * @param end The ending value (inclusive).
     * @param step The increment value.
     */
    public Range(long start, long end, long step) {
      this.start = start;
      this.end = end;
      this.step = step;
    }

    /**
     * Checks if the iteration has more elements.
     *
     * @return true if the current value is less than or equal to the end.
     */
    public boolean hasNext() {
      return start <= end;
    }

    /**
     * Returns the next element in the iteration.
     *
     * @return The next long value in the range.
     */
    public Long next() {
      long currentValue = start;
      start += step;
      return currentValue;
    }
  }

  /**
   * Implements a for-each loop construct that iterates over various iterable types.
   *
   * @param iterable The object to iterate over (can be an Iterator, Collection, Array, or Map).
   * @param function The function to execute for each item.
   * @return null after the loop completes.
   * @throws ShellException if the object is not iterable.
   */
  public Object forEach(Object iterable, EvaluableFunction function) {
    if (iterable instanceof Iterator) {
      while (((Iterator<?>) iterable).hasNext()) {
        final Object result = function.evaluate(env, new ArrayList<>(List.of(((Iterator<?>) iterable).next())));
        if (result instanceof ReturnValue) return ((ReturnValue) result).value;
      }
    } else if (iterable instanceof Collection) {
      for (Object item : (Collection<?>) iterable) {
        final Object result = function.evaluate(env, new ArrayList<>(List.of(item)));
        if (result instanceof ReturnValue) return ((ReturnValue) result).value;
      }
    } else if (iterable.getClass().isArray()) {
      for (int i = 0; i < Array.getLength(iterable); i++) {
        final Object result = function.evaluate(env, new ArrayList<>(List.of(Array.get(iterable, i))));
        if (result instanceof ReturnValue) return ((ReturnValue) result).value;
      }
    } else if (iterable instanceof Map) {
      for (Object key : ((Map<?, ?>) iterable).keySet()) {
        final Object result = function.evaluate(env, new ArrayList<>(List.of(key, ((Map<?, ?>) iterable).get(key))));
        if (result instanceof ReturnValue) return ((ReturnValue) result).value;
      }
    } else {
      throw new ShellException("Cannot iterate over " + iterable.getClass().getSimpleName());
    }

    return null;
  }

  /**
   * Implements a while loop construct.
   *
   * @param condition The condition to evaluate before each iteration.
   * @param function The function to execute in the loop body.
   * @return The result of the last executed statement in the loop.
   */
  public Object whileLoop(Evaluable condition, EvaluableFunction function) {
    while (isTruthy(condition.evaluate(env))) {
      final Object result = function.evaluate(env, new ArrayList<>());
      if (result instanceof ReturnValue) return ((ReturnValue) result).value;
    }
    return null;
  }

  /**
   * Implements a try-catch construct for error handling.
   *
   * @param tryBlock The function containing code that might throw an exception.
   * @param catchBlock The function to execute if a ShellException is caught.
   * @return The result of the try block, or the result of the catch block if an exception occurred.
   */
  public Object tryCatch(EvaluableFunction tryBlock, EvaluableFunction catchBlock) {
    try {
      return tryBlock.evaluate(env, new ArrayList<>());
    } catch (ShellException e) {
      return catchBlock.evaluate(env, new ArrayList<>(List.of(e)));
    }
  }

  /**
   * Sets a value in the global environment scope.
   *
   * @param name The name of the global variable.
   * @param value The value to set.
   * @return The value that was set.
   */
  public Object setGlobal(String name, Object value) {
    env.global.put(name, value);
    return value;
  }

  /**
   * Gets a value from the global environment scope.
   *
   * @param name The name of the global variable.
   * @return The value of the global variable.
   */
  public Object getGlobal(String name) {
    return env.global.get(name);
  }
}

/**
 * A specialized HashMap used for environment frames in the shell.
 * It provides a safe toString() implementation to prevent infinite recursion
 * when printing environments that reference themselves.
 */
class ShellMap extends HashMap<String, Object> {
  /**
   * A static accessor for the underlying map.
   *
   * @param self The ShellMap instance.
   * @return The map itself.
   */
  public static Map<String, Object> getMap(ShellMap self) {
    return self;
  }

  /**
   * Thread unsafe recursion guard.
   */
  private boolean inToStringCall = false;

  /**
   * Provides a string representation of the map, with a guard against recursive calls.
   *
   * @return A string representation of the map, or "..." if a recursive call is detected.
   */
  @Override
  public String toString() {
    if (inToStringCall) return "...";
    inToStringCall = true;
    final String retval = getMap(this).toString();
    inToStringCall = false;
    return retval;
  }
}

/**
 * Represents the state of our shell environment, including global variables and a stack frames
 */
class Environment {
  /** The global scope, accessible from anywhere. */
  public final ShellMap global = new ShellMap();
  /** The stack of function frames */
  public ArrayList<ShellMap> frames = new ArrayList<>();

  /**
   * Thread unsafe recursion guard.
   */
  private boolean inToStringCall = false;

  public Environment() {
  }

  /**
   * Pushes a new frame onto the stack for a new local scope.
   *
   * @return The newly created frame.
   */
  public ShellMap pushFrame() {
    final ShellMap frame = new ShellMap();
    frames.add(frame);
    return frame;
  }

  /**
   * Pops the current frame from the stack when a scope is exited.
   */
  public void popFrame() {
    frames.removeLast();
  }

  /**
   * Retrieves a variable by name, searches from the innermost frame and the global scope.
   *
   * @param name The name of the variable to look up.
   * @return The value of the variable, or null if not found.
   */
  public Object get(String name) {
    if (!frames.isEmpty()) { // Only need to check the last frame
      final Object retval = frames.getLast().get(name);
      if (retval != null) return retval;
    }
    return global.get(name);
  }

  /**
   * Puts a variable in the current scope (function or global scope).
   *
   * @param name  The name of the variable.
   * @param value The value to assign.
   */
  public void put(String name, Object value) {
    if (!frames.isEmpty()) {
      frames.getLast().put(name, value);
    } else {
      global.put(name, value);
    }
  }

  /**
   * Provides a string representation of the Environment, with a guard for recursive calls.
   *
   * @return A string representation of the environment.
   */
  @Override
  public String toString() {
    if (inToStringCall) return "...";
    inToStringCall = true;
    final String retval = "Environment{.global = " + global + ".frames = " + frames + "}";
    inToStringCall = false;
    return retval;
  }
}

/**
 * A custom exception class for user-level errors that occur during script execution.
 */
final class ShellException extends RuntimeException {
  public ShellException(String message) {
    super(message);
  }
}

/**
 * A wrapper class to signify a return value from a function. This is used to
 * unwind the call stack during a return statement.
 */
final class ReturnValue {
  public final Object value;

  public ReturnValue(Object value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "ReturnValue(" + value + ")";
  }
}

/**
 * An interface representing any piece of code that can be evaluated.
 */
interface Evaluable {
  /**
   * Evaluates the code fragment in context of a given environment.
   *
   * @param env The environment to use for evaluation.
   * @return The result of the evaluation.
   */
  Object evaluate(Environment env);
}

/**
 * An interface representing a callable function.
 */
interface EvaluableFunction {
  /**
   * Evaluates the function with a given set of parameters.
   *
   * @param env The environment to use for evaluation.
   * @param parameters The arguments passed to the function.
   * @return The return value of the function.
   */
  Object evaluate(Environment env, ArrayList<Object> parameters);
}

/**
 * Represents a constant literal value in the code, such as a number or a string.
 */
record ConstantStatement(Object value) implements Evaluable {

  /**
   * Returns the held constant value.
   *
   * @param env The environment (unused).
   * @return The constant value.
   */
  @Override
  public Object evaluate(Environment env) {
    return value;
  }

  @Override
  public String toString() {
    return "Constant(" + value + ")";
  }
}

/**
 * A utility class for accessing properties on objects, maps, and classes using
 * Java Reflection. This allows the script to interact with Java objects.
 */
final class Accessor {
  /**
   * Accesses a property on the environment.
   *
   * @param env The environment for the initial variable lookup.
   * @param path The path of properties to access (e.g., ["myObject", "myField"]).
   * @param accessMethods True if method resolution should be attempted for the last element.
   * @return The final value or a MaybeMethodStatement if a method was found.
   * @throws ShellException if access is invalid (e.g., property on null).
   */
  public static Object access(Environment env, List<String> path, boolean accessMethods) {
    assert (!path.isEmpty());
    Object current = env.get(path.getFirst());
    return accessObj(current, path.subList(1, path.size()), accessMethods);
  }

  /**
   * Accesses a property on an object through a given path.
   *
   * @param current The object on which the lookup will be done.
   * @param path The path of properties to access (e.g., ["myObject", "myField"]).
   * @param accessMethods True if method resolution should be attempted for the last element.
   * @return The final value or a MaybeMethodStatement if a method was found.
   * @throws ShellException if access is invalid (e.g., property on null).
   */
  public static Object accessObj(Object current, List<String> path, boolean accessMethods) {
    for (int i = 0; i < path.size(); i++) {
      if (current == null) {
        throw new ShellException("Cannot access property '" + path.get(i) + "' on a null value.");
      }

      String propertyName = path.get(i);

      if (current instanceof ShellMap) {
        current = ((ShellMap) current).get(propertyName);
        continue;
      }

      Class<?> targetClass = (current instanceof Class) ? (Class<?>) current : current.getClass();
      Object instance = (current instanceof Class) ? null : current;

      try {
        Field field = targetClass.getField(propertyName);
        current = field.get(instance);
        continue;
      } catch (NoSuchFieldException e) {
        // Maybe a private field / method!
      } catch (Exception e) {
        throw new ShellException("Cannot access field '" + propertyName + "' on " + targetClass.getSimpleName());
      }

      if (i == path.size() - 1 && accessMethods) {
        for (Method method : targetClass.getMethods()) {
          if (method.getName().equals(propertyName)) {
            return new MaybeMethodStatement(current, propertyName);
          }
        }
      }

      boolean found = false;
      for (Class<?> currentClass = targetClass; currentClass != null; currentClass = currentClass.getSuperclass()) {
        try {
          for (Class<?> c : currentClass.getDeclaredClasses()) {
            if (c.getSimpleName().equals(propertyName)) {
              current = c;
              found = true;
              break;
            }
          }
          if (found) break;
        } catch (Exception e) {
          throw new ShellException("Error accessing class '" + propertyName + "': " + e.getMessage());
        }

        try {
          Field field = currentClass.getDeclaredField(propertyName);
          if (instance == null && !Modifier.isStatic(field.getModifiers())) {
            throw new ShellException("Cannot access instance field '" + propertyName + "' from a static context on class " + targetClass.getSimpleName());
          }
          field.setAccessible(true);
          current = field.get(instance);
          found = true;
          break;
        } catch (NoSuchFieldException e) {
          //  might be a method
        } catch (Exception e) {
          throw new ShellException("Error accessing field '" + propertyName + "': " + e.getMessage());
        }

        if (i == path.size() - 1 && accessMethods) {
          for (Method method : currentClass.getDeclaredMethods()) {
            if (method.getName().equals(propertyName)) {
              return new MaybeMethodStatement(current, propertyName);
            }
          }
        }
      }

      if (!found) {
        throw new ShellException("Cannot access property '" + path.get(i) + "' on " + targetClass.getSimpleName());
      }
    }

    return current;
  }
}

/**
 * Represents a variable access statement, which can be a simple variable name
 * or a chain of property accesses.
 * e.g. `x` or `x.y`
 */
record AccessStatement(String[] path) implements Evaluable {
  AccessStatement {
    if (path.length == 0) throw new IllegalArgumentException("Access path cannot be empty.");
  }

  /**
   * Evaluates the access path to retrieve the final value.
   *
   * @param env The environment in which to evaluate.
   * @return The retrieved value.
   * @throws ShellException if the variable is not found.
   */
  @Override
  public Object evaluate(Environment env) {

    if (path.length == 1) {
      Object current = env.get(path[0]);
      if (current == null) {
        throw new ShellException("Variable '" + path[0] + "' not found.");
      }
      return current;
    }

    return Accessor.access(env, Arrays.asList(path), true);
  }

  @Override
  public String toString() {
    return "Access(" + String.join(".", path) + ")";
  }
}

/**
 * Represents an assignment statement, supports traversing
 * e.g. `x = 0;` or `x.y = 0;`
 */
record AssignmentStatement(AccessStatement left, Evaluable right) implements Evaluable {

  /**
   * Evaluates the right-hand side and assigns the result to the left-hand side.
   *
   * @param env The environment in which to evaluate.
   * @return The value that was assigned.
   * @throws ShellException if the assignment target is invalid.
   */
  @Override
  public Object evaluate(Environment env) {
    Object valueToAssign = right.evaluate(env);
    String[] path = left.path();

    if (path.length == 1) {
      env.put(path[0], valueToAssign);
      return valueToAssign;
    }

    Object toSet = Accessor.access(env, Arrays.asList(path).subList(0, path.length - 1), false);

    switch (toSet) {
      case null ->
          throw new ShellException("Cannot access property '" + path[path.length - 1] + "' on a null container.");
      case ShellMap shellMap -> {
        shellMap.put(path[path.length - 1], valueToAssign);
        return valueToAssign;
      }
      case Environment environment -> {
        environment.put(path[path.length - 1], valueToAssign);
        return valueToAssign;
      }
      default -> {
      }
    }

    Class<?> targetClass = (toSet instanceof Class) ? (Class<?>) toSet : toSet.getClass();
    for (Class<?> c : targetClass.getDeclaredClasses()) {
      try {
        Field field = c.getDeclaredField(path[path.length - 1]);
        field.setAccessible(true);
        field.set(toSet, valueToAssign);
        return valueToAssign;
      } catch (NoSuchFieldException ignored) {
      } catch (Exception e) {
        throw new ShellException("Error setting field '" + path[path.length - 1] + "': " + e.getMessage());
      }
    }

    throw new ShellException("Cannot set field '" + path[path.length - 1] + "' on " + targetClass.getSimpleName());
  }

  @Override
  public String toString() {
    return "Assignment(" + left + " = " + right + ")";
  }
}

/**
 * Represents what could be a callable method / function statement.
 * Uses reflection to find the best matching overload.
 */
final class MaybeMethodStatement implements EvaluableFunction {
  Object instance;
  String methodName;

  MaybeMethodStatement(Object object, String methodName) {
    this.instance = object;
    this.methodName = methodName;
  }

  /**
   * Evaluates the method call by finding a suitable method overload for the
   * given parameters and invoking it.
   *
   * @param env The environment (unused).
   * @param parameters The arguments for the method call.
   * @return The result of the method invocation.
   * @throws ShellException if no suitable method is found or if invocation fails.
   */
  @Override
  public Object evaluate(Environment env, ArrayList<Object> parameters) {
    Object targetInstance = (instance instanceof Class) ? null : instance;
    Class<?> targetClass = (instance instanceof Class) ? (Class<?>) instance : instance.getClass();
    Object[] args = parameters.toArray();

    ArrayList<Method> candidates = new ArrayList<>();

    for (Method method : targetClass.getMethods()) {
      if (method.getName().equals(methodName) && method.getParameterCount() == args.length) {
        candidates.add(method);
      }
    }

    for (Class<?> c : targetClass.getDeclaredClasses()) {
      for (Method method : c.getDeclaredMethods()) {
        if (method.getName().equals(methodName) && method.getParameterCount() == args.length) {
          candidates.add(method);
        }
      }
    }

    for (Method method : candidates) {
      assert (method.getName().equals(methodName));
      if (method.getParameterCount() != args.length) continue;

      boolean isStatic = Modifier.isStatic(method.getModifiers());
      if (!isStatic && targetInstance == null) continue;

      Class<?>[] paramTypes = method.getParameterTypes();
      boolean typesMatch = true;
      for (int i = 0; i < args.length; i++) {
        if (args[i] == null) {
          if (paramTypes[i].isPrimitive()) {
            typesMatch = false;
            break;
          }
        } else if (!isAssignable(paramTypes[i], args[i].getClass())) {
          typesMatch = false;
          break;
        }
      }

      if (typesMatch) {
        try {
          return method.invoke(targetInstance, args);
        } catch (Exception e) {
          throw new ShellException("Error invoking method '" + methodName + "': " + e.getCause().getMessage());
        }
      }
    }
    throw new ShellException("No matching method '" + methodName + "' found for the given arguments in " + targetClass.getSimpleName());
  }

  /** Map of primitive types to their corresponding wrapper classes. */
  static final Map<Class<?>, Class<?>> WRAPPER_TYPES = Map.of(
      boolean.class, Boolean.class, byte.class, Byte.class, char.class, Character.class,
      double.class, Double.class, float.class, Float.class, int.class, Integer.class,
      long.class, Long.class, short.class, Short.class);

  /**
   * Checks if a value from a source type can be assigned to a target type,
   * handling primitive-to-wrapper conversions.
   *
   * @param targetType The type of the parameter.
   * @param sourceType The type of the argument.
   * @return true if assignment is possible.
   */
  private boolean isAssignable(Class<?> targetType, Class<?> sourceType) {
    if (targetType.isAssignableFrom(sourceType)) return true;
    if (targetType.isPrimitive()) {
      return WRAPPER_TYPES.get(targetType).equals(sourceType);
    }
    return false;
  }
}

/**
 * Represents a user-defined function in the shell script.
 // e.g. `(x, y) { return(.java.lang.Math.pow(x, y)); }`
 */
final class FunctionStatement implements EvaluableFunction {
  final Evaluable[] instructions;

  final String[] parameter_names;
  final int variadicIndex;

  FunctionStatement(Evaluable[] instructions, String[] parameter_names, int variadicIndex) {
    this.instructions = instructions;
    this.parameter_names = parameter_names;
    this.variadicIndex = variadicIndex;
  }

  /**
   * Executes the function call statement
   *
   * @param env The parent environment.
   * @param parameters The arguments passed to the function.
   * @return The function's return value, or null if no return statement is executed.
   * @throws ShellException if the wrong number of arguments is provided.
   */
  @Override
  public Object evaluate(Environment env, ArrayList<Object> parameters) {
    final ShellMap frame = env.pushFrame();

    if (variadicIndex == -1) {
      if (parameter_names.length != parameters.size()) {
        throw new ShellException("Expected " + parameter_names.length + " arguments, got " + parameters.size());
      }
    }

    for (int i = 0; i < (variadicIndex == -1 ? parameter_names.length : parameter_names.length - 1); i++) {
      frame.put(parameter_names[i], parameters.get(i));
    }

    if (variadicIndex != -1) {
      frame.put(parameter_names[parameter_names.length - 1], parameters.subList(parameter_names.length - 1, parameters.size()));
    }

    for (Evaluable instruction : instructions) {
      final Object result = instruction.evaluate(env);
      if (result instanceof ReturnValue) {
        env.popFrame();
        return ((ReturnValue) result).value;
      }
    }

    env.popFrame();
    return null;
  }

  @Override
  public String toString() {
    return "Function(" + Arrays.toString(instructions) + ")";
  }
}

/**
 * Represents a resolved class that can be "called" like a function for instantiation.
 */
record ClassResultStatement(Class<?> c) implements EvaluableFunction {

  /**
   * Acts as a constructor call. It finds a matching public constructor based
   * on the arguments and creates a new instance.
   *
   * @param env        The environment (unused).
   * @param parameters The arguments for the constructor.
   * @return The newly created class instance coerced to an Object.
   * @throws ShellException if no matching constructor is found or instantiation fails.
   */
  @Override
  public Object evaluate(Environment env, ArrayList<Object> parameters) {
    Object[] args = parameters.toArray();

    // Find a matching constructor and attempt to instantiate using it.
    for (Constructor<?> constructor : c.getConstructors()) {
      if (constructor.getParameterCount() == args.length) {
        try {
          // This will throw IllegalArgumentException if the types are incorrect,
          return constructor.newInstance(args);
        } catch (IllegalArgumentException e) {
          continue; // Argument types don't match, so we continue
        } catch (Exception e) {
          throw new ShellException("Error instantiating class '" + c.getSimpleName() + "': " + e.getMessage());
        }
      }
    }

    throw new ShellException("No matching public constructor found for class '" + c.getSimpleName() + "' with " + args.length + " arguments.");
  }
}

/**
 * Represents a statement that resolves a Java class by its fully qualified name
 * (e.g., `.java.lang.String`).
 */
final class ClassResolutionStatement implements Evaluable {
  Class<?> resolvedClass;
  public List<String> subPath;

  ClassResolutionStatement(String originalName) {
    String name = originalName;
    this.subPath = new ArrayList<>();

    while (true) {
      final int lastDot = name.lastIndexOf(".");
      try {
        this.resolvedClass = Class.forName(name);
        this.subPath = this.subPath.reversed();
        return;
      } catch (ClassNotFoundException e) {
        // ... maybe field / method access on class
      }
      if (lastDot == -1) break;
      subPath.add(name.substring(lastDot + 1));
      name = name.substring(0, lastDot);
    }

    throw new ShellException("Class not found: " + originalName);
  }

  /**
   * Resolves the class name into a Class object using Reflection.
   *
   * @param env The environment (unused).
   * @return A ClassResultStatement wrapping the resolved Class object if no subPath, or the resolved object.
   * @throws ShellException if the class cannot be found.
   */
  @Override
  public Object evaluate(Environment env) {
    if (subPath.isEmpty()) {
      return resolvedClass;
    } else {
      return Accessor.accessObj(resolvedClass, subPath, true);
    }
  }

  @Override
  public String toString() {
    final String retval = "ClassResolution(" + resolvedClass.getSimpleName() + ")";
    if (subPath.isEmpty()) return retval;
    return retval + "." + String.join(".", subPath);
  }
}

/**
 * Represents a function call statement
 * e.g. `x(0, 1, 2);`
 * NOTE: calling a class creates a new instance of that class
 */
record FunctionCallStatement(Evaluable caller, ArrayList<Evaluable> arguments) implements Evaluable {

  /**
   * Evaluates the caller and the arguments, then invokes the caller with the resolved arguments.
   *
   * @param env The environment in which to evaluate.
   * @return The result of the function call.
   * @throws ShellException if the caller is not a function.
   */
  @Override
  public Object evaluate(Environment env) {
    Object trueCaller = caller.evaluate(env);
    if (trueCaller instanceof Class<?>) {
      trueCaller = new ClassResultStatement((Class<?>) trueCaller);
    } else if (!(trueCaller instanceof EvaluableFunction)) {
      throw new ShellException("Cannot call non-function value '" + trueCaller + "'.");
    }

    final EvaluableFunction function = (EvaluableFunction) trueCaller;

    ArrayList<Object> parameters = new ArrayList<>();
    for (Evaluable arg : arguments) parameters.add(arg.evaluate(env));
    return function.evaluate(env, parameters);
  }

  @Override
  public String toString() {
    return "FunctionCall(" + caller + ", " + arguments + ")";
  }
}

/**
 * Parser implementation, this is what converts texts to 'Evaluable / EvaluableFunction' objects
 */
class Parser {
  private final String source;
  private int pos = 0;

  Parser(String source) {
    this.source = source;
  }

  /**
   * Checks if the parser has reached the end of the source string.
   *
   * @return true if at the end, false otherwise.
   */
  public boolean isAtEnd() {
    skipWhitespace();
    return pos >= source.length();
  }

  /**
   * Looks at the current character without consuming it.
   *
   * @return The character at the current position, or '\0' if at the end.
   */
  private char peek() {
    if (pos >= source.length()) return '\0';
    return source.charAt(pos);
  }

  /**
   * Consumes and returns the current character, advancing the position.
   *
   * @return The consumed character.
   */
  private char advance() {
    return source.charAt(pos++);
  }

  /**
   * Checks if the current character matches the expected character.
   * If it does, consumes it and returns true.
   *
   * @param expected The character to match.
   * @return true on match, false otherwise.
   */
  private boolean match(char expected) {
    if (isAtEnd() || peek() != expected) return false;
    pos++;
    return true;
  }

  /**
   * Requires the next character to be a specific character, throwing an error if it's not.
   *
   * @param expected The expected character.
   * @param message The error message to throw on failure.
   */
  private void consume(char expected, String message) {
    skipWhitespace();
    if (!match(expected)) {
      throw new ShellException(message);
    }
  }

  /**
   * Skips over any whitespace characters.
   */
  private void skipWhitespace() {
    while (pos < source.length() && Character.isWhitespace(source.charAt(pos))) {
      pos++;
    }
  }

  /**
   * Parses a single statement, which is an expression followed by a semicolon.
   *
   * @return The parsed Evaluable statement.
   */
  public Evaluable parseStatement() {
    Evaluable expr = parseExpression();
    consume(';', "Expected ';' after statement.");
    return expr;
  }

  /**
   * Parses an expression, which can be a primary value, an assignment, or a function call.
   *
   * @return The parsed Evaluable expression.
   */
  private Evaluable parseExpression() {
    Evaluable expr = parsePrimary();

    skipWhitespace();
    if (match('=')) { // Assignment
      if (!(expr instanceof AccessStatement)) {
        throw new ShellException("Invalid assignment target.");
      }
      Evaluable value = parseExpression();
      expr = new AssignmentStatement((AccessStatement) expr, value);
    } else if (match('(')) { // Function Call
      ArrayList<Evaluable> args = new ArrayList<>();
      if (peek() != ')') {
        do {
          args.add(parseExpression());
        } while (match(','));
      }
      consume(')', "Expected ')' after arguments.");
      expr = new FunctionCallStatement(expr, args);
    }

    return expr;
  }

  /**
   * Parses a primary expression, such as a number, string, variable access,
   * or function definition `(params) ...`.
   *
   * @return The parsed Evaluable primary expression.
   */
  private Evaluable parsePrimary() {
    skipWhitespace();
    char c = peek();

    if (Character.isDigit(c) || c == '-') return parseNumber();
    if (c == '"' || c == '\'') return parseStringOrChar();
    if (c == '.') return parseClassResolution();
    if (c == '(') return parseFunctionDef();
    if (Character.isLetter(c) || c == '_') return parseAccess();

    throw new ShellException("Unexpected character: " + c);
  }

  /**
   * Parses a number literal (integer, float, double, long).
   *
   * @return A ConstantStatement containing the parsed number.
   */
  private ConstantStatement parseNumber() {
    int start = pos;
    match('-');

    while (!isAtEnd() && Character.isDigit(peek())) advance();
    if (peek() == '.') {
      do advance();
      while (!isAtEnd() && Character.isDigit(peek()));
    }

    char suffix = Character.toLowerCase(peek());
    String numberStr = source.substring(start, pos);

    if (suffix == 'l') {
      advance();
      return new ConstantStatement(Long.parseLong(numberStr));
    }
    if (suffix == 'd') {
      advance();
      return new ConstantStatement(Double.parseDouble(numberStr));
    }

    if (numberStr.contains(".")) return new ConstantStatement(Float.parseFloat(numberStr));
    return new ConstantStatement(Integer.parseInt(numberStr));
  }

  /**
   * Parses a string (double-quoted) or character (single-quoted) literal.
   *
   * @return A ConstantStatement containing the parsed string or char.
   */
  private ConstantStatement parseStringOrChar() {
    char quote = advance(); // Consume opening quote
    int start = pos;
    while (!isAtEnd() && peek() != quote) {
      advance();
    }
    String value = source.substring(start, pos);
    consume(quote, "Unterminated string or char literal.");

    if (quote == '\'') {
      if (value.length() != 1) throw new ShellException("Character literal must be a single character.");
      return new ConstantStatement(value.charAt(0));
    }
    return new ConstantStatement(value);
  }

  /**
   * Parses a class resolution expression (e.g., `.java.lang.String`).
   *
   * @return A ClassResolutionStatement.
   */
  private Evaluable parseClassResolution() {
    consume('.', "Expected '.' for class resolution.");
    return new ClassResolutionStatement(readIdentifierWithDots());
  }

  /**
   * Parses a function definition `(params) { ... }`.
   *
   * @return A ConstantStatement containing the new FunctionStatement.
   */
  private Evaluable parseFunctionDef() {
    consume('(', "Expected '(' for group or function definition.");
    skipWhitespace();

    ArrayList<String> params = new ArrayList<>();
    int variadicIndex = -1;

    if (peek() != ')') {
      do {
        skipWhitespace();

        final int at = pos;
        if (variadicIndex == -1 && match('.') && match('.') && match('.')) variadicIndex = params.size();
        if (at != pos && variadicIndex == -1)
          throw new ShellException("Unexpected token" + peek() + " encountered when expecting `...`");

        params.add(readIdentifier());
      } while (match(','));
    }
    consume(')', "Expected ')' after function parameters.");
    skipWhitespace();
    if (peek() == '{') {
      return new ConstantStatement(parseFunctionBody(params.toArray(new String[0]), variadicIndex));
    } else {
      throw new ShellException("Unexpected token " + peek() + ". Function body `{ ... }` expected.");
    }
  }

  /**
   * Parses the body of a function, which is a sequence of statements inside curly braces.
   *
   * @param params The names of the function's parameters.
   * @param variadicIndex The index of the variadic parameter, or -1 if none.
   * @return A FunctionStatement representing the parsed function.
   */
  private FunctionStatement parseFunctionBody(String[] params, int variadicIndex) {
    consume('{', "Expected '{' to start function body.");
    ArrayList<Evaluable> body = new ArrayList<>();
    while (!isAtEnd() && peek() != '}') body.add(parseStatement());
    consume('}', "Expected '}' to end function body.");
    return new FunctionStatement(body.toArray(new Evaluable[0]), params, variadicIndex);
  }

  /**
   * Parses a variable access or property access chain (e.g., `x` or `x.y.z`).
   *
   * @return An AccessStatement.
   */
  private Evaluable parseAccess() {
    ArrayList<String> path = new ArrayList<>();
    do path.add(readIdentifier());
    while (match('.'));
    return new AccessStatement(path.toArray(new String[0]));
  }

  /**
   * Reads an identifier (variable name)
   *
   * @return The identifier string.
   */
  private String readIdentifier() {
    int start = pos;
    while (!isAtEnd() && (Character.isLetterOrDigit(peek()) || peek() == '_')) advance();
    return source.substring(start, pos).trim();
  }

  /**
   * Reads an identifier that is allowed to contain dots (for class names).
   *
   * @return The identifier string.
   */
  private String readIdentifierWithDots() {
    int start = pos;
    while (!isAtEnd() && (Character.isLetterOrDigit(peek()) || peek() == '_' || peek() == '.')) advance();
    return source.substring(start, pos);
  }
}

