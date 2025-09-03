package com.csse3200.game.ui.inventoryscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Group;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Align;
import com.badlogic.gdx.utils.Scaling;
import com.csse3200.game.components.minimap.MinimapComponent;
import com.csse3200.game.components.minimap.MinimapDisplay;
import com.csse3200.game.components.pausemenu.MapDisplay;
import com.csse3200.game.entities.Entity;
import com.csse3200.game.services.ServiceLocator;

import java.util.Map;

public class MapTab implements InventoryTabInterface {
    private final Texture bgTex = new Texture(Gdx.files.internal("inventory-screen/map-selected.png"));

    // base dimensions of upgrades-selected.png
    private static final float BASE_W = 770f;
    private static final float BASE_H = 768f;
    private static final float BASE_ASPECT = BASE_W / BASE_H;


    @Override
    public Actor build(Skin skin) {
        syncMarkers();
        float screenH = Gdx.graphics.getHeight();
        float canvasH = screenH * (2f / 3f);
        float canvasW = canvasH * BASE_ASPECT;
//
//        Image background = new Image(new TextureRegionDrawable(new TextureRegion(bgTex)));
//        background.setScaling(Scaling.stretch);
//        background.setSize(canvasW, canvasH);
//
//        Container<Image> centered = new Container<>(background);
//        centered.size(canvasW, canvasH);
//        centered.align(Align.center);

        MapDisplay map = new MapDisplay();
        Container<MapDisplay> centered = new Container<>(map);
        centered.size(canvasW, canvasH);
        centered.align(Align.center);

        Stack stack = new Stack();
//        stack.add(centered);
        stack.add(centered);

//        map.pad(90f, 10f, 30f, 10f);

        return stack;
    }

  private void syncMarkers() {
    Group markerGroup = ServiceLocator.getMinimapService().getMapMarkerGroup();
    for (Map.Entry<Entity, Image> serviceEntry : ServiceLocator.getMinimapService().getTrackedEntities().entrySet()) {
      markerGroup.addActor(serviceEntry.getValue());
    }
  }
}