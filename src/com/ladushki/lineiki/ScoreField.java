package com.ladushki.lineiki;

import org.anddev.andengine.entity.Entity;
import org.anddev.andengine.entity.sprite.Sprite;


public class ScoreField extends Entity {

	private ITextureProvider mTextureProvider;
	private int mNumDigits;

	public ScoreField(ITextureProvider pTextureProvider, LineikiActivity pParentActivity, int pNumDigits) {
		
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
	}

}
