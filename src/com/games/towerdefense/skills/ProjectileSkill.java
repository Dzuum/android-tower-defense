package com.games.towerdefense.skills;

import java.util.ArrayList;

import com.games.towerdefense.Enemy;
import com.games.towerdefense.GameManager;
import com.games.towerdefense.Map;
import com.games.towerdefense.Projectile;
import com.games.towerdefense.Tower;
import com.games.towerdefense.enums.DamageType;
import com.games.towerdefense.enums.ProjectileType;

public class ProjectileSkill extends Skill
{
	public ProjectileSkill(DamageType damageType, int damage, float rangeTiles, float cooldown)
	{
		super(damageType, damage, rangeTiles, cooldown);
	}
	
	//Vois laittaa directionit luokan muuttujiks ja laskea t‰ss‰, kun periytet‰‰n
	@Override
	public void Attack(Tower tower, ArrayList<Enemy> enemyList, int targetIndex)
	{
		float directionX = enemyList.get(targetIndex).GetScaledOriginX() - tower.GetOriginX(Map.GetTileSize()) * GameManager.GetScale();
		float directionY = enemyList.get(targetIndex).GetScaledOriginY() - tower.GetOriginY(Map.GetTileSize()) * GameManager.GetScale();
		
		float x = tower.GetOriginX(Map.GetTileSize());
		float y = tower.GetOriginY(Map.GetTileSize());
		tower.AddProjectile(new Projectile(ProjectileType.Ball,
				tower.GetOriginX(Map.GetTileSize()) * GameManager.GetScale(), tower.GetOriginY(Map.GetTileSize()) * GameManager.GetScale(),
				directionX, directionY, 10.0f * Map.GetScaledTileSize(), damage));
	}
}
