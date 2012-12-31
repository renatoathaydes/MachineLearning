package com.athaydes.gui

import javafx.application.Application
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Group
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.ColorPicker
import javafx.scene.input.MouseEvent
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.shape.Rectangle
import javafx.stage.Stage

class KMeansGUI extends Application {

	static final int WIDTH = 500
	static final int HEIGHT = 500

	@Override
	public void start( Stage stage ) throws Exception {
		Parent root = FXMLLoader.load( getClass().getResource( "kmeans.fxml" ) );
		stage.title = "K-Means Visual Test"
		stage.scene = new Scene( root, WIDTH, HEIGHT )
		stage.show()
	}

}

class KMeansGUIController {
	@FXML protected Group kMeansCanvas
	@FXML protected final ColorPicker colorPicker

	private Color currColor
	def WIDTH = 300
	def HEIGHT = 300

	void initialize() {
		Rectangle back = new Rectangle(WIDTH, HEIGHT, Paint.valueOf("#C0C0C0"))
		back.arcHeight = 25
		back.arcWidth = 25
		kMeansCanvas.children.add(back)
	}

	void onColorChange( ) {
		println "Color changing to: " + colorPicker.value
		currColor = colorPicker.value
	}

	void onClickCanvas(MouseEvent event) {
		println( event.x + " " + event.y )
		def rect = new Rectangle( 10, 10, currColor )
		rect.x = event.x; rect.y = event.y
		kMeansCanvas.children.add( rect )
	}
}


Application.launch( KMeansGUI.class )

