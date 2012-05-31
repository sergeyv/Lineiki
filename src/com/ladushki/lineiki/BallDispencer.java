package com.ladushki.lineiki;

import org.anddev.andengine.entity.Entity;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;

public class BallDispencer extends Entity {
	
	final static int NUM_BALLS = 3;
	
	MapTile [] mField;
	TiledTextureRegion mTextureRegion;

	BallDispencer(TiledTextureRegion pTextureRegion) {
		super();
		
		this.mTextureRegion = pTextureRegion;	
		mField = new MapTile[NUM_BALLS]; 
		initBackground();
	}
	
	private void initBackground() {
		for (int i = 0; i < NUM_BALLS; i++) {			
			final MapTile tile = new MapTile(i, 0, this.mTextureRegion.deepCopy(), (i & 1) == 0);
			final BallSprite ball = new BallSprite(0, 0, this.mTextureRegion.deepCopy(), BallColor.randomColor());
			tile.setBall(ball);
			this.attachChild(tile);
			mField[i] = tile;
		}

	}
	
	BallColor [] getNextBalls() {
		BallColor[] colors = new BallColor[NUM_BALLS];
		for (int i = 0; i < NUM_BALLS; i++) {
			final MapTile tile = mField[i];
			final BallSprite ball = tile.getBall();
			colors[i] = ball.getColor();
			ball.setColor(BallColor.randomColor());
		}
		return colors;
	}
}
