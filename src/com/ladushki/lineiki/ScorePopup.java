package com.ladushki.lineiki;

import javax.microedition.khronos.opengles.GL10;

import org.anddev.andengine.entity.Entity;
import org.anddev.andengine.entity.IEntity;
import org.anddev.andengine.entity.modifier.AlphaModifier;
import org.anddev.andengine.entity.modifier.ParallelEntityModifier;
import org.anddev.andengine.entity.modifier.ScaleModifier;
import org.anddev.andengine.entity.modifier.IEntityModifier.IEntityModifierListener;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.util.modifier.IModifier;


public class ScorePopup extends Entity {
	/*
	 * A popup which appears when score changes
	 */
	
	private int nextX;
	private ITextureProvider mTextureProvider;
	
	public ScorePopup(ITextureProvider pTextureProvider, int pDelta) {
		nextX = 0;
		mTextureProvider = pTextureProvider;
		
		AnimatedSprite digit;
		
		/// plus/minus sign

		digit = newDigit();
		if (pDelta > 0) {
			digit.setCurrentTileIndex(10);
		} else {
			digit.setCurrentTileIndex(11);
		}
			
		/// hundreds
		if (pDelta > 99) {
			digit = newDigit();
			digit.setCurrentTileIndex(pDelta % 1000 / 100);
		}
		// tens
		if (pDelta > 9) {
			digit = newDigit();
			digit.setCurrentTileIndex(pDelta % 100 / 10);
		}
		// single numbers
		digit = newDigit();
		digit.setCurrentTileIndex(pDelta % 10);		
		
		this.setScaleCenter(nextX/2, mTextureProvider.getTileSize()/2);
	}

	private AnimatedSprite newDigit() {
		AnimatedSprite digit = new AnimatedSprite(nextX, 0, mTextureProvider.getDigitsTexture().deepCopy());
		digit.setBlendFunction(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
		digit.setAlpha(0.5f);
		
		digit.registerEntityModifier(new ParallelEntityModifier(
				new ScaleModifier(2.0f, 1, 2),
				new AlphaModifier(2.0f, 0.5f, 0.0f)
				));

		
		nextX += mTextureProvider.getTileSize();
		this.attachChild(digit);
		return digit;
	}
}
