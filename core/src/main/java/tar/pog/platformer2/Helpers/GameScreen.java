package tar.pog.platformer2.Helpers;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.*;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.ScreenUtils;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import tar.pog.platformer2.Main.Main;
import tar.pog.platformer2.Menu.GameOverScreen;
import tar.pog.platformer2.Menu.MainMenuScreen;
import tar.pog.platformer2.Obstacles.Fire;
import tar.pog.platformer2.NPC.Slime;
import tar.pog.platformer2.Player.Player;
import tar.pog.platformer2.Rewards.Coin;
import tar.pog.platformer2.Rewards.Liquor;

public class GameScreen extends InputAdapter implements Screen {

    final Main game;

    // Core game objects
    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera; // Main game camera
    private Viewport viewport; // Main game viewport
    private OrthographicCamera uiCamera; // UI camera for GUI elements
    private Viewport uiViewport; // Viewport for UI elements
    private Texture playerTexture, playerTextureStand;
    private Animation<TextureRegion> stand, walk, jump;
    private Player player;
    private Fire[] fires;
    private Coin coin;
    private Liquor[] liquors; // Multiple liquors
    private TouchInputHandler touchInputHandler;
    private Pool<Rectangle> rectPool = new Pool<Rectangle>() {
        @Override
        protected Rectangle newObject() {
            return new Rectangle();
        }
    };

    private Array<Rectangle> tiles = new Array<>();
    private boolean debug = true; // Enabled for debugging
    private ShapeRenderer debugRenderer;
    private TileManager tileManager;
    private Slime.SlimeManager slimeManager;

    public String mapFile = "level1.tmx"; // Default

    // Level progression: add all your level files here in order
    private static final String[] MAP_FILES = {
        "level1.tmx",
        "level2.tmx",
        "level3.tmx",
        "level5.tmx",
        "level4.tmx",
        "level6.tmx"
        // Add more as needed
    };

    private boolean coinCollected = false; // Track if coin was just collected

    public GameScreen(Main game) {
        this(game, "level1.tmx");
    }

    public GameScreen(Main game, String mapFile) {
        this.game = game;
        if (mapFile != null && !mapFile.isEmpty()) {
            this.mapFile = mapFile;
        }
        create();
    }

    private void create() {
        // Main game camera and viewport setup
        camera = new OrthographicCamera();
        viewport = new FitViewport(30, 20, camera); // Fixed virtual size of 30x20 for the game world
        viewport.apply();

        // UI camera and viewport setup
        uiCamera = new OrthographicCamera();
        uiViewport = new FitViewport(1020, 475, uiCamera); // Fixed virtual size for UI elements
        uiViewport.apply(true); // Center the UI camera

        touchInputHandler = new TouchInputHandler(uiCamera);

        // Load map
        try {
            map = new TmxMapLoader().load(mapFile);
        } catch (Exception e) {
            System.err.println("Failed to load " + mapFile + ": " + e.getMessage());
            Gdx.app.exit();
            return;
        }

        renderer = new OrthogonalTiledMapRenderer(map, 1 / 16f); // Assuming 16x16 pixel tiles
        tileManager = new TileManager(map, 16f, 1 / 16f);

        // Load player textures
        try {
            playerTexture = new Texture("img/player/player_run.png");
            playerTextureStand = new Texture("img/player/player_standing.png");
        } catch (Exception e) {
            System.err.println("Failed to load player textures: " + e.getMessage());
            Gdx.app.exit();
            return;
        }

        // Setup player animations
        setupPlayerAnimations();

        // Initialize game objects
        initializeGameObjects();
        debugRenderer = new ShapeRenderer();
        Gdx.input.setInputProcessor(this);
    }

    private void setupPlayerAnimations() {
        TextureRegion[] regions_forStanding = TextureRegion.split(playerTextureStand, 16, 16)[0];
        TextureRegion[] regions = TextureRegion.split(playerTexture, 16, 16)[0];
        stand = new Animation<>(0.15f, regions_forStanding);
        jump = new Animation<>(0, regions[1]);
        walk = new Animation<>(0.15f, regions);
        walk.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);

        Player.WIDTH = 1.5f * (1 / 16f * regions[0].getRegionWidth());
        Player.HEIGHT = 1.5f * (1 / 16f * regions[0].getRegionHeight());
    }

    /**
     * This method uses different positions for fire, coin, slimeManager, and player
     * depending on which map is loaded.
     */
    private void initializeGameObjects() {
        float mapWidth;
        try {
            mapWidth = ((TiledMapTileLayer) map.getLayers().get("walls")).getWidth();
        } catch (Exception e) {
            mapWidth = 100f;
        }

        // Example: 2 liquors in level4.tmx
        if ("level4.tmx".equals(mapFile)) {
            slimeManager = new Slime.SlimeManager(mapWidth, mapFile);
            fires = new Fire[] {
                new Fire(50, 11),
                new Fire(55, 11),
                new Fire(97, 5)
            };
            coin = new Coin(200, 6);
            liquors = new Liquor[] {
                new Liquor(60, 11),
                new Liquor(119, 3)
            };
            player = new Player(touchInputHandler, tileManager, fires, coin, liquors, slimeManager.getSlimes(), rectPool, tiles);
            player.position.set(15, 11);
        }

        else if ("level3.tmx".equals(mapFile)) {
            slimeManager = new Slime.SlimeManager(mapWidth, mapFile);
            fires = new Fire[] {
                new Fire(195, 2),
                new Fire(78, 5),
                new Fire(118, 5)
            };
            coin = new Coin(200, 2);

            player = new Player(touchInputHandler, tileManager, fires, coin, liquors, slimeManager.getSlimes(), rectPool, tiles);
            player.position.set(15, 8);
        }

        else if ("level2.tmx".equals(mapFile)) {
            slimeManager = new Slime.SlimeManager(mapWidth, mapFile);
            fires = new Fire[] {
                new Fire(46, 7),
                new Fire(73, 2),
                new Fire(116, 6)
            };
            coin = new Coin(205, 2);

            player = new Player(touchInputHandler, tileManager, fires, coin, liquors, slimeManager.getSlimes(), rectPool, tiles);
            player.position.set(15, 8);
        }


        else if ("level5.tmx".equals(mapFile)) {
            slimeManager = new Slime.SlimeManager(mapWidth, mapFile);
            fires = new Fire[] {
                new Fire(65, 8),
            };
            coin = new Coin(203, 14);
            liquors = new Liquor[] { new Liquor(198, 2) };
            player = new Player(touchInputHandler, tileManager, fires, coin, liquors, slimeManager.getSlimes(), rectPool, tiles);
            player.position.set(15, 8);
        } else if ("level6.tmx".equals(mapFile)) {
            slimeManager = new Slime.SlimeManager(mapWidth, mapFile);
            fires = new Fire[] {
                new Fire(50, 3),
                new Fire(60, 3),
                new Fire(80, 2)
            };
            coin = new Coin(100, 1);
            liquors = new Liquor[] { new Liquor(70, 2) };
            player = new Player(touchInputHandler, tileManager, fires, coin, liquors, slimeManager.getSlimes(), rectPool, tiles);
            player.position.set(15, 8);
        } else {
            // Default positions (for level1 and others)
            slimeManager = new Slime.SlimeManager(mapWidth, mapFile);
            fires = new Fire[] {
                new Fire(100, 2),
                new Fire(125, 2),
                new Fire(140, 2)
            };
            coin = new Coin(200, 2);
//            liquors = new Liquor[] { new Liquor(90, 2) };
            player = new Player(touchInputHandler, tileManager, fires, coin, liquors, slimeManager.getSlimes(), rectPool, tiles);
            player.position.set(15, 10);
        }
        coinCollected = false; // Reset on new level
    }

    private int getCurrentMapIndex() {
        for (int i = 0; i < MAP_FILES.length; i++) {
            if (MAP_FILES[i].equals(mapFile)) return i;
        }
        return 0;
    }

    private String getNextMapFile() {
        int idx = getCurrentMapIndex();
        if (idx + 1 < MAP_FILES.length) {
            return MAP_FILES[idx + 1];
        } else {
            // Optionally go to menu or "You Win" screen
            return null;
        }
    }

    private void updateCamera() {
        float mapWidthInUnits = ((TiledMapTileLayer) map.getLayers().get("walls")).getWidth();
        float mapHeightInUnits = ((TiledMapTileLayer) map.getLayers().get("walls")).getHeight();

        camera.position.set(player.position.x, player.position.y + Player.HEIGHT / 2, 0);

        camera.position.x = MathUtils.clamp(camera.position.x, camera.viewportWidth / 2, mapWidthInUnits - camera.viewportWidth / 2);
        camera.position.y = MathUtils.clamp(camera.position.y, camera.viewportHeight / 2, mapHeightInUnits - camera.viewportHeight / 2);
        camera.update();
    }

    @Override
    public void render(float deltaTime) {
        ScreenUtils.clear(0.5f, 0.7f, 1, 1);

        update(deltaTime);

        if (player.isDead()) {
            System.out.println("Player is dead - switching to GameOverScreen");
            game.setScreen(new GameOverScreen(game, mapFile));
            dispose();
            return;
        }

        // --- Level progression: if coin was collected, go to next level ---
        if (!coinCollected && coin != null && coin.isCollected()) {
            coinCollected = true;
            String nextMap = getNextMapFile();
            if (nextMap != null) {
                game.setScreen(new GameScreen(game, nextMap));
            } else {
                // If no more maps, go to main menu (optional: add win screen)
                game.setScreen(new MainMenuScreen(game));
            }
            dispose();
            return;
        }

        updateCamera();
        renderer.setView(camera);
        renderer.render();

        Batch batch = renderer.getBatch();
        batch.begin();
        slimeManager.render(batch);
        coin.renderCoin(batch);
        if (liquors != null) {
            for (Liquor liquor : liquors) {
                if (liquor != null) liquor.render(batch);
            }
        }
        for (Fire fire : fires) {
            fire.renderFire(batch);
        }
        batch.end();

        // Render player
        renderPlayer(deltaTime);

        // Render GUI elements using the UI camera
        uiViewport.apply();
        uiCamera.update();
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        touchInputHandler.render(batch);
        batch.end();

        if (debug) renderDebug();
    }

    private void update(float deltaTime) {
        player.update(deltaTime);
        coin.updateCoin(deltaTime);
        // Optionally: update liquor animations here if needed
        for (Fire fire : fires) {
            fire.updateFire(deltaTime);
        }

        float cameraLeft = camera.position.x - camera.viewportWidth / 2;
        slimeManager.update(deltaTime, cameraLeft);
        player.updateSlimes(slimeManager.getSlimes());
    }

    private void renderPlayer(float deltaTime) {
        TextureRegion frame = null;
        switch (player.state) {
            case Standing:
                frame = stand.getKeyFrame(player.stateTime);
                break;
            case Walking:
                frame = walk.getKeyFrame(player.stateTime);
                break;
            case Jumping:
                frame = jump.getKeyFrame(player.stateTime);
                break;
        }

        if (frame != null) {
            Batch batch = renderer.getBatch();
            batch.begin();
            if (player.facesRight) {
                batch.draw(frame, player.position.x, player.position.y, Player.WIDTH, Player.HEIGHT);
            } else {
                batch.draw(frame, player.position.x + Player.WIDTH, player.position.y, -Player.WIDTH, Player.HEIGHT);
            }
            batch.end();
        }
    }

    private void renderDebug() {
        debugRenderer.setProjectionMatrix(camera.combined);
        debugRenderer.begin(ShapeRenderer.ShapeType.Line);

        // Player bounds
        // debugRenderer.setColor(Color.RED);
        // debugRenderer.rect(player.position.x, player.position.y, Player.WIDTH, Player.HEIGHT);

        // Map tiles
        debugRenderer.setColor(Color.YELLOW);
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get("walls");
        for (int y = 0; y < layer.getHeight(); y++) {
            for (int x = 0; x < layer.getWidth(); x++) {
                TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                if (cell != null && camera.frustum.boundsInFrustum(x + 0.5f, y + 0.5f, 0, 1, 1, 0)) {
                    // debugRenderer.rect(x, y, 1, 1);
                }
            }
        }

        // Camera bounds
        // debugRenderer.setColor(Color.GREEN);
        // debugRenderer.rect(camera.position.x - camera.viewportWidth / 2,
        //     camera.position.y - camera.viewportHeight / 2,
        //     camera.viewportWidth, camera.viewportHeight);

        debugRenderer.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height);
        uiViewport.update(width, height, true);
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void show() {}

    @Override
    public void dispose() {
        if (renderer != null) renderer.dispose();
        if (map != null) map.dispose();
        if (playerTexture != null) playerTexture.dispose();
        if (playerTextureStand != null) playerTextureStand.dispose();
        if (slimeManager != null) slimeManager.dispose();
        if (debugRenderer != null) debugRenderer.dispose();
        if (touchInputHandler != null) touchInputHandler.dispose();
        if (liquors != null) {
            for (Liquor liquor : liquors) {
                if (liquor != null) liquor.dispose();
            }
        }
        for (Fire fire : fires) {
            fire.dispose();
        }
    }
}
