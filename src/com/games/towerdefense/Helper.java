package com.games.towerdefense;

public final class Helper
{
	public static float Floor(float value, int decimalPlaces)
	{
		return (float)(Math.floor(value * Math.pow(10, decimalPlaces)) / Math.pow(10, decimalPlaces));
	}
	
	public static float Round(float value, int decimalPlaces)
	{
		return (float)(Math.round(value * Math.pow(10, decimalPlaces)) / Math.pow(10, decimalPlaces));
	}
	
	public static float Ceil(float value, int decimalPlaces)
	{
		return (float)(Math.ceil(value * Math.pow(10, decimalPlaces)) / Math.pow(10, decimalPlaces));
	}
}
