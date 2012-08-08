package com.ladushki.lineiki;

import org.anddev.andengine.entity.Entity;
import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.modifier.IEntityModifier.IEntityModifierListener;
import org.anddev.andengine.entity.modifier.MoveYModifier;
import org.anddev.andengine.entity.modifier.ParallelEntityModifier;
import org.anddev.andengine.entity.modifier.ScaleModifier;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.entity.sprite.Sprite;
import org.anddev.andengine.util.modifier.IModifier;


public class ScoreDisplay extends Entity {

	private ITextureProvider mTextureProvider;
	private int mNumDigits;
	private AnimatedSprite[] mDigits;
	private AnimatedSprite[] mAnimDigits;
	private int[] mCurrentScore;
	

	public ScoreDisplay(ITextureProvider pTextureProvider, int pNumDigits) {
		
		this.mTextureProvider = pTextureProvider;	
		this.mNumDigits = pNumDigits;
		
		this.mCurrentScore = new int[mNumDigits];
		initBackground();
	}

	private void initBackground() {
		Sprite bg = new Sprite(0,0, mTextureProvider.getScoreBGTexture());
		bg.setWidth(bg.getHeight()*mNumDigits);
		this.attachChild(bg);
		
		this.mDigits = new AnimatedSprite[mNumDigits];
		
		for (int i = 0; i < mNumDigits; i++) {
			mDigits[i] = new AnimatedSprite(i*mTextureProvider.getTileSize(), 0,
				this.mTextureProvider.getDigitsTexture().deepCopy());
			this.attachChild(mDigits[i]);
		}
		this.mAnimDigits = new AnimatedSprite[mNumDigits];
		
		for (int i = 0; i < mNumDigits; i++) {
			AnimatedSprite digit;
			digit = new AnimatedSprite(
					i*mTextureProvider.getTileSize(), 
					0,
					this.mTextureProvider.getDigitsTexture().deepCopy());
			
			/*digit.setHeight(0);*/
			digit.setVisible(false);
			
			this.attachChild(digit);
			mAnimDigits[i] = digit;
		}
	}
	
	public void setScore(int score) {
		float DURATION = 1.0f;
		int absScore = Math.abs(score);
		final int [] newDigits = {absScore % 1000 / 100, absScore % 100 / 10, absScore % 10};

		if (score < 0) {
			newDigits[0] = 11;
		}
		
		for (int i = 0; i < mNumDigits; i++) {
			
			if (newDigits[i] == mCurrentScore[i]) {
				continue;
			}
			
			final AnimatedSprite digit = mDigits[i];
			final AnimatedSprite anim_digit = mAnimDigits[i];
			final int digit_num = newDigits[i];
			
			anim_digit.registerEntityModifier(new ParallelEntityModifier(
					new IEntityModifierListener() {
	
						public void onModifierStarted(
								IModifier<IEntity> pModifier, IEntity pItem) {
							anim_digit.setCurrentTileIndex(digit_num);
							anim_digit.setVisible(true);
						}
	
						public void onModifierFinished(
								IModifier<IEntity> pModifier, IEntity pItem) {
							anim_digit.setVisible(false);
							digit.setScale(1.0f, 1.0f);
							digit.setPosition(digit.getX(), 0);
							digit.setCurrentTileIndex(digit_num);
							
						}
					},
					new MoveYModifier(DURATION, mTextureProvider.getTileSize()/2, 0),
					new ScaleModifier(DURATION, 1.0f, 1.0f, 0.0f, 1.0f)
				)
			);
			
			digit.registerEntityModifier(new ParallelEntityModifier(
					new MoveYModifier(DURATION, 0, -mTextureProvider.getTileSize()/2),
					new ScaleModifier(DURATION, 1.0f, 1.0f, 1.0f, 0.0f)
				)
			);
		}
				
		mCurrentScore[0] = newDigits[0];
		mCurrentScore[1] = newDigits[1];
		mCurrentScore[2] = newDigits[2];
	}

}
