package tar.pog.platformer2;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputAdapter;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
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
    private Texture koalaTexture;
    private Animation<TextureRegion> stand;
    private Animation<TextureRegion> walk;
    private Animation<TextureRegion> jump;
    private Koala koala;
    private Fire fire;
    private Coin coin;


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
        // load the koala frames, split them, and assign them to Animations
        koalaTexture = new Texture("koalio.png");
        TextureRegion[] regions = TextureRegion.split(koalaTexture, 18, 26)[0];
        stand = new Animation<TextureRegion>(0, regions[0]);
        jump = new Animation<TextureRegion>(0, regions[1]);
        walk = new Animation<TextureRegion>(0.15f, regions[2], regions[3], regions[4]);
        walk.setPlayMode(Animation.PlayMode.LOOP_PINGPONG);

        // figure out the width and height of the koala for collision
        // detection and rendering by converting a koala frames pixel
        // size into world units (1 unit == 16 pixels)
        Koala.WIDTH = 1 / 16f * regions[0].getRegionWidth();
        Koala.HEIGHT = 1 / 16f * regions[0].getRegionHeight();

        // load the map, set the unit scale to 1/16 (1 unit == 16 pixels)
        map = new TmxMapLoader().load("level1.tmx");
        renderer = new OrthogonalTiledMapRenderer(map, 1 / 16f);

        // create an orthographic camera, shows us 30x20 units of the world
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 30, 20);
        camera.update();

        // create the Koala we want to move around the world
        koala = new Koala();
        koala.position.set(20, 20);

        coin = new Coin(koala.position.x + 20,koala.position.y - 32);

        fire = new Fire(koala.position.x + 30,koala.position.y - 47);

        debugRenderer = new ShapeRenderer();


    }


    @Override
    public void render () {
        // clear the screen
        ScreenUtils.clear(0.5f, 0.7f, 1, 1);


        // get the delta time
        float deltaTime = Gdx.graphics.getDeltaTime();

        // update the koala (process input, collision detection, position update)
        updateKoala(deltaTime);

        // let the camera follow the koala, x-axis only
        camera.position.x = koala.position.x;
        camera.update();

        // set the TiledMapRenderer view based on what the
        // camera sees, and render the map
        renderer.setView(camera);
        renderer.render();

        // render the koala
        renderKoala(deltaTime);

        Batch batch = renderer.getBatch();

        coin.updateCoin(deltaTime);
        fire.updateFire(deltaTime);

        batch.begin();

        coin.renderCoin(batch);
        fire.renderFire(batch);

        batch.end();
        // render debug rectangles
        if (debug) renderDebug();
    }

    public void updateKoala (float deltaTime) {
        if (deltaTime == 0) return;

        if (deltaTime > 0.1f)
            deltaTime = 0.1f;

        koala.stateTime += deltaTime;

        // check input and apply to velocity & state
        //jumping
        if (isTouched(0.5f, 1) && koala.grounded) {
            koala.velocity.y += Koala.JUMP_VELOCITY;
            koala.state = Koala.State.Jumping;
            koala.grounded = false;
        }
        // turning to the left
        if (isTouched(0, 0.25f)) {
            koala.velocity.x = -Koala.MAX_VELOCITY;
            if (koala.grounded) koala.state = Koala.State.Walking;
            koala.facesRight = false;
        }
        //turning to the right
        if (isTouched(0.25f, 0.5f)) {
            koala.velocity.x = Koala.MAX_VELOCITY;
            if (koala.grounded) koala.state = Koala.State.Walking;
            koala.facesRight = true;
        }

//        if (Gdx.input.isKeyJustPressed(Keys.B))
//            debug = !debug;

        // apply gravity if we are falling
        koala.velocity.add(0, GRAVITY);

        // clamp the velocity to the maximum, x-axis only
        koala.velocity.x = MathUtils.clamp(koala.velocity.x,
            -Koala.MAX_VELOCITY, Koala.MAX_VELOCITY);

        // If the velocity is < 1, set it to 0 and set state to Standing
        if (Math.abs(koala.velocity.x) < 1) {
            koala.velocity.x = 0;
            if (koala.grounded) koala.state = Koala.State.Standing;
        }

        // multiply by delta time so we know how far we go
        // in this frame
        koala.velocity.scl(deltaTime);

        // perform collision detection & response, on each axis, separately
        // if the koala is moving right, check the tiles to the right of it's
        // right bounding box edge, otherwise check the ones to the left
        Rectangle koalaRect = rectPool.obtain();

        koalaRect.set(koala.position.x, koala.position.y, Koala.WIDTH, Koala.HEIGHT);

        int startX, startY, endX, endY;

        if (koala.velocity.x > 0) {
            startX = endX = (int)(koala.position.x + Koala.WIDTH + koala.velocity.x);
        } else {
            startX = endX = (int)(koala.position.x + koala.velocity.x);
        }

        startY = (int)(koala.position.y);
        endY = (int)(koala.position.y + Koala.HEIGHT);
        getTiles(startX, startY, endX, endY, tiles);
        koalaRect.x += koala.velocity.x;

        for (Rectangle tile : tiles) {
            if (koalaRect.overlaps(tile)) {
                koala.velocity.x = 0;
                break;
            }
        }
        koalaRect.x = koala.position.x;

        // if the koala is moving upwards, check the tiles to the top of its
        // top bounding box edge, otherwise check the ones to the bottom
        if (koala.velocity.y > 0) {
            startY = endY = (int)(koala.position.y + Koala.HEIGHT + koala.velocity.y);
        } else {
            startY = endY = (int)(koala.position.y + koala.velocity.y);
        }
        startX = (int)(koala.position.x);
        endX = (int)(koala.position.x + Koala.WIDTH);
        getTiles(startX, startY, endX, endY, tiles);
        koalaRect.y += koala.velocity.y;

        for (Rectangle tile : tiles) {
            if (koalaRect.overlaps(tile)) {
                if (koala.velocity.y > 0) {
                    koala.position.y = tile.y - Koala.HEIGHT;
                } else {
                    koala.position.y = tile.y + tile.height;
                    koala.grounded = true;
                }
                koala.velocity.y = 0;
                break;
            }
        }

// 🔥 Check for collision with Fire
//        Rectangle fireRect = new Rectangle(fire.getX(), fire.getY(), Fire.WIDTH, Fire.HEIGHT);

        if (koalaRect.overlaps(fire.getBoundingBox())) {
            restartGame();  // Call restart function when touching fire
        }
//        if (koalaRect.overlaps(coin.getBoundingBox())) {
//            restartGame();
//        }
        rectPool.free(koalaRect);

        if (koala.position.y < 0){
            restartGame();
        }
        // unscale the velocity by the inverse delta time and set
        // the latest position
        koala.position.add(koala.velocity);
        koala.velocity.scl(1 / deltaTime);

        // Apply damping to the velocity on the x-axis so we don't
        // walk infinitely once a key was pressed
        koala.velocity.x *= Koala.DAMPING;
    }

    private boolean isTouched (float startX, float endX) {
        // Check for touch inputs between startX and endX
        // startX/endX are given between 0 (left edge of the screen) and 1 (right edge of the screen)
        for (int i = 0; i < 2; i++) {
            float x = Gdx.input.getX(i) / (float)Gdx.graphics.getBackBufferWidth();
            if (Gdx.input.isTouched(i) && (x >= startX && x <= endX)) {
                return true;
            }
        }
        return false;
    }

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
    }

    private void renderKoala (float deltaTime) {
        // based on the koala state, get the animation frame
        TextureRegion frame = null;
        switch (koala.state) {
            case Standing:
                frame = stand.getKeyFrame(koala.stateTime);
                break;
            case Walking:
                frame = walk.getKeyFrame(koala.stateTime);
                break;
            case Jumping:
                frame = jump.getKeyFrame(koala.stateTime);
                break;
        }

        // draw the koala, depending on the current velocity
        // on the x-axis, draw the koala facing either right
        // or left
        Batch batch = renderer.getBatch();
        batch.begin();
        if (koala.facesRight) {
            batch.draw(frame, koala.position.x, koala.position.y, Koala.WIDTH, Koala.HEIGHT);
        } else {
            batch.draw(frame, koala.position.x + Koala.WIDTH, koala.position.y, -Koala.WIDTH, Koala.HEIGHT);
        }
        batch.end();
    }

    private void renderDebug () {
        debugRenderer.setProjectionMatrix(camera.combined);
        debugRenderer.begin(ShapeType.Line);

        debugRenderer.setColor(Color.RED);
        debugRenderer.rect(koala.position.x, koala.position.y, Koala.WIDTH, Koala.HEIGHT);

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
        System.out.println("🔥 Koala touched fire! Restarting...");
        // Reset Koala's position and velocity
        koala.position.set(20, 20);  // Adjust starting position
        koala.velocity.set(0, 0);
        // If needed, reset other game elements
    }


    @Override
    public void dispose () {
        renderer.dispose();
        fire.dispose();
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
