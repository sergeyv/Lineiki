package com.ladushki.lineiki;

import android.graphics.Point;

public class HistoryStep {
	
	Point mSource;
	Point mDest;
	FieldItem [] mBallsRemovedFirstPass;
	FieldItem [] mBallsDropped;
	FieldItem [] mBallsRemovedSecondPass;
	int mScore;

	public HistoryStep() {
	}
	
	public void clear() {
		mSource = new Point(-1,-1);
		mDest = new Point(-1,-1);
		mBallsRemovedFirstPass = null;
		mBallsDropped = null;
		mBallsRemovedSecondPass = null;
		mScore = 0;
	}

}
