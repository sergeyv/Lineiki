/**
 * 
 */
package com.ladushki.lineiki;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import org.anddev.andengine.entity.Entity;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.ITouchArea;
import org.anddev.andengine.entity.shape.RectangularShape;
import org.anddev.andengine.entity.shape.Shape;
import org.anddev.andengine.entity.sprite.BaseSprite;
import org.anddev.andengine.entity.text.ChangeableText;
import org.anddev.andengine.entity.text.Text;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;

import android.graphics.Point;
import android.widget.Toast;

/**
 * @author sergey
 *
 */
public class PlayingField extends Entity implements ITouchArea {
	
	final int FIELD_WIDTH = 9;
	final int FIELD_HEIGHT = 9;
	
	final int TILE_WIDTH = 35;
	final int TILE_HEIGHT = 35;

	private static final Random RANDOM = new Random();

	GameState mGameState;
	TiledTextureRegion mTextureRegion;
	BallDispencer mDispencer; 
	MapTile [][] mField;
	
	
	Point mSelectedSource;
	Point mSelectedDestination;
	private ChangeableText mScoreField;
	private int mScore;
	
	public PlayingField(TiledTextureRegion pTextureRegion, BallDispencer pDispencer) {
		this.mTextureRegion = pTextureRegion;	
		this.mDispencer = pDispencer;
		this.mGameState = GameState.SELECT_BALL;
		this.mSelectedSource = new Point(-1, -1);
		this.mSelectedDestination = new Point(-1, -1);
		mScore = 0;
		mField = new MapTile[FIELD_WIDTH][FIELD_HEIGHT]; 
		initBackground();
		try {
			dropNextBalls();
		} catch (GameOverException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}
	}

	private void initBackground() {
		for (int j = 0; j < FIELD_HEIGHT; j++) {
			for (int i = 0; i < FIELD_WIDTH; i++) {
				
				final MapTile tile = new MapTile(i, j, this.mTextureRegion.deepCopy(), (i+j & 1) == 0);
				this.mField[i][j] = tile;
				this.attachChild(tile);
			}
		}

	}
	
	public BallSprite addBall(int pX, int pY, BallColor pColor) {
		final BallSprite ball = new BallSprite(0, 0, this.mTextureRegion.deepCopy(), pColor);
		this.getTileAt(pX, pY).setBall(ball);
		
		return ball;

	}

	public BallSprite addBall(MapTile tile, BallColor pColor) {
		final BallSprite ball = new BallSprite(0, 0, this.mTextureRegion.deepCopy(), pColor);
		tile.setBall(ball);
		
		return ball;

	}

	public MapTile getTileAt(int pX, int pY) {
		return this.mField[pX][pY];
	}

	@Override
	public boolean contains(float pX, float pY) {
		if (pX < FIELD_WIDTH * TILE_WIDTH && pY < FIELD_HEIGHT*TILE_HEIGHT) {
			return true;
		}
		return false;
	}

	@Override
	public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
		if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
			final int x = (int) pSceneTouchEvent.getX() / TILE_WIDTH;
			final int y = (int) pSceneTouchEvent.getY() / TILE_HEIGHT;
			
			final MapTile tile = this.getTileAt(x, y);
			final BallSprite ball = tile.getBall();
			
			switch(this.mGameState) {
			case SELECT_BALL:
				if (ball != null) {
					this.mSelectedSource.set(x,y);
					this.mGameState = GameState.SELECT_DESTINATION;
					tile.startBlinking();
				}
				break;
			case SELECT_DESTINATION:
				if (ball == null) {
					/// selected an empty cell, move the ball there if possible
					this.mSelectedDestination.set(x,y);
					boolean ballMoved = moveBall(this.mSelectedSource, this.mSelectedDestination);
					this.mGameState = GameState.SELECT_BALL;
					if (ballMoved) {
						try {
							dropNextBalls();
						} catch (GameOverException e) {
							gameOver();
						}
					}
					MapTile prevTile = getTileAt(mSelectedSource.x, mSelectedSource.y);
					prevTile.stopBlinking();
				} else {
					/// selected another ball, deselect the current one and select the new one
					MapTile prevTile = getTileAt(mSelectedSource.x, mSelectedSource.y);
					prevTile.stopBlinking();
					this.mSelectedSource.set(x,y);
					this.mGameState = GameState.SELECT_DESTINATION;
					tile.startBlinking();
				}
				break;
			}		
			return true;
		}
		return false;
	}

	private void gameOver() {
		
		
	}

	private void dropNextBalls() throws GameOverException {
		BallColor[] next_colors = mDispencer.getNextBalls();
		for (int i = 0; i < next_colors.length; i++) {
			MapTile free_tile = getFreeTile();
			if (free_tile == null) {
				throw new GameOverException();
			}
			addBall(free_tile, next_colors[i]);
		}
		removeLines();

	}
	
	private boolean moveBall(Point pSource, Point pDest) {
		/*
		 * Returns true if the ball is successfully moved, false if
		 * it can't be moved
		 */
		MapTile src = getTileAt(pSource.x, pSource.y);
		MapTile dest = getTileAt(pDest.x, pDest.y);
		
		for (int j = 0; j < FIELD_HEIGHT; j++) {
			for (int i = 0; i < FIELD_WIDTH; i++) {
				final MapTile tile = this.mField[i][j];
				tile.stopBlinking();
			}
		}

		Point[] path = findPath(pSource, pDest);
		
		if (path == null) {
			return false;
		}
					
		for (int i = 0; i < path.length; i++) {
			Point p = path[i];
			final MapTile tile = this.mField[p.x][p.y];
			tile.startBlinking();
		}
		dest.setBall(src.detachBall());
		removeLines();
		return true;
	}

	MapTile getFreeTile() {
		ArrayList<MapTile> freePoints = new ArrayList<MapTile>();
		for (int j = 0; j < FIELD_HEIGHT; j++) {
			for (int i = 0; i < FIELD_WIDTH; i++) {
				final MapTile tile = this.mField[i][j];
				if (tile.getBall() == null) {
					freePoints.add(tile);
				}
			}
		}
		if (freePoints.size() == 0) {
			return null;
		}
		
		return freePoints.get(RANDOM.nextInt(freePoints.size()));
	}
	
	Point[] findPath(Point pSource, Point pDest) {
		/*
		 * Finds a path from pSource to pDest, if no path exists returns null
		 */
		
		PathFinder finder = new PathFinder(FIELD_WIDTH, FIELD_HEIGHT);
		for (int y = 0; y < FIELD_HEIGHT; y++) {
			for (int x = 0; x < FIELD_WIDTH; x++) {
				MapTile tile = this.getTileAt(x, y);
				finder.setPassable(x, y, !tile.isOccupied());
			}
		}
		return finder.findPath(pSource.x, pSource.y, pDest.x, pDest.y);
	}
	
	void removeLines() {
		SequenceChecker checker = new SequenceChecker();
		
		/// check horizontals
		for (int j = 0; j < FIELD_HEIGHT; j++) {
			checker.startRow();
			for (int i = 0; i < FIELD_WIDTH; i++) {
				final MapTile tile = this.mField[i][j];
				checker.check(tile);
			}
		}
		
		/// check verticals
		for (int j = 0; j < FIELD_WIDTH; j++) {
			checker.startRow();
			for (int i = 0; i < FIELD_HEIGHT; i++) {
				final MapTile tile = this.mField[j][i];
				checker.check(tile);
			}
		}

		/// check diagonals (top-right to bottom-left)
		for (int j = 0; j < FIELD_WIDTH; j++) {
			checker.startRow();
			for (int i = 0; i < FIELD_HEIGHT; i++) {
				int y = i; 
				int x = j + 4 - y;
				if (x < 0) continue;
				if (x >= FIELD_WIDTH) continue;
				
				final MapTile tile = this.mField[x][y];
				checker.check(tile);
			}
		}

		/// check diagonals (top-left to bottom-right)
		for (int j = 0; j < FIELD_WIDTH; j++) {
			checker.startRow();
			for (int i = 0; i < FIELD_HEIGHT; i++) {
				int y = i; 
				int x = j - 4 + y;
				if (x < 0) continue;
				if (x >= FIELD_WIDTH) continue;
				
				final MapTile tile = this.mField[x][y];
				checker.check(tile);
			}
		}

		MapTile[] tiles_to_remove = checker.getMatchedTiles();
		
		if (tiles_to_remove != null) {
			
			/// score stuff
			this.addScore(tiles_to_remove.length);
			
			for (MapTile tile : tiles_to_remove) {
				tile.setBall(null);
			}
		}
	}

	public void setScoreField(ChangeableText pField) {
		this.mScoreField = pField;
		this.mScoreField.setText("!!!");
		
	}
	
	public void setScore(int pScore) {
		this.mScore = pScore;
		this.mScoreField.setText(Integer.toString(mScore));
	}
	
	public void addScore(int pScore) {
		setScore(this.mScore + pScore);
	}
}
