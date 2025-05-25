package tar.pog.platformer2.Rewards;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;

public class Coin implements Disposable {
    private Texture coinTexture;
    private Animation<TextureRegion> coinAnimation;
    private float stateTime;
    private float x, y;
    private float width, height;
    public int coinCount = 0;

    public Coin(float x, float y) {
        this.x = x;
        this.y = y;

        coinTexture = new Texture("img/Rewards/coin_sprite.png");
        int frameCols = 14;
        int frameRows = 1;
        int frameWidth = coinTexture.getWidth() / frameCols;
        int frameHeight = coinTexture.getHeight() / frameRows;

        this.width = frameWidth / 16f; // Convert to world units (1 unit = 16 pixels)
        this.height = frameHeight / 16f;

        TextureRegion[][] tmp = TextureRegion.split(coinTexture, frameWidth, frameHeight);
        TextureRegion[] frames = new TextureRegion[frameCols];
        for (int i = 0; i < frameCols; i++) {
            frames[i] = tmp[0][i];
        }

        coinAnimation = new Animation<>(0.1f, frames);
        coinAnimation.setPlayMode(Animation.PlayMode.LOOP);
        stateTime = 0f;
    }

    public void updateCoin(float deltaTime) {
        stateTime += deltaTime;
    }

    public void renderCoin(Batch batch) {
        float scale = 1.2f; // Scale factor
        batch.draw(coinAnimation.getKeyFrame(stateTime, true), x, y, width * scale, height * scale);
    }

    public Rectangle getBoundingBox() {
        float scale = 0.1f; // Match render scale
        return new Rectangle(x, y, width * scale, height * scale);
    }

    public float getHeight() { return height; }
    public float getWidth() { return width; }

    @Override
    public void dispose() {
        if (coinTexture != null) {
            coinTexture.dispose();
        }
    }
}
