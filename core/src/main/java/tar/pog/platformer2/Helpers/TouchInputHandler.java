package tar.pog.platformer2.Helpers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;

public class TouchInputHandler {
    private Sprite buttonRight;
    private Sprite buttonLeft;
    private Sprite buttonUpward;

    public TouchInputHandler() {
        buttonRight = new Sprite(new Texture("button_to_right.png"));
        buttonLeft = new Sprite(new Texture("button_to_left.png"));
        buttonUpward = new Sprite(new Texture("button_upward.png"));

        // Set button positions and sizes
        buttonRight.setPosition(Gdx.graphics.getWidth() - buttonRight.getWidth() - 20, 20);
        buttonLeft.setPosition(20, 20);
        buttonUpward.setPosition(Gdx.graphics.getWidth() / 2 - buttonUpward.getWidth() / 2, Gdx.graphics.getHeight() - buttonUpward.getHeight() - 20);
    }

    public void render(Batch batch) {
        buttonRight.draw(batch);
        buttonLeft.draw(batch);
        buttonUpward.draw(batch);
    }

    public boolean isTouched(float startX, float endX) {
        for (int i = 0; i < 2; i++) {
            float x = Gdx.input.getX(i) / (float) Gdx.graphics.getBackBufferWidth();
            if (Gdx.input.isTouched(i) && (x >= startX && x <= endX)) {
                return true;
            }
        }
        return false;
    }

    public boolean isRightButtonTouched() {
        for (int i = 0; i < 2; i++) {
            if (Gdx.input.isTouched(i)) {
                float x = Gdx.input.getX(i);
                float y = Gdx.graphics.getHeight() - Gdx.input.getY(i); // Convert to screen coordinates
                if (buttonRight.getBoundingRectangle().contains(x, y)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isLeftButtonTouched() {
        for (int i = 0; i < 2; i++) {
            if (Gdx.input.isTouched(i)) {
                float x = Gdx.input.getX(i);
                float y = Gdx.graphics.getHeight() - Gdx.input.getY(i); // Convert to screen coordinates
                if (buttonLeft.getBoundingRectangle().contains(x, y)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean isUpwardButtonTouched() {
        for (int i = 0; i < 2; i++) {
            if (Gdx.input.isTouched(i)) {
                float x = Gdx.input.getX(i);
                float y = Gdx.graphics.getHeight() - Gdx.input.getY(i); // Convert to screen coordinates
                if (buttonUpward.getBoundingRectangle().contains(x, y)) {
                    return true;
                }
            }
        }
        return false;
    }
}
