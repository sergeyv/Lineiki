package com.ladushki.lineiki;

import java.util.ArrayList;
import java.util.Random;

import org.anddev.andengine.entity.text.ChangeableText;

import android.content.SharedPreferences;
import android.graphics.Point;
import android.widget.Toast;

public class GameLogic implements IGameEvent {
	
	private enum GameState {
		SELECT_BALL,
		SELECT_DESTINATION,
		ANIMATING_MOVING_BALL,
		DROPPING_NEW_BALLS,
		ANIMATING_UNDOING,
		GAME_OVER,
	}

	/* saved state stuff */
	private static final String STATE_VERSION_KEY = "VERSION";
	private static final int STATE_VERSION = 2;
	private static final String STATE_FIELD_KEY = "PLAYING_FIELD";
	private static final String STATE_NEXT_BALLS_KEY = "NEXT_BALLS";
	private static final String STATE_SCORE_KEY = "SCORE";
	private static final String STATE_ZOOM_KEY = "ZOOM";


	GameState mGameState;
	FieldItem [][] mField;
	
	BallDispencer mDispencer; 
	PlayingField mPlayingField;
	private static final Random RANDOM = new Random();

	private ScoreDisplay mScoreDisplay;
	private int mScore;

	private boolean mZoomMode;
	
	Point mSelectedSource;
	Point mSelectedDestination;
	
	HistoryStep mUndoState;
	boolean mCanUndo;


	public GameLogic(PlayingField pPlayingField, BallDispencer pDispencer) {
		mPlayingField = pPlayingField;
		mDispencer = pDispencer;
		mUndoState = new HistoryStep();
		mZoomMode = false;
	}
	
	private void initGame() {
		setScore(0);
		setCanUndo(false);
		this.mGameState = GameState.SELECT_BALL;
		this.mSelectedSource = new Point(-1, -1);
		this.mSelectedDestination = new Point(-1, -1);		
	}

	public void startGame() {
		
		initGame();
		mPlayingField.animateClear(new IAnimationListener() {

			public void done() {
				dropNextBalls();
			}
			
		});
	}

	private void setCanUndo(boolean b) {
		mCanUndo = b;
	}

	private void dropNextBalls() {
		BallColor[] next_colors = mDispencer.getNextBalls();
		mUndoState.mBallsDropped = new FieldItem[3];
		
		for (int i = 0; i < next_colors.length; i++) {
			Point free_pt = getFreeTile();
			if (free_pt == null) {
				this.gameOver();
				return;
			}
			mPlayingField.addBall(free_pt, next_colors[i], i);
			mUndoState.mBallsDropped[i] = new FieldItem(next_colors[i], free_pt.x, free_pt.y);
		}
		mUndoState.mBallsRemovedSecondPass = removeLines();

	}
	
	private void gameOver() {
		mGameState = GameState.GAME_OVER;
		//TODO: add some animation and stuff
	}

	private boolean moveBall(Point pSource, Point pDest) {
		/*
		 * Returns true if the ball is successfully moved, false if
		 * it can't be moved
		 */
		
		Point[] path = findPath(pSource, pDest);
		
		if (path == null) {
			// can not move the ball
			ballTrapped(pSource);
			return false;
		}
		
		mGameState = GameState.ANIMATING_MOVING_BALL; 
		mPlayingField.indicateDestSelected(pDest.x, pDest.y);
		mPlayingField.animateMovingBall(pSource, pDest, path,
				new IAnimationListener() {

					public void done() {
						//addScore(1); // TODO: remove when not needed
						
						// only drop balls if the previous move removed nothing
						FieldItem[] matches = removeLines(); 
						GameLogic.this.mUndoState.mBallsRemovedFirstPass = matches;
						if (matches == null) {			
							dropNextBalls();
						}
						setCanUndo(true);
						GameLogic.this.mGameState = GameState.SELECT_BALL;
						
					}
			
		});
		
		newUndoState(pSource, pDest);
		
		return true;
	}


	private void checkTile(int x, int y, boolean [][] checked, float delay, boolean is_initial_tile) {
		if (x >= 0 && x < mPlayingField.FIELD_WIDTH &&
			y >= 0 && y < mPlayingField.FIELD_HEIGHT &&
			!checked[x][y]) {
				if (is_initial_tile || !mPlayingField.getTileAt(x, y).isOccupied()) {
					mPlayingField.getTileAt(x, y).blink(delay);
					checked[x][y] = true;
					delay = delay + 0.01f;
					checkTile(x-1, y, checked, delay, false);
					checkTile(x+1, y, checked, delay, false);
					checkTile(x, y-1, checked, delay, false);
					checkTile(x, y+1, checked, delay, false);
				}
		}

	}
	
	private void ballTrapped(Point p) {
		/* Indicates that the ball cannot be moved
		 * by recursively finding and blinking all the tiles
		 * which can be reached from the current position
		 */
		boolean [][] checked = new boolean[mPlayingField.FIELD_WIDTH][mPlayingField.FIELD_HEIGHT];
		checkTile(p.x, p.y, checked, 0.0f, true);
		
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
			
			Point p = mPlayingField.removeBalls(tiles_to_remove);
			mPlayingField.showScoreDelta(scoreDelta, p.x, p.y);


			
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

	private void newUndoState(Point pSource, Point pDest) {
		mUndoState.clear();
		mUndoState.mScore = mScore;
		mUndoState.mSource = pSource;
		mUndoState.mDest = pDest;
	}


	public void undoLastStep() {
		
		mGameState = GameState.ANIMATING_UNDOING; 
		setCanUndo(false);
		
		int scoreDelta = mUndoState.mScore - mScore; 
		mScore = mUndoState.mScore;
		mScoreDisplay.setScore(mScore);
		//mPlayingField.showScoreDelta(scoreDelta);
		if (mUndoState.mBallsRemovedSecondPass != null) {
			for (FieldItem f : mUndoState.mBallsRemovedSecondPass) {
				mPlayingField.addBall(new Point(f.mX, f.mY), f.mColor, 0);
			}
		}

		if (mUndoState.mBallsDropped != null) {
			mPlayingField.removeBalls(mUndoState.mBallsDropped);
			BallColor [] colors = new BallColor[3];
			for (int i = 0; i < 3; i++) {
				FieldItem fi = mUndoState.mBallsDropped[i];
				colors[i] = mUndoState.mBallsDropped[i].mColor;
			}
			mDispencer.setBalls(colors);
		}
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
		/// Bove the ball back to its position
		Point[] path = findPath(mUndoState.mDest, mUndoState.mSource);
		
		mPlayingField.animateMovingBall(mUndoState.mDest, mUndoState.mSource, path,
				new IAnimationListener() {

					public void done() {
						GameLogic.this.mGameState = GameState.SELECT_BALL;						
					}
			
		});	
	}
	
	public void loadGameState(SharedPreferences settings) {
	       
	       // check if the config had been created with the same version of the app
	       // if yes then we can assume all the data is in a proper format
	       int config_version = settings.getInt(STATE_VERSION_KEY, 0);
	       if (config_version == STATE_VERSION) {
	    	   
	    	   initGame();
	    	   
	    	   String field = settings.getString(STATE_FIELD_KEY, null);
	    	   //Toast.makeText(this, field, Toast.LENGTH_LONG).show();
	           mPlayingField.deserialize(field);
	           
	    	   String next_balls = settings.getString(STATE_NEXT_BALLS_KEY, null);
	    	   //Toast.makeText(this, next_balls, Toast.LENGTH_LONG).show();
	           mDispencer.deserialize(next_balls);
	           
	           setScore(settings.getInt(STATE_SCORE_KEY, 0));
	           
	           setZoomMode(settings.getBoolean(STATE_ZOOM_KEY, false));
	       } else {
	    	   this.startGame();
	       }
		}

	private void setZoomMode(boolean pZoomMode) {
		this.mZoomMode = pZoomMode;
	}

	public void saveGameState(SharedPreferences settings) {
		
		  // We need an Editor object to make preference changes.
		  // All objects are from android.context.Context

		  SharedPreferences.Editor editor = settings.edit();
		  
		  // remember the version the config had been created with
		  editor.putInt(STATE_VERSION_KEY, STATE_VERSION);
		  // playing field
		  String field = mPlayingField.serialize();
		  editor.putString(STATE_FIELD_KEY, field);
		  //Toast.makeText(this, field, Toast.LENGTH_LONG).show();

		  String next_balls = mDispencer.serialize();
		  editor.putString(STATE_NEXT_BALLS_KEY, next_balls);
		  editor.putInt(STATE_SCORE_KEY, mScore);
		  editor.putBoolean(STATE_ZOOM_KEY, mZoomMode);
		  // Commit the edits!
		  editor.commit();
	}

	
}
