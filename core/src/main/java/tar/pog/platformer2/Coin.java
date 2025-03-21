package tar.pog.platformer2;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle; // ✅ Import Rectangle for hitbox
import com.badlogic.gdx.utils.Disposable;

public class Coin implements Disposable {
    private Texture coinTexture;
    private Animation<TextureRegion> coinAnimation;
    private float stateTime;
    private float x, y; // Fire position
    private float width, height; // Fire dimensions
    public int coinCount = 0;

    public Coin(float x, float y) {
        this.x = x;
        this.y = y;

        // Load fire sprite sheet
        coinTexture = new Texture("coin_sprite.png");

        // Ensure the texture is correctly divided
        int frameCols = 14; // Number of columns in sprite sheet
        int frameRows = 1; // Assuming only 1 row for simplicity
        int frameWidth = coinTexture.getWidth() / frameCols;
        int frameHeight = coinTexture.getHeight() / frameRows; // Adjusted to handle rows

        // Set width & height (🔥 Fix: Updated to match actual frame dimensions)
        this.width = frameWidth;
        this.height = frameHeight;

        // Split sprite sheet into frames
        TextureRegion[][] tmp = TextureRegion.split(coinTexture, frameWidth, frameHeight);
        TextureRegion[] fireFrames = new TextureRegion[frameCols]; // Only using the first row, so frameCols is sufficient

        for (int i = 0; i < frameCols; i++) {
            fireFrames[i] = tmp[0][i]; // Assuming 1 row, 8 columns
        }

        // Create animation
        coinAnimation = new Animation<>(0.1f, fireFrames);
        coinAnimation.setPlayMode(Animation.PlayMode.LOOP);
        stateTime = 0f;


    }

    // Update fire animation
    public void updateCoin(float deltaTime) {
        stateTime += deltaTime;
    }

    // Render the fire animation
    public void renderCoin(Batch batch) {
        // Add scaling factor (e.g., scale by 0.5 to make it smaller)
        float scaleX = 0.1f; // Scale factor for width
        float scaleY = 0.1f; // Scale factor for height

        batch.draw(coinAnimation.getKeyFrame(stateTime, true), x, y, width, height, width, height, scaleX, scaleY, 0f);
    }


    // Get Fire's hitbox for collision detection
// Get Fire's hitbox for collision detection
    public Rectangle getBoundingBox() {

        return new Rectangle(x, y , width , height);
    }


    public float getHeight() { return height; }
    public float getWidth() { return width; }

    // Dispose resources
    @Override
    public void dispose() {
        if (coinTexture != null) {
            coinTexture.dispose();
        }
    }
}
