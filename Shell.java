import java.io.PrintStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

// Shell: A simple, single-file, dependency-free scripting language interpreter written in vanilla Java.
public class Shell {
  // A read / write iinterface implementation
  static public interface Console {
    // Prints any generic object
    // NOTE: It is assumed that calls to the print method are cheap,
    //     Therefore, this should probably be buffered
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
  }

  public void run() {
    StringBuilder inputBuffer = new StringBuilder();
    while (true) {
      console.print(inputBuffer.length() == 0 ? "> " : "... ");
      if (!console.hasNextLine()) return; // Exit if no more input

      final String line = console.nextLine();
      inputBuffer.append(line);

      if (!line.trim().endsWith(";") && !line.trim().isEmpty()) {
        inputBuffer.append(" ");
        continue;
      }

      final String source = inputBuffer.toString().trim();
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

      @Override public void print(Object obj) { out.print(obj); }
      @Override public String nextLine() { return scanner.nextLine(); }
      @Override public boolean hasNextLine() { return scanner.hasNextLine(); }
      @Override public void close() { scanner.close(); }
    }).run();
  }

  @Override public String toString() { return "Shell{\n.env = " + env + "}"; }
}

// A simple typedef
class ShellMap extends HashMap<String, Object> {}

// This is an object that represents the state of our shell envoirnment
class Environment {
  final public ShellMap global = new ShellMap ();
  public ArrayList<ShellMap> frames = new ArrayList<ShellMap>();
  
  public Environment() {}

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

  @Override public String toString() {
    return "Environment{\n.global = " + global + "\n.frames = " + frames + "}";
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
  private Void() {}
  public static final Object VOID = new Void();

  public String toString() { return "VOID"; }
}

// Represents a return value from a function
final class ReturnValue {
  public final Object value;

  ReturnValue(Object value) {
    this.value = value;
  }

  @Override public String toString() { return "ReturnValue(" + value + ")"; }
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

  @Override public String toString() { return "Constant(" + value + ")"; }
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
  @SuppressWarnings("unchecked")
  public Object evaluate(Environment env) {
    Object current = env.get(path[0]);

    if (path.length == 1 && current == null) {
      throw new ShellException("Variable '" + path[0] + "' not found.");
    }

    for (int i = 1; i < path.length; i++) {
      if (current == null) {
        throw new ShellException("Cannot access property '" + path[i] + "' on a null value.");
      }

      String propertyName = path[i];

      if (current instanceof Map) {
        current = ((Map<String, Object>) current).get(propertyName);
        continue;
      }

      Class<?> targetClass = (current instanceof Class) ? (Class<?>) current : current.getClass();
      Object instance = (current instanceof Class) ? null : current;

      // Try to access as a field (instance or static)
      try {
        Field field = targetClass.getField(propertyName);
        if (instance == null && !Modifier.isStatic(field.getModifiers())) {
          throw new ShellException("Cannot access instance field '" + propertyName + "' from a static context on class " + targetClass.getSimpleName());
        }
        current = field.get(instance);
        continue;
      } catch (NoSuchFieldException e) {
        //  might be a method
      } catch (Exception e) {
        throw new ShellException("Error accessing field '" + propertyName + "': " + e.getMessage());
      }

      // Try a method (instance or static)
      if (i != path.length - 1) throw new ShellException("Cannot access '" + propertyName + "' on " + current.toString());

      return new MaybeMethodStatement(instance, propertyName);

      // try {
      //   Method method = targetClass.getMethod(propertyName); // Finds public method with no parameters
      //
      //   if (Modifier.isStatic(method.getModifiers())) {
      //     return new StaticMethodStatement(method);
      //   } else {
      //     return new InstanceMethodStatement(method, instance);
      //   }
      //
      // } catch (NoSuchMethodException e) {
      //   // Not a field or a method.
      // } catch (ShellException e) {
      //   throw e;
      // } catch (Exception e) {
      //   throw new ShellException("Error accessing method '" + propertyName + "': " + e.getMessage());
      // }

      // If neither a field nor method was found
    }

    return current;
  }

  @Override public String toString() { return "Access(" + String.join(".", path) + ")"; }
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
  @SuppressWarnings("unchecked")
  public Object evaluate(Environment env) {
    Object valueToAssign = right.evaluate(env);
    String[] path = left.path;

    if (path.length == 1) {
      env.put(path[0], valueToAssign);
      return valueToAssign;
    }

    Object container = env.get(path[0]);
    if (container == null) {
      throw new ShellException("Variable '" + path[0] + "' not found.");
    }

    // Traverse the path up to the second-to-last element to find the container.
    for (int i = 1; i < path.length - 1; i++) {
      if (container == null) {
        throw new ShellException("Cannot access property '" + path[i] + "' on a null value.");
      }

      String propertyName = path[i];

      if (container instanceof Map) {
        container = ((Map<String, Object>) container).get(propertyName);
      } else {
        try {
          Field field = container.getClass().getField(propertyName);
          container = field.get(container);
        } catch (NoSuchFieldException e) {
          throw new ShellException("Public field '" + propertyName + "' not found in object of type " + container.getClass().getSimpleName());
        } catch (Exception e) {
          throw new ShellException("Error accessing field '" + propertyName + "' during assignment: " + e.getMessage());
        }
      }
    }

    if (container == null) {
      throw new ShellException("Cannot assign to property '" + path[path.length - 1] + "' on a null container.");
    }

    String propertyToSet = path[path.length - 1];

    if (container instanceof Map) {
      ((Map<String, Object>) container).put(propertyToSet, valueToAssign);
    } else {
      try {
        Field field = container.getClass().getField(propertyToSet);
        field.set(container, valueToAssign);
      } catch (NoSuchFieldException e) {
        throw new ShellException("Public field '" + propertyToSet + "' not found in object of type " + container.getClass().getSimpleName());
      } catch (IllegalAccessException e) {
        throw new ShellException("Cannot access or modify field '" + propertyToSet + "'.");
      } catch (Exception e) {
        throw new ShellException("Error assigning to field '" + propertyToSet + "': " + e.getMessage());
      }
    }
    
    return valueToAssign;
  }

  @Override public String toString() { return "Assignment(" + left + " = " + right + ")"; }
}

final class MaybeMethodStatement implements EvaluableFunction {
  Object instance;
  String methodName;

  MaybeMethodStatement(Object object, String methodName) {
    this.instance = object;
    this.methodName = methodName;
  }

  @Override
  public Object evaluate(Environment env, ArrayList<Object> parameters) {
    // try {
    //   return method.invoke(null, parameters.toArray());
    // } catch (Exception e) {
    //   throw new ShellException("Error invoking method " + method.getName() + ": " + e.getMessage());
    // }

    Object targetInstance = (instance instanceof Class) ? null : instance;
    Class<?> targetClass = (instance instanceof Class) ? (Class<?>) instance : instance.getClass();
    Object[] args = parameters.toArray();

    for (Method method: targetClass.getMethods()) {
      if (!method.getName().equals(methodName) || method.getParameterCount() != args.length) continue;

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

  @Override public String toString() { return "Function(" + instructions + ")"; }
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

  @Override public String toString() { return "ClassResolution(" + name + ")"; }
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

  @Override public String toString() { return "FunctionCall(" + caller + ", " + arguments + ")"; }
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

    if (suffix == 'l') { advance(); return new ConstantStatement(Long.parseLong(numberStr)); }
    if (suffix == 'd') { advance(); return new ConstantStatement(Double.parseDouble(numberStr)); }

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
        if (at != pos && variadicIndex == -1) throw new ShellException("Unexpected token" + peek() + " encountered when expecting `...`");

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
    List<Evaluable> body = new ArrayList<>();
    while (!isAtEnd() && peek() != '}') body.add(parseStatement());
    consume('}', "Expected '}' to end function body.");
    return new FunctionStatement(body.toArray(new Evaluable[0]), params, variadicIndex);
  }

  private Evaluable parseAccess() {
    ArrayList<String> path = new ArrayList<>();
    path.add(readIdentifier());
    while(match('.')) path.add(readIdentifier());
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

