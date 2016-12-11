package org.eclipse.epsilon.zest.utils;

import javafx.scene.paint.Color;
import javafx.scene.shape.Polygon;

/**
 * Class with factory methods for various arrow types.
 */
public class ArrowTypes {

	private ArrowTypes() {}

	public static Polygon filledTriangle() {
		Polygon triangle = new Polygon(5, -5, 5, 5, 0, 0);
		triangle.setFill(Color.BLACK);
		return triangle;
	}
}
