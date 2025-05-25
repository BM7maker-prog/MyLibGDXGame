package tar.pog.platformer2.Rewards;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Disposable;

public class Liquor implements Disposable {
    private Texture liquorTexture;
    private TextureRegion liquorRegion;
    private float x, y;
    private float width, height;
    private boolean collected = false;
    private float scale = 1.6f;
    public Liquor(float x, float y) {
        this.x = x;
        this.y = y;
        this.liquorTexture = new Texture("img/Rewards/liquor.png"); // Provide the correct path!
        this.liquorRegion = new TextureRegion(liquorTexture);
        this.width = liquorTexture.getWidth() / 16f * scale;
        this.height = liquorTexture.getHeight() / 16f * scale;
    }

    public void render(Batch batch) {
        if (!collected) {
            batch.draw(liquorRegion, x, y, width, height);
        }
    }

    public Rectangle getBoundingBox() {
        return new Rectangle(x, y, width, height);
    }

    public boolean isCollected() {
        return collected;
    }

    public void collect() {
        collected = true;
    }

    public void reset() {
        collected = false;
    }

    @Override
    public void dispose() {
        if (liquorTexture != null) {
            liquorTexture.dispose();
        }
    }
}
