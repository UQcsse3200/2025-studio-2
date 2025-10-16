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
      setGlobal("print", (...stuff) {
        if(eql((){}(), stuff), () { return(return()); });
        globalThis.forEach(stuff, globalThis.console.print);
      });

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

      "--- Conditional logic ---";
      setGlobal("and", globalThis.and);
      setGlobal("or", globalThis.or);
      setGlobal("not", globalThis.not);
      setGlobal("eql", .java.util.Objects.deepEquals);
      setGlobal("isNull", .java.util.Objects.isNull);

      "--- Other ---";

      "Returns true if the give object is actually a class type";
      setGlobal("isClass", globalThis.isClass);

      "Return from n nested scopes, returnN(1, value) is same as return(value)";
      setGlobal("returnN", (n, value) {
        setGlobal(".value", value);
        forEach(Range(1, n), (_) { (_){}(setGlobal(".value", return(getGlobal(".value")))); });
        return(getGlobal(".value"));
      });

      "Returns true if the given variable exists in the current scope";
      setGlobal("exists", globalThis.exists);

      "Get a value from the parent scope, parent scope must me a function scope, not global";
      setGlobal("getParentVar", (key) {
        globalThis = globalThis;
        frames = globalThis.env.frames;
        map = globalThis.ShellMapClass.getMap(frames.get(.java.lang.Integer.sum(frames.size(), -3)));
        return(map.get(key));
      });
    };

    init();
  """;

  /**
   * Setup debug functionality.
   * Note: There may be bugs due to external functionality changing.
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

            setGlobal(".oldTimeScale", 1.0);

            "Resume the game in the background";
            setGlobal("resume", () {
              ts = .com.csse3200.game.ui.terminal.TerminalService;
              ts.customTimeScale = getGlobal(".oldTimeScale");
            });

            "Pause the game if resume was called before";
            setGlobal("pause", () {
              ts = .com.csse3200.game.ui.terminal.TerminalService;
              setGlobal(".oldTimeScale", ts.customTimeScale);
              ts.customTimeScale = 0.0;
            });

            "Toggle physics debug rendering. e.g. debug(true);";
            setGlobal("debug", (active) {
              debug = ServiceLocator.renderService.getDebug();
              debug.setActive(globalThis.isTruthy(active));
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
                if (eql(entity.getId(), getGlobal(".id")), () { returnN(3, getGlobal(".entity")); });
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

            "Find the player, returns null if nothing is found";
            setGlobal("getPlayer", () {
              forEach(getEntities(), (entity) {
                setGlobal(".entity", entity);
                if (not(isNull(entity.getComponent(.com.csse3200.game.components.player.InventoryComponent))), () {
                  returnN(3, getGlobal(".entity"));
                });
              });
            });
                           
                           

            "Prints this help message";
            setGlobal("help", "
              --- Help: In-Game Debug Shell ---
              --- Core Functions and Constants ---
              globalThis            - A reference to the current Shell instance.
              true / false          - Boolean constants.
              Shell                 - The Java Class object for the Shell interpreter.
              Range                 - The Java Class object for creating numerical ranges.
              print(...stuff)       - Prints one or more arguments to the terminal.
              setGlobal(name, value)- Sets a variable in the global scope.
              getGlobal(name)       - Retrieves a variable from the global scope.
              exists(varName)       - Returns true if a variable with the given name exists.
              getParentVar(key)     - Retrieves a variable from the parent function's scope.

              --- Conditional Logic & Control Flow ---
              return(value)         - Returns a value from a function.
              returnN(n, value)     - Returns a value from n nested function scopes.
              if(cond, func)        - Executes func if cond is truthy.
              ifElse(c, t, f)       - Executes t if c is truthy, else f.
              while(cond, func)     - Executes func while cond is truthy.
              forEach(iter, func)   - Executes func for each item in an iterable.
              tryCatch(try, catch)  - Executes try func, calls catch func on ShellException.
              eql(a, b)             - Performs a deep equality check on two objects.
              and(a, b) / or(a, b)  - Logical AND and OR operations.
              not(a)                - Logical NOT operation.
              isNull(a)             - Returns true if the object a is null.

              --- Game Control & State ---
              debug(active)         - Toggles the physics debug view (truthy/falsy).
              pause()               - Pauses the game by setting timescale to 0.
              resume()              - Resumes the game by restoring the custom timescale.
              setTimescale(scale)   - Sets the game's time scale (1.0 is normal, 0.0 is paused).
                                      This is kept even after the terminal is closed

              --- Entity Manipulation ---
              getPlayer()           - Finds and returns the player entity if it exists.
              getEntities()         - Returns an Array of all the game entities.
              getEntityById(id)     - Finds and returns an entity given it's integer ID.

              --- Service Accessors ---
              TerminalService       - Java Class for the TerminalService.
              ServiceLocator        - Java Class for the ServiceLocator.
              entityService()       - Returns the EntityService instance.
              renderService()       - Returns the RenderService instance.
              physicsService()      - Returns the PhysicsService instance.
              inputService()        - Returns the InputService instance.
              resourceService()     - Returns the ResourceService instance.
              timeSource()          - Returns the GameTime instance.

              --- Debugging Utilities ---
              inspect(obj)          - Prints all public fields/methods of an object/class.
              env()                 - Returns the Environment object (global state of the shell).
              help                  - This help string.
            ");
          };

          debugInit();
          """;

  private static final String cheats = """
                     
           setGlobal("kill", (entity) {
                "Get the CombatStatsComponent from the target entity.";
                "Note the use of .class to get the class object.";
                     
                stats = entity.getComponent(.com.csse3200.game.components.CombatStatsComponent);
                ifElse(exists("stats"), () {
                  stats = getParentVar("stats");
                  stats.setHealth(0);
                  entity = getParentVar("entity");
                  print("Entity ", entity.getId(), " has been killed!\\n");
                }, () {
                  entity = getParentVar("entity");
                  print("Entity ", entity.getId(), " has no health to set!\\n");
                });
              });
              
          setGlobal("godMode", () {
              player = getPlayer();
              stats = player.getComponent(.com.csse3200.game.components.CombatStatsComponent);
              ifElse(exists("stats"), () {
                stats = getParentVar("stats");
                i = stats.getIsInvulnerable();
                stats.setIsInvulnerable(not(i));
                print("God mode toggled!\n");
              }, () {
                print("Unable to enable god mode");
              });
          });
         
          setGlobal("spawnJetpack", () {
              es = entityService();
              jetpack = .com.csse3200.game.entities.factories.CollectableFactory.createJetpackUpgrade();
              player = getPlayer();
              physics = player.getComponent(.com.csse3200.game.physics.components.PhysicsComponent);
              body = physics.getBody();
               
              es.register(jetpack);
              jetpack.setPosition(body.getWorldCenter());
              
              print("Jetpack spawned!\n");
          });
          
          setGlobal("spawnGlider", () {
              es = entityService();
              glider = .com.csse3200.game.entities.factories.CollectableFactory.createGlideUpgrade();
              player = getPlayer();
              physics = player.getComponent(.com.csse3200.game.physics.components.PhysicsComponent);
              body = physics.getBody();
               
              es.register(glider);
              glider.setPosition(body.getWorldCenter());
              
              print("Glider spawned!\n");
          });
          
          setGlobal("spawnDash", () {
              es = entityService();
              dash = .com.csse3200.game.entities.factories.CollectableFactory.createDashUpgrade();
              player = getPlayer();
              physics = player.getComponent(.com.csse3200.game.physics.components.PhysicsComponent);
              body = physics.getBody();
               
              es.register(dash);
              dash.setPosition(body.getWorldCenter());
              
              print("Dash Upgrade spawned!\n");
          });
          
          setGlobal("spawnAllUpgrades", () {
              spawnGlider();
              spawnJetpack();
              spawnDash();
              
              print("All upgrades spawned!\n");
          });
          
          setGlobal("spawnDoorKey", () {
              es = entityService();
              key = .com.csse3200.game.entities.factories.CollectableFactory.createCollectable("key:door");
              player = getPlayer();
              physics = player.getComponent(.com.csse3200.game.physics.components.PhysicsComponent);
              body = physics.getBody();
              
              es.register(key);
              key.setPosition(body.getWorldCenter());
              
              print("Door Key Spawned!\n");
          });
          
          setGlobal("getGameArea", () {
          
              screen = ServiceLocator.getMainGameScreen();
              gameAreaEnum = screen.getAreaEnum();
              gameArea = screen.getGameArea(gameAreaEnum);
              returnN(1, gameArea);
          });
          
          setGlobal("goNextLevel", () {
              player = getPlayer();
              screen = ServiceLocator.getMainGameScreen();
              gameAreaEnum = screen.getAreaEnum();
              nextArea = screen.getNextArea(gameAreaEnum);
              
              screen.switchAreaRunnable(nextArea, player);
              print("Level Changed!\n");   
          });
          
          setGlobal("debugRender", () {
              renderService = ServiceLocator.getRenderService();
              renderer = renderService.getDebug();
              active = renderer.getActive();
              setGlobal(".renderer", renderer);
              
              ifElse(active, () {
                  renderer = getGlobal(".renderer");
                  renderer.setActive(false);
                  print("Debug Renderer is Off!\n");
              }, () {
                  renderer = getGlobal(".renderer");
                  renderer.setActive(true);
                  print("Debug Renderer is Active!\n");
              });
          });
                 
          setGlobal("toggleOnAI", () {  
              forEachAI((droneEntity) {
                setGlobal(".droneEntity", droneEntity);
                ifElse(droneEntity.getEnabled, () {
                    droneEntity = getGlobal(".droneEntity");
                    droneEntity.setEnabled(false);
                }, () {
                    droneEntity = getGlobal(".droneEntity");
                    droneEntity.setEnabled(true);
                });
              });
              print("Drone AI toggled\n");
          });
          
          setGlobal("fly", () {
              player = getPlayer();
              keyBoardComponent = player.getComponent(.com.csse3200.game.components.player.KeyboardPlayerInputComponent);
              setGlobal(".keyBoardComponent", keyBoardComponent);
              ifElse(not(isNull(keyBoardComponent)), () {
                  keyBoardComponent = getGlobal(".keyBoardComponent");
                  ifElse(keyBoardComponent.getIsCheatsOn(), () {
                      keyBoardComponent = getGlobal(".keyBoardComponent");
                      keyBoardComponent.setIsCheatsOn(false);
                      print("Flight disabled!\n");
                  }, () {
                      keyBoardComponent = getGlobal(".keyBoardComponent");
                      keyBoardComponent.setIsCheatsOn(true);
                      print("Flight enabled!\n");
                  });
              }, () {
                  print("Keyboard component unable to be reached!\n");
              });
          });
          
          setGlobal("teleport", (x, y) {
              player = getPlayer();
              physics = player.getComponent(.com.csse3200.game.physics.components.PhysicsComponent);
              body = physics.getBody();
              vector = .com.badlogic.gdx.math.Vector2(x, y);
              body.setTransform(vector, body.getAngle());
              print("Player teleported to: (", x, ",", y, ")\n");
          });
          
          setGlobal("setSpeed", (x, y) {
              player = getPlayer();
              playerActions = player.getComponent(.com.csse3200.game.components.player.PlayerActions);
              playerActions.setWalkSpeed(x, y);
              print("Horizontal walk speed set to ", x, " and vertical walk speed set to ", y, "\n");
          });
          
          setGlobal("printInventory", () {
              player = getPlayer();
              inv = player.getComponent(.com.csse3200.game.components.player.InventoryComponent);
              print(inv.printItems());
          });
          
          setGlobal("setHealth", (amount) {
              player = getPlayer();
              cbs = player.getComponent(.com.csse3200.game.components.CombatStatsComponent);
              
              cbs.setHealth(amount);
              print("Player health set to ", amount, "!\n");
          });
          """;
}
