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
    private float buttonSize = 220f; // Size in pixels
    private float padding = 60f;

    public TouchInputHandler() {
        // Load textures with error handling
        buttonRight = new Sprite(new Texture("img/buttons/right.png"));
        buttonLeft = new Sprite(new Texture("img/buttons/left.png"));
        buttonUpward = new Sprite(new Texture("img/buttons/up.png"));

        // Set button sizes
        buttonRight.setSize(buttonSize, buttonSize);
        buttonLeft.setSize(buttonSize, buttonSize);
        buttonUpward.setSize(buttonSize, buttonSize);

        // Position buttons (left, right at bottom, jump at top-right)
        buttonLeft.setPosition(padding + 25f, padding);
        buttonRight.setPosition(padding * 2 + buttonSize+25f, padding);
        buttonUpward.setPosition(Gdx.graphics.getWidth() - buttonSize - padding -90f,
            padding);
    }

    public void render(Batch batch) {
        // Draw buttons with transparency when pressed
        float alpha = 1f;
        if (isLeftButtonTouched()) {
            alpha = 0.7f;
            buttonLeft.setAlpha(alpha);
        } else {
            buttonLeft.setAlpha(1f);
        }
        buttonLeft.draw(batch);

        if (isRightButtonTouched()) {
            alpha = 0.7f;
            buttonRight.setAlpha(alpha);
        } else {
            buttonRight.setAlpha(1f);
        }
        buttonRight.draw(batch);

        if (isUpwardButtonTouched()) {
            alpha = 0.7f;
            buttonUpward.setAlpha(alpha);
        } else {
            buttonUpward.setAlpha(1f);
        }
        buttonUpward.draw(batch);
    }

    public boolean isRightButtonTouched() {
        return checkButtonTouch(buttonRight);
    }

    public boolean isLeftButtonTouched() {
        return checkButtonTouch(buttonLeft);
    }

    public boolean isUpwardButtonTouched() {
        return checkButtonTouch(buttonUpward);
    }

    private boolean checkButtonTouch(Sprite button) {
        for (int i = 0; i < 5; i++) { // Check up to 5 simultaneous touches
            if (Gdx.input.isTouched(i)) {
                float x = Gdx.input.getX(i);
                float y = Gdx.graphics.getHeight() - Gdx.input.getY(i); // Convert to screen coordinates
                if (button.getBoundingRectangle().contains(x, y)) {
                    return true;
                }
            }
        }
        return false;
    }
}
