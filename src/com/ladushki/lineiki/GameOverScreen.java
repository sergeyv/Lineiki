package com.ladushki.lineiki;

import javax.microedition.khronos.opengles.GL10;

import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.entity.modifier.AlphaModifier;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.menu.MenuScene;
import org.anddev.andengine.entity.scene.menu.item.SpriteMenuItem;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.entity.text.Text;
import org.anddev.andengine.util.HorizontalAlign;


public class GameOverScreen extends MenuScene {
	
	private ScoreDisplay mScoreDisplay;

	public GameOverScreen(Camera pCamera, LineikiActivity pActivity, int pScore, int pHighscore) {
		super(pCamera);
		
		this.setBackgroundEnabled(false);
		
		
		/*final SpriteMenuItem resetMenuItem = new SpriteMenuItem(MENU_RESET, this.mMenuNewGame);
		resetMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		this.mMenuthis.addMenuItem(resetMenuItem);*/

		//mGameOverthis.setBackground(new ColorBackground(0.5f, 0.1f, 0.1f, 0.5f));
		
		Rectangle r = new Rectangle(0, 0, pActivity.getScreenWidth(), pActivity.getScreenHeight());
		r.setColor(0, 0, 0, 0.0f);
		r.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		r.registerEntityModifier(new AlphaModifier(1.0f, 0.0f, 0.5f));

		this.attachChild(r);
		int w = pActivity.getTileSize();
		
		final Sprite game_over = new Sprite(w*.5f ,w*2, pActivity.getGameOverTexture());
		this.attachChild(game_over);
		
		/*mScoreDisplay = new ScoreDisplay(pActivity, 3);
		mScoreDisplay.setPosition(w*3, w*4);
		mScoreDisplay.setScore(pScore);
		this.attachChild(mScoreDisplay);*/

		final SpriteMenuItem resetMenuItem = new SpriteMenuItem(LineikiActivity.MENU_RESET, pActivity.mMenuNewGame);
		resetMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		this.addMenuItem(resetMenuItem);

		final SpriteMenuItem quitMenuItem = new SpriteMenuItem(LineikiActivity.MENU_QUIT, pActivity.mMenuQuit);
		quitMenuItem.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		this.addMenuItem(quitMenuItem);

		this.buildAnimations();
		// manually override menu items position
		resetMenuItem.setPosition(w*1, w*11);
		quitMenuItem.setPosition(w*5, w*11);

		this.setBackgroundEnabled(false);

		Text t;
		
		t = new Text(100, 60, 
			pActivity.getFont(), 
			"Your score: " + Integer.valueOf(pScore).toString(), 
			HorizontalAlign.CENTER);
		this.attachChild(t);		
		t.setPosition((w*9 - t.getWidth())/2, w*6);

		if (pHighscore > 0) { // highscore is zero initially
			t = new Text(100, 60, 
				pActivity.getFont(), 
				"Personal best: " + Integer.valueOf(pHighscore).toString(), 
				HorizontalAlign.CENTER);
			this.attachChild(t);
			t.setPosition((w*9 - t.getWidth())/2, w*7f);
			
			if (pScore > pHighscore) {
				t = new Text(100, 60, 
					pActivity.getFont(), 
					"YOU WIN!!!", 
					HorizontalAlign.CENTER);
				this.attachChild(t);
				t.setPosition((w*9 - t.getWidth())/2, w*8);				
			}
		} 
		

		
		this.setOnMenuItemClickListener(pActivity);
	}

}
