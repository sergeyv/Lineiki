package com.ladushki.lineiki;

import javax.microedition.khronos.opengles.GL10;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.ZoomCamera;
import org.anddev.andengine.engine.camera.hud.HUD;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.scene.menu.MenuScene;
import org.anddev.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.anddev.andengine.entity.scene.menu.item.IMenuItem;
import org.anddev.andengine.entity.scene.menu.item.SpriteMenuItem;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.extension.input.touch.controller.MultiTouch;
import org.anddev.andengine.extension.input.touch.controller.MultiTouchController;
import org.anddev.andengine.extension.input.touch.detector.PinchZoomDetector;
import org.anddev.andengine.extension.input.touch.detector.PinchZoomDetector.IPinchZoomDetectorListener;
import org.anddev.andengine.extension.input.touch.exception.MultiTouchException;
import org.anddev.andengine.extension.svg.opengl.texture.atlas.bitmap.SVGBitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.input.touch.detector.ClickDetector;
import org.anddev.andengine.input.touch.detector.ClickDetector.IClickDetectorListener;
import org.anddev.andengine.input.touch.detector.HoldDetector;
import org.anddev.andengine.input.touch.detector.HoldDetector.IHoldDetectorListener;
import org.anddev.andengine.input.touch.detector.ScrollDetector;
import org.anddev.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
import org.anddev.andengine.input.touch.detector.SurfaceScrollDetector;
import org.anddev.andengine.opengl.font.Font;
import org.anddev.andengine.opengl.font.FontFactory;
import org.anddev.andengine.opengl.font.FontManager;
import org.anddev.andengine.opengl.texture.TextureOptions;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BitmapTextureAtlasTextureRegionFactory;
import org.anddev.andengine.opengl.texture.atlas.bitmap.BuildableBitmapTextureAtlas;
import org.anddev.andengine.opengl.texture.atlas.bitmap.source.IBitmapTextureAtlasSource;
import org.anddev.andengine.opengl.texture.atlas.buildable.builder.BlackPawnTextureBuilder;
import org.anddev.andengine.opengl.texture.atlas.buildable.builder.ITextureBuilder.TextureAtlasSourcePackingException;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import org.anddev.andengine.util.Debug;

import android.app.Dialog;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.widget.Toast;

public class LineikiActivity 
	extends 
		BaseGameActivity 
	implements 
		ITextureProvider, 
		IOnMenuItemClickListener, 
		IScrollDetectorListener, 
		IPinchZoomDetectorListener, 
		IOnSceneTouchListener,
		IClickDetectorListener
  {

	/*static final int CAMERA_WIDTH = 320;
	static final int CAMERA_HEIGHT = 480;*/
	
	protected static final int MENU_RESET = 0;
	protected static final int MENU_UNDO = 1;
	protected static final int MENU_QUIT = 2;

	private static final int COUNT = 0;
	
	private static final String PREFS_NAME = "LineikiPrefsFile";

	
	private ZoomCamera mCamera;
	/*private BitmapTextureAtlas mTexture;
	private TiledTextureRegion mTextureRegion;*/
	private BitmapTextureAtlas mFontTexture;
	private Font mFont;
	
	
	private GameLogic mGameLogic;
	private BuildableBitmapTextureAtlas mBuildableBitmapTextureAtlas;
	private TiledTextureRegion mBallTextureRegion;
	private TiledTextureRegion mFieldBgTextureRegion;
	private TextureRegion mDotTextureRegion;
	private TextureRegion mMenuNewGame;
	private TextureRegion mMenuUndo;
	private TextureRegion mMenuQuit;
	private Scene mMainScene;
	private MenuScene mMenuScene;
	private HUD mHUD;
	
	private int mScreenWidth;
	private int mScreenHeight;
	private int mLeftBorder;
	private TextureRegion mScoreFieldBackground;
	private TiledTextureRegion mScoreDigits;
	private TextureRegion mBallMarkerRegion;
	private TextureRegion mSquareMarkerRegion;
	
	
	private HoldDetector mHold;
	private SurfaceScrollDetector mScrollDetector;
	private PinchZoomDetector mPinchZoomDetector;
	private ClickDetector mClickDetector;
	private float mPinchZoomStartedCameraZoomFactor;
	private PlayingField mPlayingField;


	@Override
	public FontManager getFontManager() {
		/// BaseActivity.getFontManager has a bug
		/// http://code.google.com/p/andengine/issues/detail?id=47#c6
		return this.mEngine.getFontManager();
	}

	
	/*@Override
	public void onLoadComplete() {
		Toast.makeText(this, "Load Complete!", Toast.LENGTH_LONG).show();
	}*/

	public Engine onLoadEngine() {		
		
		final DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        this.mScreenWidth = displayMetrics.widthPixels;
        this.mScreenHeight = displayMetrics.heightPixels;
        this.mLeftBorder = mScreenWidth % 9 / 2;

		this.mCamera = new ZoomCamera(0, 0, mScreenWidth, mScreenHeight);

		/*final int maxVBound = mScreenWidth;
		final int maxHBound = mScreenHeight;*/
		this.mCamera.setBounds(0, mScreenWidth, 0, mScreenHeight);
		this.mCamera.setBoundsEnabled(true);

		final EngineOptions engineOptions = new EngineOptions(true,
				ScreenOrientation.PORTRAIT, new RatioResolutionPolicy(
						mScreenWidth, mScreenHeight), this.mCamera);

			engineOptions.setNeedsSound(true);
			engineOptions.setNeedsMusic(true);


			
			
		mHold = new HoldDetector(new IHoldDetectorListener() {
		        
		        public void onHoldFinished(HoldDetector pHoldDetector,
		                        long pHoldTimeMilliseconds, float pHoldX, float pHoldY) {
		                Toast.makeText(LineikiActivity.this, "HOLD FINISHED", Toast.LENGTH_SHORT).show();
		        }
		       
		        public void onHold(HoldDetector pHoldDetector, long pHoldTimeMilliseconds,
		                        float pHoldX, float pHoldY) {
		                //mShoot = false;
	                //Toast.makeText(getBaseContext(), "HOLD " + pHoldTimeMilliseconds, Toast.LENGTH_SHORT).show();
		        }
		});
		
		
		Engine engine = new Engine(engineOptions);
		
		try {
            if(MultiTouch.isSupported(this)) {
                    engine.setTouchController(new MultiTouchController());
            } else {
                    Toast.makeText(this, "Sorry your device does NOT support MultiTouch!\n\n(No PinchZoom is possible!)", Toast.LENGTH_LONG).show();
            }
	    } catch (final MultiTouchException e) {
	            Toast.makeText(this, "Sorry your Android Version does NOT support MultiTouch!\n\n(No PinchZoom is possible!)", Toast.LENGTH_LONG).show();
	    }

		return engine;
	}

	public void onLoadResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		
  		/*mTexture = new BitmapTextureAtlas(512, 512, TextureOptions.DEFAULT);
		mTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mTexture, this, "lineiki.png", 0, 0, 10, 10);
		mEngine.getTextureManager().loadTextures(mTexture);*/
		
		mFontTexture = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);

		FontFactory.setAssetBasePath("fonts/");
		mFont = FontFactory.createFromAsset(mFontTexture, this, "LCD.ttf", 36, true, Color.YELLOW);
		mEngine.getTextureManager().loadTexture(mFontTexture);
		getFontManager().loadFont(mFont);
		
		
		mBuildableBitmapTextureAtlas = new BuildableBitmapTextureAtlas(1024, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);//
		SVGBitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
				
		int tile_size = this.getTileSize();
		
		mBallTextureRegion	= SVGBitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mBuildableBitmapTextureAtlas, this, "balls.svg", tile_size, tile_size*7, 1, 7);
		mFieldBgTextureRegion = SVGBitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mBuildableBitmapTextureAtlas, this, "field_bg.svg", tile_size, tile_size*2, 1, 2);
		mDotTextureRegion = SVGBitmapTextureAtlasTextureRegionFactory.createFromAsset(mBuildableBitmapTextureAtlas, this, "dot.svg", tile_size, tile_size);
		mBallMarkerRegion = SVGBitmapTextureAtlasTextureRegionFactory.createFromAsset(mBuildableBitmapTextureAtlas, this, "selected_ball.svg", tile_size, tile_size);
		mSquareMarkerRegion = SVGBitmapTextureAtlasTextureRegionFactory.createFromAsset(mBuildableBitmapTextureAtlas, this, "selected_square.svg", tile_size, tile_size);
		
		mMenuNewGame = SVGBitmapTextureAtlasTextureRegionFactory.createFromAsset(mBuildableBitmapTextureAtlas, this, "menu_new_game.svg", 200, 50);
		mMenuUndo = SVGBitmapTextureAtlasTextureRegionFactory.createFromAsset(mBuildableBitmapTextureAtlas, this, "menu_undo.svg", 200, 50);
		mMenuQuit = SVGBitmapTextureAtlasTextureRegionFactory.createFromAsset(mBuildableBitmapTextureAtlas, this, "menu_quit.svg", 200, 50);

		mScoreFieldBackground = SVGBitmapTextureAtlasTextureRegionFactory.createFromAsset(mBuildableBitmapTextureAtlas, this, "score_bg.svg", tile_size, tile_size);
		mScoreDigits = SVGBitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mBuildableBitmapTextureAtlas, this, "digits.svg", tile_size*12, tile_size, 12, 1);
		
		try {
			mBuildableBitmapTextureAtlas.build(new BlackPawnTextureBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(1));
		} catch (final TextureAtlasSourcePackingException e) {
			Debug.e(e);
		}

		mEngine.getTextureManager().loadTexture(mBuildableBitmapTextureAtlas);
	}

	public Scene onLoadScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());
		
		/// menu
		this.createMenuScene();

		// HUD
		this.mHUD = new HUD();
		mCamera.setHUD(this.mHUD);

		final BallDispencer disp = new BallDispencer(this);
		disp.setPosition(mLeftBorder + getTileSize()*3, getTileSize()*0.2f);
		mHUD.attachChild(disp);
				
		final ScoreDisplay score = new ScoreDisplay(this, 3);
		score.setPosition(getTileSize()*3, getTileSize()*11);
		mHUD.attachChild(score);
		
		/// main scene
		this.mMainScene = new Scene();
				
		mMainScene.setBackground(new ColorBackground(0.1f, 0.1f, 0.1f));
		
		this.mPlayingField = new PlayingField(this, this);
		mPlayingField.setPosition(mLeftBorder, getTileSize()*1.4f);
		mMainScene.attachChild(mPlayingField);
		//mMainScene.registerTouchArea(field);	
		//mMainScene.setTouchAreaBindingEnabled(true);
		
		mGameLogic = new GameLogic(mPlayingField, disp);
		mGameLogic.setScoreDisplay(score);
		
		mPlayingField.setEvent(mGameLogic);
		
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
		
		mGameLogic.loadGameState(settings);
		
		// click
		this.mClickDetector = new ClickDetector(this);
		
		// scroll
		this.mScrollDetector = new SurfaceScrollDetector(this);
		
		// pinch zoom
        if(MultiTouch.isSupportedByAndroidVersion()) {
                try {
                        this.mPinchZoomDetector = new PinchZoomDetector(this);
                } catch (final MultiTouchException e) {
                        this.mPinchZoomDetector = null;
                }
        } else {
                this.mPinchZoomDetector = null;
        }

        this.mMainScene.setOnSceneTouchListener(this);
        this.mMainScene.setTouchAreaBindingEnabled(true);

		return mMainScene;
	}

	
	public TiledTextureRegion getBallTexture() {
		return mBallTextureRegion;
	}
	
	public TiledTextureRegion getFieldBGTexture() {
		return this.mFieldBgTextureRegion;
	}

	public TextureRegion getDotTexture() {
		return mDotTextureRegion;
	}

	protected Dialog onCreateDialog(int id) {
	    Dialog dialog;
	    switch(id) {
	    /*case DIALOG_PAUSED_ID:
	        // do the work to define the pause Dialog
	        break;
	    case DIALOG_GAMEOVER_ID:
	        // do the work to define the game over Dialog
	        break;*/
	    default:
	        dialog = null;
	    }
	    return dialog;
	}
	
    public void runOnUpdateThread(final Runnable pRunnable) {
        this.mEngine.runOnUpdateThread(pRunnable);
    }

	public void onLoadComplete() {
		// TODO Auto-generated method stub
		
	}

	/* *** MENU STUFF *** */
	@Override
	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
		if(pKeyCode == KeyEvent.KEYCODE_MENU && pEvent.getAction() == KeyEvent.ACTION_DOWN) {
			if(this.mMainScene.hasChildScene()) {
				/* Remove the menu and reset it. */
				this.mMenuScene.back();
			} else {
				/* Attach the menu. */
				this.mMainScene.setChildScene(this.mMenuScene, false, true, true);
			}
			return true;
		} else {
			return super.onKeyDown(pKeyCode, pEvent);
		}
	}

	protected void createMenuScene() {
		this.mMenuScene = new MenuScene(this.mCamera);

		final SpriteMenuItem resetMenuItem = new SpriteMenuItem(MENU_RESET, this.mMenuNewGame);
		resetMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		this.mMenuScene.addMenuItem(resetMenuItem);

		final SpriteMenuItem undoMenuItem = new SpriteMenuItem(MENU_UNDO, this.mMenuUndo);
		undoMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		this.mMenuScene.addMenuItem(undoMenuItem);

		final SpriteMenuItem quitMenuItem = new SpriteMenuItem(MENU_QUIT, this.mMenuQuit);
		quitMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		this.mMenuScene.addMenuItem(quitMenuItem);

		this.mMenuScene.buildAnimations();

		this.mMenuScene.setBackgroundEnabled(false);

		this.mMenuScene.setOnMenuItemClickListener(this);
	}

	public boolean onMenuItemClicked(final MenuScene pMenuScene, final IMenuItem pMenuItem, final float pMenuItemLocalX, final float pMenuItemLocalY) {
		switch(pMenuItem.getID()) {
			case MENU_RESET:
				// Restart the animation. 
				mGameLogic.startGame();

				// Remove the menu and reset it.
				this.mMainScene.clearChildScene();
				this.mMenuScene.reset();
				return true;
			case MENU_UNDO:
				// End Activity.
				mGameLogic.undoLastStep();
				this.mMainScene.clearChildScene();
				this.mMenuScene.reset();
				return true;
			case MENU_QUIT:
				// End Activity.
				this.finish();
				return true;
			default:
				return false;
		}
	}


	public int getTileSize() {
		/* Returns the size of a single playing field cell in pixels */
		return this.mScreenWidth/9;
	}


	public TextureRegion getScoreBGTexture() {
		return mScoreFieldBackground;
	}


	public TiledTextureRegion getDigitsTexture() {
		return mScoreDigits;
	}


	public TextureRegion getBallMarkerTexture() {
		return mBallMarkerRegion;
	}

	public TextureRegion getSquareMarkerTexture() {
		return mSquareMarkerRegion;
	}

	public void onSaveInstanceState(Bundle outBundle) {
		/* Invoked when the activity needs to be destroyed and re-created
		 * within the same process's lifecycle, i.e. when screen is rotated */
		super.onSaveInstanceState(outBundle);
		// outBundle.putChar("key", 'A');
	}
	
	public void onRestoreInstanceState(Bundle inBundle) {
		super.onRestoreInstanceState(inBundle);
		// char c = inBundle.getChar("key");
		
	}
	
 
    @Override
    protected void onStop(){
       super.onStop();
       SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);
       mGameLogic.saveGameState(settings);
    }

    /* PINCH ZOOM */
    public boolean onSceneTouchEvent(final Scene pScene, final TouchEvent pSceneTouchEvent) {
            if(this.mPinchZoomDetector != null) {
                    this.mPinchZoomDetector.onTouchEvent(pSceneTouchEvent);

                    if(this.mPinchZoomDetector.isZooming()) {
                            this.mScrollDetector.setEnabled(false);
                    } else {
                            if(pSceneTouchEvent.isActionDown()) {
                                    this.mScrollDetector.setEnabled(true);
                            }
                            this.mScrollDetector.onTouchEvent(pSceneTouchEvent);
                    }
            } else {
                    this.mScrollDetector.onTouchEvent(pSceneTouchEvent);
            }
            this.mClickDetector.onTouchEvent(pSceneTouchEvent);

            return true;
    }


    
    
    public void onScroll(final ScrollDetector pScollDetector, final TouchEvent pTouchEvent, final float pDistanceX, final float pDistanceY) {
            final float zoomFactor = this.mCamera.getZoomFactor();
            this.mCamera.offsetCenter(-pDistanceX / zoomFactor, -pDistanceY / zoomFactor);
    }
    
    public void onPinchZoomStarted(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent) {
            this.mPinchZoomStartedCameraZoomFactor = this.mCamera.getZoomFactor();
    }

    private float limitZoom(float pZoomFactor) {
    	/* restricts zoom to 1.0..2.0 range */
    	float zoom = this.mPinchZoomStartedCameraZoomFactor * pZoomFactor;
    	if (zoom < 1.0f) {
    		return 1.0f;
    	}
    	if (zoom > 2.0f) {
    		return 2.0f;
    	}
    	return zoom;
    	
    }
    
    public void onPinchZoom(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent, final float pZoomFactor) {
            this.mCamera.setZoomFactor(this.limitZoom(pZoomFactor));
    }

    public void onPinchZoomFinished(final PinchZoomDetector pPinchZoomDetector, final TouchEvent pTouchEvent, final float pZoomFactor) {
            this.mCamera.setZoomFactor(this.limitZoom(pZoomFactor));
    }

	public void onClick(ClickDetector pClickDetector, TouchEvent pTouchEvent) {
		/*Toast.makeText(
				this,
				"CLICK", 
				//String.format("ACTION: %d", pSceneTouchEvent.getPointerID()),
				Toast.LENGTH_SHORT).show();*/
		
		float[] coords = this.mPlayingField.convertSceneToLocalCoordinates(pTouchEvent.getX(), pTouchEvent.getY());
		mPlayingField.onTouch(coords[0], coords[1]);
	}

    
	/*@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		//this.mScrollDetector.onTouchEvent(pSceneTouchEvent);
		final AnimatedSprite ball = new AnimatedSprite(pSceneTouchEvent.getX(), pSceneTouchEvent.getY(), this.mTextureRegion);
		pScene.attachChild(ball);

		return true;
	}*/
}