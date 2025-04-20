package tar.pog.platformer2;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer.Cell;
import com.badlogic.gdx.maps.tiled.TmxMapLoader;
import com.badlogic.gdx.maps.tiled.renderers.OrthogonalTiledMapRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.ScreenUtils;
import tar.pog.platformer2.Helpers.TileManager;
import tar.pog.platformer2.Helpers.TouchInputHandler;
import tar.pog.platformer2.Obstacles.Fire;
import tar.pog.platformer2.Obstacles.Slime;
import tar.pog.platformer2.Rewards.Coin;

public class Main extends InputAdapter implements ApplicationListener {

    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;
    private OrthographicCamera uiCamera;
    private Texture playerTexture;
    private Texture playerTextureStand;
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

    @Override
    public void create() {
        touchInputHandler = new TouchInputHandler();

        try {
            map = new TmxMapLoader().load("level1.tmx");
        } catch (Exception e) {
            System.err.println("Failed to load level1.tmx: " + e.getMessage());
            Gdx.app.exit(); return;
        }

        renderer = new OrthogonalTiledMapRenderer(map, 1 / 16f);
        tileManager = new TileManager(map);

        try {
            playerTexture = new Texture("player_run.png");
            playerTextureStand = new Texture("player_standing.png");
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

        // Setup UI Camera
        uiCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        uiCamera.setToOrtho(false);
        uiCamera.update();

        float mapWidth;
        try {
            mapWidth = ((TiledMapTileLayer) map.getLayers().get("walls")).getWidth();
        } catch (Exception e) {
            System.err.println("Failed to get map width: " + e.getMessage());
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

        // Set input processor to handle key presses
        Gdx.input.setInputProcessor(this);
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.5f, 0.7f, 1, 1);
        if (map == null || tileManager == null || player == null) return;

        float deltaTime = Gdx.graphics.getDeltaTime();
        update(deltaTime);

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
        if (player != null) player.update(deltaTime);
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
            case Standing: frame = stand.getKeyFrame(player.stateTime); break;
            case Walking: frame = walk.getKeyFrame(player.stateTime); break;
            case Jumping: frame = jump.getKeyFrame(player.stateTime); break;
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
        debugRenderer.begin(ShapeType.Line);
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
        // Reset player position
        player.position.set(20, 20);
        // Reset slimes
        slimeManager.reset();
        // Reset camera to follow player
        camera.position.x = player.position.x;
        camera.update();
        // Add other reset logic as needed (e.g., coin, fires, etc.)
    }

    @Override
    public boolean keyDown(int keycode) {
        // Trigger game reset on 'R' key press
        if (keycode == Keys.R) {
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

    @Override public void resume() {}
    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
}
