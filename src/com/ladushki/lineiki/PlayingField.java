/**
 * 
 */
package com.ladushki.lineiki;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import javax.microedition.khronos.opengles.GL10;

import org.anddev.andengine.entity.Entity;
import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.modifier.AlphaModifier;
import org.anddev.andengine.entity.modifier.DelayModifier;
import org.anddev.andengine.entity.modifier.FadeInModifier;
import org.anddev.andengine.entity.modifier.FadeOutModifier;
import org.anddev.andengine.entity.modifier.LoopEntityModifier;
import org.anddev.andengine.entity.modifier.MoveModifier;
import org.anddev.andengine.entity.modifier.ParallelEntityModifier;
import org.anddev.andengine.entity.modifier.PathModifier;
import org.anddev.andengine.entity.modifier.RotationByModifier;
import org.anddev.andengine.entity.modifier.RotationModifier;
import org.anddev.andengine.entity.modifier.ScaleModifier;
import org.anddev.andengine.entity.modifier.SequenceEntityModifier;
import org.anddev.andengine.entity.modifier.IEntityModifier.IEntityModifierListener;
import org.anddev.andengine.entity.modifier.LoopEntityModifier.ILoopEntityModifierListener;
import org.anddev.andengine.entity.modifier.PathModifier.IPathModifierListener;
import org.anddev.andengine.entity.modifier.PathModifier.Path;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.ITouchArea;
import org.anddev.andengine.entity.shape.RectangularShape;
import org.anddev.andengine.entity.shape.Shape;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.sprite.BaseSprite;
import org.anddev.andengine.entity.text.ChangeableText;
import org.anddev.andengine.entity.text.Text;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.util.Debug;
import org.anddev.andengine.util.modifier.IModifier;
import org.anddev.andengine.util.modifier.LoopModifier;
import org.anddev.andengine.util.modifier.ease.EaseSineInOut;

import android.app.Activity;
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

	Activity mParentActivity;
	
	TiledTextureRegion mTextureRegion;
	MapTile [][] mField;
	
	IGameEvent mEvent;
	
	
	public PlayingField(TiledTextureRegion pTextureRegion, Activity pParentActivity) {
		
		mParentActivity = pParentActivity;
		
		this.mTextureRegion = pTextureRegion;	
		mField = new MapTile[FIELD_WIDTH][FIELD_HEIGHT]; 
		initBackground();
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
			mEvent.onTileTouched(x,y);
			return true;
		}
		return false;
	}

	public void setEvent(IGameEvent pEvent) {
		this.mEvent = pEvent;
	}
	
	private void createPathBreadcrumb(int x, int y, int num, int totalDots) {
		AnimatedSprite dot = new AnimatedSprite(x, y, mTextureRegion);
		dot.setCurrentTileIndex(90);
		
		dot.setAlpha(0.0f);
		dot.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		dot.registerEntityModifier(new SequenceEntityModifier(
					new IEntityModifierListener() {

						@Override
						public void onModifierStarted(
								IModifier<IEntity> pModifier, IEntity pItem) {
							// TODO Auto-generated method stub
							
						}

						@Override
						public void onModifierFinished(
								IModifier<IEntity> pModifier, IEntity pItem) {
							
							final IEntity item = pItem;
							mParentActivity.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									//Toast.makeText(mParentActivity, "Sequence finished.", Toast.LENGTH_SHORT).show();
									//pItem.clearEntityModifiers();
									detachChild(item);
								}
							});

							
						}
					},
					new DelayModifier(num * 0.1f),
					new AlphaModifier(0.3f, 0.0f, 1.0f),
					new DelayModifier(0.1f*totalDots)//,
					//new AlphaModifier(0.3f, 1.0f, 0.0f)
				)
		);
		
		attachChild(dot);
	}
	
	public void animateMovingBall(Point pSource, Point pDest, Point [] pPath) {
		Point srcPt = pSource;
		Point destPt = pDest;
		
		/*for (int j = 0; j < FIELD_HEIGHT; j++) {
			for (int i = 0; i < FIELD_WIDTH; i++) {
				final MapTile tile = getTileAt(i,j);
				tile.stopBlinking();
			}
		}*/
		
		MapTile src = getTileAt(srcPt.x, srcPt.y);
		MapTile dest = getTileAt(destPt.x, destPt.y);
		
		final BallSprite ball = src.getBall();
		
		int dotNum = 0;
		
		for (int i = pPath.length - 1; i >= 0; i--) {
			Point p = pPath[i];
			createPathBreadcrumb(p.x*35, p.y*35, dotNum++, pPath.length*2);
			
			if (i > 0) {
				Point next = pPath[i-1];
				p.x = (p.x*35 + next.x*35)/2;
				p.y = (p.y*35 + next.y*35)/2;
				createPathBreadcrumb(p.x, p.y, dotNum++, pPath.length*2);
			}
		}
		
		//final Path path = new Path(5).to(-5, -5).to(-5, 5).to(5, 5).to(5, -5).to(-5, -5);

		/* Add the proper animation when a waypoint of the path is passed. */
		//ball.registerEntityModifier(new MoveModifier(5, srcPt.x*32.0f, destPt.x*32.0f, srcPt.y*32.0f, destPt.y*32.0f)
				
				/*new PathModifier(0.3f, path, null, new IPathModifierListener() {
			@Override
			public void onPathStarted(final PathModifier pPathModifier, final IEntity pEntity) {
				Debug.d("onPathStarted");
			}

			@Override
			public void onPathWaypointStarted(final PathModifier pPathModifier, final IEntity pEntity, final int pWaypointIndex) {
				Debug.d("onPathWaypointStarted:  " + pWaypointIndex);
			}

			@Override
			public void onPathWaypointFinished(final PathModifier pPathModifier, final IEntity pEntity, final int pWaypointIndex) {
				Debug.d("onPathWaypointFinished: " + pWaypointIndex);
			}

			@Override
			public void onPathFinished(final PathModifier pPathModifier, final IEntity pEntity) {
				Debug.d("onPathFinished");
			}
		}, EaseSineInOut.getInstance()));*/


		/*for (int i = 0; i < pPath.length; i++) {
			Point p = pPath[i];
			final MapTile tile = getTileAt(p.x,p.y);
			tile.startBlinking();
		}*/
		dest.setBall(src.detachBall());
		
		//ball.setZIndex(999);

	}
}
