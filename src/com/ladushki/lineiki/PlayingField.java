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
import org.anddev.andengine.entity.sprite.Sprite;
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

	LineikiActivity mParentActivity;
	
	ITextureProvider mTextureProvider;
	MapTile [][] mField;
	
	Sprite mSelectedSourceMarker;
	
	IGameEvent mEvent;
	private Sprite mSelectedDestMarker;
	
	public PlayingField(ITextureProvider pTextureProvider, LineikiActivity pParentActivity) {
		
		mParentActivity = pParentActivity;		
		this.mTextureProvider = pTextureProvider;	
		mField = new MapTile[FIELD_WIDTH][FIELD_HEIGHT]; 

		initBackground();
		initBallMarker();
		initSquareMarker();
	}

	private void initSquareMarker() {
		mSelectedDestMarker = new Sprite(0, 0, mTextureProvider.getSquareMarkerTexture());
		this.attachChild(mSelectedDestMarker);
		//mSelectedDestMarker.setVisible(false);
		mSelectedDestMarker.setAlpha(0.0f);
		mSelectedDestMarker.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

	}

	private void initBackground() {
		int tile_size = mTextureProvider.getTileSize();

		for (int j = 0; j < FIELD_HEIGHT; j++) {
			for (int i = 0; i < FIELD_WIDTH; i++) {
				
				final MapTile tile = new MapTile(i*tile_size, j*tile_size, this.mTextureProvider.getFieldBGTexture().deepCopy(), (i+j & 1) == 0);
				this.mField[i][j] = tile;				
				this.attachChild(tile);
			}
		}

	}
	
	public BallSprite addBall(MapTile tile, BallColor pColor, int num) {
		final BallSprite ball = new BallSprite(0, 0, this.mTextureProvider.getBallTexture().deepCopy(), pColor);
		tile.setBall(ball);
		
		ball.setScale(0.0f);
		
		ball.registerEntityModifier(new SequenceEntityModifier(
				new DelayModifier(0.3f*num),
				new ScaleModifier(0.3f, 0.0f, 1.2f),
				new ScaleModifier(0.2f, 1.2f, 1.0f)
				));

		return ball;
	}

	public MapTile getTileAt(int pX, int pY) {
		return this.mField[pX][pY];
	}

	public boolean contains(float pX, float pY) {
		int tile_size = mTextureProvider.getTileSize();
		pX -= this.getX();
		pY -= this.getY();
		if (pX < FIELD_WIDTH * tile_size && pY < FIELD_HEIGHT*tile_size) {
			return true;
		}
		return false;
	}

	public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final float pTouchAreaLocalX, final float pTouchAreaLocalY) {
		int tile_size = mTextureProvider.getTileSize();
		if (pSceneTouchEvent.getAction() == TouchEvent.ACTION_DOWN) {
			final int x = (int) ((pSceneTouchEvent.getX() - this.getX()) / tile_size);
			final int y = (int) ((pSceneTouchEvent.getY() - this.getY())  / tile_size);
			mEvent.onTileTouched(x,y);
			return true;
		}
		return false;
	}

	public void setEvent(IGameEvent pEvent) {
		this.mEvent = pEvent;
	}
	
	private void createPathBreadcrumb(int x, int y, int num, int totalDots) {
		Sprite dot = new Sprite(x, y, mTextureProvider.getDotTexture());
		
		dot.setAlpha(0.0f);
		dot.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);

		dot.registerEntityModifier(new SequenceEntityModifier(
					new IEntityModifierListener() {

						public void onModifierStarted(
								IModifier<IEntity> pModifier, IEntity pItem) {
						}

						public void onModifierFinished(
								IModifier<IEntity> pModifier, IEntity pItem) {
							
							final IEntity item = pItem;
							mParentActivity.runOnUpdateThread(new Runnable() {
								public void run() {
									detachChild(item);
								}
							});

							
						}
					},
					new DelayModifier(num * 0.03f),
					new AlphaModifier(0.1f, 0.0f, 1.0f),
					new DelayModifier(0.03f*totalDots),
					new AlphaModifier(0.2f, 1.0f, 0.0f)
				)
		);
		
		attachChild(dot);
	}
	
	public void animateMovingBall(Point pSource, Point pDest, Point [] pPath) {
		Point srcPt = pSource;
		Point destPt = pDest;
		int dotNum = 0;

		int tile_size = mTextureProvider.getTileSize();
		
		for (int i = pPath.length - 1; i >= 0; i--) {
			Point p = pPath[i];
			createPathBreadcrumb(p.x*tile_size, p.y*tile_size, dotNum++, pPath.length*2);
			
			if (i > 0) {
				Point next = pPath[i-1];
				p.x = (p.x*tile_size + next.x*tile_size)/2;
				p.y = (p.y*tile_size + next.y*tile_size)/2;
				createPathBreadcrumb(p.x, p.y, dotNum++, pPath.length*2);
			}
		}
		
		final MapTile src = getTileAt(srcPt.x, srcPt.y);
		final MapTile dest = getTileAt(destPt.x, destPt.y);
		
		final BallSprite ball = src.getBall();

		/// wait until the dots are drawn
		/*registerEntityModifier(
				})
			);*/
		
		ball.registerEntityModifier(new SequenceEntityModifier(
					new ScaleModifier(0.2f, 1.0f, 1.2f),
					new ScaleModifier(0.3f, 1.2f, 0.0f),
					new DelayModifier(0.0f,
						new IEntityModifierListener() {

							public void onModifierStarted(IModifier<IEntity> pModifier,
									IEntity pItem) {
							}

							public void onModifierFinished(
									IModifier<IEntity> pModifier, IEntity pItem) {
								dest.setBall(src.detachBall());
							}
						}),
					new ScaleModifier(0.3f, 0.0f, 1.2f),
					new ScaleModifier(0.2f, 1.2f, 1.0f),
					new DelayModifier(0.0f,
							new IEntityModifierListener() {

								public void onModifierStarted(IModifier<IEntity> pModifier,
										IEntity pItem) {
								}

								public void onModifierFinished(
										IModifier<IEntity> pModifier, IEntity pItem) {
									mEvent.onMovingBallFinished();
								}
							})
					));
		
		
		
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
		//ball.setZIndex(999);

	}

	public void clearTile(final MapTile tile) {
		BallSprite ball = tile.getBall();
		ball.registerEntityModifier(new SequenceEntityModifier(
				new IEntityModifierListener() {

					public void onModifierStarted(
							IModifier<IEntity> pModifier, IEntity pItem) {
					}

					public void onModifierFinished(
							IModifier<IEntity> pModifier, IEntity pItem) {
						
						final IEntity item = pItem;
						mParentActivity.runOnUpdateThread(new Runnable() {
							public void run() {
								tile.setBall(null);
							}
						});

						
					}
				},
				new ScaleModifier(0.2f, 1.0f, 1.2f),
				new ScaleModifier(0.3f, 1.2f, 0.0f)
			)
	);

	}

	/* Ball (source) marker stuff */
	private void initBallMarker() {
		mSelectedSourceMarker = new Sprite(0, 0, mTextureProvider.getBallMarkerTexture());
		this.attachChild(mSelectedSourceMarker);
		mSelectedSourceMarker.registerEntityModifier(new LoopEntityModifier(
				new RotationModifier(0.3f, 0, 360)
				));
		mSelectedSourceMarker.setVisible(false);
	}


	public void indicateSourceSelected(int x, int y) {
		mSelectedSourceMarker.setVisible(true);
		mSelectedSourceMarker.setPosition(x*mTextureProvider.getTileSize(), y*mTextureProvider.getTileSize());
	}
	
	public void unselectSource() {
		mSelectedSourceMarker.setVisible(false);
	}
	
	public void indicateDestSelected(int x, int y) {
		mSelectedDestMarker.setVisible(true);
		mSelectedDestMarker.setPosition(x*mTextureProvider.getTileSize(), y*mTextureProvider.getTileSize());
		mSelectedDestMarker.registerEntityModifier(new ParallelEntityModifier(
				new ScaleModifier(2.0f, 1, 3),
				new AlphaModifier(2.0f, 1.0f, 0.0f)
				));
		
	}

}
