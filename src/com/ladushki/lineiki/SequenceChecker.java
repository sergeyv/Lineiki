package com.ladushki.lineiki;

import java.util.ArrayList;

public class SequenceChecker {

	private BallColor m_lastColor;
	ArrayList<MapTile> m_currentSequence = new ArrayList<MapTile>();
	ArrayList<MapTile> m_allMatches = new ArrayList<MapTile>();
	
	SequenceChecker() {
		m_lastColor = null;
	}

	public void check(MapTile tile) {
		BallSprite ball = tile.getBall();
		if (ball == null) {
			endSequence();
			return;
		}
		
		if (m_lastColor != null && ball.getColor() != m_lastColor) {
			endSequence();
		}
		
		m_currentSequence.add(tile);
		m_lastColor = ball.getColor();
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
	
	public MapTile[] getMatchedTiles() {
		int num = m_allMatches.size();
		if (num > 0) {
			MapTile[] result = new MapTile[num];
			for (int i = 0; i < num; i++) {
				result[i] = m_allMatches.get(i);
			}
			return result;
		}
		return null;
		
	}
}
