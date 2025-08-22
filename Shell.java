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

// Shell: A simple, single-file, dependency-free scripting language interpreter written in vanilla Java.
public class Shell {
  final public static Class<?> RangeClass = Range.class;
  final public static Class<?> ShellMapClass = ShellMap.class;
  final public static Class<?> ReturnValueClass = ReturnValue.class;

  // A read / write iinterface implementation
  static public interface Console {
    // Prints any generic object
    // NOTE: It is assumed that calls to the print method are cheap,
    //   Therefore, this should probably be buffered
    void print(Object obj);

    // Reads a line from the console
    String nextLine();

    // Checks if there is a next line to be read
    boolean hasNextLine();

    // Closes the console, this is just forwarded by the shell implementation
    void close();
  }

  final private Console console;
  public Environment env;

  public Shell(Console console) {
    this(console, new Environment());
  }

  public Shell(Console console, Environment env) {
    this.console = console;
    this.env = env;
    this.env.put("globalThis", this);
  }

  public void run() {
    StringBuilder inputBuffer = new StringBuilder();
    while (true) {
      if (inputBuffer.length() == 0) console.print("> ");
      if (!console.hasNextLine()) return;

      final String line = console.nextLine();
      inputBuffer.append(line);

      if (!line.trim().endsWith("!")) {
        inputBuffer.append("\n");
        continue;
      }

      String source = inputBuffer.toString().trim();
      source = source.substring(0, source.length() - 1);
      inputBuffer.setLength(0);
      if (source.isEmpty()) continue;

      try {
        final Object result = eval(source);
        if (result != Void.VOID) {
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

  public void close() {
    console.close();
  }

  Object eval(String source) {
    if (source.trim().isEmpty()) return Void.VOID;

    Parser parser = new Parser(source);
    ArrayList<Evaluable> statements = new ArrayList<>();

    // Parse all statements
    while (!parser.isAtEnd()) statements.add(parser.parseStatement());

    Object lastResult = Void.VOID;
    // Execute all statements
    for (Evaluable s : statements) lastResult = s.evaluate(env);

    return lastResult;
  }

  public static void main(String[] args) {
    new Shell(new Console() {
      final Scanner scanner = new Scanner(System.in);
      final PrintStream out = System.out;

      @Override
      public void print(Object obj) {
        out.print(obj);
      }

      @Override
      public String nextLine() {
        return scanner.nextLine();
      }

      @Override
      public boolean hasNextLine() {
        return scanner.hasNextLine();
      }

      @Override
      public void close() {
        scanner.close();
      }
    }).run();
  }

  @Override
  public String toString() {
    return "Shell{.env = " + env + "}";
  }

  public static Object and(Object l, Object r) {
    return isTruthy(l) && isTruthy(r);
  }

  public static Object or(Object l, Object r) {
    return isTruthy(r) || isTruthy(r);
  }

  public static Object not(Object x) {
    return !isTruthy(x);
  }

  public static boolean isTruthy(Object obj) {
    if (obj == null) return false;
    if (obj instanceof Boolean) return (Boolean) obj;
    if (obj instanceof Number) return ((Number) obj).doubleValue() != 0.0;
    if (obj instanceof String) return !((String) obj).isEmpty();
    if (obj instanceof Character) return (Character) obj != '\0';
    if (obj instanceof Collection) return !((Collection<?>) obj).isEmpty();
    if (obj instanceof Map) return !((Map<?, ?>) obj).isEmpty();
    if (obj.getClass().isArray()) return Array.getLength(obj) != 0;
    return true;
  }

  public Object ifThen(Object condition, EvaluableFunction function) {
    if (isTruthy(condition)) {
      return function.evaluate(env, new ArrayList<>());
    }
    return Void.VOID;
  }

  public Object ifElse(Object condition, EvaluableFunction ifFunction, EvaluableFunction elseFunction) {
    if (isTruthy(condition)) {
      return ifFunction.evaluate(env, new ArrayList<>());
    }
    return elseFunction.evaluate(env, new ArrayList<>());
  }

  public class Range implements Iterator<Long> {
    private long start;
    private final long end;
    private final long step;

    public Range(long start, long end) {
      this(start, end, 1);
    }

    public Range(long start, long end, long step) {
      this.start = start;
      this.end = end;
      this.step = step;
    }

    public boolean hasNext() {
      return start <= end;
    }

    public Long next() {
      return start += step;
    }
  }

  public Object forEach(Object iterable, EvaluableFunction function) {
    if (iterable instanceof Iterator) {
      while (((Iterator<?>) iterable).hasNext()) {
        function.evaluate(env, new ArrayList<>(List.of(((Iterator<?>) iterable).next())));
      }
      return Void.VOID;
    } else if (iterable instanceof Collection) {
      for (Object item : (Collection<?>) iterable) {
        function.evaluate(env, new ArrayList<>(List.of(item)));
      }
    } else if (iterable.getClass().isArray()) {
      for (int i = 0; i < Array.getLength(iterable); i++) {
        function.evaluate(env, new ArrayList<>(List.of(Array.get(iterable, i))));
      }
    } else if (iterable instanceof Map) {
      for (Object key : ((Map<?, ?>) iterable).keySet()) {
        function.evaluate(env, new ArrayList<>(List.of(key, ((Map<?, ?>) iterable).get(key))));
      }
    }

    throw new ShellException("Cannot iterate over " + iterable.getClass().getSimpleName());
  }

  public Object whileLoop(Evaluable condition, EvaluableFunction function) {
    Object result = Void.VOID;
    while (isTruthy(condition.evaluate(env))) {
      result = function.evaluate(env, new ArrayList<>());
    }
    return result;
  }

  public Object tryCatch(EvaluableFunction tryBlock, EvaluableFunction catchBlock) {
    try {
      return tryBlock.evaluate(env, new ArrayList<>());
    } catch (ShellException e) {
      return catchBlock.evaluate(env, new ArrayList<>(List.of(e)));
    }
  }

  public Object setGlobal(String name, Object value) {
    env.global.put(name, value);
    return value;
  }

  public Object getGlobal(String name) {
    return env.global.get(name);
  }
}

// A simple typedef
class ShellMap extends HashMap<String, Object> {
  public static Map<String, Object> getMap(ShellMap self) {
    return self;
  }

  private boolean inToStringCall = false;

  @Override
  public String toString() {
    if (inToStringCall) return "...";
    inToStringCall = true;
    final String retval = getMap(this).toString();
    inToStringCall = false;
    return retval;
  }
}

// This is an object that represents the state of our shell envoirnment
class Environment {
  final public ShellMap global = new ShellMap();
  public ArrayList<ShellMap> frames = new ArrayList<ShellMap>();
  private boolean inToStringCall = false;

  public Environment() {
  }

  public ShellMap pushFrame() {
    final ShellMap frame = new ShellMap();
    frames.add(frame);
    return frame;
  }

  public ShellMap popFrame() {
    return frames.removeLast();
  }

  public Object get(String name) {
    if (!frames.isEmpty()) { // Only need to check the last frame
      final Object retval = frames.get(frames.size() - 1).get(name);
      if (retval != null) return retval;
    }
    return global.get(name);
  }

  public void put(String name, Object value) {
    if (!frames.isEmpty()) {
      frames.get(frames.size() - 1).put(name, value);
    } else {
      global.put(name, value);
    }
  }

  @Override
  public String toString() {
    if (inToStringCall) return "...";
    inToStringCall = true;
    final String retval = "Environment{.global = " + global + ".frames = " + frames + "}";
    inToStringCall = false;
    return retval;
  }
}

// Exception Class for Shell
final class ShellException extends RuntimeException {
  public ShellException(String message) {
    super(message);
  }
}

// Special void class to represent absence of value
final class Void {
  private Void() {
  }

  public static final Object VOID = new Void();

  @Override
  public String toString() {
    return "VOID";
  }
}

// Represents a return value from a function
final class ReturnValue {
  public final Object value;

  ReturnValue(Object value) {
    this.value = value;
  }

  @Override
  public String toString() {
    return "ReturnValue(" + value + ")";
  }
}

// This represents an evaluable fragment
interface Evaluable {
  public Object evaluate(Environment env);
}

// This represents an callable fragment
interface EvaluableFunction {
  public Object evaluate(Environment env, ArrayList<Object> parameters);
}

// Just a thin wrapper that returns the stored value
// used for constant definitions
final class ConstantStatement implements Evaluable {
  final Object value;

  ConstantStatement(Object value) {
    this.value = value;
  }

  @Override
  public Object evaluate(Environment env) {
    return value;
  }

  @Override
  public String toString() {
    return "Constant(" + value + ")";
  }
}

// A utility class for accessing properties on objects / maps / etc
final class Accessor {
  public static Object access(Environment env, List<String> path, boolean accessMethods) {
    assert (path.size() >= 1);

    Object current = env.get(path.get(0));

    for (int i = 1; i < path.size(); i++) {
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
            return new MaybeMethodStatement(instance, propertyName);
          }
        }
      }

      for (Class<?> currentClass = targetClass; currentClass != null; currentClass = currentClass.getSuperclass()) {
        try {
          boolean found = false;
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
          break;
        } catch (NoSuchFieldException e) {
          //  might be a method
        } catch (Exception e) {
          throw new ShellException("Error accessing field '" + propertyName + "': " + e.getMessage());
        }

        if (i == path.size() - 1 && accessMethods) {
          for (Method method : currentClass.getDeclaredMethods()) {
            if (method.getName().equals(propertyName)) {
              return new MaybeMethodStatement(instance, propertyName);
            }
          }
        }
      }
    }

    return current;
  }
}

// Accesses a variable, supports traversing
// e.g. `x` or `x.y`
final class AccessStatement implements Evaluable {
  final String[] path;

  AccessStatement(String[] path) {
    if (path.length == 0) throw new IllegalArgumentException("Access path cannot be empty.");
    this.path = path;
  }

  @Override
  public Object evaluate(Environment env) {

    if (path.length == 1) {
      Object current = env.get(path[0]);
      if (current == null) {
        throw new ShellException("Variable '" + path[0] + "' not found.");
      }
      return current;
    }

    Object current = Accessor.access(env, Arrays.asList(path), true);
    return current;
  }

  @Override
  public String toString() {
    return "Access(" + String.join(".", path) + ")";
  }
}

// Represents an assignment statement, supports traversing
// e.g. `x = 0;` or `x.y = 0;`
final class AssignmentStatement implements Evaluable {
  final AccessStatement left;
  final Evaluable right;

  AssignmentStatement(AccessStatement left, Evaluable right) {
    this.left = left;
    this.right = right;
  }

  @Override
  public Object evaluate(Environment env) {
    Object valueToAssign = right.evaluate(env);
    String[] path = left.path;

    if (path.length == 1) {
      env.put(path[0], valueToAssign);
      return valueToAssign;
    }

    Object toSet = Accessor.access(env, Arrays.asList(path).subList(0, path.length - 1), false);

    if (toSet == null) {
      throw new ShellException("Cannot access property '" + path[path.length - 1] + "' on a null container.");
    } else if (toSet instanceof ShellMap) {
      ((ShellMap) toSet).put(path[path.length - 1], valueToAssign);
      return valueToAssign;
    } else if (toSet instanceof Environment) {
      ((Environment) toSet).put(path[path.length - 1], valueToAssign);
      return valueToAssign;
    }

    Class<?> targetClass = (toSet instanceof Class) ? (Class<?>) toSet : toSet.getClass();
    for (Class<?> c : targetClass.getDeclaredClasses()) {
      try {
        Field field = c.getDeclaredField(path[path.length - 1]);
        field.setAccessible(true);
        field.set(toSet, valueToAssign);
        return valueToAssign;
      } catch (NoSuchFieldException e) {
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

// represents what could be a method statement.
// This is used to determine which onverride to use for method call.
final class MaybeMethodStatement implements EvaluableFunction {
  Object instance;
  String methodName;

  MaybeMethodStatement(Object object, String methodName) {
    this.instance = object;
    this.methodName = methodName;
  }

  @Override
  public Object evaluate(Environment env, ArrayList<Object> parameters) {
    Object targetInstance = (instance instanceof Class) ? null : instance;
    Class<?> targetClass = (instance instanceof Class) ? (Class<?>) instance : instance.getClass();
    Object[] args = parameters.toArray();

    ArrayList<Method> candidates = new ArrayList<>();

    targetClass.getMethods();
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

  static final Map<Class<?>, Class<?>> WRAPPER_TYPES = Map.of(
      boolean.class, Boolean.class, byte.class, Byte.class, char.class, Character.class,
      double.class, Double.class, float.class, Float.class, int.class, Integer.class,
      long.class, Long.class, short.class, Short.class);

  private boolean isAssignable(Class<?> targetType, Class<?> sourceType) {
    if (targetType.isAssignableFrom(sourceType)) return true;
    if (targetType.isPrimitive()) {
      return WRAPPER_TYPES.get(targetType).equals(sourceType);
    }
    return false;
  }
}

// Represents a function definition
// e.g. `(x, y, z) { return(x + y + z); }`
final class FunctionStatement implements EvaluableFunction {
  final Evaluable[] instructions;

  final String[] parameter_names;
  final int variadicIndex;

  FunctionStatement(Evaluable[] instructions, String[] parameter_names, int variadicIndex) {
    this.instructions = instructions;
    this.parameter_names = parameter_names;
    this.variadicIndex = variadicIndex;
  }

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
    return Void.VOID;
  }

  @Override
  public String toString() {
    return "Function(" + instructions + ")";
  }
}

// Represents a resolved class that can be called like a function for instantiation
final class ClassResultStatement implements EvaluableFunction {
  final Class<?> c;

  ClassResultStatement(Class<?> c) {
    this.c = c;
  }

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

// Represents a class resolution statement
// e.g. `.java.lang.String`
final class ClassResolutionStatement implements Evaluable {
  final String name;

  ClassResolutionStatement(String name) {
    this.name = name;
  }

  @Override
  public Object evaluate(Environment env) {
    try {
      return new ClassResultStatement(Class.forName(name));
    } catch (ClassNotFoundException e) {
      throw new ShellException("Class not found: " + name);
    }
  }

  @Override
  public String toString() {
    return "ClassResolution(" + name + ")";
  }
}

// Represents a function call statement
// e.g. `x(0, 1, 2);`
// NOTE: calling a class creates a new instance of that class
final class FunctionCallStatement implements Evaluable {
  final Evaluable caller;
  final ArrayList<Evaluable> arguments;

  FunctionCallStatement(Evaluable caller, ArrayList<Evaluable> arguments) {
    this.caller = caller;
    this.arguments = arguments;
  }

  @Override
  public Object evaluate(Environment env) {
    final Object trueCaller = caller.evaluate(env);
    if (!(trueCaller instanceof EvaluableFunction)) {
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

// Parser implementation, this is what converts texts to 'Evaluable / EvaluableFunction' objects
class Parser {
  private final String source;
  private int pos = 0;

  Parser(String source) {
    this.source = source;
  }

  public boolean isAtEnd() {
    skipWhitespace();
    return pos >= source.length();
  }

  private char peek() {
    if (pos >= source.length()) return '\0';
    return source.charAt(pos);
  }

  private char advance() {
    return source.charAt(pos++);
  }

  private boolean match(char expected) {
    if (isAtEnd() || peek() != expected) return false;
    pos++;
    return true;
  }

  private void consume(char expected, String message) {
    skipWhitespace();
    if (!match(expected)) {
      throw new ShellException(message);
    }
  }

  private void skipWhitespace() {
    while (pos < source.length() && Character.isWhitespace(source.charAt(pos))) {
      pos++;
    }
  }

  public Evaluable parseStatement() {
    Evaluable expr = parseExpression();
    consume(';', "Expected ';' after statement.");
    return expr;
  }

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

  private Evaluable parsePrimary() {
    skipWhitespace();
    char c = peek();

    if (Character.isDigit(c)) return parseNumber();
    if (c == '"' || c == '\'') return parseStringOrChar();
    if (c == '.') return parseClassResolution();
    if (c == '(') return parseFunctionDef();
    if (Character.isLetter(c) || c == '_') return parseAccess();

    throw new ShellException("Unexpected character: " + c);
  }

  private ConstantStatement parseNumber() {
    int start = pos;
    while (!isAtEnd() && Character.isDigit(peek())) advance();
    if (peek() == '.') {
      advance();
      while (!isAtEnd() && Character.isDigit(peek())) advance();
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

  private Evaluable parseClassResolution() {
    consume('.', "Expected '.' for class resolution.");
    return new ClassResolutionStatement(readIdentifierWithDots());
  }

  // Check for function definition: `(params) { ... }`
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

  private FunctionStatement parseFunctionBody(String[] params, int variadicIndex) {
    consume('{', "Expected '{' to start function body.");
    ArrayList<Evaluable> body = new ArrayList<>();
    while (!isAtEnd() && peek() != '}') body.add(parseStatement());
    consume('}', "Expected '}' to end function body.");
    return new FunctionStatement(body.toArray(new Evaluable[0]), params, variadicIndex);
  }

  private Evaluable parseAccess() {
    ArrayList<String> path = new ArrayList<>();
    path.add(readIdentifier());
    while (match('.')) path.add(readIdentifier());
    return new AccessStatement(path.toArray(new String[0]));
  }

  private String readIdentifier() {
    int start = pos;
    while (!isAtEnd() && (Character.isLetterOrDigit(peek()) || peek() == '_')) advance();
    return source.substring(start, pos).trim();
  }

  private String readIdentifierWithDots() {
    int start = pos;
    while (!isAtEnd() && (Character.isLetterOrDigit(peek()) || peek() == '_' || peek() == '.')) advance();
    return source.substring(start, pos);
  }
}

