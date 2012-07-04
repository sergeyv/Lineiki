/**
 * 
 */
package com.ladushki.lineiki;

import javax.microedition.khronos.opengles.GL10;

import org.anddev.andengine.entity.Entity;
import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.modifier.AlphaModifier;
import org.anddev.andengine.entity.modifier.DelayModifier;
import org.anddev.andengine.entity.modifier.IEntityModifier.IEntityModifierListener;
import org.anddev.andengine.entity.modifier.LoopEntityModifier;
import org.anddev.andengine.entity.modifier.ParallelEntityModifier;
import org.anddev.andengine.entity.modifier.RotationModifier;
import org.anddev.andengine.entity.modifier.ScaleModifier;
import org.anddev.andengine.entity.modifier.SequenceEntityModifier;
import org.anddev.andengine.entity.scene.Scene.ITouchArea;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.util.modifier.IModifier;
import org.anddev.andengine.util.modifier.ease.EaseCubicInOut;

import android.content.Context;
import android.graphics.Point;
import android.os.Vibrator;

/**
 * @author sergey
 * 
 */
public class PlayingField extends Entity {
	final int FIELD_WIDTH = 9;
	final int FIELD_HEIGHT = 9;

	LineikiActivity mParentActivity;

	ITextureProvider mTextureProvider;
	MapTile[][] mField;

	Point mLastTouch;

	Sprite mSelectedSourceMarker;

	IGameEvent mEvent;
	private Sprite mSelectedDestMarker;

	public PlayingField(ITextureProvider pTextureProvider,
			LineikiActivity pParentActivity) {

		mParentActivity = pParentActivity;
		this.mTextureProvider = pTextureProvider;
		mField = new MapTile[FIELD_WIDTH][FIELD_HEIGHT];

		mLastTouch = new Point();

		initBackground();
		initBallMarker();
		initSquareMarker();
	}

	private void initSquareMarker() {
		mSelectedDestMarker = new Sprite(0, 0,
				mTextureProvider.getSquareMarkerTexture());
		this.attachChild(mSelectedDestMarker);
		// mSelectedDestMarker.setVisible(false);
		mSelectedDestMarker.setAlpha(0.0f);
		mSelectedDestMarker.setBlendFunction(GL10.GL_SRC_ALPHA,
				GL10.GL_ONE_MINUS_SRC_ALPHA);

	}

	private void initBackground() {
		int tile_size = mTextureProvider.getTileSize();

		for (int j = 0; j < FIELD_HEIGHT; j++) {
			for (int i = 0; i < FIELD_WIDTH; i++) {

				final MapTile tile = new MapTile(i * tile_size, j * tile_size,
						this.mTextureProvider.getFieldBGTexture().deepCopy(),
						(i + j & 1) == 0);
				this.mField[i][j] = tile;
				this.attachChild(tile);
			}
		}

	}

	public BallSprite addBall(Point pt, BallColor pColor, int num) {
		final MapTile tile = getTileAt(pt.x, pt.y);
		final BallSprite ball = new BallSprite(0, 0, this.mTextureProvider
				.getBallTexture().deepCopy(), pColor);
		tile.setBall(ball);

		ball.setScale(0.0f);

		ball.registerEntityModifier(new SequenceEntityModifier(
				new DelayModifier(0.3f * num), new ScaleModifier(0.3f, 0.0f,
						1.2f), new ScaleModifier(0.2f, 1.2f, 1.0f)));

		return ball;
	}

	public MapTile getTileAt(int pX, int pY) {
		return this.mField[pX][pY];
	}

	public BallColor getBallColorAt(int pX, int pY) {
		MapTile tile = getTileAt(pX, pY);
		BallSprite ball = tile.getBall();
		if (ball != null) {
			return ball.getColor();
		}
		return null;
	}

	public boolean contains(float pX, float pY) {
		int tile_size = mTextureProvider.getTileSize();
		pX -= this.getX();
		pY -= this.getY();
		if (pX < FIELD_WIDTH * tile_size && pY < FIELD_HEIGHT * tile_size) {
			return true;
		}
		return false;
	}

	/*
	 * public boolean onAreaTouched(final TouchEvent pSceneTouchEvent, final
	 * float pTouchAreaLocalX, final float pTouchAreaLocalY) {
	 * 
	 * 
	 * if (pSceneTouchEvent.getPointerID() != 0) { return false; }
	 * 
	 * if (pSceneTouchEvent.isActionUp()) { int tile_size =
	 * mTextureProvider.getTileSize(); final int x = (int) (pTouchAreaLocalX /
	 * tile_size); final int y = (int) (pTouchAreaLocalY / tile_size);
	 * mEvent.onTileTouched(x,y);
	 * 
	 * mLastTouch.x = x * mTextureProvider.getTileSize(); mLastTouch.y = y *
	 * mTextureProvider.getTileSize(); return false; } return false; }
	 */

	public void onTouch(float pX, float pY) {
		int tile_size = mTextureProvider.getTileSize();
		final int x = (int) (pX / tile_size);
		final int y = (int) (pY / tile_size);
		mEvent.onTileTouched(x, y);

		mLastTouch.x = x * mTextureProvider.getTileSize();
		mLastTouch.y = y * mTextureProvider.getTileSize();
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

					public void onModifierStarted(IModifier<IEntity> pModifier,
							IEntity pItem) {
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
				}, new DelayModifier(num * 0.03f), new AlphaModifier(0.1f,
						0.0f, 1.0f), new DelayModifier(0.03f * totalDots),
				new AlphaModifier(0.2f, 1.0f, 0.0f)));

		attachChild(dot);
	}

	public void animateMovingBall(Point pSource, Point pDest, Point[] pPath,
			final IAnimationListener listener) {
		Point srcPt = pSource;
		Point destPt = pDest;
		int dotNum = 0;

		int tile_size = mTextureProvider.getTileSize();

		for (int i = pPath.length - 1; i >= 0; i--) {
			Point p = pPath[i];
			createPathBreadcrumb(p.x * tile_size, p.y * tile_size, dotNum++,
					pPath.length * 2);

			if (i > 0) {
				Point next = pPath[i - 1];
				p.x = (p.x * tile_size + next.x * tile_size) / 2;
				p.y = (p.y * tile_size + next.y * tile_size) / 2;
				createPathBreadcrumb(p.x, p.y, dotNum++, pPath.length * 2);
			}
		}

		final MapTile src = getTileAt(srcPt.x, srcPt.y);
		final MapTile dest = getTileAt(destPt.x, destPt.y);

		final BallSprite ball = src.getBall();

		ball.registerEntityModifier(new SequenceEntityModifier(
				new ScaleModifier(0.2f, 1.0f, 1.2f),
				new ScaleModifier(0.3f, 1.2f, 0.0f),
				new DelayModifier(0.0f, new IEntityModifierListener() {

					public void onModifierStarted(IModifier<IEntity> pModifier,
							IEntity pItem) {
					}

					public void onModifierFinished(
							IModifier<IEntity> pModifier, IEntity pItem) {
						dest.setBall(src.detachBall());
					}
				}), new ScaleModifier(0.3f, 0.0f, 1.2f),
				new ScaleModifier(0.2f, 1.2f, 1.0f), new DelayModifier(0.0f,
						new IEntityModifierListener() {

							public void onModifierStarted(
									IModifier<IEntity> pModifier, IEntity pItem) {
							}

							public void onModifierFinished(
									IModifier<IEntity> pModifier, IEntity pItem) {
								listener.done();
							}
						})));

		// final Path path = new Path(5).to(-5, -5).to(-5, 5).to(5, 5).to(5,
		// -5).to(-5, -5);

		/* Add the proper animation when a waypoint of the path is passed. */
		// ball.registerEntityModifier(new MoveModifier(5, srcPt.x*32.0f,
		// destPt.x*32.0f, srcPt.y*32.0f, destPt.y*32.0f)

		/*
		 * new PathModifier(0.3f, path, null, new IPathModifierListener() {
		 * 
		 * @Override public void onPathStarted(final PathModifier pPathModifier,
		 * final IEntity pEntity) { Debug.d("onPathStarted"); }
		 * 
		 * @Override public void onPathWaypointStarted(final PathModifier
		 * pPathModifier, final IEntity pEntity, final int pWaypointIndex) {
		 * Debug.d("onPathWaypointStarted:  " + pWaypointIndex); }
		 * 
		 * @Override public void onPathWaypointFinished(final PathModifier
		 * pPathModifier, final IEntity pEntity, final int pWaypointIndex) {
		 * Debug.d("onPathWaypointFinished: " + pWaypointIndex); }
		 * 
		 * @Override public void onPathFinished(final PathModifier
		 * pPathModifier, final IEntity pEntity) { Debug.d("onPathFinished"); }
		 * }, EaseSineInOut.getInstance()));
		 */

		/*
		 * for (int i = 0; i < pPath.length; i++) { Point p = pPath[i]; final
		 * MapTile tile = getTileAt(p.x,p.y); tile.startBlinking(); }
		 */
		// ball.setZIndex(999);

	}

	public void clearTile(final MapTile tile) {
		BallSprite ball = tile.getBall();
		if (ball == null) {
			return;
		}
		ball.registerEntityModifier(new SequenceEntityModifier(
				new IEntityModifierListener() {

					public void onModifierStarted(IModifier<IEntity> pModifier,
							IEntity pItem) {
					}

					public void onModifierFinished(
							IModifier<IEntity> pModifier, IEntity pItem) {

						mParentActivity.runOnUpdateThread(new Runnable() {
							public void run() {
								tile.setBall(null);
							}
						});

					}
				}, new ScaleModifier(0.2f, 1.0f, 1.2f), new ScaleModifier(0.3f,
						1.2f, 0.0f)));

	}

	/* Ball (source) marker stuff */
	private void initBallMarker() {
		mSelectedSourceMarker = new Sprite(0, 0,
				mTextureProvider.getBallMarkerTexture());
		this.attachChild(mSelectedSourceMarker);
		mSelectedSourceMarker.registerEntityModifier(new LoopEntityModifier(
				new RotationModifier(0.3f, 0, 360)));
		mSelectedSourceMarker.setVisible(false);
	}

	public void indicateSourceSelected(int x, int y) {
		mSelectedSourceMarker.setVisible(true);
		mSelectedSourceMarker.setPosition(x * mTextureProvider.getTileSize(), y
				* mTextureProvider.getTileSize());
	}

	public void unselectSource() {
		mSelectedSourceMarker.setVisible(false);
	}

	public void indicateDestSelected(int x, int y) {
		mSelectedDestMarker.setVisible(true);
		mSelectedDestMarker.setPosition(x * mTextureProvider.getTileSize(), y
				* mTextureProvider.getTileSize());
		mSelectedDestMarker.registerEntityModifier(new ParallelEntityModifier(
				new ScaleModifier(2.0f, 1, 3), new AlphaModifier(2.0f, 1.0f,
						0.0f)));

	}

	public void showScoreDelta(int pDelta, float x, float y) {
		ScorePopup p = new ScorePopup(this.mTextureProvider, pDelta);
		p.setPosition(x, y);

		this.attachChild(p);
		p.registerEntityModifier(new ParallelEntityModifier(
				new IEntityModifierListener() {

					public void onModifierStarted(IModifier<IEntity> pModifier,
							IEntity pItem) {
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

				new ScaleModifier(2.0f, 1, 3)
		// / AlphaModifier doesn't work with sub-objects of an Entity
		// / so we're applying it separately to each digit in ScorePopup
		));
	}

	public void showScoreDelta(int pDelta) {
		showScoreDelta(pDelta, mLastTouch.x, mLastTouch.y);
	}

	public Point removeBalls(FieldItem[] tiles_to_remove) {
		/*
		 * Removes balls. Returns a Point which contains an approximate center
		 * of the balls removed for UI purposes (displaying score popup)
		 */
		int x = 0;
		int y = 0;
		for (FieldItem tile : tiles_to_remove) {
			x += tile.mX * mTextureProvider.getTileSize();
			y += tile.mY * mTextureProvider.getTileSize();
			this.clearTile(getTileAt(tile.mX, tile.mY));
		}
		x = x / tiles_to_remove.length;
		y = y / tiles_to_remove.length;

		return new Point(x, y);
	}

	public void animateClear(final IAnimationListener listener) {
		/*
		 * Removes all the balls from the field Need to make sure that the field
		 * animation takes longer than the removing balls animation, because the
		 * callback is invoked when the _field animation_ is complete
		 */
		for (int j = 0; j < FIELD_HEIGHT; j++) {
			for (int i = 0; i < FIELD_WIDTH; i++) {
				MapTile tile = getTileAt(i, j);
				if (tile.getBall() != null) {
					this.clearTile(tile);
				}
			}
		}

		this.setScaleCenter(FIELD_WIDTH * mTextureProvider.getTileSize() / 2,
				FIELD_HEIGHT * mTextureProvider.getTileSize() / 2);
		this.registerEntityModifier(new SequenceEntityModifier(
				new IEntityModifierListener() {

					public void onModifierStarted(IModifier<IEntity> pModifier,
							IEntity pItem) {
					}

					public void onModifierFinished(
							IModifier<IEntity> pModifier, IEntity pItem) {
						listener.done();
					}
				}, new ScaleModifier(0.4f, 1.0f, 0.3f), new ScaleModifier(0.6f,
						0.3f, 1.0f)));
	}

	public String serialize() {
		/*
		 * converts the field to a simple text representation suitable for
		 * storing in prefs etc. Not terribly efficient.
		 */
		String out = new String();
		for (int j = 0; j < FIELD_HEIGHT; j++) {
			String line = new String();
			for (int i = 0; i < FIELD_WIDTH; i++) {
				final BallColor color = getBallColorAt(i, j);
				if (color == null) {
					line += " ";
				} else {
					line += color.toChar();
				}
			}
			line += "\n";
			out += line;
		}
		return out;
	}

	public void deserialize(String data) {
		String[] lines = data.split("\n");
		for (int j = 0; j < FIELD_HEIGHT; j++) {
			String line = lines[j];
			for (int i = 0; i < FIELD_WIDTH; i++) {
				MapTile tile = getTileAt(i, j);
				final BallColor color = BallColor.fromChar(line.charAt(i));
				if (color != null) {
					final BallSprite ball = new BallSprite(0, 0,
							this.mTextureProvider.getBallTexture().deepCopy(),
							color);
					tile.setBall(ball);
				}
			}
		}
	}

	public void vibrate_3_times() {

		int d = 100;
		long[] pattern = { 0, // Start immediately
				d, d, d, d, d, d };
		// Get instance of Vibrator from current Context
		Vibrator v = (Vibrator) mParentActivity
				.getSystemService(Context.VIBRATOR_SERVICE);
		v.vibrate(pattern, -1);
	}

	public void zoomIn(int x, int y) {
		this.setScaleCenter((x + 0.5f) * mTextureProvider.getTileSize(),
				(y + 0.5f) * mTextureProvider.getTileSize());
		this.registerEntityModifier(new ScaleModifier(1.0f, 1.0f, 1.5f,
				EaseCubicInOut.getInstance()));
	}

	public void zoomOut() {
		this.registerEntityModifier(new ScaleModifier(1.0f, 1.5f, 1.0f,
				EaseCubicInOut.getInstance()));
	}

	public void animateHighScoreReached() {
		int w = this.mTextureProvider.getTileSize();
		Sprite p = new Sprite(w*4.5f, w*4.5f, this.mTextureProvider.getHighscoreReachedTexture());
		this.attachChild(p);
		
		p.registerEntityModifier(new ParallelEntityModifier(
				new IEntityModifierListener() {

					public void onModifierStarted(IModifier<IEntity> pModifier,
							IEntity pItem) {
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

				new ScaleModifier(2.0f, 1, 3),
				new AlphaModifier(2.0f, 0.6f, 0.0f)
		));
	}

	/*
	 * private void flash_red_led() { NotificationManager nm = (
	 * NotificationManager ) mParentActivity.getSystemService(
	 * Context.NOTIFICATION_SERVICE ); Notification notif = new Notification();
	 * notif.ledARGB = 0xFFff0000; notif.flags = Notification.FLAG_SHOW_LIGHTS;
	 * notif.ledOnMS = 100; notif.ledOffMS = 100; nm.notify(0, notif); }
	 */

}
