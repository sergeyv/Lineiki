package com.ladushki.lineiki;

public interface IGameEvent {
	
	void onTileTouched(int x, int y);
	
	void onMovingBallFinished();

}
