package tar.pog.platformer2.NPC;

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
    public static final float WIDTH = 1.2f;
    public static final float HEIGHT = 1.7f;
    private static final float SPEED = 6.0f;
    private final Animation<TextureRegion> animation;
    private float stateTime;
    private final float minX;
    private final float maxX;
    private boolean facingRight;

    public Slime(float x, float y, Animation<TextureRegion> animation, float minX, float maxX) {
        this.position = new Vector2(x, y);
        this.velocity = new Vector2(-SPEED, 0);
        this.boundingBox = new Rectangle(x, y, WIDTH, HEIGHT);
        this.animation = animation;
        this.stateTime = 0;
        this.minX = minX;
        this.maxX = maxX;
        this.facingRight = false;
    }

    public void update(float deltaTime) {
        position.add(velocity.x * deltaTime, velocity.y * deltaTime);
        if (position.x <= minX) {
            position.x = minX;
            velocity.x = SPEED;
            facingRight = true;
        } else if (position.x + WIDTH >= maxX) {
            position.x = maxX - WIDTH;
            velocity.x = -SPEED;
            facingRight = false;
        }
        boundingBox.setPosition(position.x, position.y);
        stateTime += deltaTime;
    }

    public boolean isOffScreen(float cameraLeftEdge) {
        return position.x + WIDTH < cameraLeftEdge;
    }

    public Rectangle getBoundingBox() {
        return boundingBox;
    }

    public Vector2 getPosition() {
        return position;
    }

    public TextureRegion getCurrentFrame() {
        return animation.getKeyFrame(stateTime, true);
    }

    public boolean isFacingRight() {
        return facingRight;
    }

    public static class SlimeManager {
        private final Array<Slime> slimes;
        private final Animation<TextureRegion> slimeAnimation;
        private TextureRegion fallbackTexture;
        private final float mapWidth;
        private Texture slimeTexture;
        private String currentMapFile;

        // Each map has its own patrol zones for slimes: {minX, maxX, y}
        private static final java.util.Map<String, float[][]> mapPatrolZones = new java.util.HashMap<>();

        // Default patrol zones if map is unknown, all at y=2.0f
        private static final float[][] DEFAULT_PATROL_ZONES = {
            {30, 46, 2.0f},
            {50, 69, 2.0f},
            {109, 122, 2.0f}
        };

        static {
            // Example patrol zones for each map (customize y as needed)
            mapPatrolZones.put("level1.tmx", new float[][]{
                {30, 46, 2.0f},
                {50, 69, 2.0f},
                {109, 122, 2.0f}
            });
            mapPatrolZones.put("level2.tmx", new float[][]{
                {25 , 40, 5.0f},   // Slime 1 at y=2.0
                {57, 65, 13.0f},   // Slime 2 at y=3.5 (higher)
                {96, 103, 14.0f}    // Slime 3 at y=1.0 (lower)
            });
            mapPatrolZones.put("level3.tmx", new float[][]{
                {36 , 48, 6.0f},
                {57, 74, 2f},
                {101, 113, 2f},
                {146, 170, 2f},
                {146, 169, 2f},
                {172, 189, 2f},
            });
            mapPatrolZones.put("level4.tmx", new float[][]{
                {20, 37, 4.0f},
                {20, 34, 4.0f},
                {181, 193, 14.0f},
//                {200, 210, 1.0f}
            });
            mapPatrolZones.put("level5.tmx", new float[][]{
                {18, 27, 3.0f},

            });
            mapPatrolZones.put("level6.tmx", new float[][]{
                {15, 23, 2.0f},
                {60, 70, 3.2f},
                {135, 150, 2.6f}
            });
        }

        // Set to true if slime_walk.png faces left by default, false if it faces right
        private static final boolean SPRITE_FACES_LEFT = true;

        /**
         * @param mapWidth The width of the map in world units
         * @param mapFile  The file name of the map (e.g., "level2.tmx")
         */
        public SlimeManager(float mapWidth, String mapFile) {
            this.slimes = new Array<>();
            this.mapWidth = mapWidth;
            this.currentMapFile = mapFile;

            Animation<TextureRegion> tempAnimation = null;
            try {
                slimeTexture = new Texture("img/NPCs/slime/slime_walk.png");
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
            slimeAnimation = tempAnimation;

            // Fallback texture (red rectangle)
            Pixmap pixmap = new Pixmap(16, 24, Pixmap.Format.RGBA8888);
            pixmap.setColor(1, 0, 0, 1);
            pixmap.fillRectangle(0, 0, 16, 24);
            fallbackTexture = new TextureRegion(new Texture(pixmap));
            pixmap.dispose();

            if (slimeAnimation == null) {
                System.err.println("SlimeManager: No valid slime animation, using red 16x24 fallback texture");
            }

            reset();
        }

        /**
         * Alternative constructor for compatibility (defaults to level1.tmx)
         */
        public SlimeManager(float mapWidth) {
            this(mapWidth, "level1.tmx");
        }

        public void setMapFile(String mapFile) {
            if (mapFile != null && !mapFile.isEmpty() && !mapFile.equals(currentMapFile)) {
                this.currentMapFile = mapFile;
                reset();
            }
        }

        public void reset() {
            slimes.clear();
            float[][] patrolZones = mapPatrolZones.getOrDefault(currentMapFile, DEFAULT_PATROL_ZONES);

            for (float[] zone : patrolZones) {
                float minX = Math.max(0, zone[0]);
                float maxX = Math.min(mapWidth, zone[1]);
                float y = (zone.length > 2) ? zone[2] : 2.0f; // Use custom Y if given, else default
                float x = maxX - WIDTH;
                Slime slime = new Slime(x, y, slimeAnimation != null ? slimeAnimation : new Animation<TextureRegion>(0.1f, fallbackTexture), minX, maxX);
                slimes.add(slime);
            }
        }

        public void update(float deltaTime, float cameraLeftEdge) {
            for (Slime slime : slimes) {
                slime.update(deltaTime);
            }
        }

        public void render(com.badlogic.gdx.graphics.g2d.Batch batch) {
            for (Slime slime : slimes) {
                TextureRegion frame = slime.getCurrentFrame() != null ? slime.getCurrentFrame() : fallbackTexture;
                boolean shouldFaceRight = slime.isFacingRight();
                if (SPRITE_FACES_LEFT) {
                    shouldFaceRight = !shouldFaceRight;
                }
                if (shouldFaceRight) {
                    batch.draw(frame, slime.getPosition().x, slime.getPosition().y, Slime.WIDTH, Slime.HEIGHT);
                } else {
                    batch.draw(frame, slime.getPosition().x + Slime.WIDTH, slime.getPosition().y, -Slime.WIDTH, Slime.HEIGHT);
                }
            }
        }

        public void debugRender(com.badlogic.gdx.graphics.glutils.ShapeRenderer debugRenderer) {
            debugRenderer.setColor(com.badlogic.gdx.graphics.Color.GREEN);
            for (Slime slime : slimes) {
                debugRenderer.rect(slime.getPosition().x, slime.getPosition().y, Slime.WIDTH, Slime.HEIGHT);
            }
        }

        public Slime[] getSlimes() {
            return slimes.toArray(Slime.class);
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
