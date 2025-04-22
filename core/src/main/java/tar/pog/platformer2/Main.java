package tar.pog.platformer2;

import com.badlogic.gdx.Game;
import com.badlogic.gdx.Gdx;

public class Main extends Game {
    @Override
    public void create() {
        Gdx.app.log("Main", "Game started. Showing MainMenuScreen...");
        setScreen(new MainMenuScreen(this));
    }

    @Override
    public void render() {
        super.render(); // delegates to the current screen (MainMenuScreen or GameScreen)
    }

    @Override
    public void dispose() {
        super.dispose(); // disposes current screen
    }
}
