package tar.pog.platformer2.Helpers;

import com.badlogic.gdx.maps.tiled.TiledMap;
import com.badlogic.gdx.maps.tiled.TiledMapTileLayer;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;

/**
 * Manages tile-based collision detection for a Tiled map.
 * Provides collision tiles in world units for use in the game.
 */
public class TileManager {
    private final TiledMap map;
    private final Pool<Rectangle> rectPool;
    private final float tileSize;
    private final float scale;
    private final String collisionLayerName;

    /**
     * Constructs a TileManager for the specified Tiled map.
     *
     * @param map The Tiled map containing collision tiles
     * @param tileSize The size of a tile in pixels (e.g., 32)
     * @param scale The world unit scale (e.g., 1/16f for rendering)
     */
    public TileManager(TiledMap map, float tileSize, float scale) {
        this.map = map;
        this.tileSize = tileSize;
        this.scale = scale;
        this.collisionLayerName = "walls";
        this.rectPool = new Pool<Rectangle>() {
            @Override
            protected Rectangle newObject() {
                return new Rectangle();
            }
        };
    }

    /**
     * Retrieves collision tiles within the specified tile coordinate range.
     * Tiles are returned as Rectangles in world units.
     *
     * @param startX Starting X tile coordinate
     * @param startY Starting Y tile coordinate
     * @param endX Ending X tile coordinate
     * @param endY Ending Y tile coordinate
     * @param tiles Array to store the collision tile Rectangles
     */
    public void getTiles(int startX, int startY, int endX, int endY, Array<Rectangle> tiles) {
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(collisionLayerName);
        if (layer == null) {
            System.err.println("Collision layer '" + collisionLayerName + "' not found in map");
            return;
        }

        rectPool.freeAll(tiles);
        tiles.clear();

        for (int y = startY; y <= endY; y++) {
            for (int x = startX; x <= endX; x++) {
                TiledMapTileLayer.Cell cell = layer.getCell(x, y);
                if (cell != null) {
                    Rectangle rect = rectPool.obtain();
                    rect.set(x * tileSize * scale, y * tileSize * scale, tileSize * scale, tileSize * scale);
                    tiles.add(rect);
                }
            }
        }
    }

    /**
     * Returns the map width in world units.
     *
     * @return The map width
     */
    public float getMapWidth() {
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(collisionLayerName);
        if (layer == null) {
            System.err.println("Collision layer '" + collisionLayerName + "' not found in map");
            return 100f * tileSize * scale; // Default width
        }
        return layer.getWidth() * tileSize * scale;
    }

    /**
     * Returns the map height in world units.
     *
     * @return The map height
     */
    public float getMapHeight() {
        TiledMapTileLayer layer = (TiledMapTileLayer) map.getLayers().get(collisionLayerName);
        if (layer == null) {
            System.err.println("Collision layer '" + collisionLayerName + "' not found in map");
            return 100f * tileSize * scale; // Default height
        }
        return layer.getHeight() * tileSize * scale;
    }
}
