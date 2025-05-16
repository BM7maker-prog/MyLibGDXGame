package tar.pog.platformer2.Helpers;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;
import com.badlogic.gdx.math.Vector3;

public class TouchInputHandler {
    private final Sprite buttonRight;
    private final Sprite buttonLeft;
    private final Sprite buttonUpward;

    private float buttonSize; // Size in virtual units
    private float padding; // Padding in virtual units
    private float buttonSpacing; // Distance between buttons in virtual units
    private final Camera uiCamera;
    private final float touchedAlpha; // Alpha when button is touched
    private final float untouchedAlpha; // Alpha when button is not touched

    // Virtual viewport dimensions
    private static final float VIRTUAL_WIDTH = 1050f;
    private static final float VIRTUAL_HEIGHT = 480f;
    private static final int MAX_TOUCH_POINTS = 10; // Maximum touch points to check

    public TouchInputHandler(Camera uiCamera) {
        if (uiCamera == null) {
            throw new IllegalArgumentException("UI Camera cannot be null");
        }
        this.uiCamera = uiCamera;
        this.touchedAlpha = 0.7f;
        this.untouchedAlpha = 1.0f;

        // Initialize button textures with linear filtering
        try {
            Texture rightTexture = new Texture("img/buttons/right.png");
            rightTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            buttonRight = new Sprite(rightTexture);

            Texture leftTexture = new Texture("img/buttons/left.png");
            leftTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            buttonLeft = new Sprite(leftTexture);

            Texture upTexture = new Texture("img/buttons/up.png");
            upTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
            buttonUpward = new Sprite(upTexture);
        } catch (Exception e) {
            throw new RuntimeException("Failed to load button textures: " + e.getMessage(), e);
        }

        // Calculate button layout
        recalculateButtonLayout();
    }

    private void recalculateButtonLayout() {
        // Calculate button size and spacing based on virtual height
        buttonSize = VIRTUAL_HEIGHT * 0.20f; // 20% of virtual height (96 units)
        padding = VIRTUAL_HEIGHT * 0.04f; // 4% of virtual height (19.2 units)
        buttonSpacing = VIRTUAL_HEIGHT * 0.02f; // 2% of virtual height (9.6 units)

        // Scale buttons to the calculated size
        buttonRight.setSize(buttonSize, buttonSize);
        buttonLeft.setSize(buttonSize, buttonSize);
        buttonUpward.setSize(buttonSize, buttonSize);

        // Position buttons in virtual coordinates
        float buttonY = padding;
        buttonLeft.setPosition(padding, buttonY);
        buttonRight.setPosition(buttonLeft.getX() + buttonSize + buttonSpacing, buttonY);

        // Move jump button left by an offset, keeping it at same elevation as others
        float jumpButtonOffset = 1.5f * (buttonSize + buttonSpacing); // Move left by 1.5 * button+spacing
        buttonUpward.setPosition(
            VIRTUAL_WIDTH - buttonSize  + 25 + padding - jumpButtonOffset,
            buttonY
        );
    }

    public void render(Batch batch) {
        // Render buttons with appropriate transparency
        buttonLeft.setAlpha(isLeftButtonTouched() ? touchedAlpha : untouchedAlpha);
        buttonLeft.draw(batch);

        buttonRight.setAlpha(isRightButtonTouched() ? touchedAlpha : untouchedAlpha);
        buttonRight.draw(batch);

        buttonUpward.setAlpha(isUpwardButtonTouched() ? touchedAlpha : untouchedAlpha);
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
        for (int i = 0; i < MAX_TOUCH_POINTS; i++) {
            if (Gdx.input.isTouched(i)) {
                touchPos.set(Gdx.input.getX(i), Gdx.input.getY(i), 0);
                uiCamera.unproject(touchPos); // Convert to virtual coordinates
                if (button.getBoundingRectangle().contains(touchPos.x, touchPos.y)) {
                    return true; // Early exit on first touch detected
                }
            }
        }
        return false;
    }

    public void resize(int screenWidth, int screenHeight) {
        // No recalculation needed since layout uses virtual coordinates
    }

    public void dispose() {
        // Dispose textures owned by the sprites
        buttonRight.getTexture().dispose();
        buttonLeft.getTexture().dispose();
        buttonUpward.getTexture().dispose();
    }
}
