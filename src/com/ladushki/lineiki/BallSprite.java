package com.ladushki.lineiki;

import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;


public class BallSprite extends AnimatedSprite {

	private BallColor m_nColor;

	public BallSprite(int pX, int pY, TiledTextureRegion pTiledTextureRegion, BallColor pColor) {
		super(pX*35, pY*35, pTiledTextureRegion);
		setColor(pColor);
	}
	
	BallColor getColor() {
		return m_nColor;
	}
	
	void setColor(BallColor pColor) {
		m_nColor = pColor;		
		this.setCurrentTileIndex(m_nColor.getStartTile());			
	}

}
