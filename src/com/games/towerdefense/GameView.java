package com.games.towerdefense;

import android.content.Context;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class GameView extends SurfaceView implements SurfaceHolder.Callback
{
	private GameLoop	gameLoop;
	private GameManager	gameManager;

	public int			Width		= 0;
	public int			Height		= 0;
	public int			HalfWidth	= 0;
	public int			HalfHeight	= 0;
	
	private int			index;
	private int			id;
	
	public GameView(Context context)
	{
		super(context);
		
		getHolder().addCallback(this);
		
		setFocusable(true);
		
		gameLoop = new GameLoop(getHolder(), this);
		gameManager = gameLoop.gameManager;
	}
	
	@Override
	public boolean onTouchEvent(MotionEvent event)
	{
		switch (event.getActionMasked())
		{
			case MotionEvent.ACTION_DOWN:
			case MotionEvent.ACTION_POINTER_DOWN:
				index = event.getActionIndex();
				id = event.getPointerId(index);
				gameManager.UpdateTouchDown(id, event.getX(index), event.getY(index));
				
				break;
			case MotionEvent.ACTION_MOVE:
				for (int i = 0; i < event.getPointerCount(); i++)
				{
					gameManager.UpdateTouchMove(event.getPointerId(i), event.getX(i), event.getY(i));
				}
				
				break;
			case MotionEvent.ACTION_UP:
			case MotionEvent.ACTION_POINTER_UP:
				index = event.getActionIndex();
				id = event.getPointerId(index);
				gameManager.UpdateTouchUp(id, event.getX(index), event.getY(index));
				
				break;
		}
		
		return true;
	}
	
	public void onPause()
	{
		gameManager.OnPause();
	}
	
	@Override
	public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
		this.Width = width;
		this.Height = height;
		this.HalfWidth = (int)Math.floor(width / 2.0f);
		this.HalfHeight = (int)Math.floor(height / 2.0f);
		
		gameManager.ClampCameraPosition();
	}
	
	@Override
	public void surfaceCreated(SurfaceHolder holder)
	{
		gameLoop.isRunning = true;
		gameLoop.start();
	}
	
	@Override
	public void surfaceDestroyed(SurfaceHolder holder)
	{
		gameLoop.isRunning = false;
		
		boolean retry = true;
		while (retry)
		{
			try
			{
				gameLoop.join();
				retry = false;
			}
			catch (InterruptedException ex) { }
		}
	}
}
