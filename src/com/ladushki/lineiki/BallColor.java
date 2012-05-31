/**
 * 
 */
package com.ladushki.lineiki;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * @author sergey
 *
 */
public enum BallColor {
	BLUE  (20),
	GREEN (30),
	RED   (40),
	PURPLE(50),
	CYAN  (60),
	DARK_GREEN (70),
	ORANGE (80);
	
	private static final List<BallColor> VALUES =
		    Collections.unmodifiableList(Arrays.asList(values()));
	private static final int SIZE = VALUES.size();
	private static final Random RANDOM = new Random();
	
	
	
	private final int m_nStartTile;
	
	BallColor(int pStartTile) {
		this.m_nStartTile = pStartTile;
	}
	
	public int getStartTile() {
		return this.m_nStartTile;
	}
	
	public static BallColor randomColor()  {
	    return VALUES.get(RANDOM.nextInt(SIZE));
	  }

}
