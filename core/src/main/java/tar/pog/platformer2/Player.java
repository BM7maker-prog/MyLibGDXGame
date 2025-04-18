package tar.pog.platformer2;

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

    // Dependencies
    private final TouchInputHandler touchInputHandler;
    private final TileManager tileManager;
    private final Fire[] fires;
    private final Coin coin;
    private final Pool<Rectangle> rectPool;
    private final Array<Rectangle> tiles;

    public Player(TouchInputHandler touchInputHandler, TileManager tileManager, Fire[] fires, Coin coin,
                  Pool<Rectangle> rectPool, Array<Rectangle> tiles) {
        this.touchInputHandler = touchInputHandler;
        this.tileManager = tileManager;
        this.fires = fires;
        this.coin = coin;
        this.rectPool = rectPool;
        this.tiles = tiles;
    }

    public void update(float deltaTime) {
        if (deltaTime == 0) return;
        if (deltaTime > 0.1f) deltaTime = 0.1f;

        stateTime += deltaTime;

        // Check input and apply to velocity & state
        if (touchInputHandler.isUpwardButtonTouched() && grounded) {
            velocity.y += JUMP_VELOCITY;
            state = State.Jumping;
            grounded = false;
        }
        if (touchInputHandler.isLeftButtonTouched()) {
            velocity.x = -MAX_VELOCITY;
            if (grounded) state = State.Walking;
            facesRight = false;
        }
        if (touchInputHandler.isRightButtonTouched()) {
            velocity.x = MAX_VELOCITY;
            if (grounded) state = State.Walking;
            facesRight = true;
        }

        // Apply gravity
        velocity.add(0, GRAVITY);

        // Clamp velocity (x-axis)
        velocity.x = MathUtils.clamp(velocity.x, -MAX_VELOCITY, MAX_VELOCITY);

        // Stop movement if velocity is small
        if (Math.abs(velocity.x) < 1) {
            velocity.x = 0;
            if (grounded) state = State.Standing;
        }

        // Scale velocity for frame
        velocity.scl(deltaTime);

        // Collision detection
        Rectangle playerRect = rectPool.obtain();
        playerRect.set(position.x, position.y, WIDTH, HEIGHT);

        int startX, startY, endX, endY;

        // Horizontal collisions
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

        // Vertical collisions
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

        // Check collisions with fires and coin
        for (Fire fire : fires) {
            if (playerRect.overlaps(fire.getBoundingBox())) {
                restart();
            }
        }
        if (playerRect.overlaps(coin.getBoundingBox())) {
            restart();
        }

        rectPool.free(playerRect);

        // Check if player falls off map
        if (position.y < 0) {
            restart();
        }

        // Update position and unscale velocity
        position.add(velocity);
        velocity.scl(1 / deltaTime);

        // Apply damping
        velocity.x *= DAMPING;
    }

    public void restart() {
        System.out.println("🔥 Player touched fire, coin, or fell! Restarting...");
        position.set(20, 20);
        velocity.set(0, 0);
        state = State.Standing;
        grounded = false;
        facesRight = true;
        stateTime = 0;
    }
}
