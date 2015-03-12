package com.games.towerdefense;

import java.util.ArrayList;
import java.util.Random;

import android.graphics.Canvas;
import android.graphics.Paint;

import com.games.towerdefense.enums.EnemyType;

public class EnemyWave
{
	public ArrayList<Enemy>	EnemyList	= new ArrayList<Enemy>();
	private int				spawnInterval;							//Milliseconds;
	private int				spawnCounter;
	
	private int				enemiesGotAwayThisFrame;
	
	public Boolean			HasFinished	= false;
	
	public EnemyWave(Random random, Map map, int enemyCount, int spawnInterval)
	{
		this.spawnInterval = spawnInterval;
		this.spawnCounter = spawnInterval;
		
		for (int i = 0; i < enemyCount; i++)
		{
			switch (random.nextInt(EnemyType.values().length))
			{
				case 0:
					EnemyList.add(new Enemy(EnemyType.values()[0], map.GetStartTileX(), map.GetStartTileY(), map.GetUnscaledTileSize(),
							map.GetStartDirection(), 1.5f, 3));
					break;
				case 1:
					EnemyList.add(new Enemy(EnemyType.values()[1], map.GetStartTileX(), map.GetStartTileY(), map.GetUnscaledTileSize(),
							map.GetStartDirection(), 2.0f, 5));
					break;
			}
		}
	}
	
	public void Update(float elapsedMilliseconds, Map tileMap, float cameraX, float cameraY)
	{
		enemiesGotAwayThisFrame = 0;
		
		if (EnemyList.size() == 0)
		{
			HasFinished = true;
			return;
		}
		
		if (!EnemyList.get(EnemyList.size() - 1).HasSpawned)
			spawnCounter += elapsedMilliseconds;
		
		for (int i = 0; i < EnemyList.size(); i++)
		{
			if (!EnemyList.get(i).IsAlive())
			{
				EnemyList.remove(i);
				i--;
				continue;
			}
			
			if (EnemyList.get(i).HasGotAway())
			{
				EnemyList.remove(i);
				i--;
				enemiesGotAwayThisFrame++;
				continue;
			}
		}
		
		for (int i = 0; i < EnemyList.size(); i++)
			if (EnemyList.get(i).HasSpawned)
				EnemyList.get(i).Update(elapsedMilliseconds, tileMap, cameraX, cameraY);
		
		if (spawnCounter >= spawnInterval)
		{
			spawnCounter -= spawnInterval;
			
			for (int i = 0; i < EnemyList.size(); i++)
			{
				if (EnemyList.get(i).HasSpawned == false)
				{
					EnemyList.get(i).HasSpawned = true;
					break;
				}
			}
		}
	}
	
	public void Draw(Canvas canvas, float cameraX, float cameraY)
	{
		for (int i = 0; i < EnemyList.size(); i++)
		{
			if (!EnemyList.get(i).HasSpawned)
				continue;
			
			EnemyList.get(i).Draw(canvas, cameraX, cameraY);
		}
	}
	
	public void DrawHealthBars(Canvas canvas, Paint paint, float tileSize, float cameraX, float cameraY)
	{
		for (int i = 0; i < EnemyList.size(); i++)
		{
			if (!EnemyList.get(i).HasSpawned)
				continue;
		
			EnemyList.get(i).DrawHealth(canvas, paint, tileSize, cameraX, cameraY);
		}
	}
	
	public int GetEnemiesGotAwayCount() { return enemiesGotAwayThisFrame; }
}
