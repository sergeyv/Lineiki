package com.ladushki.lineiki;

import org.anddev.andengine.entity.Entity;

public class BallDispencer extends Entity {
	
	final static int NUM_BALLS = 3;
	
	MapTile [] mField;
	ITextureProvider mTextureProvider;

	BallDispencer(ITextureProvider pTextureProvider) {
		super();
		
		this.mTextureProvider = pTextureProvider;	
		mField = new MapTile[NUM_BALLS]; 
		initBackground();
	}
	
	private void initBackground() {
		int tile_size = mTextureProvider.getTileSize();

		for (int i = 0; i < NUM_BALLS; i++) {			
			final MapTile tile = new MapTile(i*tile_size, 0, mTextureProvider.getFieldBGTexture().deepCopy(), (i & 1) == 0);
			final BallSprite ball = new BallSprite(0, 0, mTextureProvider.getBallTexture().deepCopy(), BallColor.randomColor());
			tile.setBall(ball);
			this.attachChild(tile);
			mField[i] = tile;
		}

	}
	
	public BallColor [] getNextBalls(boolean pReset) {
		/* return the currently displayed balls
		 * if pReset is true, the current balls are then 
		 * replaced by a new set
		 */
		BallColor[] colors = new BallColor[NUM_BALLS];
		for (int i = 0; i < NUM_BALLS; i++) {
			final MapTile tile = mField[i];
			final BallSprite ball = tile.getBall();
			colors[i] = ball.getColor();
			if (pReset) {
				ball.setColor(BallColor.randomColor());
			}
		}
		return colors;
	}

	public BallColor [] getNextBalls() {
		/* 
		 * returns the current ball colors and replace them with a new set
		 */
		return getNextBalls(true);
	}
	
	public void setBalls(BallColor [] pColors) {
		/* 
		 * Sets new ball colors
		 */
		for (int i = 0; i < NUM_BALLS; i++) {
			final MapTile tile = mField[i];
			final BallSprite ball = tile.getBall();
			ball.setColor(pColors[i]);
		}
	}
	
	public String serialize() {
		/* converts the field to a simple text representation suitable for 
		 * storing in prefs etc.
		 */
		BallColor [] colors = getNextBalls(false);
		String line = new String();
		for (int i = 0; i < NUM_BALLS; i++) {
			line += colors[i].toChar();
		}
		return line;
	}

	public void deserialize(String data) {
		BallColor [] colors = new BallColor[NUM_BALLS];
		for (int i = 0; i < NUM_BALLS; i++) {
			colors[i] = BallColor.fromChar(data.charAt(i));
		}
		setBalls(colors);
	}		

}
