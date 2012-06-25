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
import org.anddev.andengine.entity.scene.background.SpriteBackground;
import org.anddev.andengine.entity.scene.menu.MenuScene;
import org.anddev.andengine.entity.scene.menu.MenuScene.IOnMenuItemClickListener;
import org.anddev.andengine.entity.scene.menu.item.IMenuItem;
import org.anddev.andengine.entity.scene.menu.item.SpriteMenuItem;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.text.ChangeableText;
import org.anddev.andengine.entity.text.Text;
import org.anddev.andengine.entity.util.FPSLogger;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.input.touch.detector.ScrollDetector;
import org.anddev.andengine.input.touch.detector.SurfaceScrollDetector;
import org.anddev.andengine.input.touch.detector.ScrollDetector.IScrollDetectorListener;
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
import org.anddev.andengine.opengl.texture.region.BaseTextureRegion;
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import org.anddev.andengine.util.Debug;
import org.anddev.andengine.util.HorizontalAlign;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXTiledMap;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXLoader;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXLayer;
import org.anddev.andengine.entity.layer.tiled.tmx.util.exception.TMXLoadException;
import org.anddev.andengine.extension.svg.opengl.texture.atlas.bitmap.SVGBitmapTextureAtlasTextureRegionFactory;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

public class LineikiActivity extends BaseGameActivity implements ITextureProvider, IOnMenuItemClickListener  {

	/*static final int CAMERA_WIDTH = 320;
	static final int CAMERA_HEIGHT = 480;*/
	
	protected static final int MENU_RESET = 0;
	protected static final int MENU_UNDO = 1;
	protected static final int MENU_QUIT = 2;

	private static final int COUNT = 0;
	
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
		/*final int maxVBound = CAMERA_HEIGHT*2;
		final int maxHBound = CAMERA_HEIGHT*2;
		this.mCamera.setBounds(-maxHBound, maxHBound, -maxVBound, maxVBound);
		this.mCamera.setBoundsEnabled(true);*/
		
		final DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        this.mScreenWidth = displayMetrics.widthPixels;
        this.mScreenHeight = displayMetrics.heightPixels;
        this.mLeftBorder = mScreenWidth % 9 / 2;

		this.mCamera = new ZoomCamera(0, 0, mScreenWidth, mScreenHeight);


		final EngineOptions engineOptions = new EngineOptions(true,
				ScreenOrientation.PORTRAIT, new RatioResolutionPolicy(
						mScreenWidth, mScreenHeight), this.mCamera);

			engineOptions.setNeedsSound(true);
			engineOptions.setNeedsMusic(true);

		return new Engine(engineOptions);
		


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
				
		mMainScene.setBackground(new ColorBackground(0.09804f, 0.6274f, 0.8785f));
		
		final PlayingField playingField = new PlayingField(this, this);
		playingField.setPosition(mLeftBorder, getTileSize()*1.4f);
		mMainScene.attachChild(playingField);
		mMainScene.registerTouchArea(playingField);	
		mMainScene.setTouchAreaBindingEnabled(true);
		
		mGameLogic = new GameLogic(playingField, disp);
		mGameLogic.setScoreDisplay(score);
		
		playingField.setEvent(mGameLogic);

		mGameLogic.startGame();
			
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
	/*@Override
	public void onScroll(ScrollDetector pScollDetector, TouchEvent pTouchEvent,
			float pDistanceX, float pDistanceY) {
		Log.d(TAG, "Scroll {x:"+pDistanceX+", y: "+pDistanceY+"}");  
		this.mCamera.offsetCenter(-pDistanceX, -pDistanceY);
	}*/


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

	/*@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		//this.mScrollDetector.onTouchEvent(pSceneTouchEvent);
		final AnimatedSprite ball = new AnimatedSprite(pSceneTouchEvent.getX(), pSceneTouchEvent.getY(), this.mTextureRegion);
		pScene.attachChild(ball);

		return true;
	}*/
}