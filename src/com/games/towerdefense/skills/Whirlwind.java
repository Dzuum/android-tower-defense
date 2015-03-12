package com.games.towerdefense.skills;

import java.util.ArrayList;

import com.games.towerdefense.Enemy;
import com.games.towerdefense.GameManager;
import com.games.towerdefense.Map;
import com.games.towerdefense.Tower;
import com.games.towerdefense.enums.DamageType;

public class Whirlwind extends Skill
{
	public Whirlwind(DamageType damageType, int damage, float rangeTiles, float cooldown)
	{
		super(damageType, damage, rangeTiles, cooldown);
	}
	
	@Override
	public void Attack(Tower tower, ArrayList<Enemy> enemyList, int targetIndex)
	{
		for (int i = 0; i < enemyList.size(); i++)
		{
			float distance = (float)Math.sqrt(
					Math.pow(((tower.GetOriginX(Map.GetTileSize()) * GameManager.GetScale()) - enemyList.get(i).GetScaledOriginX()), 2) +
					Math.pow(((tower.GetOriginY(Map.GetTileSize()) * GameManager.GetScale()) - enemyList.get(i).GetScaledOriginY()), 2));
			
			if (distance <= rangeTiles * Map.GetTileSize() * GameManager.GetScale())
			{
				enemyList.get(i).Damage(damage);
			}
		}
	}
}
