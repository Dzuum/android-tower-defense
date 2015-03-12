package com.games.towerdefense;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.RectF;

import com.games.towerdefense.enums.ProjectileType;

public class Projectile
{
	private static Bitmap[]	projectileBitmaps;	//Samassa järjestyksessä kuin ProjectileType enumissa

	private ProjectileType	projectileType;

	private float			x;
	private float			y;

	private float			directionX;
	private float			directionY;
	
	private float			speedPerSecond;
	private float			movementThisFrame;
	
	public Boolean			DidHit	= false;
	
	private RectF			bounds	= new RectF();
	
	public final int		Damage;

	static
	{
		projectileBitmaps = new Bitmap[1];
		projectileBitmaps[0] = BitmapFactory.decodeResource(GameManager.GetResources(), R.drawable.projectile_ball);
	}
	
	public Projectile(ProjectileType projectileType, float x, float y,
			float directionX, float directionY, float speedPerSecond, int damage)
	{
		this.projectileType = projectileType;
		
		this.x = x - projectileBitmaps[projectileType.ordinal()].getWidth() * 0.5f * GameManager.GetScale();
		this.y = y - projectileBitmaps[projectileType.ordinal()].getHeight() * 0.5f * GameManager.GetScale();
		
		this.directionX = directionX;
		this.directionY = directionY;
		
		float magnitude = (float)Math.sqrt(Math.pow(directionX, 2) + Math.pow(directionY, 2));
		
		this.directionX /= magnitude;
		this.directionY /= magnitude;
		
		this.speedPerSecond = speedPerSecond;
		
		Damage = damage;
	}
	
	public void Update(float elapsedMilliseconds)
	{
		movementThisFrame = speedPerSecond * elapsedMilliseconds / 1000.0f;
		
		x += directionX * movementThisFrame;
		y += directionY * movementThisFrame;
		
		bounds.left = x;
		bounds.top = y;
		//bounds.right = x + 
	}
	
	public void Draw(Canvas canvas, float cameraX, float cameraY)
	{
		canvas.drawBitmap(projectileBitmaps[projectileType.ordinal()], null,
				new RectF(
						x - cameraX,
						y - cameraY,
						x + projectileBitmaps[projectileType.ordinal()].getWidth() * GameManager.GetScale() - 1 - cameraX,
						y + projectileBitmaps[projectileType.ordinal()].getHeight() * GameManager.GetScale() - 1 - cameraY), null);
	}
	
	public RectF GetBounds()
	{
		float scaledFrameWidth = projectileBitmaps[projectileType.ordinal()].getWidth() * GameManager.GetScale();
		float scaledFrameHeight = projectileBitmaps[projectileType.ordinal()].getHeight() * GameManager.GetScale();
		
		RectF bounds = new RectF((int)Math.ceil(x + scaledFrameWidth * 0.1f), (int)Math.ceil(y + scaledFrameHeight * 0.1f),
				(int)Math.floor(x + scaledFrameWidth * 0.9f - 1),
				(int)Math.floor(y + scaledFrameHeight * 0.9f - 1));
		
		return bounds;
	}
}
