package com.ladushki.lineiki;

import java.util.ArrayList;
import java.util.Random;

import org.anddev.andengine.entity.text.ChangeableText;

import android.graphics.Point;

public class GameLogic implements IGameEvent {
	
	private enum GameState {
		SELECT_BALL,
		SELECT_DESTINATION,
		MOVING_BALL,
		DROPPING_NEW_BALLS
	}

	
	GameState mGameState;
	FieldItem [][] mField;
	
	BallDispencer mDispencer; 
	PlayingField mPlayingField;
	private static final Random RANDOM = new Random();

	private ScoreDisplay mScoreDisplay;
	private int mScore;

	
	
	Point mSelectedSource;
	Point mSelectedDestination;
	
	HistoryStep mUndoState;


	public GameLogic(PlayingField pPlayingField, BallDispencer pDispencer) {
		mPlayingField = pPlayingField;
		mDispencer = pDispencer;
		mUndoState = new HistoryStep();
	}

	public void startGame() {
		
		mScore = 0;
		this.mGameState = GameState.SELECT_BALL;
		this.mSelectedSource = new Point(-1, -1);
		this.mSelectedDestination = new Point(-1, -1);

		try {
			dropNextBalls();
		} catch (GameOverException e) {
			// TODO
		}
	}

	private void dropNextBalls() throws GameOverException {
		BallColor[] next_colors = mDispencer.getNextBalls();
		mUndoState.mBallsDropped = new FieldItem[3];
		
		for (int i = 0; i < next_colors.length; i++) {
			Point free_pt = getFreeTile();
			if (free_pt == null) {
				throw new GameOverException();
			}
			mPlayingField.addBall(free_pt, next_colors[i], i);
			mUndoState.mBallsDropped[i] = new FieldItem(next_colors[i], free_pt.x, free_pt.y);
		}
		mUndoState.mBallsRemovedSecondPass = removeLines();

	}
	
	private boolean moveBall(Point pSource, Point pDest) {
		/*
		 * Returns true if the ball is successfully moved, false if
		 * it can't be moved
		 */
		
		Point[] path = findPath(pSource, pDest);
		
		if (path == null) {
			// TODO: somehow animate that the ball can't be moved
			return false;
		}
		
		mPlayingField.indicateDestSelected(pDest.x, pDest.y);
		mPlayingField.animateMovingBall(pSource, pDest, path);
		
		newUndoState(pSource, pDest);
		
		return true;
	}
	
	private void newUndoState(Point pSource, Point pDest) {
		mUndoState.clear();
		mUndoState.mScore = mScore;
		mUndoState.mSource = pSource;
		mUndoState.mDest = pDest;
	}

	public void onMovingBallFinished() {
		
		//addScore(1); // TODO: remove when not needed
		
		// only drop balls if the previous move removed nothing
		FieldItem[] matches = removeLines(); 
		this.mUndoState.mBallsRemovedFirstPass = matches;
		if (matches == null) {			
			try {
				dropNextBalls();
			} catch (GameOverException e) {
				//gameOver(); TODO: Game over
			}
		}
		this.mGameState = GameState.SELECT_BALL;
	}


	private Point getFreeTile() {
		ArrayList<Point> freePoints = new ArrayList<Point>();
		for (int j = 0; j < mPlayingField.FIELD_HEIGHT; j++) {
			for (int i = 0; i < mPlayingField.FIELD_WIDTH; i++) {
				final BallColor color = mPlayingField.getBallColorAt(i, j);
				if (color == null) {
					freePoints.add(new Point(i, j));
				}
			}
		}
		if (freePoints.size() == 0) {
			return null;
		}
		
		return freePoints.get(RANDOM.nextInt(freePoints.size()));
	}
		
	private Point[] findPath(Point pSource, Point pDest) {
		/*
		 * Finds a path from pSource to pDest, if no path exists returns null
		 */
		
		PathFinder finder = new PathFinder(mPlayingField.FIELD_WIDTH, mPlayingField.FIELD_HEIGHT);
		for (int y = 0; y < mPlayingField.FIELD_HEIGHT; y++) {
			for (int x = 0; x < mPlayingField.FIELD_WIDTH; x++) {
				BallColor color = mPlayingField.getBallColorAt(x, y);
				finder.setPassable(x, y, color == null);
			}
		}
		return finder.findPath(pSource.x, pSource.y, pDest.x, pDest.y);
	}
	
	private FieldItem[] removeLines() {
		/// returns the number of balls removed
		SequenceChecker checker = new SequenceChecker();
		
		/// check horizontals
		for (int j = 0; j < mPlayingField.FIELD_HEIGHT; j++) {
			checker.startRow();
			for (int i = 0; i < mPlayingField.FIELD_WIDTH; i++) {
				final BallColor color = mPlayingField.getBallColorAt(i, j);
				checker.check(color, i, j);
			}
		}
		
		/// check verticals
		for (int j = 0; j < mPlayingField.FIELD_WIDTH; j++) {
			checker.startRow();
			for (int i = 0; i < mPlayingField.FIELD_HEIGHT; i++) {
				final BallColor color = mPlayingField.getBallColorAt(j, i);
				checker.check(color, j, i);
			}
		}

		/// check diagonals (top-right to bottom-left)
		for (int j = 0; j < mPlayingField.FIELD_WIDTH; j++) {
			checker.startRow();
			for (int i = 0; i < mPlayingField.FIELD_HEIGHT; i++) {
				int y = i; 
				int x = j + 4 - y;
				if (x < 0) continue;
				if (x >= mPlayingField.FIELD_WIDTH) continue;
				
				final BallColor color = mPlayingField.getBallColorAt(x, y);
				checker.check(color, x, y);
			}
		}

		/// check diagonals (top-left to bottom-right)
		for (int j = 0; j < mPlayingField.FIELD_WIDTH; j++) {
			checker.startRow();
			for (int i = 0; i < mPlayingField.FIELD_HEIGHT; i++) {
				int y = i; 
				int x = j - 4 + y;
				if (x < 0) continue;
				if (x >= mPlayingField.FIELD_WIDTH) continue;
				
				final BallColor color = mPlayingField.getBallColorAt(x, y);
				checker.check(color, x, y);
			}
		}

		FieldItem[] tiles_to_remove = checker.getMatchedTiles();
		
		if (tiles_to_remove != null) {
			
			/// score stuff
			int scoreDelta = tiles_to_remove.length;
			this.addScore(scoreDelta);
			
			mPlayingField.removeBalls(tiles_to_remove, scoreDelta);
			
		}
		return tiles_to_remove;
	}

	
	public void setScoreDisplay(ScoreDisplay pScore) {
		this.mScoreDisplay = pScore;
		this.mScoreDisplay.setScore(0);
		
	}
	
	public void setScore(int pScore) {
		this.mScore = pScore;
		this.mScoreDisplay.setScore(this.mScore);
	}
	
	public void addScore(int pDelta) {
		setScore(this.mScore + pDelta);
	}

	
	public void onTileTouched(int x, int y) {
		
		final BallColor color = mPlayingField.getBallColorAt(x, y);
		
		switch(this.mGameState) {
		case SELECT_BALL:
			if (color != null) {
				this.mSelectedSource.set(x,y);
				this.mGameState = GameState.SELECT_DESTINATION;
				//tile.startBlinking();
				mPlayingField.indicateSourceSelected(x,y);
			}
			break;
		case SELECT_DESTINATION:
			if (color == null) {
				/// selected an empty cell, move the ball there if possible
				this.mSelectedDestination.set(x,y);
				boolean ballMoved = moveBall(this.mSelectedSource, this.mSelectedDestination);
				mPlayingField.unselectSource();
			} else {
				/// selected another ball, deselect the current one and select the new one
				this.mSelectedSource.set(x,y);
				this.mGameState = GameState.SELECT_DESTINATION;
				mPlayingField.indicateSourceSelected(x,y);
			}
			break;
		}	
	}

	public void undoLastStep() {
		int scoreDelta = mUndoState.mScore - mScore; 
		mScore = mUndoState.mScore;
		mScoreDisplay.setScore(mScore);
		//mPlayingField.showScoreDelta(scoreDelta);
		if (mUndoState.mBallsRemovedSecondPass != null) {
			for (FieldItem f : mUndoState.mBallsRemovedSecondPass) {
				mPlayingField.addBall(new Point(f.mX, f.mY), f.mColor, 0);
			}
		}

		mPlayingField.removeBalls(mUndoState.mBallsDropped, scoreDelta);
		
		/*if (mUndoState.mBallsDropped != null) {
			for (FieldItem f : mUndoState.mBallsDropped) {
				mPlayingField.addBall(new Point(f.mX, f.mY), f.mColor, 0);
			}
		}*/

		if (mUndoState.mBallsRemovedFirstPass != null) {
			for (FieldItem f : mUndoState.mBallsRemovedFirstPass) {
				mPlayingField.addBall(new Point(f.mX, f.mY), f.mColor, 0);
			}
		}
		
		moveBall(mUndoState.mDest, mUndoState.mSource);
		/*Point[] path = findPath(mUndoState.mDest, mUnd);
		
		if (path == null) {
			// TODO: somehow animate that the ball can't be moved
			return false;
		}
		
		mPlayingField.indicateDestSelected(pDest.x, pDest.y);

		mPlayingField.animateMovingBall(mUndoState.mDest, mUndoState.pSource, pPath)*/
		
	}
}
