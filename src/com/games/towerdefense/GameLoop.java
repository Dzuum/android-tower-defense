package com.games.towerdefense;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

public class GameLoop extends Thread
{
	private SurfaceHolder	surfaceHolder;
	
	public GameManager		gameManager;
	
	public Boolean			isRunning				= false;
	
	final int				FPS						= 60;
	final int				TICK_SLEEP_MILLISECONDS	= 1000 / FPS;
	
	long					currentTime				= System.nanoTime();
	long					previousTime			= System.nanoTime();
	long					elapsedMilliseconds		= 0;
	long					sleepThisTick			= 0;
	
	private Canvas			canvas					= null;
	
	public GameLoop(SurfaceHolder surfaceHolder, GameView view)
	{
		this.surfaceHolder = surfaceHolder;
		
		gameManager = new GameManager(view.getContext(), view);
	}
	
	@Override
	public void run()
	{
		while (isRunning)
		{
			previousTime = currentTime;
			currentTime = System.nanoTime();
			elapsedMilliseconds = (currentTime - previousTime) / 1000000;
			
			gameManager.Update(elapsedMilliseconds);
			
			canvas = null;
			try
			{
				canvas = surfaceHolder.lockCanvas();
				
				gameManager.Draw(canvas, elapsedMilliseconds);
			}
			catch (Exception ex) { }
			finally
			{
				if (canvas != null)
					surfaceHolder.unlockCanvasAndPost(canvas);
			}
			
			sleepThisTick = TICK_SLEEP_MILLISECONDS - elapsedMilliseconds;
			try
			{
				if (sleepThisTick > 0)
					GameLoop.sleep(sleepThisTick);
			}
			catch (InterruptedException e) { }
		}
	}
}
