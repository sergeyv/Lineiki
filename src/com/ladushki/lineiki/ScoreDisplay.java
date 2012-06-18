package com.ladushki.lineiki;

import org.anddev.andengine.entity.Entity;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.sprite.Sprite;


public class ScoreDisplay extends Entity {

	private ITextureProvider mTextureProvider;
	private int mNumDigits;
	private AnimatedSprite[] mDigits;
	

	public ScoreDisplay(ITextureProvider pTextureProvider, LineikiActivity pParentActivity, int pNumDigits) {
		
		LineikiActivity mParentActivity = pParentActivity;		
		this.mTextureProvider = pTextureProvider;	
		this.mNumDigits = pNumDigits;
		initBackground();
	}

	private void initBackground() {
		// TODO Auto-generated method stub
		Sprite bg = new Sprite(0,0, mTextureProvider.getScoreBGTexture());
		bg.setWidth(bg.getHeight()*mNumDigits);
		this.attachChild(bg);
		
		this.mDigits = new AnimatedSprite[mNumDigits];
		
		for (int i = 0; i < mNumDigits; i++) {
			mDigits[i] = new AnimatedSprite(i*mTextureProvider.getTileSize(), 0,
				this.mTextureProvider.getDigitsTexture().deepCopy());
			this.attachChild(mDigits[i]);
		}
	}
	
	public void setScore(int score) {
		int x1 = score % 10;
		int x10 = score % 100 / 10;
		int x100 = score % 1000 / 100;
		
		mDigits[0].setCurrentTileIndex(x100);
		mDigits[1].setCurrentTileIndex(x10);
		mDigits[2].setCurrentTileIndex(x1);
	}

}
