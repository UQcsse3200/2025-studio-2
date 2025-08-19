import java.util.ArrayList;
import java.util.HashMap;

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
}

