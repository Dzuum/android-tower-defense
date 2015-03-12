package com.games.towerdefense.skills;

import java.util.ArrayList;

import com.games.towerdefense.Enemy;
import com.games.towerdefense.Tower;
import com.games.towerdefense.enums.DamageType;

public class Lunge extends Skill
{
	public Lunge(DamageType damageType, int damage, float rangeTiles, float cooldown)
	{
		super(damageType, damage, rangeTiles, cooldown);
	}
	
	@Override
	public void Attack(Tower tower, ArrayList<Enemy> enemyList, int targetIndex)
	{
		enemyList.get(targetIndex).Damage(damage);
	}
}
