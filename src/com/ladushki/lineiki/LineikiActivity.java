package com.ladushki.lineiki;

import javax.microedition.khronos.opengles.GL10;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.ZoomCamera;
import org.anddev.andengine.engine.camera.hud.HUD;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.modifier.AlphaModifier;
import org.anddev.andengine.entity.modifier.MoveXModifier;
import org.anddev.andengine.entity.modifier.ScaleModifier;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.scene.menu.MenuScene;
import org.anddev.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.anddev.andengine.entity.scene.menu.item.IMenuItem;
import org.anddev.andengine.entity.scene.menu.item.SpriteMenuItem;
import org.anddev.andengine.entity.scene.menu.item.TextMenuItem;
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
import org.anddev.andengine.input.touch.detector.ScrollDetector;
import org.anddev.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
import org.anddev.andengine.input.touch.detector.SurfaceScrollDetector;
import org.anddev.andengine.opengl.font.Font;
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
import org.anddev.andengine.util.modifier.ease.EaseBackIn;
import org.anddev.andengine.util.modifier.ease.EaseBackOut;
import org.anddev.andengine.util.modifier.ease.EaseSineInOut;

import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.Typeface;
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

	public enum CurrentScreen {
		GAME,
		MENU,
		GAMEOVER,
	}
	
	CurrentScreen mCurrentScreen;
	
	protected static final int MENU_RESET = 0;
	protected static final int MENU_UNDO = 1;
	protected static final int MENU_QUIT = 2;
	protected static final int MENU_CONTINUE = 3;
	
	private static final String PREFS_NAME = "LineikiPrefsFile";

	
	private ZoomCamera mCamera;
	private BitmapTextureAtlas mFontTexture;
	Font mFont;
	
	
	private GameLogic mGameLogic;
	private BuildableBitmapTextureAtlas mBuildableBitmapTextureAtlas;
	private TiledTextureRegion mBallTextureRegion;
	private TiledTextureRegion mFieldBgTextureRegion;
	private TextureRegion mDotTextureRegion;
	TextureRegion mMenuNewGame;
	private TextureRegion mMenuUndo;
	TextureRegion mMenuQuit;
	private TextureRegion mHighScoreReached;
	private TextureRegion mGameOver;
	
	private Scene mMainScene;
	//private MenuScene mMenuScene;
	//private Scene mGameOverScene;
	private HUD mHUD;
	
	private int mScreenWidth;
	private int mScreenHeight;
	private int mLeftBorder;
	private TextureRegion mScoreFieldBackground;
	private TiledTextureRegion mScoreDigits;
	private TextureRegion mBallMarkerRegion;
	private TextureRegion mSquareMarkerRegion;
		
	private SurfaceScrollDetector mScrollDetector;
	private PinchZoomDetector mPinchZoomDetector;
	private ClickDetector mClickDetector;
	private float mPinchZoomStartedCameraZoomFactor;
	private PlayingField mPlayingField;
	
	private boolean mIsTablet;
	//private ScoreDisplay mGameoverScreenScore;


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
        
        this.mIsTablet = isTabletDevice();

		this.mCamera = new ZoomCamera(0, 0, mScreenWidth, mScreenHeight);

		this.mCamera.setBounds(0, mScreenWidth, 0, mScreenHeight);
		this.mCamera.setBoundsEnabled(true);
		
		ScreenOrientation orientation = mIsTablet ? ScreenOrientation.LANDSCAPE: ScreenOrientation.PORTRAIT;

		final EngineOptions engineOptions = new EngineOptions(
				true,
				orientation, 
				new RatioResolutionPolicy(mScreenWidth, mScreenHeight), 
				this.mCamera);

			engineOptions.setNeedsSound(true);
			engineOptions.setNeedsMusic(true);		
		
		Engine engine = new Engine(engineOptions);
		
		if (!this.getIsTablet()) { // don't need pinch zoom on a tablet
			try {
	            if(MultiTouch.isSupported(this)) {
	                    engine.setTouchController(new MultiTouchController());
	            } else {
	                    Toast.makeText(this, "Sorry your device does NOT support MultiTouch!\n\n(No PinchZoom is possible!)", Toast.LENGTH_LONG).show();
	            }
		    } catch (final MultiTouchException e) {
		            Toast.makeText(this, "Sorry your Android Version does NOT support MultiTouch!\n\n(No PinchZoom is possible!)", Toast.LENGTH_LONG).show();
		    }
		}

		return engine;
	}

	public void onLoadResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		
  		/*mTexture = new BitmapTextureAtlas(512, 512, TextureOptions.DEFAULT);
		mTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mTexture, this, "lineiki.png", 0, 0, 10, 10);
		mEngine.getTextureManager().loadTextures(mTexture);*/
		
		/*mFontTexture = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);

		FontFactory.setAssetBasePath("fonts/");
		mFont = FontFactory.createFromAsset(mFontTexture, this, "LCD.ttf", 36, true, Color.YELLOW);
		mEngine.getTextureManager().loadTexture(mFontTexture);
		getFontManager().loadFont(mFont);*/
		
		
		mBuildableBitmapTextureAtlas = new BuildableBitmapTextureAtlas(2048, 1024, TextureOptions.BILINEAR_PREMULTIPLYALPHA);//
		SVGBitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
				
		int tile_size = this.getTileSize();
		
		mBallTextureRegion	= SVGBitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mBuildableBitmapTextureAtlas, this, "balls.svg", tile_size, tile_size*7, 1, 7);
		mFieldBgTextureRegion = SVGBitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mBuildableBitmapTextureAtlas, this, "field_bg.svg", tile_size, tile_size*2, 1, 2);
		mDotTextureRegion = SVGBitmapTextureAtlasTextureRegionFactory.createFromAsset(mBuildableBitmapTextureAtlas, this, "dot.svg", tile_size, tile_size);
		mBallMarkerRegion = SVGBitmapTextureAtlasTextureRegionFactory.createFromAsset(mBuildableBitmapTextureAtlas, this, "selected_ball.svg", tile_size, tile_size);
		mSquareMarkerRegion = SVGBitmapTextureAtlasTextureRegionFactory.createFromAsset(mBuildableBitmapTextureAtlas, this, "selected_square.svg", tile_size, tile_size);
		
		mMenuNewGame = SVGBitmapTextureAtlasTextureRegionFactory.createFromAsset(mBuildableBitmapTextureAtlas, this, "menu_new_game.svg", tile_size*4, tile_size);
		mMenuUndo = SVGBitmapTextureAtlasTextureRegionFactory.createFromAsset(mBuildableBitmapTextureAtlas, this, "menu_undo.svg", tile_size*4, tile_size);
		mMenuQuit = SVGBitmapTextureAtlasTextureRegionFactory.createFromAsset(mBuildableBitmapTextureAtlas, this, "menu_quit.svg", tile_size*4, tile_size);

		mHighScoreReached = SVGBitmapTextureAtlasTextureRegionFactory.createFromAsset(mBuildableBitmapTextureAtlas, this, "tada.svg", 128, 32);
		mGameOver = SVGBitmapTextureAtlasTextureRegionFactory.createFromAsset(mBuildableBitmapTextureAtlas, this, "game_over.svg", tile_size*8, tile_size*2);

		mScoreFieldBackground = SVGBitmapTextureAtlasTextureRegionFactory.createFromAsset(mBuildableBitmapTextureAtlas, this, "score_bg.svg", tile_size, tile_size);
		mScoreDigits = SVGBitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(mBuildableBitmapTextureAtlas, this, "digits.svg", tile_size*12, tile_size, 12, 1);
		
		try {
			mBuildableBitmapTextureAtlas.build(new BlackPawnTextureBuilder<IBitmapTextureAtlasSource, BitmapTextureAtlas>(1));
		} catch (final TextureAtlasSourcePackingException e) {
			Debug.e(e);
			Toast.makeText(this, e.getMessage(), Toast.LENGTH_LONG).show();
		}

		mEngine.getTextureManager().loadTexture(mBuildableBitmapTextureAtlas);
		
		/* LOAD FONT */
		this.mFontTexture = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);

		this.mFont = new Font(this.mFontTexture, 
				Typeface.create(Typeface.DEFAULT, Typeface.BOLD), 
				tile_size/3.0f*2, true, Color.WHITE);
		this.mEngine.getTextureManager().loadTexture(this.mFontTexture);
		this.getFontManager().loadFont(this.mFont);

	}

	public Scene onLoadScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());
		
		// HUD
		this.mHUD = new HUD();
		mCamera.setHUD(this.mHUD);
		
		final BallDispencer disp = new BallDispencer(this);
		mHUD.attachChild(disp);
				
		final ScoreDisplay score = new ScoreDisplay(this, 3);
		mHUD.attachChild(score);

		final ScoreDisplay highscore = new ScoreDisplay(this, 3);
		mHUD.attachChild(highscore);
		
		/// main scene
		this.mMainScene = new Scene();		
		mMainScene.setBackground(new ColorBackground(0.1f, 0.1f, 0.1f));
		
		this.mPlayingField = new PlayingField(this, this);
		mMainScene.attachChild(mPlayingField);
		//mMainScene.registerTouchArea(field);	
		//mMainScene.setTouchAreaBindingEnabled(true);
		
		int t = getTileSize();
		// position stuff
		if (mIsTablet) {
			// TABLET
			disp.setPosition(t*10.5f, t*1.5f);
			score.setPosition(t*10.5f, t*5.5f);
			highscore.setPosition(t*10.5f, t*7.5f);
			mPlayingField.setPosition(t*0.5f, t*0.5f);			
			
		} else {
			// PHONE
			disp.setPosition(mLeftBorder + t*3, t*0.2f);
			score.setPosition(t*5, t*11);
			highscore.setPosition(t*1, t*11);
			mPlayingField.setPosition(mLeftBorder, t*1.4f);			
		}

		
		mGameLogic = new GameLogic(mPlayingField, disp);
		mGameLogic.setScoreDisplay(score, highscore);		
		mPlayingField.setEvent(mGameLogic);
		
		// prefs
        SharedPreferences settings = getSharedPreferences(PREFS_NAME, 0);		
		mGameLogic.loadGameState(settings);
		
		// click
		this.mClickDetector = new ClickDetector(this);
		
		// scroll
		this.mScrollDetector = new SurfaceScrollDetector(this);
		
		// pinch zoom
        if (MultiTouch.isSupportedByAndroidVersion()) {
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

		mCurrentScreen = CurrentScreen.GAME;
        
		return mMainScene;
	}

	
	public void showGameOverScreen() {
		final GameOverScreen scene = new GameOverScreen(
				this.mCamera,
				this,
				this.mGameLogic.getScore(),
				this.mGameLogic.getHighScore());
		scene.setScaleCenter(mScreenWidth/2, mScreenHeight/2);
		scene.setScale(0.1f);
		animateOverlaySceneShown(scene, CurrentScreen.GAMEOVER);
	}

	private void animateOverlaySceneShown(Scene scene, CurrentScreen pCurrentScreen) {
		mMainScene.setChildScene(scene, false, false, true);
		mCurrentScreen = pCurrentScreen;
		scene.setScaleCenter(mScreenWidth/2, mScreenHeight/2);
		scene.registerEntityModifier(
				new ScaleModifier(2.0f, 0.1f, 1.0f, EaseSineInOut.getInstance())
				);
		
		mHUD.registerEntityModifier(
				new MoveXModifier(1.0f, 0.0f, mScreenWidth, EaseBackIn.getInstance())
				);
	}


	private void animateOverlaySceneHidden() {
		mMainScene.clearChildScene();
		mCurrentScreen = CurrentScreen.GAME;
		mHUD.registerEntityModifier(
				new MoveXModifier(1.0f, -mScreenWidth, 0.0f, EaseBackOut.getInstance())
				);
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
	
    public void runOnUpdateThread(final Runnable pRunnable) {
        this.mEngine.runOnUpdateThread(pRunnable);
    }

	public void onLoadComplete() {
		// TODO Auto-generated method stub
		
	}

	/* *** MENU STUFF *** */
	@Override
	public boolean onKeyDown(final int pKeyCode, final KeyEvent pEvent) {
		if((pKeyCode == KeyEvent.KEYCODE_MENU || pKeyCode == KeyEvent.KEYCODE_BACK) 
				&& pEvent.getAction() == KeyEvent.ACTION_DOWN ) {
			
			switch(mCurrentScreen) {
			case GAME:
				animateOverlaySceneShown(createMenuScene(), CurrentScreen.MENU);
				return false;
			case MENU:
				animateOverlaySceneHidden();
				return true;
			case GAMEOVER:
				animateOverlaySceneHidden();
				this.finish();
				return true;				
			}
			return true;
		} else {
			return super.onKeyDown(pKeyCode, pEvent);
		}
	}

	protected MenuScene createMenuScene() {
		MenuScene ms = new MenuScene(this.mCamera);
		
		Rectangle r = new Rectangle(0, 0, mScreenWidth, mScreenHeight);
		r.setColor(0, 0, 0, 0.0f);
		r.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		r.registerEntityModifier(new AlphaModifier(1.0f, 0.0f, 0.5f));
		ms.attachChild(r);

		final TextMenuItem continueMenuItem = new TextMenuItem(MENU_CONTINUE, mFont, "Continue");
		//final SpriteMenuItem undoMenuItem = new SpriteMenuItem(MENU_UNDO, this.mMenuUndo);
		//undoMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		ms.addMenuItem(continueMenuItem);

		if (mGameLogic.getCanUndo()) {
			final TextMenuItem undoMenuItem = new TextMenuItem(MENU_UNDO, mFont, "Undo");
			//final SpriteMenuItem undoMenuItem = new SpriteMenuItem(MENU_UNDO, this.mMenuUndo);
			//undoMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
			ms.addMenuItem(undoMenuItem);
		}

		//final SpriteMenuItem resetMenuItem = new SpriteMenuItem(MENU_RESET, this.mMenuNewGame);
		final TextMenuItem resetMenuItem = new TextMenuItem(MENU_RESET, mFont, "New Game");
		//resetMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		ms.addMenuItem(resetMenuItem);

		final TextMenuItem quitMenuItem = new TextMenuItem(MENU_QUIT, mFont, "Quit");
		//final SpriteMenuItem quitMenuItem = new SpriteMenuItem(MENU_QUIT, this.mMenuQuit);
		//quitMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		ms.addMenuItem(quitMenuItem);

		ms.buildAnimations();
		ms.setBackgroundEnabled(false);
		ms.setOnMenuItemClickListener(this);
		
		return ms;
	}

	public boolean onMenuItemClicked(final MenuScene pMenuScene, final IMenuItem pMenuItem, final float pMenuItemLocalX, final float pMenuItemLocalY) {
		
		switch(pMenuItem.getID()) {
			case MENU_CONTINUE: // hide the menu and continue game
				break;
			case MENU_RESET: // new game
				mGameLogic.startGame();
				break;
			case MENU_UNDO: // Undo last step.
				mGameLogic.undoLastStep();
				break;
			case MENU_QUIT: // End Activity.
				this.finish();
				break;
			default:
				return false;
		}		
		animateOverlaySceneHidden();
		return true;
	}


	public int getTileSize() {
		/* Returns the size of a single playing field cell in pixels */
		if (getIsTablet()) {
			return this.mScreenHeight/10;			
		}
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
	
	public TextureRegion getHighscoreReachedTexture() {
		return mHighScoreReached;
	}

	public TextureRegion getGameOverTexture() {
		return mGameOver;
	}
	
	//public void onSaveInstanceState(Bundle outBundle) {
		/* Invoked when the activity needs to be destroyed and re-created
		 * within the same process's lifecycle, i.e. when screen is rotated */
		//super.onSaveInstanceState(outBundle);
		// outBundle.putChar("key", 'A');
	//}
	
	/*public void onRestoreInstanceState(Bundle inBundle) {
		super.onRestoreInstanceState(inBundle);
		// char c = inBundle.getChar("key");
		
	}*/
	
 
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
		float[] coords = this.mPlayingField.convertSceneToLocalCoordinates(pTouchEvent.getX(), pTouchEvent.getY());
		mPlayingField.onTouch(coords[0], coords[1]);
	}

	public int getScreenWidth() {
		return mScreenWidth;
	}

	public int getScreenHeight() {
		return mScreenHeight;
	}

	Font getFont() {
		return mFont;
	}

	private boolean isTabletDevice() {
	    // Verifies if the Generalized Size of the device is XLARGE to be
	    // considered a Tablet
	    boolean xlarge = ((this.getResources().getConfiguration().screenLayout & 
	                        Configuration.SCREENLAYOUT_SIZE_MASK) == 
	                        Configuration.SCREENLAYOUT_SIZE_XLARGE);
	    return xlarge;

	}
	
	public boolean getIsTablet() {
		return this.mIsTablet;
	}
}