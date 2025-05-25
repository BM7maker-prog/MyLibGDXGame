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
    private static final int NUM_MAPS = 7;
    private static final float BUTTON_WIDTH = 120f;
    private static final float BUTTON_HEIGHT = 240f;
    private static final float BUTTON_SPACING = 30f;
    private final Rectangle[] buttonRects = new Rectangle[NUM_MAPS];

    // ---- CONSTANT COLORS ----
    private static final Color BG_COLOR = new Color(0, 0.15f, 0.15f, 1);
    private static final Color BUTTON_NORMAL = Color.GRAY.cpy();
    private static final Color BUTTON_HOVER = Color.DARK_GRAY.cpy();
    private static final Color BUTTON_TEXT = Color.WHITE.cpy();
    private static final Color BUTTON_TEXT_HOVER = Color.YELLOW.cpy();
    private static final Color BUTTON_DISABLED = Color.DARK_GRAY.cpy();
    private static final Color BUTTON_TEXT_DISABLED = new Color(1f, 1f, 1f, 0.4f);
    private static final Color SCROLLBAR_BG = new Color(0.2f, 0.2f, 0.2f, 0.4f);
    private static final Color SCROLLBAR_THUMB = Color.LIGHT_GRAY.cpy();
    private static final Color SCROLLBAR_THUMB_DRAG = Color.YELLOW.cpy();

    // Horizontal scroll variables
    private float scrollX = 0;
    private float maxScrollX = 0;
    private float lastTouchX = -1;
    private boolean dragging = false;

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
        font.setColor(BUTTON_TEXT);
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
        Gdx.gl.glClearColor(BG_COLOR.r, BG_COLOR.g, BG_COLOR.b, BG_COLOR.a);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        handleTouchScroll();

        viewport.apply();
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        batch.begin();
        layout.setText(font, "Select Map");
        font.setColor(BUTTON_TEXT); // Always reset before drawing text
        font.draw(batch, layout, (VIRTUAL_WIDTH - layout.width) / 2, VIRTUAL_HEIGHT - 80);

        // Draw buttons (with scrollX offset)
        for (int i = 0; i < NUM_MAPS; i++) {
            Rectangle rect = buttonRects[i];
            float rx = rect.x - scrollX;
            Rectangle drawRect = new Rectangle(rx, rect.y, rect.width, rect.height);

            boolean isDisabled = (i == 5 || i == 6); // 6th and 7th buttons disabled (0-based)
            boolean hovered = drawRect.contains(getInputX(), getInputY()) && !isTouchOnScrollbar();

            if (isDisabled) {
                batch.setColor(BUTTON_DISABLED);
            } else {
                batch.setColor(hovered ? BUTTON_HOVER : BUTTON_NORMAL);
            }
            drawRect(drawRect.x, drawRect.y, drawRect.width, drawRect.height);
            batch.setColor(Color.WHITE);

            if (isDisabled) {
                font.setColor(BUTTON_TEXT_DISABLED);
            } else {
                font.setColor(hovered ? BUTTON_TEXT_HOVER : BUTTON_TEXT);
            }
            String label = "Map " + (i + 1);
            if (isDisabled) label += "\nLocked";
            layout.setText(font, label);
            font.draw(batch, layout,
                drawRect.x + (drawRect.width - layout.width) / 2,
                drawRect.y + drawRect.height / 2 + layout.height / 2
            );
            font.setColor(BUTTON_TEXT);
        }

        // Draw scrollbar
        drawScrollbar();

        batch.end();

        // Button click (disable for 6th and 7th)
        if (Gdx.input.justTouched() && !dragging && !isTouchOnScrollbar()) {
            float inputX = getInputX() + scrollX;
            float inputY = getInputY();
            for (int i = 0; i < NUM_MAPS; i++) {
                boolean isDisabled = (i == 5 || i == 6);
                if (buttonRects[i].contains(inputX, inputY) && !isDisabled) {
                    String mapFile;
                    if (i == 1) {
                        mapFile = "level2.tmx";
                    } else if (i == 2) {
                        mapFile = "level3.tmx";
                    } else if (i == 3) {
                        mapFile = "level5.tmx";
                    } else if (i == 4) {
                        mapFile = "level4.tmx";
                    } else {
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

        if (Gdx.input.isTouched()) {
            float tx = getInputX();
            float ty = getInputY();

            if (!dragging && Gdx.input.justTouched() && isTouchOnScrollbar()) {
                scrollbarTouched = true;
                scrollbarTouchOffset = tx - scrollbarRect.x;
            }

            if (scrollbarTouched) {
                float barWidth = scrollbarRect.width;
                float newBarX = tx - scrollbarTouchOffset;
                newBarX = Math.max(0, Math.min(newBarX, VIRTUAL_WIDTH - barWidth));
                scrollbarRect.x = newBarX;
                scrollX = maxScrollX * (scrollbarRect.x / (VIRTUAL_WIDTH - barWidth));
            } else {
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

        if (!scrollbarTouched && maxScrollX > 0) {
            float barWidth = scrollbarRect.width;
            scrollbarRect.x = (scrollX / maxScrollX) * (VIRTUAL_WIDTH - barWidth);
        }
    }

    private void drawScrollbar() {
        if (maxScrollX <= 0) return;
        batch.setColor(SCROLLBAR_BG);
        drawRect(0, SCROLLBAR_Y, VIRTUAL_WIDTH, SCROLLBAR_HEIGHT);
        batch.setColor(scrollbarTouched ? SCROLLBAR_THUMB_DRAG : SCROLLBAR_THUMB);
        drawRect(scrollbarRect.x, scrollbarRect.y, scrollbarRect.width, scrollbarRect.height);
        batch.setColor(Color.WHITE);
    }

    private float getInputX() {
        return Gdx.input.getX() * (VIRTUAL_WIDTH / (float)Gdx.graphics.getWidth());
    }

    private float getInputY() {
        return VIRTUAL_HEIGHT - Gdx.input.getY() * (VIRTUAL_HEIGHT / (float)Gdx.graphics.getHeight());
    }

    private boolean isTouchOnScrollbar() {
        if (maxScrollX <= 0) return false;
        float tx = getInputX();
        float ty = getInputY();
        return scrollbarRect.contains(tx, ty);
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
