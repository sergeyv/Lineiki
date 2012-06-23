package com.ladushki.lineiki;

import java.util.ArrayList;

public class SequenceChecker {

	private BallColor m_lastColor;
	ArrayList<FieldItem> m_currentSequence = new ArrayList<FieldItem>();
	ArrayList<FieldItem> m_allMatches = new ArrayList<FieldItem>();
	
	SequenceChecker() {
		m_lastColor = null;
	}

	public void check(BallColor color, int x, int y) {
		//BallSprite ball = tile.getBall();
		if (color == null) {
			endSequence();
			return;
		}
		
		if (m_lastColor != null && color != m_lastColor) {
			endSequence();
		}
		
		m_currentSequence.add(new FieldItem(color, x, y));
		m_lastColor = color;
	}
	
	private void endSequence() {
		if (m_currentSequence.size() >= 5) {
			m_allMatches.addAll(m_currentSequence);
		}
		m_currentSequence.clear();
	}
	
	public void startRow() {
		m_lastColor = null;
		endSequence();
	}
	
	public FieldItem[] getMatchedTiles() {
		int num = m_allMatches.size();
		if (num > 0) {
			FieldItem[] result = new FieldItem[num];
			for (int i = 0; i < num; i++) {
				result[i] = m_allMatches.get(i);
			}
			return result;
		}
		return null;
		
	}
}
