package tar.pog.platformer2.Obstacles;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;

public class Fire implements Disposable {
    private Texture fireTexture;
    private Animation<TextureRegion> fireAnimation;
    private float stateTime;
    private float x, y; // Fire position in world units (tile units)
    private float worldWidth, worldHeight; // Fire size in world units

    // Scaling factor: tile size in world units (e.g., 1.0 = 1 tile)
    // This should match the tile size used in GameScreen's TiledMapTileLayer and renderer
    private static final float TILE_SIZE_IN_WORLD_UNITS = 2.0f; // Each tile = 1 world unit

    // Shrink factor for hitbox (e.g., 0.7 = 70% of original size)
    private static final float HITBOX_SHRINK = 0.5f;

    public Fire(float x, float y) {
        this.x = x;
        this.y = y;

        // Load fire sprite sheet
        fireTexture = new Texture("img/Obstacles/burning_loop_1.png");

        int frameCols = 8;
        int frameRows = 1;
        int frameWidth = fireTexture.getWidth() / frameCols;
        int frameHeight = fireTexture.getHeight() / frameRows;

        // The fire will be drawn to fit exactly 1x1 tile in the map (adjust if you want it bigger/smaller)
        this.worldWidth = TILE_SIZE_IN_WORLD_UNITS;
        this.worldHeight = TILE_SIZE_IN_WORLD_UNITS;

        // Split sprite sheet into frames
        TextureRegion[][] tmp = TextureRegion.split(fireTexture, frameWidth, frameHeight);
        TextureRegion[] fireFrames = new TextureRegion[frameCols];

        for (int i = 0; i < frameCols; i++) {
            fireFrames[i] = tmp[0][i];
        }

        fireAnimation = new Animation<>(0.1f, fireFrames);
        fireAnimation.setPlayMode(Animation.PlayMode.LOOP);
        stateTime = 0f;
    }

    // Update fire animation
    public void updateFire(float deltaTime) {
        stateTime += deltaTime;
    }

    // Render the fire animation at x/y in world coordinates, scaled to worldWidth/worldHeight
    public void renderFire(Batch batch) {
        TextureRegion frame = fireAnimation.getKeyFrame(stateTime, true);

        // Calculate scale from source (pixel) size to world size
        float scaleX = worldWidth / frame.getRegionWidth();
        float scaleY = worldHeight / frame.getRegionHeight();

        // Draw at world position, scaled to fit the tile/grid size
        batch.draw(
            frame,
            x, y,
            0, 0, // origin for scale/rotation is at bottom left
            frame.getRegionWidth(), frame.getRegionHeight(),
            scaleX, scaleY,
            0f
        );
    }

    // Render the hitbox using a ShapeRenderer (call this from your debug rendering code)
    public void renderHitbox(ShapeRenderer shapeRenderer) {
        Rectangle hitbox = getBoundingBox();
        shapeRenderer.rect(hitbox.x, hitbox.y, hitbox.width, hitbox.height);
    }

    // Get Fire's hitbox for collision detection (shrunken version)
    public Rectangle getBoundingBox() {
        float shrinkW = worldWidth * HITBOX_SHRINK;
        float shrinkH = worldHeight * HITBOX_SHRINK;
        float offsetX = (worldWidth - shrinkW) / 2f;
        float offsetY = (worldHeight - shrinkH) / 2f;
        return new Rectangle(x + offsetX, y + offsetY, shrinkW, shrinkH);
    }

    public float getHeight() { return worldHeight; }
    public float getWidth() { return worldWidth; }

    @Override
    public void dispose() {
        if (fireTexture != null) {
            fireTexture.dispose();
        }
    }
}
