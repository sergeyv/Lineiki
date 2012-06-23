package com.ladushki.lineiki;

import android.graphics.Point;

public class GameStep {
	
	Point mSource;
	Point mDest;
	FieldItem [] mBallsRemoved;
	FieldItem [] mBallsDropped;
	FieldItem [] mBallsRemovedSecondPass;
	int mScoreDelta;

	public GameStep() {
	}
	
	public void clear() {
		mSource = new Point(-1,-1);
		mDest = new Point(-1,-1);
		mBallsRemoved = null;
		mBallsDropped = null;
		mBallsRemovedSecondPass = null;
		mScoreDelta = 0;
	}

}
