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
		/*try {
			dropNextBalls();
		} catch (GameOverException e) {
			// TODO Auto-generated catch block
			//e.printStackTrace();
		}*/
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
}
