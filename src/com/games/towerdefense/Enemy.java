package com.games.towerdefense;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.Style;

import com.games.towerdefense.enums.EnemyType;
import com.games.towerdefense.enums.MovementDirection;

public class Enemy
{
	private static Bitmap[]		enemySprites;					//Samassa järjestyksessä kuin EnemyType enumissa

	public final EnemyType		EnemyType;

	private float				originX;						//UNSCALED
	private float				originY;						//UNSCALED
	private int					tileX;
	private int					tileY;

	private RectF				bounds			= new RectF();	//SCALED

	private MovementDirection	direction;
	private float				tileSpeedPerSecond;				//UNSCALED
	private float				movementThisFrame;

	private int					currentTileIndex;
	private int					previousTileIndex;

	private int					distanceMoved;

	private Boolean				isAlive			= true;
	private int					maxHealth;
	private int					currentHealth;

	public Boolean				HasSpawned		= false;

	private Boolean				hasEnteredMap	= false;
	private Boolean				isWithinMap		= false;
	
	static
	{
		enemySprites = new Bitmap[2];
		enemySprites[0] = BitmapFactory.decodeResource(GameManager.GetResources(), R.drawable.enemy_brown);
		enemySprites[1] = BitmapFactory.decodeResource(GameManager.GetResources(), R.drawable.enemy_red);
	}
	
	public Enemy(EnemyType enemyType, int startTileX, int startTileY, int unscaledTileSize,
			MovementDirection startDirection, float tileSpeedPerSecond, int maxHealth)
	{
		EnemyType = enemyType;
		
		originX = (startTileX + 0.5f) * unscaledTileSize;
		originY = (startTileY + 0.5f) * unscaledTileSize;
		
		direction = startDirection;
		this.tileSpeedPerSecond = tileSpeedPerSecond;
		
		this.maxHealth = maxHealth;
		this.currentHealth = maxHealth;
	}
	
	public void Update(float elapsedMilliseconds, Map tileMap, float cameraX, float cameraY)
	{
		if (isAlive)
		{
			movementThisFrame = tileSpeedPerSecond * tileMap.GetUnscaledTileSize() * elapsedMilliseconds / 1000.0f;
			
			switch (direction)
			{
				case Right:
					originX += movementThisFrame;
					break;
				case Down:
					originY += movementThisFrame;
					break;
				case Left:
					originX -= movementThisFrame;
					break;
				case Up:
					originY -= movementThisFrame;
					break;
				default:
					break;
			}
			
			originX = Helper.Ceil(originX, 2);
			originY = Helper.Ceil(originY, 2);
			
			tileX = (int)Math.floor(originX / tileMap.GetUnscaledTileSize());
			tileY = (int)Math.floor(originY / tileMap.GetUnscaledTileSize());
			currentTileIndex = tileY * tileMap.GetWidthInTiles() + tileX;
			
			bounds.left = (originX - Math.round(enemySprites[EnemyType.ordinal()].getWidth() * 0.5f)) * GameManager.GetScale();
			bounds.top = (originY - Math.round(enemySprites[EnemyType.ordinal()].getHeight() * 0.5f)) * GameManager.GetScale();
			bounds.right = (originX + Math.round(enemySprites[EnemyType.ordinal()].getWidth() * 0.5f)) * GameManager.GetScale() - 1.0f;
			bounds.bottom = (originY + Math.round(enemySprites[EnemyType.ordinal()].getHeight() * 0.5f)) * GameManager.GetScale() - 1.0f;
			
			//if (GetDrawX() > tileMap.GetPixelWidth() || GetDrawX() + enemySprites[EnemyType.ordinal()].getWidth() * GameManager.GetScale() < 0 ||
			//	GetDrawY() > tileMap.GetPixelHeight() || GetDrawY() + enemySprites[EnemyType.ordinal()].getHeight() * GameManager.GetScale() < 0)
			if (bounds.right < 0 || bounds.bottom < 0 || bounds.left > tileMap.GetPixelHeight() || bounds.top > tileMap.GetPixelHeight())
			{
				isWithinMap = false;
			}
			else
			{
				isWithinMap = true;
				hasEnteredMap = true;
			}
			
			if (currentTileIndex != previousTileIndex)
			{
				distanceMoved++;
				
				if ((direction == MovementDirection.Right && originX >= (tileX + 0.5f) * tileMap.GetUnscaledTileSize()) ||
					(direction == MovementDirection.Down && originY >= (tileY + 0.5f) * tileMap.GetUnscaledTileSize()) ||
					(direction == MovementDirection.Left && originX <= (tileX + 0.5f) * tileMap.GetUnscaledTileSize()) ||
					(direction == MovementDirection.Up && originY <= (tileY + 0.5f) * tileMap.GetUnscaledTileSize()))
					ChooseDirection(tileMap, tileX, tileY);
			}
		}
	}
	
	public void Draw(Canvas canvas, float cameraX, float cameraY)
	{
		canvas.drawBitmap(
				enemySprites[EnemyType.ordinal()], null,
					new RectF(
						bounds.left - cameraX,
						bounds.top - cameraY,
						bounds.right - cameraX,
						bounds.bottom - cameraY), null);
				//new RectF(
				//		GetDrawX() - cameraX,
				//		GetDrawY() - cameraY,
				//		GetDrawX() + enemySprites[EnemyType.ordinal()].getWidth() * GameManager.GetScale() - 1 - cameraX,
				//		GetDrawY() + enemySprites[EnemyType.ordinal()].getHeight() * GameManager.GetScale() - 1 - cameraY),
				//null);
	}
	
	public void DrawHealth(Canvas canvas, Paint paint, float tileSize, float cameraX, float cameraY)
	{
		int width = Math.round(tileSize);
		int height = Math.round(4.0f * GameManager.GetScale());
		
		int x = Math.round(GetScaledOriginX() - width / 2.0f - cameraX);
		int y = Math.round(GetDrawY() - cameraY - height - GameManager.GetScale());
		
		paint.setStyle(Style.FILL);
		
		paint.setColor(Color.DKGRAY);
		canvas.drawRect(
				x - 1, y - 1,
				x + width,
				y + height,
				paint);
		
		paint.setColor(Color.LTGRAY);
		canvas.drawRect(
				x, y,
				x + width - 1,
				y + height - 1,
				paint);
		
		paint.setColor(Color.DKGRAY);
		canvas.drawLine(
				x - 1 + (float)Math.ceil(((float)currentHealth / maxHealth) * width), y,
				x + ((float)currentHealth / maxHealth) * width, y + height - 1, paint);
		
		if (currentHealth < maxHealth * 0.34f)
			paint.setColor(0xFFFF0000);
		else if (currentHealth <= maxHealth * 0.67f)
			paint.setColor(0xFFFFFF33);
		else
			paint.setColor(0xFF66CC00);
		
		canvas.drawRect(
				x, y,
				x - 1 + (float)Math.ceil(((float)currentHealth / maxHealth) * width),
				y + height - 1,
				paint);
	}
	
	public void Damage(int damage)
	{
		currentHealth -= damage;
		
		if (currentHealth <= 0)
		{
			currentHealth = 0;
			isAlive = false;
		}
	}
	
	public RectF GetBounds()
	{
		return bounds;
		
		//return new Rect(
		//		(int)Math.ceil(GetDrawX()),
		//		(int)Math.ceil(GetDrawY()),
		//		(int)Math.floor(GetDrawX() + enemySprites[EnemyType.ordinal()].getWidth() * GameManager.GetScale() - 1),
		//		(int)Math.floor(GetDrawY() + enemySprites[EnemyType.ordinal()].getHeight() * GameManager.GetScale() - 1));
	}
	
	public float GetDrawX() { return (originX - enemySprites[EnemyType.ordinal()].getWidth() * 0.5f) * GameManager.GetScale(); }
	public float GetDrawY() { return (originY - enemySprites[EnemyType.ordinal()].getHeight() * 0.5f) * GameManager.GetScale(); }
	public float GetScaledOriginX() { return originX * GameManager.GetScale(); }
	public float GetScaledOriginY() { return originY * GameManager.GetScale(); }
	
	public int GetDistanceMoved() { return distanceMoved; }
	public float GetSpeed() { return tileSpeedPerSecond; }
	public Boolean IsAlive() { return isAlive; }
	public Boolean HasGotAway() { return hasEnteredMap && !isWithinMap; }
	
	private void ChooseDirection(Map tileMap, int tileX, int tileY)
	{
		if (direction != MovementDirection.Right && tileX - 1 > -1 && tileMap.IsRoadTile(tileX - 1, tileY))
		{
			direction = MovementDirection.Left;
		}
		else if (direction != MovementDirection.Left && tileX + 1 <= tileMap.GetWidthInTiles() && tileMap.IsRoadTile(tileX + 1, tileY))
		{
			direction = MovementDirection.Right;
		}
		else if (direction != MovementDirection.Down && tileY - 1 > -1 && tileMap.IsRoadTile(tileX, tileY - 1))
		{
			direction = MovementDirection.Up;
		}
		else if (direction != MovementDirection.Up && tileY + 1 <= tileMap.GetHeightInTiles() && tileMap.IsRoadTile(tileX, tileY + 1))
		{
			direction = MovementDirection.Down;
		}
		
		originX = (tileX + 0.5f) * tileMap.GetUnscaledTileSize();
		originY = (tileY + 0.5f) * tileMap.GetUnscaledTileSize();
		
		previousTileIndex = currentTileIndex;
	}
}
