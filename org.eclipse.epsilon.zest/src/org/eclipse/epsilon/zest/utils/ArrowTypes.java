package org.eclipse.epsilon.zest.utils;


import javafx.scene.shape.Polygon;

/**
 * Class with factory methods for various arrow types.
 */
public class ArrowTypes {

	private ArrowTypes() {}

	public static Polygon filledTriangle() {
		Polygon triangle = new Polygon(5, -5, 5, 5, 0, 0);

		// setFill doesn't help - it's overridden by the default Zest CSS,
		// so we have to use setStyle or add our own CSS to the Scene.
		triangle.setStyle("-fx-fill: black");

		return triangle;
	}
}
