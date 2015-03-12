package com.games.towerdefense;

import java.util.ArrayList;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Paint.Style;

import com.games.towerdefense.enums.ProjectileType;
import com.games.towerdefense.enums.TargetPriority;
import com.games.towerdefense.enums.TowerType;

import com.games.towerdefense.skills.*;

public class Tower
{
	private final int				INVALID_TARGET	= -1;
	
	private static Bitmap[]			towerBitmaps;	//Samassa järjestyksessä kuin TowerType enumissa

	private final TowerType			Type;

	public final int				TileX;
	public final int				TileY;

	private TargetPriority			targetPriority;

	private int						damage;
	private float					rangeTiles;
	private float					attackSpeed;
	private float					attackTimer;
	private int						targetIndex;
	
	private Skill					autoSkill;

	private ArrayList<Projectile>	projectileList	= new ArrayList<Projectile>();
	
	public int						KillCount;

	static
	{
		towerBitmaps = new Bitmap[2];
		towerBitmaps[0] = BitmapFactory.decodeResource(GameManager.GetResources(), R.drawable.tower_melee_icon);
		towerBitmaps[1] = BitmapFactory.decodeResource(GameManager.GetResources(), R.drawable.tower_ranged_icon);
	}
	
	public Tower(TowerType towerType, Skill autoSkill, int damage, float rangeTiles, float attackSpeed)
	{
		Type = towerType;
		
		TileX = 0;
		TileY = 0;
		
		targetPriority = TargetPriority.First;
		
		this.autoSkill = autoSkill;
		
		this.damage = damage;
		this.rangeTiles = rangeTiles;
		this.attackSpeed = attackSpeed;
	}
	
	public Tower(Tower tower, int tileX, int tileY)
	{
		this.Type = tower.Type;
		
		this.TileX = tileX;
		this.TileY = tileY;
		
		targetPriority = TargetPriority.First;
		
		this.autoSkill = tower.autoSkill;
		
		this.damage = tower.damage;
		this.rangeTiles = tower.rangeTiles;
		this.attackSpeed = tower.attackSpeed;
	}
	
	public void Update(float timeElapsed, ArrayList<Enemy> enemyList, float tileSize)
	{
		/*if (attackTimer == attackSpeed)
		{
			ChooseTarget(enemyList, tileSize);
			Attack(enemyList, tileSize);
			
			if (targetIndex != INVALID_TARGET)
				attackTimer -= attackSpeed;
		}
		
		attackTimer += timeElapsed;
		if (attackTimer >= attackSpeed)
			attackTimer = attackSpeed;*/
		
		autoSkill.Update(timeElapsed, this, enemyList, targetIndex);
		
		/*if (autoSkill.GetCooldown() == 0)
		{
			ChooseTarget(enemyList, tileSize);
			autoSkill.Attack(this, enemyList, targetIndex);
		}*/
		
		if (projectileList.size() != 0)
			UpdateProjectiles(timeElapsed);
	}
	
	public void Draw(Canvas canvas, float tileSize, float cameraX, float cameraY, Paint paint)
	{
		canvas.drawBitmap(towerBitmaps[Type.ordinal()], null,
				new RectF(
						TileX * tileSize - cameraX,
						TileY * tileSize - cameraY,
						(TileX + 1) * tileSize - cameraX - 1,
						(TileY + 1) * tileSize - cameraY - 1), null);
		
		DrawAttackTimer(canvas, paint, tileSize, cameraX, cameraY);
		
		DrawProjectiles(canvas, cameraX, cameraY);
	}
	
	public RectF[] GetProjectileBounds()
	{
		if (projectileList.size() == 0 || projectileList == null)
			return null;
		
		RectF[] bounds = new RectF[projectileList.size()];
		
		for (int i = 0; i < projectileList.size(); i++)
			bounds[i] = projectileList.get(i).GetBounds();
		
		return bounds;
	}
	
	public void UpdateEndOfRound()
	{
		attackTimer = 0;
	}
	
	public Bitmap GetBitmap() { return towerBitmaps[Type.ordinal()]; }
	
	public int GetDamage() { return damage; }
	public Projectile GetProjectile(int projectileIndex) { return projectileList.get(projectileIndex); }
	public float GetRangeTiles() { return rangeTiles; }
	public float GetSpeed() { return 1.0f / attackSpeed * 1000.0f; }
	
	private void DrawAttackTimer(Canvas canvas, Paint paint, float tileSize, float cameraX, float cameraY)
	{
		int width = Math.round(GameManager.GetScale() * 3);
		int height = Math.round(tileSize);
		
		int y = Math.round(TileY * tileSize - cameraY);
		
		int x;
		if (TileX == 0)
			x = Math.round((TileX + 1) * tileSize - cameraX + GameManager.GetScale());
		else
			x = Math.round(TileX * tileSize - cameraX - width - GameManager.GetScale());
		
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
		
		if (attackTimer > attackSpeed * 0.9f)
			paint.setColor(0xFFCC2200);
		else if (attackTimer > attackSpeed * 0.8f)
			paint.setColor(0xFFBB3300);
		else if (attackTimer > attackSpeed * 0.7f)
			paint.setColor(0xFFAA4400);
		else if (attackTimer > attackSpeed * 0.6f)
			paint.setColor(0xFF995500);
		else if (attackTimer > attackSpeed * 0.5f)
			paint.setColor(0xFF886600);
		else if (attackTimer > attackSpeed * 0.4f)
			paint.setColor(0xFF777700);
		else if (attackTimer > attackSpeed * 0.3f)
			paint.setColor(0xFF668800);
		else if (attackTimer > attackSpeed * 0.2f)
			paint.setColor(0xFF559900);
		else if (attackTimer > attackSpeed * 0.1f)
			paint.setColor(0xFF44AA00);
		else
			paint.setColor(0xFF33BB00);
		
		if (attackTimer < attackSpeed)
		{
			canvas.drawRect(
					x, y + (float)Math.ceil((attackTimer / attackSpeed) * height),
					x + width - 1,
					y + height - 1,
					paint);
		}
	}
	
	private void DrawProjectiles(Canvas canvas, float cameraX, float cameraY)
	{
		for (int i = 0; i < projectileList.size(); i++)
			projectileList.get(i).Draw(canvas, cameraX, cameraY);
	}
	
	public void ChooseTarget(ArrayList<Enemy> enemyList, float tileSize)
	{
		targetIndex = INVALID_TARGET;
		
		float distance;
		for (int i = 0; i < enemyList.size(); i++)
		{
			if (enemyList.get(i).IsAlive() == false)
				continue;
			
			distance = (float)Math.sqrt(
					Math.pow((((TileX + 0.5f) * tileSize) - enemyList.get(i).GetScaledOriginX()), 2) +
					Math.pow((((TileY + 0.5f) * tileSize) - enemyList.get(i).GetScaledOriginY()), 2));
			
			if (distance <= rangeTiles * tileSize)
			{
				if (targetIndex == INVALID_TARGET)
					targetIndex = i;
				else
				{
					switch (targetPriority)
					{
						case First:
							if (enemyList.get(i).GetDistanceMoved() > enemyList.get(targetIndex).GetDistanceMoved())
								targetIndex = i;
							break;
						case Last:
							if (enemyList.get(i).GetDistanceMoved() < enemyList.get(targetIndex).GetDistanceMoved())
								targetIndex = i;
							break;
						case Fast:
							if (enemyList.get(i).GetSpeed() > enemyList.get(targetIndex).GetSpeed())
								targetIndex = i;
							break;
						case Slow:
							if (enemyList.get(i).GetSpeed() < enemyList.get(targetIndex).GetSpeed())
								targetIndex = i;
							break;
					}
				}
			}
		}
	}
	
	public int GetTargetIndex(ArrayList<Enemy> enemyList, float rangeTiles2)
	{
		int targetIndex2 = INVALID_TARGET;
		
		float distance;
		for (int i = 0; i < enemyList.size(); i++)
		{
			if (enemyList.get(i).IsAlive() == false)
				continue;
			
			distance = (float)Math.sqrt(
					Math.pow((((TileX + 0.5f) * Map.GetScaledTileSize()) - enemyList.get(i).GetScaledOriginX()), 2) +
					Math.pow((((TileY + 0.5f) * Map.GetScaledTileSize()) - enemyList.get(i).GetScaledOriginY()), 2));
			
			if (distance <= rangeTiles2 * Map.GetTileSize() * GameManager.GetScale())
			{
				if (targetIndex2 == INVALID_TARGET)
					targetIndex2 = i;
				else
				{
					switch (targetPriority)
					{
						case First:
							if (enemyList.get(i).GetDistanceMoved() > enemyList.get(targetIndex2).GetDistanceMoved())
								targetIndex2 = i;
							break;
						case Last:
							if (enemyList.get(i).GetDistanceMoved() < enemyList.get(targetIndex2).GetDistanceMoved())
								targetIndex2 = i;
							break;
						case Fast:
							if (enemyList.get(i).GetSpeed() > enemyList.get(targetIndex2).GetSpeed())
								targetIndex2 = i;
							break;
						case Slow:
							if (enemyList.get(i).GetSpeed() < enemyList.get(targetIndex2).GetSpeed())
								targetIndex2 = i;
							break;
					}
				}
			}
		}
		
		return targetIndex2;
	}
	
	private void Attack(ArrayList<Enemy> enemyList, float tileSize)
	{
		if (targetIndex != INVALID_TARGET)
		{
			if (Type == TowerType.Melee)
			{
				enemyList.get(targetIndex).Damage(damage);
				
				if (!enemyList.get(targetIndex).IsAlive())
					KillCount++;
			}
			else if (Type == TowerType.Ranged)
			{
				float directionX = enemyList.get(targetIndex).GetScaledOriginX() - GetOriginX(tileSize);
				float directionY = enemyList.get(targetIndex).GetScaledOriginY() - GetOriginY(tileSize);
				
				projectileList.add(new Projectile(ProjectileType.Ball,
						GetOriginX(tileSize), GetOriginY(tileSize),
						directionX, directionY, 10.0f * tileSize, damage));
			}
		}
	}
	
	private void UpdateProjectiles(float elapsedMilliseconds)
	{
		for (int i = 0; i < projectileList.size(); i++)
		{
			if (projectileList.get(i).DidHit)
			{
				projectileList.remove(i);
				i--;
				continue;
			}
			
			projectileList.get(i).Update(elapsedMilliseconds);
		}
	}
	
	public float GetOriginX(float tileSize) { return (TileX + 0.5f) * tileSize; }
	public float GetOriginY(float tileSize) { return (TileY + 0.5f) * tileSize; }
	
	public void AddProjectile(Projectile projectile)
	{
		projectileList.add(projectile);
	}
}
