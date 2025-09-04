package com.csse3200.game.components.tooltip;

import com.csse3200.game.components.tooltip.TooltipSystem.*;
import com.csse3200.game.extensions.GameExtension;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(GameExtension.class)
@ExtendWith(MockitoExtension.class)
public class TooltipSystemTest {

  @Test
  void shouldCreateTooltipComponent() {
    // Test basic creation with different constructors
    assertDoesNotThrow(() -> new TooltipComponent("Test message"));
    assertDoesNotThrow(() -> new TooltipComponent("Warning!", TooltipStyle.WARNING));
    assertDoesNotThrow(() -> new TooltipComponent("Success!", TooltipStyle.SUCCESS, 3.0f, 2.5f));
  }

  @Test
  void shouldHaveCorrectTooltipStyleProperties() {
    // Test DEFAULT style
    assertEquals("button", TooltipStyle.DEFAULT.getBackgroundDrawable());
    assertEquals("white", TooltipStyle.DEFAULT.getTextColor());
    
    // Test WARNING style
    assertEquals("button", TooltipStyle.WARNING.getBackgroundDrawable());
    assertEquals("red", TooltipStyle.WARNING.getTextColor());
    
    // Test SUCCESS style
    assertEquals("button", TooltipStyle.SUCCESS.getBackgroundDrawable());
    assertEquals("green", TooltipStyle.SUCCESS.getTextColor());
  }

  @Test
  void shouldHandleTooltipManagerOperations() {
    // Test that TooltipManager methods can be called without errors
    assertDoesNotThrow(() -> {
      TooltipManager.setActiveDisplay(null);
      TooltipManager.showTooltip("Test", TooltipStyle.DEFAULT);
      TooltipManager.hideTooltip();
    });
  }

  @Test
  void shouldCreateTooltipComponentInstances() {
    TooltipComponent basicTooltip = new TooltipComponent("Basic tooltip");
    TooltipComponent styledTooltip = new TooltipComponent("Styled tooltip", TooltipStyle.WARNING);
    TooltipComponent customTooltip = new TooltipComponent("Custom tooltip", TooltipStyle.SUCCESS, 2.0f, 1.5f);
    
    assertNotNull(basicTooltip);
    assertNotNull(styledTooltip);
    assertNotNull(customTooltip);
  }
}
