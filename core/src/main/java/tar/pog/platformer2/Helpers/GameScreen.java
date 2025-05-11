package tar.pog.platformer2.Helpers;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.maps.tiled.*;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.utils.ScreenUtils;

import tar.pog.platformer2.Main.Main;
import tar.pog.platformer2.Menu.GameOverScreen;
import tar.pog.platformer2.Obstacles.Fire;
import tar.pog.platformer2.NPC.Slime;
import tar.pog.platformer2.Player.Player;
import tar.pog.platformer2.Rewards.Coin;

public class GameScreen extends InputAdapter implements Screen {

    final Main game;

    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera, uiCamera;
    private Texture playerTexture, playerTextureStand;
    private Animation<TextureRegion> stand, walk, jump;
    private Player player;
    private Fire fire, fire1, fire2, fire3, fire4;
    private Coin coin;
    private TouchInputHandler touchInputHandler;
    private Pool<Rectangle> rectPool = new Pool<Rectangle>() {
        @Override
        protected Rectangle newObject() {
            return new Rectangle();
        }
    };

    private Array<Rectangle> tiles = new Array<>();
    private boolean debug = false;
    private ShapeRenderer debugRenderer;
    private TileManager tileManager;
    private Slime.SlimeManager slimeManager;

    public GameScreen(Main game) {
        this.game = game;
        create();
    }

    private void create() {
        // Initialize uiCamera before touchInputHandler
        uiCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        uiCamera.setToOrtho(false);
        uiCamera.update();

        touchInputHandler = new TouchInputHandler(uiCamera);

        try {
            map = new TmxMapLoader().load("level1.tmx");
        } catch (Exception e) {
            System.err.println("Failed to load level1.tmx: " + e.getMessage());
            Gdx.app.exit(); return;
        }

        renderer = new OrthogonalTiledMapRenderer(map, 1 / 16f);
        tileManager = new TileManager(map, 16f, 1 / 16f);

        try {
            playerTexture = new Texture("img/player/player_run.png");
            playerTextureStand = new Texture("img/player/player_standing.png");
        } catch (Exception e) {
            System.err.println("Failed to load player textures: " + e.getMessage());
            Gdx.app.exit(); return;
        }

        TextureRegion[] regions_forStanding = TextureRegion.split(playerTextureStand, 16, 16)[0];
        TextureRegion[] regions = TextureRegion.split(playerTexture, 16, 16)[0];
        stand = new Animation<>(0.15f, regions_forStanding);
        jump = new Animation<>(0, regions[1]);
        walk = new Animation<>(0.15f, regions);
        walk.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);

        Player.WIDTH = 1.5f * (1 / 16f * regions[0].getRegionWidth());
        Player.HEIGHT = 1.5f * (1 / 16f * regions[0].getRegionHeight());

        camera = new OrthographicCamera();
        camera.setToOrtho(false, 30, 20);
        camera.update();

        float mapWidth;
        try {
            mapWidth = ((TiledMapTileLayer) map.getLayers().get("walls")).getWidth();
        } catch (Exception e) {
            mapWidth = 100f;
        }

        slimeManager = new Slime.SlimeManager(mapWidth);

        fire = new Fire(70, -27); fire1 = new Fire(80, -27); fire2 = new Fire(100, -27);
        fire3 = new Fire(85, -27); fire4 = new Fire(110, -27);
        coin = new Coin(187, -12);

        Fire[] fires = {fire, fire1, fire2, fire3, fire4};
        player = new Player(touchInputHandler, tileManager, fires, coin, slimeManager.getSlimes(), rectPool, tiles);
        player.position.set(20, 20);

        debugRenderer = new ShapeRenderer();
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render(float deltaTime) {
        ScreenUtils.clear(0.5f, 0.7f, 1, 1);
        if (map == null || tileManager == null || player == null) return;

        update(deltaTime);

        // Transition to GameOverScreen if the player dies
        if (player.isDead()) {
            System.out.println("Player is dead - switching to GameOverScreen");
            game.setScreen(new GameOverScreen(game));
            dispose();
            return;
        }

        camera.position.x = player.position.x;
        camera.update();
        renderer.setView(camera);
        renderer.render();

        Batch batch = renderer.getBatch();
        batch.begin();
        slimeManager.render(batch);
        coin.renderCoin(batch);
        fire.renderFire(batch); fire1.renderFire(batch);
        fire2.renderFire(batch); fire3.renderFire(batch); fire4.renderFire(batch);
        batch.end();

        renderPlayer(deltaTime);

        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        touchInputHandler.render(batch);
        batch.end();

        if (debug) renderDebug();
    }

    private void update(float deltaTime) {
        player.update(deltaTime);
        coin.updateCoin(deltaTime);
        fire.updateFire(deltaTime); fire1.updateFire(deltaTime);
        fire2.updateFire(deltaTime); fire3.updateFire(deltaTime); fire4.updateFire(deltaTime);
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
        debugRenderer.setColor(Color.RED);
        debugRenderer.rect(player.position.x, player.position.y, Player.WIDTH, Player.HEIGHT);
        slimeManager.debugRender(debugRenderer);
        debugRenderer.setColor(Color.YELLOW);
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get("walls");
        for (int y = 0; y <= layer.getHeight(); y++) {
            for (int x = 0; x <= layer.getWidth(); x++) {
                Cell cell = layer.getCell(x, y);
                if (cell != null && camera.frustum.boundsInFrustum(x + 0.5f, y + 0.5f, 0, 1, 1, 0)) {
                    debugRenderer.rect(x, y, 1, 1);
                }
            }
        }
        debugRenderer.end();
    }

    private void resetGame() {
        player.position.set(20, 20);
        slimeManager.reset();
        camera.position.x = player.position.x;
        camera.update();
    }

    @Override
    public boolean keyDown(int keycode) {
        if (keycode == Input.Keys.R) {
            resetGame();
            return true;
        }
        return false;
    }

    @Override
    public void dispose() {
        if (renderer != null) renderer.dispose();
        if (map != null) map.dispose();
        if (playerTexture != null) playerTexture.dispose();
        if (playerTextureStand != null) playerTextureStand.dispose();
        if (slimeManager != null) slimeManager.dispose();
        if (fire != null) fire.dispose();
        if (debugRenderer != null) debugRenderer.dispose();
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void show() {}
    @Override public void hide() {}
}
