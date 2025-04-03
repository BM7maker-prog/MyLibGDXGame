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
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.ScreenUtils;

import tar.pog.platformer2.Helpers.TouchInputHandler;
import tar.pog.platformer2.Player;
import tar.pog.platformer2.Obstacles.Fire;
import tar.pog.platformer2.Rewards.Coin;

/** Super Mario Brothers-like very basic platformer, using a tile map built using <a href="https://www.mapeditor.org/">Tiled</a> and a
 * tileset and sprites by <a href="http://www.vickiwenderlich.com/">Vicky Wenderlich</a></p>
 *
 * Shows simple platformer collision detection as well as on-the-fly map modifications through destructible blocks!
 * @author mzechner */
public class Main extends InputAdapter implements ApplicationListener {
    /** The player character, has state and state time, */
    private TiledMap map;

    private OrthogonalTiledMapRenderer renderer;
    private OrthographicCamera camera;
    private Texture playerTexture;
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
        protected Rectangle newObject () {
            return new Rectangle();
        }
    };

    private Array<Rectangle> tiles = new Array<Rectangle>();

    private static final float GRAVITY = -2.5f;

    private boolean debug = false;
    private ShapeRenderer debugRenderer;

    @Override
    public void create () {
        touchInputHandler = new TouchInputHandler();
        // load the player frames, split them, and assign them to Animations
        playerTexture = new Texture("player_run.png");

        TextureRegion[] regions = TextureRegion.split(playerTexture, 16, 16)[0];
        stand = new Animation<TextureRegion>(0, regions[0]);
        jump = new Animation<TextureRegion>(0, regions[1]);
        walk = new Animation<TextureRegion>(0.15f, regions[0], regions[1], regions[2], regions[3], regions[4], regions[5]);
        walk.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);

        // figure out the width and height of the player for collision
        // detection and rendering by converting a player frames pixel
        // size into world units (1 unit == 16 pixels)
        Player.WIDTH = 1.5f * (1 / 16f * regions[0].getRegionWidth());
        Player.HEIGHT = 1.5f * (1 / 16f * regions[0].getRegionHeight());

        // load the map, set the unit scale to 1/16 (1 unit == 16 pixels)
        map = new TmxMapLoader().load("level1.tmx");
        renderer = new OrthogonalTiledMapRenderer(map, 1 / 16f);

        // create an orthographic camera, shows us 30x20 units of the world
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 30, 20);
        camera.update();

        // create the Player we want to move around the world
        player = new Player();
        player.position.set(20, 20);

        coin = new Coin(187,-12);

        fire = new Fire(70,-27);
        fire1 = new Fire(80,-27);
        fire2 = new Fire(100,-27);
        fire3 = new Fire(85,-27);
        fire4 = new Fire(110,-27);
        debugRenderer = new ShapeRenderer();
    }


    @Override
    public void render () {
        // clear the screen
        ScreenUtils.clear(0.5f, 0.7f, 1, 1);


        // get the delta time
        float deltaTime = Gdx.graphics.getDeltaTime();

        // update the player (process input, collision detection, position update)
        updatePlayer(deltaTime);

        // let the camera follow the player, x-axis only
        camera.position.x = player.position.x;
        camera.update();

        // set the TiledMapRenderer view based on what the
        // camera sees, and render the map
        renderer.setView(camera);
        renderer.render();

        // render the player
        renderPlayer(deltaTime);

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
        // render debug rectangles
        if (debug) renderDebug();
    }

    public void updatePlayer (float deltaTime) {
        if (deltaTime == 0) return;

        if (deltaTime > 0.1f)
            deltaTime = 0.1f;

        player.stateTime += deltaTime;

        // check input and apply to velocity & state
        //jumping
        if (touchInputHandler.isTouched(0.5f, 1) && player.grounded) {
            player.velocity.y += Player.JUMP_VELOCITY;
            player.state = Player.State.Jumping;
            player.grounded = false;
        }
        // turning to the left
        if (touchInputHandler.isTouched(0, 0.25f)) {
            player.velocity.x = -Player.MAX_VELOCITY;
            if (player.grounded) player.state = Player.State.Walking;
            player.facesRight = false;
        }
        //turning to the right
        if (touchInputHandler.isTouched(0.25f, 0.5f)) {
            player.velocity.x = Player.MAX_VELOCITY;
            if (player.grounded) player.state = Player.State.Walking;
            player.facesRight = true;
        }

//        if (Gdx.input.isKeyJustPressed(Keys.B))
//            debug = !debug;

        // apply gravity if we are falling
        player.velocity.add(0, GRAVITY);

        // clamp the velocity to the maximum, x-axis only
        player.velocity.x = MathUtils.clamp(player.velocity.x,
            -Player.MAX_VELOCITY, Player.MAX_VELOCITY);

        // If the velocity is < 1, set it to 0 and set state to Standing
        if (Math.abs(player.velocity.x) < 1) {
            player.velocity.x = 0;
            if (player.grounded) player.state = Player.State.Standing;
        }

        // multiply by delta time so we know how far we go
        // in this frame
        player.velocity.scl(deltaTime);

        // perform collision detection & response, on each axis, separately
        // if the player is moving right, check the tiles to the right of it's
        // right bounding box edge, otherwise check the ones to the left
        Rectangle playerRect = rectPool.obtain();

        playerRect.set(player.position.x, player.position.y, Player.WIDTH, Player.HEIGHT);

        int startX, startY, endX, endY;

        if (player.velocity.x > 0) {
            startX = endX = (int)(player.position.x + Player.WIDTH + player.velocity.x);
        } else {
            startX = endX = (int)(player.position.x + player.velocity.x);
        }

        startY = (int)(player.position.y);
        endY = (int)(player.position.y + Player.HEIGHT);
        getTiles(startX, startY, endX, endY, tiles);
        getTiles(startX, startY, endX, endY, tiles);
        playerRect.x += player.velocity.x;

        for (Rectangle tile : tiles) {
            if (playerRect.overlaps(tile)) {
                player.velocity.x = 0;
                break;
            }
        }
        playerRect.x = player.position.x;

        // if the player is moving upwards, check the tiles to the top of its
        // top bounding box edge, otherwise check the ones to the bottom
        if (player.velocity.y > 0) {
            startY = endY = (int)(player.position.y + Player.HEIGHT + player.velocity.y);
        } else {
            startY = endY = (int)(player.position.y + player.velocity.y);
        }
        startX = (int)(player.position.x);
        endX = (int)(player.position.x + Player.WIDTH);
        getTiles(startX, startY, endX, endY, tiles);
        playerRect.y += player.velocity.y;

        for (Rectangle tile : tiles) {
            if (playerRect.overlaps(tile)) {
                if (player.velocity.y > 0) {
                    player.position.y = tile.y - Player.HEIGHT;
                } else {
                    player.position.y = tile.y + tile.height;
                    player.grounded = true;
                }
                player.velocity.y = 0;
                break;
            }
        }

// 🔥 Check for collision with Fire
//        Rectangle fireRect = new Rectangle(fire.getX(), fire.getY(), Fire.WIDTH, Fire.HEIGHT);

        if (playerRect.overlaps(fire.getBoundingBox())) {
            restartGame();  // Call restart function when touching fire
        }
        if (playerRect.overlaps(fire1.getBoundingBox())) {
            restartGame();  // Call restart function when touching fire
        }
        if (playerRect.overlaps(fire2.getBoundingBox())) {
            restartGame();  // Call restart function when touching fire
        }
        if (playerRect.overlaps(fire3.getBoundingBox())) {
            restartGame();  // Call restart function when touching fire
        }
        if (playerRect.overlaps(fire4.getBoundingBox())) {
            restartGame();  // Call restart function when touching fire
        }
        if (playerRect.overlaps(coin.getBoundingBox())) {
            restartGame();
        }

        rectPool.free(playerRect);

        if (player.position.y < 0){
            restartGame();
        }
        // unscale the velocity by the inverse delta time and set
        // the latest position
        player.position.add(player.velocity);
        player.velocity.scl(1 / deltaTime);

        // Apply damping to the velocity on the x-axis so we don't
        // walk infinitely once a key was pressed
        player.velocity.x *= Player.DAMPING;
    } //player


    private void getTiles (int startX, int startY, int endX, int endY, Array<Rectangle> tiles) {
        TiledMapTileLayer layer = (TiledMapTileLayer)map.getLayers().get("walls");
        rectPool.freeAll(tiles);
        tiles.clear();
        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                Cell cell = layer.getCell(x, y);
                if (cell != null) {
                    Rectangle rect = rectPool.obtain();
                    rect.set(x, y, 1, 1);
                    tiles.add(rect);
                }
            }
        }
    }//map manager

    private void renderPlayer (float deltaTime) {
        // based on the player state, get the animation frame
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

        // draw the player, depending on the current velocity
        // on the x-axis, draw the player facing either right
        // or left
        Batch batch = renderer.getBatch();
        batch.begin();
        if (player.facesRight) {
            batch.draw(frame, player.position.x, player.position.y, Player.WIDTH, Player.HEIGHT);
        } else {
            batch.draw(frame, player.position.x + Player.WIDTH, player.position.y, -Player.WIDTH, Player.HEIGHT);
        }
        batch.end();
    }//player

    private void renderDebug () {
        debugRenderer.setProjectionMatrix(camera.combined);
        debugRenderer.begin(ShapeType.Line);

        debugRenderer.setColor(Color.RED);
        debugRenderer.rect(player.position.x, player.position.y, Player.WIDTH, Player.HEIGHT);

        debugRenderer.setColor(Color.YELLOW);
        TiledMapTileLayer layer = (TiledMapTileLayer)map.getLayers().get("walls");
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

    private void restartGame() {
        System.out.println("🔥 Player touched fire! Restarting...");
        // Reset Player's position and velocity
        player.position.set(20, 20);  // Adjust starting position
        player.velocity.set(0, 0);
        // If needed, reset other game elements
    }//player



    @Override
    public void dispose () {
        renderer.dispose();
        fire.dispose();
        map.dispose();
    }

    @Override
    public void resume () {
    }


    @Override
    public void resize(int width, int height) {
    }

    @Override
    public void pause() {
    }
}
