package com.ladushki.lineiki;

import java.util.ArrayList;
import java.util.PriorityQueue;

import android.graphics.Point;

public class PathFinder {
	/*
	 * Implements A* algorithm
	 */
	
	int	m_width;
	int m_height;
	PathElement [][] m_field;
	
	int m_targetX;
	int m_targetY;
	
	/* Elements in the PriorityQueue are sorted according to their "natural sort order
	 * which for PathElement is implemented as comparison of its cost, so the "cheapest" element
	 * is always first in the queue 
	 */
	PriorityQueue<PathElement> m_openQueue;
	
	class PathElement implements Comparable<PathElement> {
		/*
		 * Inner class for the A* path elements
		 */
		public PathElement m_parent;
		
		public boolean m_passable;
		public boolean m_closed;
		
		public int m_cost;
		public int m_x;
		public int m_y;
		
		PathElement() {
			m_passable = false;
			m_closed = false;
			m_cost = 0;
		}
		
		public int compareTo(PathElement other) {
			/*
			 * Defines sort order according to element's cost
			 */
			return this.m_cost - other.m_cost;
		}
	}

	
	PathFinder(int pWidth, int pHeight) {
		m_width = pWidth;
		m_height = pHeight;
		m_field = new PathElement[m_width][m_height]; 
		
		for (int y = 0; y < m_height; y++) {
			for (int x = 0; x < m_width; x++) {
				PathElement elem = new PathElement();
				m_field[x][y] = elem;
				elem.m_x = x;
				elem.m_y = y;
			}
		}

		m_openQueue = new PriorityQueue<PathElement>();
	}
	
	void setPassable(int pX, int pY, boolean pPassable) {
		m_field[pX][pY].m_passable = pPassable;
	}
	
	private void setOrigin(int pX, int pY) {
		PathElement elem = m_field[pX][pY];
		elem.m_cost = manhattanCost(pX, pY);
		elem.m_parent = null;
		m_openQueue.add(elem);
	}
	
	private void setTarget(int pX, int pY) {
		m_targetX = pX;
		m_targetY = pY;
	}
	
	private int manhattanCost(int pX, int pY) {
		return Math.abs(pX - m_targetX) + Math.abs(pY - m_targetY);
	}
	
	private void possiblyAddElement(PathElement elem, PathElement parent) {
		if (elem.m_closed) {
			return;
		}
		if (! elem.m_passable) {
			return;
		}
		int cost = parent.m_cost + manhattanCost(elem.m_x, elem.m_y);
		if (elem.m_parent == null || cost < elem.m_cost) {
			elem.m_parent = parent;
			elem.m_cost = cost;
			m_openQueue.add(elem);
		}
	}
	
	private void addAdjacentElementsOf(PathElement elem) {
		if (elem.m_y > 0) {
			possiblyAddElement(m_field[elem.m_x][elem.m_y-1], elem);
		}
		if (elem.m_y < m_height - 1) {
			possiblyAddElement(m_field[elem.m_x][elem.m_y+1], elem);
		}
		if (elem.m_x > 0) {
			possiblyAddElement(m_field[elem.m_x-1][elem.m_y], elem);
		}
		if (elem.m_x < m_height - 1) {
			possiblyAddElement(m_field[elem.m_x+1][elem.m_y], elem);
		}
	}
	
	public Point[] findPath(int originX, int originY, int targetX, int targetY) {
		setTarget(targetX, targetY); // should go first
		setOrigin(originX, originY);
		
		PathElement elem = m_openQueue.poll();
		while (elem != null) {
			elem.m_closed = true;
			if (elem.m_x == m_targetX && elem.m_y == m_targetY) {
				ArrayList<PathElement> path = new ArrayList<PathElement>();
				PathElement e = elem; 
				while (e != null) {
					path.add(e);
					e = e.m_parent;
				}
				Point [] points = new Point[path.size()];
				for (int i = 0; i < path.size(); i++) {
					e = path.get(i);
					points[i] = new Point(e.m_x, e.m_y);
				}
				return points;
			}
			addAdjacentElementsOf(elem);
			elem = m_openQueue.poll();
		}
		/// FAILURE
		return null;
	}
	
	
}
	
