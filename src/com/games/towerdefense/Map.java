package com.games.towerdefense;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.RectF;

import com.games.towerdefense.enums.MovementDirection;

public class Map
{
	private final int			TOWER_TILE	= 42;

	private Bitmap				tileset;
	private int					tilesetTilesInRow;

	private static int			tileSize;

	private int					width;
	private int					height;

	private int[][][]			groundLayers;
	private int[][]				collisionLayer;

	private int					startTileX;
	private int					startTileY;
	private MovementDirection	startDirection;

	private Rect				rectTileInTileset;
	private RectF				rectTilePosition;

	private Bitmap				mapBitmap;
	
	static
	{
		tileSize = 32;
	}
	
	public static int GetTileSize() { return tileSize; }
	
	public Map(Context context, int tilesetID, int[][][] groundLayers, int[][] collisionLayer,
			int startTileX, int startTileY, MovementDirection startDirection)
	{
		tileset = BitmapFactory.decodeResource(context.getResources(), tilesetID);
		
		tilesetTilesInRow = tileset.getWidth() / tileSize;
		
		width = groundLayers[0][0].length; //Toisinpäin, koska GameManagerissa taulukot initialisoidessa menee x ja y toisinpäin.
		height = groundLayers[0].length;
		
		this.groundLayers = new int[groundLayers.length][width][height];
		this.collisionLayer = new int[width][height];
		
		for (int y = 0; y < height; y++)
		{
			for (int x = 0; x < width; x++)
			{
				this.collisionLayer[x][y] = collisionLayer[y][x];
				
				for (int i = 0; i < groundLayers.length; i++)
					this.groundLayers[i][x][y] = groundLayers[i][y][x];
			}
		}
		
		rectTileInTileset = new Rect();
		rectTilePosition = new RectF();
		
		mapBitmap = Bitmap.createBitmap(width * 32, height * 32, Config.ARGB_8888);
		Canvas c = new Canvas(mapBitmap);
		int tileIndexX = 0;
		int tileIndexY = 0;
		for (int y = 0; y < height; y++)
		{
			rectTilePosition.top = y * 32;
			rectTilePosition.bottom = (y + 1) * 32;
			
			for (int x = 0; x < width; x++)
			{
				rectTilePosition.left = x * 32;
				rectTilePosition.right = (x + 1) * 32;
				
				for (int i = 0; i < groundLayers.length; i++)
				{
					if (this.groundLayers[i][x][y] == -1)
						continue;
					
					tileIndexX = this.groundLayers[i][x][y] % tilesetTilesInRow;
					tileIndexY = (this.groundLayers[i][x][y] - tileIndexX) / tilesetTilesInRow;
					rectTileInTileset = new Rect(
							tileIndexX * tileSize,
							tileIndexY * tileSize,
							(tileIndexX + 1) * tileSize,
							(tileIndexY + 1) * tileSize);
					c.drawBitmap(tileset, rectTileInTileset, rectTilePosition, null);
				}
			}
		}
		
		this.startTileX = startTileX;
		this.startTileY = startTileY;
		this.startDirection = startDirection;
	}
	
	public void Draw(Canvas canvas, float cameraX, float cameraY)
	{
		canvas.drawBitmap(mapBitmap, null, new RectF(-cameraX, -cameraY,
				mapBitmap.getWidth() * GameManager.GetScale() - cameraX, mapBitmap.getHeight() * GameManager.GetScale() - cameraY), null);
	}
	
	public int GetWidthInTiles() { return groundLayers[0].length; }
	public int GetHeightInTiles() { return groundLayers[0][0].length; }
	
	public float GetPixelWidth() { return groundLayers[0].length * GetScaledTileSize(); }
	public float GetPixelHeight() { return groundLayers[0][0].length * GetScaledTileSize(); }
	
	public int GetUnscaledTileSize() { return tileSize; }
	
	public static float GetScaledTileSize() { return tileSize * GameManager.GetScale(); }
	
	public float GetTilePixelPositionX(int xIndex) { return xIndex * GetScaledTileSize(); }
	public float GetTilePixelPositionY(int yIndex) { return yIndex * GetScaledTileSize(); }
	
	public int GetStartTileX() { return startTileX; }
	public int GetStartTileY() { return startTileY; }
	public MovementDirection GetStartDirection() { return startDirection; }
	
	public Boolean IsCollisionTile(int tileX, int tileY)
	{
		if (tileX < 0 || tileY < 0 || tileX >= GetWidthInTiles() || tileY >= GetHeightInTiles())
			return false;
		else
			return collisionLayer[tileX][tileY] >= 0;
	}
	
	public Boolean IsRoadTile(int tileX, int tileY)
	{
		if (tileX < 0 || tileY < 0 || tileX >= GetWidthInTiles() || tileY >= GetHeightInTiles())
			return false;
		else
			return collisionLayer[tileX][tileY] == 0;
	}
	
	public Boolean IsTowerOccupied(int tileX, int tileY)
	{
		if (tileX < 0 || tileY < 0 || tileX >= GetWidthInTiles() || tileY >= GetHeightInTiles())
			return false;
		else
			return collisionLayer[tileX][tileY] == TOWER_TILE;
	}
	
	public void PlacedTower(int x, int y)
	{
		collisionLayer[x][y] = TOWER_TILE;
	}
	
	public int PositionToTileX(float positionX)
	{
		return (int)Math.floor(positionX / GetScaledTileSize());
	}
	
	public int PositionToTileY(float positionY)
	{
		return (int)Math.floor(positionY / GetScaledTileSize());
	}
}
