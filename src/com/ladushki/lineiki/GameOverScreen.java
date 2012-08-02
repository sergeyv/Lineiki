package com.ladushki.lineiki;

import javax.microedition.khronos.opengles.GL10;

import org.anddev.andengine.engine.camera.Camera;
import org.anddev.andengine.entity.modifier.AlphaModifier;
import org.anddev.andengine.entity.primitive.Rectangle;
import org.anddev.andengine.entity.scene.menu.MenuScene;
import org.anddev.andengine.entity.scene.menu.item.SpriteMenuItem;
import org.anddev.andengine.entity.scene.menu.item.TextMenuItem;
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
		r.setAlpha(0.5f);
		//r.registerEntityModifier(new AlphaModifier(1.0f, 0.0f, 0.5f));

		this.attachChild(r);
		int w = pActivity.getTileSize();
		
		final Sprite game_over = new Sprite(w*.5f ,w*2, pActivity.getGameOverTexture());
		this.attachChild(game_over);
		
		/*mScoreDisplay = new ScoreDisplay(pActivity, 3);
		mScoreDisplay.setPosition(w*3, w*4);
		mScoreDisplay.setScore(pScore);
		this.attachChild(mScoreDisplay);*/

		final TextMenuItem resetMenuItem = new TextMenuItem(LineikiActivity.MENU_RESET, pActivity.mFont, "New Game");
		this.addMenuItem(resetMenuItem);

		final TextMenuItem quitMenuItem = new TextMenuItem(LineikiActivity.MENU_QUIT, pActivity.mFont, "Quit");
		this.addMenuItem(quitMenuItem);

		//this.buildAnimations();
		float scoreTop;
		float scoreLeft;
		// manually override menu items position
		if (pActivity.getIsTablet()) { // TABLET
			game_over.setPosition(w*1.0f, w*1.5f);
			resetMenuItem.setPosition(w*10.5f, w*6.5f);
			quitMenuItem.setPosition(w*10.5f, w*8.5f);	
			scoreTop = w*6.5f;
			scoreLeft = w*0.5f;
		} else { // PHONE
			game_over.setPosition(w*.5f, w*2);
			resetMenuItem.setPosition(w*1, w*11);
			quitMenuItem.setPosition(w*5, w*11);
			scoreTop = w*6;
			scoreLeft = 0;
		}

		this.setBackgroundEnabled(false);

		Text t;
		
		t = new Text(100, 60, 
			pActivity.getFont(), 
			"Your score: " + Integer.valueOf(pScore).toString(), 
			HorizontalAlign.CENTER);
		this.attachChild(t);		
		t.setPosition(scoreLeft + (w*9 - t.getWidth())/2, scoreTop);

		if (pHighscore > 0) { // highscore is zero initially
			t = new Text(100, 60, 
				pActivity.getFont(), 
				"Personal best: " + Integer.valueOf(pHighscore).toString(), 
				HorizontalAlign.CENTER);
			this.attachChild(t);
			t.setPosition(scoreLeft + (w*9 - t.getWidth())/2, scoreTop + w);
			
			if (pScore > pHighscore) {
				t = new Text(100, 60, 
					pActivity.getFont(), 
					"YOU WIN!!!", 
					HorizontalAlign.CENTER);
				this.attachChild(t);
				t.setPosition(scoreLeft + (w*9 - t.getWidth())/2, scoreTop + w*2);				
			}
		} 
		

		
		this.setOnMenuItemClickListener(pActivity);
	}

}
