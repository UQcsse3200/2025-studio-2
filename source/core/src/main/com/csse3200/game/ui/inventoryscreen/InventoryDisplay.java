package com.csse3200.game.ui.inventoryscreen;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.ImageButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
import com.badlogic.gdx.utils.Disposable;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.csse3200.game.components.Component;
import com.csse3200.game.ui.inventoryscreen.Inventory.Tab;

public class InventoryDisplay extends Component implements Disposable {
//    private Stage stage;
//
//    private Texture bgTex;
//    private Texture invSelTex, upgSelTex, mapSelTex;
//
//    private Image selectedStrip;
//
//    // cached geometry for the centered strip
//    private float canvasW, canvasH;
//    private float stripW, stripH, stripX, stripY;
//
//    // Tab content (swap in/out if/when you add UI)
//    private InventoryTabInterface invTab, upgTab, mapTab;
//    private Inventory inventoryState;
//
//    private Texture onePx; // for invisible hitboxes
//
//    @Override
//    public void create() {
//        inventoryState = entity.getComponent(Inventory.class);
//        if (inventoryState == null) {
//            inventoryState = new Inventory();
//            entity.addComponent(inventoryState);
//        }
//
//        // load textures (paths adjusted to your folder names)
//        bgTex     = new Texture(Gdx.files.internal("inventory-screen/no_buttons.png"));
//        invSelTex = new Texture(Gdx.files.internal("inventory-screen/inventory_selected.png"));
//        upgSelTex = new Texture(Gdx.files.internal("inventory-screen/upgrade_selected.png"));
//        mapSelTex = new Texture(Gdx.files.internal("inventory-screen/map_selected.png"));
//
//        canvasW = bgTex.getWidth();
//        canvasH = bgTex.getHeight();
//
//        stripW = invSelTex.getWidth();
//        stripH = invSelTex.getHeight();
//        stripX = (canvasW - stripW) / 2f;
//        stripY = canvasH - stripH;
//
//        stage = new Stage(new FitViewport(canvasW, canvasH));
//        Gdx.input.setInputProcessor(stage); // receive clicks
//
//        // base background
//        Image bg = new Image(new TextureRegionDrawable(bgTex));
//        bg.setBounds(0, 0, canvasW, canvasH);
//        stage.addActor(bg);
//
//        // overlay strip that changes per tab (positioned centered at the top)
//        selectedStrip = new Image();
//        selectedStrip.setBounds(stripX, stripY, stripW, stripH);
//        stage.addActor(selectedStrip);
//
//        // invisible buttons covering each tab within the centered strip
//        stage.addActor(makeTabButton(0, Tab.INVENTORY));
//        stage.addActor(makeTabButton(1, Tab.UPGRADES));
//        stage.addActor(makeTabButton(2, Tab.MAP));
//
//        // tab content objects (hooks for later)
//        invTab = new InventoryTab();
//        upgTab = new UpgradesTab();
//        mapTab = new MapTab();
//
//        applyTab(inventoryState.getTab());
//    }
//
//    // make the buttons for the tabs
//    private ImageButton makeTabButton(int index, final Tab tab) {
//
//    }
//
//    // swap the selected strip image (same bounds, new drawable)
//    private void applyTab(Tab tab) {
//        }
//    }
//
//    private void clearTabContent() {
//    }
//
//    public void draw() {;
//    }
//
//    public void dispose() {
//    }
}