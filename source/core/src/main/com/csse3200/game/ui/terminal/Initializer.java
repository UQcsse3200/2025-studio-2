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

      "Simple true / false constants";
      setGlobal("false", globalThis.isTruthy(0));
      setGlobal("true", globalThis.isTruthy(1));

      "
        exports setGlobal function
        setGlobal function is used to set global variables from inside of function scopes
      ";
      setGlobal("setGlobal", setGlobal);

      "getGlobal can be used to get global variables that have been locally overridden";
      setGlobal("getGlobal", globalThis.getGlobal);

      "print stuff to the console";
      setGlobal("print", (...stuff) {  globalThis.forEach(stuff, globalThis.console.print); });

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
      setGlobal("eql", .java.util.Objects.deepEquals);

      "--- Other ---";

      "Returns true if the give object is actually a class type";
      setGlobal("isClass", globalThis.isClass);
    };

    init();
  """;

  /**
   * Setup debug functionality.
   * Note: None of this has been tested yet, there WILL be bugs.
   */
  private static final String debug = """
  debugInit = () {
    "This makes the function slightly faster as we only need to look at the top frame";
    setGlobal = setGlobal;

    "--- Services ---";
    setGlobal("TerminalService", .com.csse3200.game.ui.terminal.TerminalService);
    setGlobal("ServiceLocator", .com.csse3200.game.services.ServiceLocator);
    setGlobal("entityService", () { return(ServiceLocator.getEntityService()); });
    setGlobal("renderService", () { return(ServiceLocator.getRenderService()); });
    setGlobal("physicsService", () { return(ServiceLocator.getPhysicsService()); });
    setGlobal("inputService", () { return(ServiceLocator.getInputService()); });
    setGlobal("resourceService", () { return(ServiceLocator.getResourceService()); });
    setGlobal("timeSource", () { return(ServiceLocator.getTimeSource()); });

    "--- Game Control ---";
    "Set the game's time scale. e.g. timescale(0.5); for half speed.";
    setGlobal("setTimescale", (scale) { TerminalService.customTimeScale = scale; });

    "Resume the game in the background";
    setGlobal("resume", () {
      source = timeSource();
      source.setTimeScale(.com.csse3200.game.ui.terminal.TerminalService.customTimeScale);
    });

    "Pause the game if resume was called before";
    setGlobal("pause", () {
      source = timeSource();
      source.setTimeScale(.com.csse3200.game.ui.terminal.TerminalService.customTimeScale);
    });

    "Toggle physics debug rendering. e.g. debug(true);";
    setGlobal("debug", (active) {
      debug = renderService.getDebug();
      debug.setActive(active);
    });

    "--- Entity Manipulation ---";
    "Get a list of all registered entities";
    setGlobal("getEntities", () {
      es = entityService();
      return(es.entities);
    });

    "Get a single entity by its ID. e.g. getEntity(1);";
    setGlobal("getEntityById", (id) {
      setGlobal(".id", id);
      forEach(getEntities(), (entity) {
        setGlobal(".entity", entity);
        if (eql(entity.getId(), getGlobal(".id")), () { return(getGlobal(".entity")); });
      });
    });

    "--- Utilities ---";
    "Inspect an object's fields and methods. e.g. inspect(services);";
    setGlobal("inspect", (obj) {
      setGlobal(".obj", obj);
      if(not(isClass(obj)), () { obj = getGlobal(".obj"); setGlobal(".obj", obj.getClass()); });
      cls = getGlobal(".obj");
      setGlobal(".obj", obj);

      print("Inspecting Class: ", cls.getName(), "\n\n--- Fields ---\n");

      forEach(cls.getFields(), (field) {
        setGlobal(".field", field);
        tryCatch(() {
          field = getGlobal(".field");
          field.setAccessible(true);
          print(field.toGenericString(), " = ", field.get(getGlobal(".obj")), "\n");
        }, (e) {
          print("Opps, An error occurred", e, "\n");
        });
      });

      print("\n--- Methods ---\n", cls);
      forEach(cls.getMethods(), (method) {
        print(method.toGenericString(), "\n");
      });
    });

    "List all local / global variables.";
    setGlobal("env", () { return(globalThis.env); });

    "Prints this help message";
    setGlobal("help", "
      --- Help: In-Game Debug Shell ---
      --- Basic Commands ---
      print(...args) - Prints one or more values to the console.
      if(cond, func) - Executes func if cond is true.
      ifElse(cond, t, f) - Executes func t if cond is true, else func f.
      while(cond, func) - Executes func while cond is true.
      forEach(iter, func) - Executes func for each item in an iterable.
      tryCatch(try, catch) - Executes try func, calls catch func on error.
      and(a, b), or(a, b) - Logical AND and OR.
      not(a) - Logical NOT.
      eql(a, b) - Checks if two objects are deeply equal.
      --- Game Control ---
      setTimescale(scale) - Sets the game's speed. 1.0 is normal, 0.0 is paused.
      Example: setTimescale(0.5);
      resume() - Re-applies the custom timescale. Useful after manual pause.
      pause() - (Identical to resume) Re-applies the custom timescale.
      debug(boolean) - Toggles the physics debug view. e.g. debug(true);
      --- Entity Manipulation ---
      getEntities() - Returns an array of all active entities.
      getEntityById(id) - Finds an entity by its ID. e.g. getEntityById(5);
      --- Services (as functions) ---
      entityService() - Returns the EntityService instance.
      renderService() - Returns the RenderService instance.
      physicsService() - Returns the PhysicsService instance.
      inputService() - Returns the InputService instance.
      resourceService() - Returns the ResourceService instance.
      timeSource() - Returns the GameTime instance.
      --- Debugging Utilities ---
      inspect(object) - Shows all fields and methods of a Java object or class.
      Example: inspect(getEntityById(5));
      env() - Shows the current shell environment with all variables.
    ");
  };

  debugInit();
  """;
}
