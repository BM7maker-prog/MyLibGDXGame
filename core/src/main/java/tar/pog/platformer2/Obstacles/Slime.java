package tar.pog.platformer2.Obstacles;

import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Animation;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.utils.Array;

public class Slime {
    private final Vector2 position;
    private final Vector2 velocity;
    private final Rectangle boundingBox;
    public static final float WIDTH = 1.2f; // Slime width in game units (16 pixels)
    public static final float HEIGHT = 1.7f; // Slime height in game units (24 pixels)
    private static final float SPEED = -6.0f; // Slime moves left at constant speed
    private final Animation<TextureRegion> animation; // Animation for rendering
    private float stateTime; // Animation time

    public Slime(float x, float y, Animation<TextureRegion> animation) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(SPEED, 0);
        this.boundingBox = new Rectangle(x, y, WIDTH, HEIGHT);
        this.animation = animation;
        this.stateTime = 0;
    }

    public void update(float deltaTime) {
        // Update position based on velocity
        position.add(velocity.x * deltaTime, velocity.y * deltaTime);
        // Update bounding box position
        boundingBox.setPosition(position.x, position.y);
        // Update animation time
        stateTime += deltaTime;
    }

    public boolean isOffScreen(float cameraLeftEdge) {
        // Slime is off-screen if it moves beyond the left edge of the camera
        return position.x + WIDTH < cameraLeftEdge;
    }

    public Rectangle getBoundingBox() {
        return boundingBox;
    }

    public Vector2 getPosition() {
        return position;
    }

    public TextureRegion getCurrentFrame() {
        return animation.getKeyFrame(stateTime, true); // Loop animation
    }

    public static class SlimeManager {
        private final Array<Slime> slimes;
        private float slimeSpawnTimer;
        private static final float SLIME_SPAWN_INTERVAL = 2.0f; // Spawn every 2 seconds
        private final Animation<TextureRegion> slimeAnimation;
        private TextureRegion fallbackTexture; // Placeholder for missing texture
        private final float mapWidth;
        private Texture slimeTexture; // Texture for slime animation

        public SlimeManager(float mapWidth) {
            this.slimes = new Array<>();
            this.slimeSpawnTimer = 0;
            this.mapWidth = mapWidth;

            // Load slime animation
            Animation<TextureRegion> tempAnimation = null;
            try {
                slimeTexture = new Texture("slime_walk.png");
                TextureRegion[][] splitFrames = TextureRegion.split(slimeTexture, 16, 24);
                Array<TextureRegion> allFrames = new Array<>();
                for (TextureRegion[] row : splitFrames) {
                    for (TextureRegion frame : row) {
                        allFrames.add(frame);
                    }
                }
                if (allFrames.size >= 15) {
                    tempAnimation = new Animation<>(0.1f, allFrames, Animation.PlayMode.LOOP);
                    System.out.println("SlimeManager: Initialized with animation");
                } else {
                    System.err.println("slime_walk.png does not contain 15 frames, using fallback");
                }
            } catch (Exception e) {
                slimeTexture = null;
                System.err.println("Failed to load slime_walk.png: " + e.getMessage());
            }

            // Assign final animation
            slimeAnimation = tempAnimation;

            // Create fallback texture (red rectangle) for missing slime.png
            Pixmap pixmap = new Pixmap(16, 24, Pixmap.Format.RGBA8888);
            pixmap.setColor(1, 0, 0, 1); // Red
            pixmap.fillRectangle(0, 0, 16, 24);
            fallbackTexture = new TextureRegion(new Texture(pixmap));
            pixmap.dispose();

            if (slimeAnimation == null) {
                System.err.println("SlimeManager: No valid slime animation, using red 16x24 fallback texture");
            }
        }

        public void update(float deltaTime, float cameraLeftEdge) {
            // Update all slimes
            for (Slime slime : slimes) {
                slime.update(deltaTime);
            }

            // Remove off-screen slimes
            Array<Slime> slimesToRemove = new Array<>();
            for (Slime slime : slimes) {
                if (slime.isOffScreen(cameraLeftEdge)) {
                    slimesToRemove.add(slime);
                }
            }
            slimes.removeAll(slimesToRemove, true);

            // Spawn new slimes
            slimeSpawnTimer += deltaTime;
            if (slimeSpawnTimer >= SLIME_SPAWN_INTERVAL) {
                spawnSlime();
                slimeSpawnTimer = 0;
            }
        }

        private void spawnSlime() {
            // Spawn slime from right side of the map at a fixed y-position
            float x = mapWidth - 1; // Right edge of map
            float y = 2.0f; // Fixed height, adjust as needed
            Slime slime = new Slime(x, y, slimeAnimation != null ? slimeAnimation : new Animation<TextureRegion>(0.1f, fallbackTexture));
            slimes.add(slime);
        }

        public Slime[] getSlimes() {
            return slimes.toArray(Slime.class);
        }

        public void render(com.badlogic.gdx.graphics.g2d.Batch batch) {
            for (Slime slime : slimes) {
                TextureRegion frame = slime.getCurrentFrame() != null ? slime.getCurrentFrame() : fallbackTexture;
                batch.draw(frame, slime.getPosition().x, slime.getPosition().y, Slime.WIDTH, Slime.HEIGHT);
            }
        }

        public void debugRender(com.badlogic.gdx.graphics.glutils.ShapeRenderer debugRenderer) {
            debugRenderer.setColor(com.badlogic.gdx.graphics.Color.GREEN);
            for (Slime slime : slimes) {
                debugRenderer.rect(slime.getPosition().x, slime.getPosition().y, Slime.WIDTH, Slime.HEIGHT);
            }
        }

        public void dispose() {
            if (slimeTexture != null) {
                slimeTexture.dispose();
            }
            if (fallbackTexture != null && fallbackTexture.getTexture() != null) {
                fallbackTexture.getTexture().dispose();
            }
        }
    }
}
