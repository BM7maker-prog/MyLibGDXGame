package tar.pog.platformer2.Menu;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;

import tar.pog.platformer2.Helpers.GameScreen;
import tar.pog.platformer2.Main.Main;

public class GameOverScreen implements Screen {

    final Main game;
    OrthographicCamera camera;
    SpriteBatch batch;
    BitmapFont font;
    GlyphLayout layout;

    public GameOverScreen(final Main game) {
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
        System.out.println("GameOverScreen rendering..."); // Debug log

        Gdx.gl.glClearColor(0.6f, 0, 0, 1); // Dark red background
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        layout.setText(font, "Game Over");
        font.draw(batch, layout, (800 - layout.width) / 2, 350);

        layout.setText(font, "Touch the screen to Retry");
        font.draw(batch, layout, (800 - layout.width) / 2, 250);

//        layout.setText(font, "Press M to Return to Main Menu");
//        font.draw(batch, layout, (800 - layout.width) / 2, 200);
        batch.end();

        // Retry logic
        if (Gdx.input.justTouched()) {
            System.out.println("Touch the screen - restarting game");
            game.setScreen(new GameScreen(game)); // Restart the game
            dispose();
        }

//        // Return to main menu
//        if (Gdx.input.isKeyJustPressed(Input.Keys.M)) {
//            System.out.println("M key pressed - returning to MainMenuScreen");
//            game.setScreen(new MainMenuScreen(game)); // Go to the main menu
//            dispose();
//        }
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
