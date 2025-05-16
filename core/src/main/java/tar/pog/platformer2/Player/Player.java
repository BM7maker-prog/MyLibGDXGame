package tar.pog.platformer2.Player;

import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import tar.pog.platformer2.Helpers.TileManager;
import tar.pog.platformer2.Helpers.TouchInputHandler;
import tar.pog.platformer2.Obstacles.Fire;
import tar.pog.platformer2.NPC.Slime;
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

    private boolean dead = false; // Tracks whether the player is dead

    // Dependencies
    private final TouchInputHandler touchInputHandler;
    private final TileManager tileManager;
    private final Fire[] fires;
    private final Coin coin;
    private Slime[] slimes; // Nullable slimes array
    private final Pool<Rectangle> rectPool;
    private final Array<Rectangle> tiles;

    // Original constructor (no slimes)
    public Player(TouchInputHandler touchInputHandler, TileManager tileManager, Fire[] fires, Coin coin,
                  Pool<Rectangle> rectPool, Array<Rectangle> tiles) {
        this(touchInputHandler, tileManager, fires, coin, null, rectPool, tiles);
    }

    // Constructor with slimes
    public Player(TouchInputHandler touchInputHandler, TileManager tileManager, Fire[] fires, Coin coin,
                  Slime[] slimes, Pool<Rectangle> rectPool, Array<Rectangle> tiles) {
        this.touchInputHandler = touchInputHandler;
        this.tileManager = tileManager;
        this.fires = fires;
        this.coin = coin;
        this.slimes = slimes; // May be null
        this.rectPool = rectPool;
        this.tiles = tiles;
    }

    public void updateSlimes(Slime[] newSlimes) {
        this.slimes = newSlimes;
    }

    public void update(float deltaTime) {
        if (deltaTime == 0) return;
        if (deltaTime > 0.1f) deltaTime = 0.1f;

        if (dead) return; // Skip update if the player is dead

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

        float startX, startY, endX, endY;

        // Horizontal collisions (use float, no rounding to int)
        if (tileManager != null) {
            if (velocity.x > 0) {
                startX = endX = position.x + WIDTH + velocity.x;
            } else {
                startX = endX = position.x + velocity.x;
            }
            startY = position.y;
            endY = position.y + HEIGHT;
            tileManager.getTiles((int)Math.floor(startX), (int)Math.floor(startY), (int)Math.ceil(endX), (int)Math.ceil(endY), tiles);
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
                startY = endY = position.y + HEIGHT + velocity.y;
            } else {
                startY = endY = position.y + velocity.y;
            }
            startX = position.x;
            endX = position.x + WIDTH;
            tileManager.getTiles((int)Math.floor(startX), (int)Math.floor(startY), (int)Math.ceil(endX), (int)Math.ceil(endY), tiles);
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
        } else {
            System.err.println("Warning: tileManager is null, skipping collision detection");
        }

        // Check collisions with fires, coin, and slimes
        for (Fire fire : fires) {
            if (playerRect.overlaps(fire.getBoundingBox())) {
                markAsDead();
            }
        }
        if (playerRect.overlaps(coin.getBoundingBox())) {
            markAsDead();
        }
        // Only check slimes if the array is not null
        if (slimes != null) {
            for (Slime slime : slimes) {
                if (playerRect.overlaps(slime.getBoundingBox())) {
                    markAsDead();
                }
            }
        }

        rectPool.free(playerRect);

        // Check if player falls off map
        if (position.y < 0) {
            markAsDead();
        }

        // Update position and unscale velocity
        position.add(velocity);
        velocity.scl(1 / deltaTime);

        // Apply damping
        velocity.x *= DAMPING;
    }

    public void markAsDead() {
        dead = true;
        System.out.println("🔥 Player is dead!");
    }

    public boolean isDead() {
        return dead;
    }

    public void restart() {
        System.out.println("🔥 Player touched fire, coin, slime, or fell! Restarting...");
        position.set(20, 20);
        velocity.set(0, 0);
        state = State.Standing;
        grounded = false;
        facesRight = true;
        stateTime = 0;
        dead = false; // Reset death state
    }
}
