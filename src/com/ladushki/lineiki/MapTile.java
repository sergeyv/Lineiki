/**
 * 
 */
package com.ladushki.lineiki;

import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.input.touch.TouchEvent;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;

/**
 * @author sergey
 *
 */
public class MapTile extends AnimatedSprite {
	
	private boolean m_bEven;
	private boolean m_bBlinking;
	
	private BallSprite m_ball;

	public MapTile(int pX, int pY, TiledTextureRegion pTiledTextureRegion, boolean pEven) {
		super(pX*35, pY*35, pTiledTextureRegion);
		m_bEven = pEven;
		
		if (m_bEven) {
			this.setCurrentTileIndex(0);
		} else {
			this.setCurrentTileIndex(10);			
		}
	}
	
	public void startBlinking() {
		int start, end;
		if (m_bEven) {
			start = 0; //
			end = 9;
		} else {
			start = 10;
			end = 19;
		}
		this.animate(new long[]{50, 50, 50, 50, 50, 50, 50, 50, 50, 50}, start, end, true);
		this.m_bBlinking = true;
	}
	
	public void stopBlinking() {
		this.stopAnimation();
		if (m_bEven) {
			this.setCurrentTileIndex(0);
		} else {
			this.setCurrentTileIndex(10);			
		}		
		this.m_bBlinking = false;
	}
	
	public boolean IsBlinking() {
		return m_bBlinking;
	}

	void setBall(BallSprite pBall) {
		if (m_ball != null) {
			/// either delete the existing ball or raise an error
			detachBall();
		}
		m_ball = pBall;
		if (pBall != null) {
			this.attachChild(pBall);
		}
	}
	
	BallSprite detachBall() {
		this.detachChild(m_ball);
		BallSprite b = this.m_ball;
		this.m_ball = null;
		return b;
	}
	
	BallSprite getBall() {
		return m_ball;
	}
	
	boolean isOccupied() {
		return this.m_ball != null;
	}


}
