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
    private static final float RESTART_BUTTON_Y = 220f;
    private static final float MENU_BUTTON_Y = 120f;

    private Rectangle mainMenuButtonRect;
    private Rectangle restartButtonRect;

    // Texture for drawing button background
    private static Texture white;

    // Store the map file that was active when the player died
    private final String mapFile;

    // Constructor now takes current map file as argument
    public GameOverScreen(final Main game, String mapFile) {
        this.game = game;
        this.mapFile = mapFile != null ? mapFile : "level1.tmx";

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

        mainMenuButtonRect = new Rectangle(BUTTON_X, MENU_BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT);
        restartButtonRect = new Rectangle(BUTTON_X, RESTART_BUTTON_Y, BUTTON_WIDTH, BUTTON_HEIGHT);
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
        batch.end();

        // Draw the Restart button
        drawRect(restartButtonRect.x, restartButtonRect.y, restartButtonRect.width, restartButtonRect.height,
            isMouseOverRestart() ? Color.DARK_GRAY : Color.GRAY);

        // Draw the Main Menu button
        drawRect(mainMenuButtonRect.x, mainMenuButtonRect.y, mainMenuButtonRect.width, mainMenuButtonRect.height,
            isMouseOverMenu() ? Color.DARK_GRAY : Color.GRAY);

        batch.begin();
        // Restart Button Text
        font.setColor(isMouseOverRestart() ? Color.YELLOW : Color.WHITE);
        layout.setText(font, "Restart");
        font.draw(batch, layout,
            restartButtonRect.x + (restartButtonRect.width - layout.width) / 2,
            restartButtonRect.y + (restartButtonRect.height + layout.height) / 2 - 8
        );

        // Main Menu Button Text
        font.setColor(isMouseOverMenu() ? Color.YELLOW : Color.WHITE);
        layout.setText(font, "Main Menu");
        font.draw(batch, layout,
            mainMenuButtonRect.x + (mainMenuButtonRect.width - layout.width) / 2,
            mainMenuButtonRect.y + (mainMenuButtonRect.height + layout.height) / 2 - 8
        );
        font.setColor(Color.RED); // Reset for "GAME OVER"
        batch.end();

        // Input handling
        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            game.setScreen(new GameScreen(game, mapFile)); // Restart map
            dispose();
            return;
        }

        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            Gdx.app.exit();
            return;
        }

        if (Gdx.input.justTouched()) {
            float mouseX = getInputX();
            float mouseY = getInputY();
            if (restartButtonRect.contains(mouseX, mouseY)) {
                game.setScreen(new GameScreen(game, mapFile));
                dispose();
                return;
            } else if (mainMenuButtonRect.contains(mouseX, mouseY)) {
                game.setScreen(new MainMenuScreen(game));
                dispose();
                return;
            }
        }
    }

    private boolean isMouseOverRestart() {
        float mouseX = getInputX();
        float mouseY = getInputY();
        return restartButtonRect.contains(mouseX, mouseY);
    }

    private boolean isMouseOverMenu() {
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
