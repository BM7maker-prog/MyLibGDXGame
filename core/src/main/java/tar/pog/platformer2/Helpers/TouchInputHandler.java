package tar.pog.platformer2.Helpers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector3;

public class TouchInputHandler {
    private Sprite buttonRight;
    private Sprite buttonLeft;
    private Sprite buttonUpward;
    private float buttonSize; // Size in pixels
    private float padding; // Padding in pixels
    private float buttonSpacing; // Distance between buttons
    private final Camera uiCamera;

    public TouchInputHandler(Camera uiCamera) {
        this.uiCamera = uiCamera;

        // Calculate button size as a percentage of screen width (e.g., 15% of screen width)
        buttonSize = Gdx.graphics.getWidth() * 0.10f; // 15% of the screen width for buttons
        padding = Gdx.graphics.getWidth() * 0.01f; // 5% of screen width for padding

        // Calculate spacing between buttons as a percentage of screen width (e.g., 5% of the screen width)
        buttonSpacing = Gdx.graphics.getWidth() * 0.05f; // 5% of screen width for spacing

        // Load button textures
        buttonRight = new Sprite(new Texture("img/buttons/right.png"));
        buttonLeft = new Sprite(new Texture("img/buttons/left.png"));
        buttonUpward = new Sprite(new Texture("img/buttons/up.png"));

        // Scale the button images according to the buttonSize
        buttonRight.setSize(buttonSize, buttonSize);
        buttonLeft.setSize(buttonSize, buttonSize);
        buttonUpward.setSize(buttonSize, buttonSize);

        // Position buttons based on screen size and buttonSpacing
        buttonLeft.setPosition(padding, padding);
        buttonRight.setPosition(buttonLeft.getX() + buttonSize + buttonSpacing, padding); // Spacing between buttons
        buttonUpward.setPosition(Gdx.graphics.getWidth() - buttonSize - padding, padding);
    }

    public void render(Batch batch) {
        float alpha;

        // Check if the buttons are touched and set their alpha accordingly
        alpha = isLeftButtonTouched() ? 0.7f : 1f;
        buttonLeft.setAlpha(alpha);
        buttonLeft.draw(batch);

        alpha = isRightButtonTouched() ? 0.7f : 1f;
        buttonRight.setAlpha(alpha);
        buttonRight.draw(batch);

        alpha = isUpwardButtonTouched() ? 0.7f : 1f;
        buttonUpward.setAlpha(alpha);
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
        Vector3 touchPos = new Vector3();
        for (int i = 0; i < 5; i++) {
            if (Gdx.input.isTouched(i)) {
                touchPos.set(Gdx.input.getX(i), Gdx.input.getY(i), 0);
                uiCamera.unproject(touchPos); // Convert touch coordinates to world coordinates
                if (button.getBoundingRectangle().contains(touchPos.x, touchPos.y)) {
                    return true;
                }
            }
        }
        return false;
    }
}
