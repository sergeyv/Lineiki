/**
 * 
 */
package com.ladushki.lineiki;

import org.anddev.andengine.entity.modifier.ColorModifier;
import org.anddev.andengine.entity.modifier.DelayModifier;
import org.anddev.andengine.entity.modifier.ParallelEntityModifier;
import org.anddev.andengine.entity.modifier.SequenceEntityModifier;
import org.anddev.andengine.entity.sprite.AnimatedSprite;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;

/**
 * @author sergey
 *
 */
public class MapTile extends AnimatedSprite {
	
	private boolean m_bEven;
	private BallSprite m_ball;

	public MapTile(int pX, int pY, TiledTextureRegion pTiledTextureRegion, boolean pEven) {
		super(pX, pY, pTiledTextureRegion);
		m_bEven = pEven;
		
		if (m_bEven) {
			this.setCurrentTileIndex(0);
		} else {
			this.setCurrentTileIndex(1);			
		}
	}
	
	/*public void startBlinking() {
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
	}*/

	void setBall(BallSprite pBall) {
		if (m_ball != null) {
			/// TODO: raise an error
			//detachBall();
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


	public void blink() {
		this.blink(0);
	}
	
	public void blink(float delay) {
		this.registerEntityModifier(new SequenceEntityModifier(
				new DelayModifier(delay),
				new ColorModifier(0.1f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f),
				new ColorModifier(0.1f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f),
				new ColorModifier(0.1f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f),
				new ColorModifier(0.1f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f),
				new ColorModifier(0.1f, 1.0f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f),
				new ColorModifier(0.1f, 1.0f, 1.0f, 0.0f, 1.0f, 0.0f, 1.0f)
				));
	}
}
