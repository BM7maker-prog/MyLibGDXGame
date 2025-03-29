package tar.pog.platformer2.Helpers;

import com.badlogic.gdx.Gdx;

public class TouchInputHandler {
    public static boolean isTouched(float startX, float endX) {
        for (int i = 0; i < 2; i++) {
            float x = Gdx.input.getX(i) / (float) Gdx.graphics.getBackBufferWidth();
            if (Gdx.input.isTouched(i) && (x >= startX && x <= endX)) {
                return true;
            }
        }
        return false;
    }
}
