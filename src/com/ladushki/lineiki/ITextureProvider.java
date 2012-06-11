package com.ladushki.lineiki;

import org.anddev.andengine.opengl.texture.region.TextureRegion;
import org.anddev.andengine.opengl.texture.region.TiledTextureRegion;

public interface ITextureProvider {
	public TiledTextureRegion getBallTexture();
	public TiledTextureRegion getFieldBGTexture();
	public TextureRegion getDotTexture();

}
