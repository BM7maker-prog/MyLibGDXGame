package tar.pog.platformer2;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
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
import tar.pog.platformer2.Rewards.Coin;

public class Main extends InputAdapter implements ApplicationListener {

    private TiledMap map;
    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;
    private Texture playerTexture;
    private Texture playerTextureStand;
    private Animation<TextureRegion> stand;
    private Animation<TextureRegion> walk;
    private Animation<TextureRegion> jump;
    private Player player;
    private Fire fire;
    private Coin coin;
    private Fire fire1;
    private Fire fire2;
    private Fire fire3;
    private Fire fire4;
    private TouchInputHandler touchInputHandler;
    private Pool<Rectangle> rectPool = new Pool<Rectangle>() {
        @Override
        protected Rectangle newObject() {
            return new Rectangle();
        }
    };
    private Array<Rectangle> tiles = new Array<Rectangle>();
    private boolean debug = false;
    private ShapeRenderer debugRenderer;
    private TileManager tileManager;

    @Override
    public void create() {
        touchInputHandler = new TouchInputHandler();
        // Load player textures and create animations
        playerTexture = new Texture("player_run.png");
        playerTextureStand = new Texture("player_standing.png");
        TextureRegion[] regions_forStanding = TextureRegion.split(playerTextureStand, 16, 16)[0];
        TextureRegion[] regions = TextureRegion.split(playerTexture, 16, 16)[0];
        stand = new Animation<TextureRegion>(0.15f, regions_forStanding[0], regions_forStanding[1],
            regions_forStanding[2], regions_forStanding[3]);
        jump = new Animation<TextureRegion>(0, regions[1]);
        walk = new Animation<TextureRegion>(0.15f, regions[0], regions[1], regions[2], regions[3],
            regions[4], regions[5]);
        walk.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);

        // Set player dimensions
        Player.WIDTH = 1.5f * (1 / 16f * regions[0].getRegionWidth());
        Player.HEIGHT = 1.5f * (1 / 16f * regions[0].getRegionHeight());

        // Load map
        map = new TmxMapLoader().load("level1.tmx");
        renderer = new OrthogonalTiledMapRenderer(map, 1 / 16f);
        tileManager = new TileManager(map);

        // Set up camera
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 30, 20);
        camera.update();

        // Create game objects
        fire = new Fire(70, -27);
        fire1 = new Fire(80, -27);
        fire2 = new Fire(100, -27);
        fire3 = new Fire(85, -27);
        fire4 = new Fire(110, -27);
        coin = new Coin(187, -12);

        // Initialize player with dependencies
        Fire[] fires = {fire, fire1, fire2, fire3, fire4};
        player = new Player(touchInputHandler, tileManager, fires, coin, rectPool, tiles);
        player.position.set(20, 20);

        debugRenderer = new ShapeRenderer();
    }

    @Override
    public void render() {
        ScreenUtils.clear(0.5f, 0.7f, 1, 1);

        float deltaTime = Gdx.graphics.getDeltaTime();

        // Update game objects
        player.update(deltaTime);

        // Update camera
        camera.position.x = player.position.x;
        camera.update();

        // Render map
        renderer.setView(camera);
        renderer.render();

        // Render player
        renderPlayer(deltaTime);

        // Render other game objects
        Batch batch = renderer.getBatch();
        coin.updateCoin(deltaTime);
        fire.updateFire(deltaTime);
        fire1.updateFire(deltaTime);
        fire2.updateFire(deltaTime);
        fire3.updateFire(deltaTime);
        fire4.updateFire(deltaTime);
        batch.begin();
        coin.renderCoin(batch);
        fire.renderFire(batch);
        fire1.renderFire(batch);
        fire2.renderFire(batch);
        fire3.renderFire(batch);
        fire4.renderFire(batch);
        batch.end();

        // Render UI
        OrthographicCamera uiCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        uiCamera.setToOrtho(false);
        uiCamera.update();
        batch.setProjectionMatrix(uiCamera.combined);
        batch.begin();
        touchInputHandler.render(batch);
        batch.end();

        if (debug) renderDebug();
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

        Batch batch = renderer.getBatch();
        batch.begin();
        if (player.facesRight) {
            batch.draw(frame, player.position.x, player.position.y, Player.WIDTH, Player.HEIGHT);
        } else {
            batch.draw(frame, player.position.x + Player.WIDTH, player.position.y, -Player.WIDTH, Player.HEIGHT);
        }
        batch.end();
    }

    private void renderDebug() {
        debugRenderer.setProjectionMatrix(camera.combined);
        debugRenderer.begin(ShapeRenderer.ShapeType.Line);

        debugRenderer.setColor(Color.RED);
        debugRenderer.rect(player.position.x, player.position.y, Player.WIDTH, Player.HEIGHT);

        debugRenderer.setColor(Color.YELLOW);
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get("walls");
        for (int y = 0; y <= layer.getHeight(); y++) {
            for (int x = 0; x <= layer.getWidth(); x++) {
                Cell cell = layer.getCell(x, y);
                if (cell != null) {
                    if (camera.frustum.boundsInFrustum(x + 0.5f, y + 0.5f, 0, 1, 1, 0))
                        debugRenderer.rect(x, y, 1, 1);
                }
            }
        }
        debugRenderer.end();
    }

    @Override
    public void dispose() {
        renderer.dispose();
        fire.dispose();
        map.dispose();
        playerTexture.dispose();
        playerTextureStand.dispose();
        debugRenderer.dispose();
    }

    @Override
    public void resume() {
    }

    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }
}
