package com.games.towerdefense.skills;

import java.util.ArrayList;

import com.games.towerdefense.Enemy;
import com.games.towerdefense.Map;
import com.games.towerdefense.Tower;
import com.games.towerdefense.enums.DamageType;

public class Skill
{
	protected DamageType damageType;
	protected int damage;
	
	protected float rangeTiles;
	
	private float cooldown;
	private float cooldownTimer;
	
	public float GetCooldown() { return cooldown; }
	
	public Skill(DamageType damageType, int damage, float rangeTiles, float cooldown)
	{
		this.damageType = damageType;
		this.damage = damage;
		
		this.rangeTiles = rangeTiles;
		
		this.cooldown = cooldown;
		cooldownTimer = cooldown;
	}
	
	public void Update(float msElapsed, Tower tower, ArrayList<Enemy> enemyList, int targetIndex)
	{
		cooldownTimer -= msElapsed;
		int targetIndex2 = -1;
		if (cooldownTimer < 0)
		{
			cooldownTimer = 0;
			targetIndex2 = tower.GetTargetIndex(enemyList, rangeTiles);
		}
		
		if (targetIndex2 > -1 && targetIndex2 < enemyList.size())
		{
			Attack(tower, enemyList, targetIndex2);
			cooldownTimer = cooldown;
		}
	}
	
	public void Attack(Tower tower, ArrayList<Enemy> enemyList, int targetIndex)
	{
	}
}
