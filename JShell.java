import java.io.PrintStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Scanner;

// JShell: A simple, single-file, dependency-free scripting language interpreter written in vanilla Java.
public class JShell {
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

  public JShell(Console console) {
    this(console, new Environment());
  }

  public JShell(Console console, Environment env) {
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
      } catch (JShellException e) {
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
    // TODO: Implement
    return null;
  }

  public static void main(String[] args) {
    new JShell(new Console() {
      final Scanner scanner = new Scanner(System.in);
      final PrintStream out = System.out;

      @Override public void print(Object obj) { out.print(obj); }
      @Override public String nextLine() { return scanner.nextLine(); }
      @Override public boolean hasNextLine() { return scanner.hasNextLine(); }
      @Override public void close() { scanner.close(); }
    }).run();
  }

  @Override public String toString() { return "JShell{\n.env = " + env + "}"; }
}

// A simple typedef
class JShellMap extends HashMap<String, Object> {}

// This is an object that represents the state of our shell envoirnment
class Environment {
  final public JShellMap global = new JShellMap ();
  public ArrayList<JShellMap> frames = new ArrayList<JShellMap>();
  
  public Environment() {}

  public JShellMap pushFrame() {
    final JShellMap frame = new JShellMap();
    frames.add(frame);
    return frame;
  }
  
  public JShellMap popFrame() {
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

// Exception Class for JShell
final class JShellException extends RuntimeException {
  public JShellException(String message) {
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

// Represents a class resolution statement
// e.g. `.java.lang.String`
final class ClassResolutionStatement implements Evaluable {
  final String name;
  
  ClassResolutionStatement(String name) {
    this.name = name;
  }
  
  @Override
  public Object evaluate(Environment env) {
    // TODO: Implement
    return null;
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
    // TODO: Implement
    return null;
  }

  @Override public String toString() { return "FunctionCall(" + caller + ", " + arguments + ")"; }
}

