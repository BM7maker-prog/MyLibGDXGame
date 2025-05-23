package tar.pog.platformer2.Menu;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import tar.pog.platformer2.Helpers.GameScreen;
import tar.pog.platformer2.Main.Main;

public class GameOverScreen implements Screen {

    final Main game;
    OrthographicCamera camera;
    Viewport viewport; // Use viewport for consistent scaling
    SpriteBatch batch;
    BitmapFont font;
    GlyphLayout layout;

    // Fixed virtual size constants for UI elements (matching UI camera in GameScreen)
    private static final float VIRTUAL_WIDTH = 800f;
    private static final float VIRTUAL_HEIGHT = 480f;

    // Button for returning to Main Menu
    private static final float BUTTON_WIDTH = 300f;
    private static final float BUTTON_HEIGHT = 70f;
    private static final float BUTTON_X = (VIRTUAL_WIDTH - BUTTON_WIDTH) / 2f;
    private static final float BUTTON_Y = 120f;

    private Rectangle mainMenuButtonRect;

    // Texture for drawing button background
    private static Texture white;

    public GameOverScreen(final Main game) {
        this.game = game;

        // Initialize camera and viewport
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera); // Fixed virtual size for UI
        viewport.apply(true); // Center the camera
        camera.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0);
        camera.update();

        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.RED);
        font.getData().setScale(2f);
        layout = new GlyphLayout();

        mainMenuButtonRect = new Rectangle(BUTTON_X, BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0.1f, 0.1f, 0.1f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Apply viewport before updating camera and rendering
        viewport.apply();
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        layout.setText(font, "GAME OVER");
        font.draw(batch, layout, (VIRTUAL_WIDTH - layout.width) / 2, 350);

        layout.setText(font, "Tap to Restart");
        font.draw(batch, layout, (VIRTUAL_WIDTH - layout.width) / 2, 250);
        batch.end();

        // Draw the Main Menu button
        drawRect(mainMenuButtonRect.x, mainMenuButtonRect.y, mainMenuButtonRect.width, mainMenuButtonRect.height,
            isMouseOverButton() ? Color.DARK_GRAY : Color.GRAY);

        batch.begin();
        font.setColor(isMouseOverButton() ? Color.YELLOW : Color.WHITE);
        layout.setText(font, "Main Menu");
        font.draw(batch, layout,
            mainMenuButtonRect.x + (mainMenuButtonRect.width - layout.width) / 2,
            mainMenuButtonRect.y + (mainMenuButtonRect.height + layout.height) / 2 - 8
        );
        font.setColor(Color.RED); // Reset for "GAME OVER"
        batch.end();

        // Input handling
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.setScreen(new MainMenuScreen(game)); // Transition to MainMenuScreen
            dispose();
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
            return;
        }

        // Touch/click handling for Main Menu button
        if (Gdx.input.justTouched()) {
            float mouseX = getInputX();
            float mouseY = getInputY();
            if (mainMenuButtonRect.contains(mouseX, mouseY)) {
                game.setScreen(new MainMenuScreen(game));
                dispose();
                return;
            } else {
                // If not main menu button, restart game
                game.setScreen(new GameScreen(game));
                dispose();
                return;
            }
        }
    }

    private boolean isMouseOverButton() {
        float mouseX = getInputX();
        float mouseY = getInputY();
        return mainMenuButtonRect.contains(mouseX, mouseY);
    }

    private float getInputX() {
        return Gdx.input.getX() * (VIRTUAL_WIDTH / (float)Gdx.graphics.getWidth());
    }

    private float getInputY() {
        return VIRTUAL_HEIGHT - Gdx.input.getY() * (VIRTUAL_HEIGHT / (float)Gdx.graphics.getHeight());
    }

    // Simple rectangle drawing (filled) - requires a 1x1 white texture
    private void drawRect(float x, float y, float w, float h, Color color) {
        if (white == null) {
            Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pix.setColor(Color.WHITE);
            pix.fill();
            white = new Texture(pix);
            pix.dispose();
        }
        batch.begin();
        batch.setColor(color);
        batch.draw(white, x, y, w, h);
        batch.setColor(Color.WHITE);
        batch.end();
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true); // Keep UI centered on resize
        camera.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0);
        camera.update();
    }

    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void show() {}

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
        if (white != null) white.dispose();
    }
}
