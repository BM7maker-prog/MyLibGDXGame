package tar.pog.platformer2.Menu;

import com.badlogic.gdx.*;
import com.badlogic.gdx.graphics.*;
import com.badlogic.gdx.graphics.g2d.*;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;
import tar.pog.platformer2.Helpers.GameScreen;
import tar.pog.platformer2.Main.Main;

public class GameSelectionScreen implements Screen {
    final Main game;
    OrthographicCamera camera;
    Viewport viewport;
    SpriteBatch batch;
    BitmapFont font;
    GlyphLayout layout;

    private static final float VIRTUAL_WIDTH = 800f;
    private static final float VIRTUAL_HEIGHT = 480f;
    private static final int NUM_MAPS = 10; // adjust as needed
    private static final float BUTTON_WIDTH = 120f;
    private static final float BUTTON_HEIGHT = 240f;
    private static final float BUTTON_SPACING = 25f;
    private final Rectangle[] buttonRects = new Rectangle[NUM_MAPS];

    // Horizontal scroll variables
    private float scrollX = 0;
    private float maxScrollX = 0;
    private float lastTouchX = -1;
    private boolean dragging = false;

    // Scrollbar visuals
    private static final float SCROLLBAR_HEIGHT = 16f;
    private static final float SCROLLBAR_Y = 32f;
    private Rectangle scrollbarRect;
    private boolean scrollbarTouched = false;
    private float scrollbarTouchOffset = 0;

    private static Texture white;

    public GameSelectionScreen(final Main game) {
        this.game = game;
        camera = new OrthographicCamera();
        viewport = new FitViewport(VIRTUAL_WIDTH, VIRTUAL_HEIGHT, camera);
        viewport.apply(true);
        camera.position.set(VIRTUAL_WIDTH / 2f, VIRTUAL_HEIGHT / 2f, 0);
        camera.update();
        batch = new SpriteBatch();
        font = new BitmapFont();
        font.setColor(Color.WHITE);
        font.getData().setScale(2f);
        layout = new GlyphLayout();
        setupButtons();
    }

    private void setupButtons() {
        float totalWidth = NUM_MAPS * BUTTON_WIDTH + (NUM_MAPS - 1) * BUTTON_SPACING;
        maxScrollX = Math.max(0, totalWidth - VIRTUAL_WIDTH);
        float startX = 0;
        float y = (VIRTUAL_HEIGHT - BUTTON_HEIGHT) / 2f;
        for (int i = 0; i < NUM_MAPS; i++) {
            float x = startX + i * (BUTTON_WIDTH + BUTTON_SPACING);
            buttonRects[i] = new Rectangle(x, y, BUTTON_WIDTH, BUTTON_HEIGHT);
        }
        float barWidth = (maxScrollX > 0) ? VIRTUAL_WIDTH * (VIRTUAL_WIDTH / totalWidth) : VIRTUAL_WIDTH;
        scrollbarRect = new Rectangle(0, SCROLLBAR_Y, barWidth, SCROLLBAR_HEIGHT);
    }

    @Override
    public void render(float delta) {
        Gdx.gl.glClearColor(0, 0.15f, 0.15f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        handleTouchScroll();

        viewport.apply();
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        layout.setText(font, "Select Map");
        font.setColor(Color.WHITE); // Always reset before drawing text
        font.draw(batch, layout, (VIRTUAL_WIDTH - layout.width) / 2, VIRTUAL_HEIGHT - 80);

        // Draw buttons (with scrollX offset)
        for (int i = 0; i < NUM_MAPS; i++) {
            Rectangle rect = buttonRects[i];
            float rx = rect.x - scrollX;
            Rectangle drawRect = new Rectangle(rx, rect.y, rect.width, rect.height);
            boolean hovered = drawRect.contains(getInputX(), getInputY()) && !isTouchOnScrollbar();
            batch.setColor(hovered ? Color.DARK_GRAY : Color.GRAY);
            drawRect(drawRect.x, drawRect.y, drawRect.width, drawRect.height);
            batch.setColor(Color.WHITE); // Reset after drawing button bg

            font.setColor(hovered ? Color.YELLOW : Color.WHITE);
            String label = "Map " + (i + 1);
            layout.setText(font, label);
            font.draw(batch, layout,
                drawRect.x + (drawRect.width - layout.width) / 2,
                drawRect.y + drawRect.height / 2 + layout.height / 2
            );
            font.setColor(Color.WHITE); // Reset font color
        }

        // Draw scrollbar
        drawScrollbar();

        batch.end();

        // Button click
        if (Gdx.input.justTouched() && !dragging && !isTouchOnScrollbar()) {
            float inputX = getInputX() + scrollX;
            float inputY = getInputY();
            for (int i = 0; i < NUM_MAPS; i++) {
                if (buttonRects[i].contains(inputX, inputY)) {
                    String mapFile;
                    if (i == 1) {
                        mapFile = "level2.tmx"; // Assign level2.tmx to Map 2 button
                    } else if (i == 2) {
                        mapFile = "level3.tmx"; // Assign level3.tmx to Map 3 button
                    } else if (i == 3) {
                        mapFile = "level4.tmx"; // Assign level3.tmx to Map 3 button
                    }else if (i == 4) {
                        mapFile = "level5.tmx"; // Assign level3.tmx to Map 3 button
                    }else if (i == 5 ) {
                        mapFile = "level6.tmx"; // Assign level3.tmx to Map 3 button
                    }else {
                        mapFile = "level" + (i + 1) + ".tmx";
                    }
                    game.setScreen(new GameScreen(game, mapFile));
                    dispose();
                    return;
                }
            }
        }
    }

    private void handleTouchScroll() {
        if (maxScrollX <= 0) return;

        // Touch handling for scrollbar (mobile friendly)
        if (Gdx.input.isTouched()) {
            float tx = getInputX();
            float ty = getInputY();

            // If just started touch and on scrollbar, capture scrollbar drag
            if (!dragging && Gdx.input.justTouched() && isTouchOnScrollbar()) {
                scrollbarTouched = true;
                scrollbarTouchOffset = tx - scrollbarRect.x;
            }

            if (scrollbarTouched) {
                // Move scrollbar bar and update scrollX accordingly
                float barWidth = scrollbarRect.width;
                float newBarX = tx - scrollbarTouchOffset;
                newBarX = Math.max(0, Math.min(newBarX, VIRTUAL_WIDTH - barWidth));
                scrollbarRect.x = newBarX;
                scrollX = maxScrollX * (scrollbarRect.x / (VIRTUAL_WIDTH - barWidth));
            } else {
                // Button area scroll by dragging
                if (!dragging) {
                    lastTouchX = tx;
                    dragging = false;
                } else if (lastTouchX >= 0) {
                    float deltaX = lastTouchX - tx;
                    if (Math.abs(deltaX) > 3) dragging = true;
                    scrollX += deltaX;
                    scrollX = Math.max(0, Math.min(scrollX, maxScrollX));
                    lastTouchX = tx;
                }
            }
        } else {
            lastTouchX = -1;
            dragging = false;
            scrollbarTouched = false;
        }

        // Update scrollbar thumb position based on scrollX if not dragging bar
        if (!scrollbarTouched && maxScrollX > 0) {
            float barWidth = scrollbarRect.width;
            scrollbarRect.x = (scrollX / maxScrollX) * (VIRTUAL_WIDTH - barWidth);
        }
    }

    private void drawScrollbar() {
        if (maxScrollX <= 0) return;
        // Scrollbar background
        batch.setColor(new Color(0.2f, 0.2f, 0.2f, 0.4f));
        drawRect(0, SCROLLBAR_Y, VIRTUAL_WIDTH, SCROLLBAR_HEIGHT);
        // Scrollbar thumb/bar
        batch.setColor(scrollbarTouched ? Color.YELLOW : Color.LIGHT_GRAY);
        drawRect(scrollbarRect.x, scrollbarRect.y, scrollbarRect.width, scrollbarRect.height);
        batch.setColor(Color.WHITE); // Reset batch color
    }

    private boolean isTouchOnScrollbar() {
        if (maxScrollX <= 0) return false;
        float tx = getInputX();
        float ty = getInputY();
        return scrollbarRect.contains(tx, ty);
    }

    private float getInputX() {
        return Gdx.input.getX() * (VIRTUAL_WIDTH / (float)Gdx.graphics.getWidth());
    }
    private float getInputY() {
        return VIRTUAL_HEIGHT - Gdx.input.getY() * (VIRTUAL_HEIGHT / (float)Gdx.graphics.getHeight());
    }

    private void drawRect(float x, float y, float w, float h) {
        if (white == null) {
            Pixmap pix = new Pixmap(1, 1, Pixmap.Format.RGBA8888);
            pix.setColor(Color.WHITE);
            pix.fill();
            white = new Texture(pix);
            pix.dispose();
        }
        batch.draw(white, x, y, w, h);
    }

    @Override
    public void resize(int width, int height) {
        viewport.update(width, height, true);
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
