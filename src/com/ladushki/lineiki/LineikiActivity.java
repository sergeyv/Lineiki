package com.ladushki.lineiki;

import org.anddev.andengine.engine.Engine;
import org.anddev.andengine.engine.camera.ZoomCamera;
import org.anddev.andengine.engine.options.EngineOptions;
import org.anddev.andengine.engine.options.EngineOptions.ScreenOrientation;
import org.anddev.andengine.engine.options.resolutionpolicy.RatioResolutionPolicy;
import org.anddev.andengine.entity.scene.Scene;
import org.anddev.andengine.entity.scene.Scene.IOnSceneTouchListener;
import org.anddev.andengine.entity.scene.background.ColorBackground;
import org.anddev.andengine.entity.scene.background.SpriteBackground;
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
import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;
import org.anddev.andengine.ui.activity.BaseGameActivity;
import org.anddev.andengine.util.Debug;
import org.anddev.andengine.util.HorizontalAlign;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXTiledMap;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXLoader;
import org.anddev.andengine.entity.layer.tiled.tmx.TMXLayer;
import org.anddev.andengine.entity.layer.tiled.tmx.util.exception.TMXLoadException;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.Typeface;
import android.util.Log;
import android.widget.Toast;

public class LineikiActivity extends BaseGameActivity {

	static final int CAMERA_WIDTH = 320;
	static final int CAMERA_HEIGHT = 480;
	//private static final String TAG = "Lineiki";
	
	private ZoomCamera mCamera;
	private BitmapTextureAtlas mTexture;
	private TiledTextureRegion mTextureRegion;
	private SurfaceScrollDetector mScrollDetector;
	private TMXTiledMap mTMXTiledMap;
	private BitmapTextureAtlas mBgTexture;
	private TextureRegion mBgTextureRegion;
	private BitmapTextureAtlas mFontTexture;
	private Font mFont;
	
	@Override
	public FontManager getFontManager() {
		/// BaseActivity.getFontManager has a bug
		/// http://code.google.com/p/andengine/issues/detail?id=47#c6
		return this.mEngine.getFontManager();
	}

	
	@Override
	public void onLoadComplete() {
		Toast.makeText(this, "Load Complete!", Toast.LENGTH_LONG).show();
	}

	@Override
	public Engine onLoadEngine() {
		this.mCamera = new ZoomCamera(0, 0, CAMERA_WIDTH, CAMERA_HEIGHT);

		
		final int maxVBound = CAMERA_HEIGHT*2;
		final int maxHBound = CAMERA_HEIGHT*2;
		this.mCamera.setBounds(-maxHBound, maxHBound, -maxVBound, maxVBound);
		this.mCamera.setBoundsEnabled(true);


		final EngineOptions engineOptions = new EngineOptions(true,
				ScreenOrientation.PORTRAIT, new RatioResolutionPolicy(
						CAMERA_WIDTH, CAMERA_HEIGHT), this.mCamera);

			engineOptions.setNeedsSound(true);
			engineOptions.setNeedsMusic(true);

		return new Engine(engineOptions);

	}

	@Override
	public void onLoadResources() {
		BitmapTextureAtlasTextureRegionFactory.setAssetBasePath("gfx/");
		
  		this.mTexture = new BitmapTextureAtlas(512, 512, TextureOptions.DEFAULT);
		this.mTextureRegion = BitmapTextureAtlasTextureRegionFactory.createTiledFromAsset(this.mTexture, this, "lineiki.png", 0, 0, 10, 10);
		this.mEngine.getTextureManager().loadTextures(this.mTexture);
		
		this.mFontTexture = new BitmapTextureAtlas(256, 256, TextureOptions.BILINEAR_PREMULTIPLYALPHA);

		FontFactory.setAssetBasePath("fonts/");
		this.mFont = FontFactory.createFromAsset(this.mFontTexture, this, "LCD.ttf", 36, true, Color.YELLOW);
		this.mEngine.getTextureManager().loadTexture(this.mFontTexture);
		this.getFontManager().loadFont(this.mFont);
	}

	@Override
	public Scene onLoadScene() {
		this.mEngine.registerUpdateHandler(new FPSLogger());
		final Scene scene = new Scene();
		
		scene.setBackground(new ColorBackground(0.09804f, 0.6274f, 0.8784f));

		final BallDispencer disp = new BallDispencer(this.mTextureRegion);
		disp.setPosition(100,320);
		scene.attachChild(disp);

		
		final PlayingField field = new PlayingField(this.mTextureRegion, disp);
		scene.attachChild(field);
		scene.registerTouchArea(field);	
		scene.setTouchAreaBindingEnabled(true);
			
		final ChangeableText scoreField = new ChangeableText(100, 400, this.mFont, "000", HorizontalAlign.CENTER, "000".length());
		scene.attachChild(scoreField);
		field.setScoreField(scoreField);

		return scene;

		
		/*
		final Sprite bg_sprite = new Sprite(0, 0, this.mBgTextureRegion);
		final SpriteBackground bg = new SpriteBackground(bg_sprite);

		scene.setBackground(bg);

	    //try {
        final TMXLoader tmxLoader = new TMXLoader(this, this.mEngine.getTextureManager(), TextureOptions.NEAREST);
        try {
			this.mTMXTiledMap = tmxLoader.loadFromAsset(this, "field.tmx");
		} catch (TMXLoadException e) {
		    Debug.e(e);
		}
        final TMXLayer tmxLayer = this.mTMXTiledMap.getTMXLayers().get(0);
        scene.attachChild(tmxLayer);*/


		/// touch and scroll setup
		/*scene.setOnAreaTouchTraversalFrontToBack();
		scene.setOnSceneTouchListener(this);
		scene.setTouchAreaBindingEnabled(true);
		this.mScrollDetector = new SurfaceScrollDetector(this);
		this.mScrollDetector.setEnabled(true);*/


		//final int centerX = (CAMERA_WIDTH - this.mTextureRegion.getWidth()) / 2;
		//final int centerY = (CAMERA_HEIGHT - this.mTextureRegion.getHeight()) / 2;

		/*final Sprite ball = new TiledSprite(10, 10, this.mTextureRegion);
		scene.attachChild(ball);*/

		
		/*final BallSprite b2 = new BallSprite(4, 5, this.mTextureRegion.deepCopy(), BallColor.BLUE);
		scene.attachChild(b2);
		scene.registerTouchArea(b2);*/
		
		//disp.setScale((float) 0.5);
		//disp.setRotation(30);
		//disp.setAlpha(50);
		
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
	/*@Override
	public void onScroll(ScrollDetector pScollDetector, TouchEvent pTouchEvent,
			float pDistanceX, float pDistanceY) {
		Log.d(TAG, "Scroll {x:"+pDistanceX+", y: "+pDistanceY+"}");  
		this.mCamera.offsetCenter(-pDistanceX, -pDistanceY);
	}*/

	/*@Override
	public boolean onSceneTouchEvent(Scene pScene, TouchEvent pSceneTouchEvent) {
		//this.mScrollDetector.onTouchEvent(pSceneTouchEvent);
		final AnimatedSprite ball = new AnimatedSprite(pSceneTouchEvent.getX(), pSceneTouchEvent.getY(), this.mTextureRegion);
		pScene.attachChild(ball);

		return true;
	}*/
}