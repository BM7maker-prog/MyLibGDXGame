package tar.pog.platformer2;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle; // ✅ Import Rectangle for hitbox
import com.badlogic.gdx.utils.Disposable;

public class Fire implements Disposable {
    private Texture fireTexture;
    private Animation<TextureRegion> fireAnimation;
    private float stateTime;
    private float x, y; // Fire position
    private float width, height; // Fire dimensions

    public Fire(float x, float y) {
        this.x = x;
        this.y = y;

        // Load fire sprite sheet
        fireTexture = new Texture("burning_loop_1.png");

        // Ensure the texture is correctly divided
        int frameCols = 8; // Number of columns in sprite sheet
        int frameRows = 1; // Assuming only 1 row for simplicity
        int frameWidth = fireTexture.getWidth() / frameCols;
        int frameHeight = fireTexture.getHeight() / frameRows; // Adjusted to handle rows

        // Set width & height (🔥 Fix: Updated to match actual frame dimensions)
        this.width = frameWidth;
        this.height = frameHeight;

        // Split sprite sheet into frames
        TextureRegion[][] tmp = TextureRegion.split(fireTexture, frameWidth, frameHeight);
        TextureRegion[] fireFrames = new TextureRegion[frameCols]; // Only using the first row, so frameCols is sufficient

        for (int i = 0; i < frameCols; i++) {
            fireFrames[i] = tmp[0][i]; // Assuming 1 row, 8 columns
        }

        // Create animation
        fireAnimation = new Animation<>(0.1f, fireFrames);
        fireAnimation.setPlayMode(Animation.PlayMode.LOOP);
        stateTime = 0f;
    }

    // Update fire animation
    public void updateFire(float deltaTime) {
        stateTime += deltaTime;
    }

    // Render the fire animation
    public void renderFire(Batch batch) {
        // Add scaling factor (e.g., scale by 0.5 to make it smaller)
        float scaleX = 0.1f; // Scale factor for width
        float scaleY = 0.1f; // Scale factor for height

        batch.draw(fireAnimation.getKeyFrame(stateTime, true), x, y, width, height, width, height, scaleX, scaleY, 0f);
    }


    // Get Fire's hitbox for collision detection
    public Rectangle getBoundingBox() {
        return new Rectangle(x, y, width, height);
    }

    public float getHeight() { return height; }
    public float getWidth() { return width; }

    // Dispose resources
    @Override
    public void dispose() {
        if (fireTexture != null) {
            fireTexture.dispose();
        }
    }
}
