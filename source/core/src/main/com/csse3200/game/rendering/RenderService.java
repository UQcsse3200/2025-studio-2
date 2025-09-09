package com.csse3200.game.rendering;

import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Disposable;
import com.csse3200.game.ui.terminal.TerminalService;
import com.csse3200.game.utils.SortedIntMap;

/**
 * Globally accessible service for registering renderable components. Any renderable registered with
 * this service has render() called once per frame.
 */
public class RenderService implements Disposable {
  private static final int INITIAL_LAYER_CAPACITY = 4;
  private static final int INITIAL_CAPACITY = 4;
  private Stage stage;
  private DebugRenderer debugRenderer;

  /**
   * Map from layer to list of renderables, allows us to render each layer in the correct order
   */
  private final SortedIntMap<Array<Renderable>> renderables =
      new SortedIntMap<>(INITIAL_LAYER_CAPACITY);

  /**
   * Register a new renderable.
   *
   * @param renderable new renderable.
   */
  public void register(Renderable renderable) {
    int layerIndex = renderable.getLayer();
    if (!renderables.containsKey(layerIndex)) {
      renderables.put(layerIndex, new Array<>(INITIAL_CAPACITY));
    }
    Array<Renderable> layer = renderables.get(layerIndex);
    layer.add(renderable);
  }

  /**
   * Trigger rendering on the given batch within the given layer range.
   * This should be called only from the main renderer.
   * <p>
   * This method will only render layers within the possible range of the IntMap,
   * as such {@code Integer.MIN_VALUE} and {@code Integer.MAX_VALUE} can be used as bounds.
   *
   * @param batch batch to render to
   * @param minLayer render layer lower bound
   * @param maxLayer render layer upper bound
   */
  public void renderLayerRange(SpriteBatch batch, int minLayer, int maxLayer) {
    if (minLayer > maxLayer) return;
    // iterate layers in order
    int n = renderables.size();
    for (int i = 0; i < n; i++) {
      int layerKey = renderables.getKeyAt(i);
      if (layerKey < minLayer) continue;
      if (layerKey > maxLayer) break;

      Array<Renderable> layer = renderables.getValueAt(i);
      layer.sort();
      for (Renderable renderable : layer) {
        renderable.render(batch);
      }
    }
  }

  /**
   * Trigger rendering on the given batch only on the given layer.
   * This should be called only from the main renderer.
   *
   * @param batch batch to render to
   * @param layer render layer to be rendered
   */
  public void renderLayer(SpriteBatch batch, int layer) {
    renderLayerRange(batch, layer, layer);
  }

  /**
   * Unregister a renderable.
   *
   * @param renderable renderable to unregister.
   */
  public void unregister(Renderable renderable) {
    Array<Renderable> layer = renderables.get(renderable.getLayer());
    if (layer != null) {
      layer.removeValue(renderable, true);
    }
  }

  /**
   * Trigger rendering on the given batch. This should be called only from the main renderer.
   *
   * @param batch batch to render to.
   */
  public void render(SpriteBatch batch) {
    for (Array<Renderable> layer : renderables) {
      // Sort into rendering order
      layer.sort();

      for (Renderable renderable : layer) {
        renderable.render(batch);
      }
    }
  }

  public void setStage(Stage stage) {
    this.stage = stage;
    TerminalService.setStage(stage);
  }

  public Stage getStage() {
    return stage;
  }

  public void setDebug(DebugRenderer debugRenderer) {
    this.debugRenderer = debugRenderer;
  }

  public DebugRenderer getDebug() {
    return debugRenderer;
  }

  @Override
  public void dispose() {
    renderables.clear();
  }
}
