package com.ladushki.lineiki;

import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;

public interface ITextureProvider {
	public TiledTextureRegion getBallTexture();
	public TiledTextureRegion getFieldBGTexture();
	public TextureRegion getDotTexture();
	public TextureRegion getBallMarkerTexture();
	public TextureRegion getSquareMarkerTexture();
	
	public TextureRegion getScoreBGTexture();
	
	public TiledTextureRegion getDigitsTexture();
	
	public TextureRegion getHighscoreReachedTexture();
	
	public	int getTileSize();
	public int getScreenWidth();
	public int getScreenHeight();
	
	public boolean getIsTablet();

}
