package com.csse3200.game.ui.terminal;

import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
class ShellTest {
  private Shell shell;
  private TestConsole console;

  @BeforeEach
  void setUp() {
    console = new TestConsole();
    shell = new Shell(console);
  }

  @Test
  void shouldEvaluateLiterals() {
    assertEquals(123, shell.eval("123;"));
    assertEquals(12.3f, shell.eval("12.3;"));
    assertEquals('h', shell.eval("'h';"));
    assertEquals("hello", shell.eval("\"hello\";"));
  }

  @Test
  void shouldHandleAssignmentAndAccess() {
    shell.eval("x = 10;");
    assertEquals(10, shell.eval("x;"));

    shell.eval("y = \"world\";");
    assertEquals("world", shell.eval("y;"));
  }

  @Test
  void shouldAccessJavaApi() {
    Object result = shell.eval(".java.lang.Math.abs(-10);");
    assertEquals(10, result);
  }

  @Test
  void shouldThrowOnInvalidSyntax() {
    assertThrows(RuntimeException.class, () -> shell.eval("1 + ;"));
  }

  @Test
  void shouldThrowOnUndefinedVariable() {
    assertThrows(RuntimeException.class, () -> shell.eval("nonExistentVar;"));
  }

  @Test
  void isTruthyTest() {
    assertTrue(Shell.isTruthy(true));
    assertFalse(Shell.isTruthy(false));
    assertTrue(Shell.isTruthy(1));
    assertFalse(Shell.isTruthy(0));
    assertTrue(Shell.isTruthy("hello"));
    assertFalse(Shell.isTruthy(""));
    assertTrue(Shell.isTruthy(new Object()));
    assertFalse(Shell.isTruthy(null));

    assertTrue(Shell.isTruthy(new Object()));
    assertFalse(Shell.isTruthy(null));
    assertTrue(Shell.isTruthy(new int[]{1, 2}));
    assertFalse(Shell.isTruthy(new int[]{}));
    assertTrue(Shell.isTruthy(List.of(1)));
    assertFalse(Shell.isTruthy(new ArrayList<>()));

  }

  @Test
  void shouldHandleIfThen() {
    assertEquals(10, shell.eval("globalThis.ifThen(1, () { globalThis.ReturnValueClass(10); });"));
    assertNull(shell.eval("globalThis.ifThen(0, () { globalThis.ReturnValueClass(10); });"));
  }

  @Test
  void shouldHandleIfElse() {
    assertEquals(10, shell.eval("globalThis.ifElse(1, () { globalThis.ReturnValueClass(10); }, () { globalThis.ReturnValueClass(20); });"));
    assertEquals(20, shell.eval("globalThis.ifElse(0, () { globalThis.ReturnValueClass(10); }, () { globalThis.ReturnValueClass(20); });"));
  }

  @Test
  void shouldAccessPrivateJavaMembers() {
    shell.eval("""
      instance = .com.csse3200.game.ui.terminal.ShellTest.TestClass();
      instance.privateField = "modified";
    """);
    assertEquals("modified", shell.eval("instance.privateField;"));
  }

  @Test
  void shouldIterateJavaArray() {
    shell.setGlobal("myArray", new String[]{"a", "b", "c"});
    shell.eval("globalThis.forEach(myArray, globalThis.console.print);");
    assertEquals("abc", console.getOutput());
  }

  @Test
  void shouldIterateJavaList() {
    shell.env.put("myList", List.of(1, 2, 3));
    shell.eval("globalThis.forEach(myList, globalThis.console.print);");
    assertEquals("123", console.getOutput());
  }

  @Test
  void shouldThrowOnMismatchedConstructorArgs() {
    assertThrows(RuntimeException.class, () -> shell.eval(".java.awt.Point(1);"));
    assertThrows(RuntimeException.class, () -> shell.eval(".java.awt.Point(\"a\", \"b\");"));
  }

  @Test
  void shouldThrowOnNonExistentMethod() {
    shell.eval("p = .java.awt.Point();");
    assertThrows(RuntimeException.class, () -> shell.eval("p.nonExistentMethod();"));
  }

  @Test
  void shouldThrowOnNonExistentFieldAccess() {
    shell.eval("p = .java.awt.Point();");
    assertThrows(RuntimeException.class, () -> shell.eval("p.nonExistentField;"));
  }

  @Test
  void shellRangeShouldIterateCorrectly() {
    Shell.Range range = new Shell.Range(1, 3);
    assertTrue(range.hasNext());
    assertEquals(1L, range.next());
    assertTrue(range.hasNext());
    assertEquals(2L, range.next());
    assertTrue(range.hasNext());
    assertEquals(3L, range.next());
    assertFalse(range.hasNext());
  }

  @Test
  void shellMapShouldHandleRecursiveToString() {
    ShellMap map = new ShellMap();
    map.put("self", map);
    assertDoesNotThrow(map::toString);
    assertEquals("{self=...}", map.toString());
  }

  private static class TestConsole implements Shell.Console {
    private final StringBuilder output = new StringBuilder();

    @Override
    public void print(Object obj) {
      output.append(obj);
    }

    @Override
    public String next() {
      return null;
    }

    @Override
    public boolean hasNext() {
      return false;
    }

    @Override
    public void close() {
    }

    public String getOutput() {
      return output.toString();
    }
  }

  public static class TestClass {
    private String privateField = "initial";
  }
}