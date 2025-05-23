package tar.pog.platformer2.Menu;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import tar.pog.platformer2.Main.Main;

public class MainMenuScreen implements Screen {

    final Main game;
    OrthographicCamera camera;
    Viewport viewport; // Use viewport for consistent scaling
    SpriteBatch batch;
    BitmapFont font;
    GlyphLayout layout;

    // Fixed virtual size constants for UI elements (matching UI camera)
    private static final float VIRTUAL_WIDTH = 800f;
    private static final float VIRTUAL_HEIGHT = 480f;

    public MainMenuScreen(final Main game) {
        this.game = game;

        // Initialize camera and viewport
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera); // Fixed virtual size for UI
        viewport.apply(true); // Center the camera
        camera.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0);
        camera.update();

        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(2f);
        layout = new GlyphLayout();
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0.3f, 0.4f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        // Apply viewport before updating camera and rendering
        viewport.apply();
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        layout.setText(font, "BraveHeart's Trail");
        font.draw(batch, layout, (VIRTUAL_WIDTH - layout.width) / 2, 350);

        layout.setText(font, "Tap or Press Enter to Start");
        font.draw(batch, layout, (VIRTUAL_WIDTH - layout.width) / 2, 250);
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER) || Gdx.input.justTouched()) {
            game.setScreen(new GameSelectionScreen(game)); // Switch to GameSelectionScreen
            dispose();
        }
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true); // Keep UI centered on resize
        camera.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0);
        camera.update();
    }

    @Override
    public void pause() {}

    @Override
    public void resume() {}

    @Override
    public void hide() {}

    @Override
    public void show() {}

    @Override
    public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
