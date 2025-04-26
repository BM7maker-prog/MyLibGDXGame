package tar.pog.platformer2.Menu;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;

import tar.pog.platformer2.Helpers.GameScreen;
import tar.pog.platformer2.Main.Main;

public class MainMenuScreen implements Screen {

    final Main game;
    OrthographicCamera camera;
    SpriteBatch batch;
    BitmapFont font;
    GlyphLayout layout;

    public MainMenuScreen(final Main game) {
        this.game = game;
        camera = new OrthographicCamera();
        camera.setToOrtho(false, 800, 480);
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(2f);
        layout = new GlyphLayout();
    }

    @Override
    public void render(float delta) {
        System.out.println("MainMenuScreen rendering..."); // Debug log

        Gdx.gl.glClearColor(0, 0.3f, 0.4f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        layout.setText(font, "BraveHeart's Trail");
        font.draw(batch, layout, (800 - layout.width) / 2, 350);

        layout.setText(font, "Press ENTER or Tap to Start");
        font.draw(batch, layout, (800 - layout.width) / 2, 250);
        batch.end();

        if (Gdx.input.isKeyJustPressed(Input.Keys.ENTER)) {
            System.out.println("Enter key pressed - switching to GameScreen");
            game.setScreen(new GameScreen(game));
            dispose();
        }

        // Fallback for touch input (mobile)
        if (Gdx.input.justTouched()) {
            System.out.println("Screen touched - switching to GameScreen");
            game.setScreen(new GameScreen(game));
            dispose();
        }
    }

    @Override public void resize(int width, int height) {}
    @Override public void pause() {}
    @Override public void resume() {}
    @Override public void hide() {}
    @Override public void show() {}
    @Override public void dispose() {
        batch.dispose();
        font.dispose();
    }
}
