package tar.pog.platformer2;

import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import tar.pog.platformer2.Helpers.TileManager;
import tar.pog.platformer2.Helpers.TouchInputHandler;
import tar.pog.platformer2.Obstacles.Fire;
import tar.pog.platformer2.Rewards.Coin;

public class Player {
    public static float WIDTH;
    public static float HEIGHT;
    public static final float JUMP_VELOCITY = 50f;
    public static final float MAX_VELOCITY = 11f;
    public static final float DAMPING = 0.9f;
    private static final float GRAVITY = -2.5f;

    public enum State {
        Standing, Walking, Jumping
    }

    public Vector2 position = new Vector2();
    public Vector2 velocity = new Vector2();
    public State state = State.Standing;
    public boolean grounded = false;
    public boolean facesRight = true;
    public float stateTime = 0;

    // Existing dependencies
    private final TouchInputHandler touchInputHandler;
    private final TileManager tileManager;
    private final Fire[] fires;
    private final Coin coin;
    private final Pool<Rectangle> rectPool;
    private final Array<Rectangle> tiles;

    // New dependencies for rendering
    private final Animation<TextureRegion> stand;
    private final Animation<TextureRegion> walk;
    private final Animation<TextureRegion> jump;

    // Updated constructor
    public Player(TouchInputHandler touchInputHandler, TileManager tileManager, Fire[] fires, Coin coin,
                  Pool<Rectangle> rectPool, Array<Rectangle> tiles,
                  Animation<TextureRegion> stand, Animation<TextureRegion> walk, Animation<TextureRegion> jump) {
        this.touchInputHandler = touchInputHandler;
        this.tileManager = tileManager;
        this.fires = fires;
        this.coin = coin;
        this.rectPool = rectPool;
        this.tiles = tiles;
        this.stand = stand;
        this.walk = walk;
        this.jump = jump;
    }

    // Existing update method (unchanged)
    public void update(float deltaTime) {
        if (deltaTime == 0) return;
        if (deltaTime > 0.1f) deltaTime = 0.1f;
        stateTime += deltaTime;

        // Jumping
        if (touchInputHandler.isUpwardButtonTouched() && grounded) {
            velocity.y += JUMP_VELOCITY;
            state = State.Jumping;
            grounded = false;
        }
        // Moving left
        if (touchInputHandler.isLeftButtonTouched()) {
            velocity.x = -MAX_VELOCITY;
            if (grounded) state = State.Walking;
            facesRight = false;
        }
        // Moving right
        if (touchInputHandler.isRightButtonTouched()) {
            velocity.x = MAX_VELOCITY;
            if (grounded) state = State.Walking;
            facesRight = true;
        }

        velocity.add(0, GRAVITY);
        velocity.x = MathUtils.clamp(velocity.x, -MAX_VELOCITY, MAX_VELOCITY);
        if (Math.abs(velocity.x) < 1) {
            velocity.x = 0;
            if (grounded) state = State.Standing;
        }

        velocity.scl(deltaTime);
        Rectangle playerRect = rectPool.obtain();
        playerRect.set(position.x, position.y, WIDTH, HEIGHT);

        int startX, startY, endX, endY;
        if (velocity.x > 0) {
            startX = endX = (int)(position.x + WIDTH + velocity.x);
        } else {
            startX = endX = (int)(position.x + velocity.x);
        }
        startY = (int)(position.y);
        endY = (int)(position.y + HEIGHT);
        tileManager.getTiles(startX, startY, endX, endY, tiles);
        playerRect.x += velocity.x;

        for (Rectangle tile : tiles) {
            if (playerRect.overlaps(tile)) {
                velocity.x = 0;
                break;
            }
        }
        playerRect.x = position.x;

        if (velocity.y > 0) {
            startY = endY = (int)(position.y + HEIGHT + velocity.y);
        } else {
            startY = endY = (int)(position.y + velocity.y);
        }
        startX = (int)(position.x);
        endX = (int)(position.x + WIDTH);
        tileManager.getTiles(startX, startY, endX, endY, tiles);
        playerRect.y += velocity.y;

        for (Rectangle tile : tiles) {
            if (playerRect.overlaps(tile)) {
                if (velocity.y > 0) {
                    position.y = tile.y - HEIGHT;
                } else {
                    position.y = tile.y + tile.height;
                    grounded = true;
                }
                velocity.y = 0;
                break;
            }
        }

        for (Fire fire : fires) {
            if (playerRect.overlaps(fire.getBoundingBox())) {
                restart();
            }
        }

        if (playerRect.overlaps(coin.getBoundingBox())) {
            restart();
        }

        rectPool.free(playerRect);

        if (position.y < 0) {
            restart();
        }

        position.add(velocity);
        velocity.scl(1 / deltaTime);
        velocity.x *= DAMPING;
    }

    public void restart() {
        System.out.println("🔥 Player touched fire or coin, or fell! Restarting...");
        position.set(20, 20);
        velocity.set(0, 0);
        state = State.Standing;
        grounded = false;
        facesRight = true;
        stateTime = 0;
    }

    // New render method
    public void render(Batch batch) {
        // Get animation frame based on state
        TextureRegion frame = null;
        switch (state) {
            case Standing:
                frame = stand.getKeyFrame(stateTime);
                break;
            case Walking:
                frame = walk.getKeyFrame(stateTime);
                break;
            case Jumping:
                frame = jump.getKeyFrame(stateTime);
                break;
        }

        // Draw player
        if (facesRight) {
            batch.draw(frame, position.x, position.y, WIDTH, HEIGHT);
        } else {
            batch.draw(frame, position.x + WIDTH, position.y, -WIDTH, HEIGHT);
        }
    }
}
