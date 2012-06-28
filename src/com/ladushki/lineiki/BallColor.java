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
	BLUE  (0),
	GREEN (1),
	RED   (2),
	PURPLE(3),
	CYAN  (4),
	DARK_GREEN (5),
	ORANGE (6);
	
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

	public char toChar() {
		switch(this) {
		case BLUE:
			return 'B';
		case GREEN:
			return 'G';
		case RED:
			return 'R';
		case PURPLE: 
			return 'P';
		case CYAN:
			return 'C';
		case DARK_GREEN:
			return 'D';
		case ORANGE:
			return 'O';
		}
		return ' ';
	}

	public static BallColor fromChar(char value) {
		switch(value) {
		case 'B':
			return BLUE;
		case 'G':
			return GREEN;
		case 'R':
			return RED;
		case 'P': 
			return PURPLE;
		case 'C':
			return CYAN;
		case 'D':
			return DARK_GREEN;
		case 'O':
			return ORANGE;
		}
		return null;
	}

}
