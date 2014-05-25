package com.softsquare.side;

import com.badlogic.gdx.math.Vector2;

public class Shaper {
	private int count, counter = 0;
	private Vector2[] vectorList;

	Shaper(int count) {
		this.count = count;
		vectorList = new Vector2[count];
	}

	public void add(Vector2 v) {
		if (counter < count) {
			vectorList[counter] = v;
			counter++;
		} else {
			System.out.println("Too many vectors");
		}
	}

	public void set(Vector2[] tab) {
		if (tab.length > vectorList.length) {
			System.out.println("Too long array");
		} else {
			for (int i = 0; i < vectorList.length; i++) {
				vectorList[i] = tab[i];
			}
		}
	}

	public int getCount() {
		return count;
	}

	public void setCount(int count) {
		this.count = count;
	}

	public Vector2[] getVectorList() {
		return vectorList;
	}

	public void setVectorList(Vector2[] vectorList) {
		this.vectorList = vectorList;
	}
}
