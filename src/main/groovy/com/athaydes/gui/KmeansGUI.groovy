package com.athaydes.gui

import com.athaydes.algorithms.KMeans
import com.athaydes.algorithms.MemoryClusterStore
import com.athaydes.algorithms.Sample
import javafx.application.Application
import javafx.beans.value.ChangeListener
import javafx.event.EventHandler
import javafx.fxml.FXML
import javafx.fxml.FXMLLoader
import javafx.scene.Group
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.ChoiceBox
import javafx.scene.control.ColorPicker
import javafx.scene.control.Label
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.shape.Rectangle
import javafx.stage.Stage

import javafx.geometry.Dimension2D

class KMeansGUI extends Application {

	def WIDTH = 500
	def HEIGHT = 500

	@Override
	public void start( Stage stage ) throws Exception {
		Parent root = FXMLLoader.load( getClass().getResource( "kmeans.fxml" ) );
		stage.title = "K-Means Visual Test"
		stage.scene = new Scene( root, WIDTH, HEIGHT )
		stage.show()
	}

}

class KMeansGUIController {
	@FXML
	protected final Group kMeansCanvas
	@FXML
	protected final VBox clusterColorsGroup
	@FXML
	protected final ChoiceBox<Integer> clustersPicker

	List<Color> clusterColors = [ ]
	def WIDTH = 300
	def HEIGHT = 300

	KMeans kMeans
	def clusterIndexById = [ : ]

	void initialize( ) {
		initBackground()
		initClusterPicker()
	}

	private void initBackground( ) {
		Rectangle back = new Rectangle( WIDTH, HEIGHT, Paint.valueOf( "#C0C0C0" ) )
		back.arcHeight = 25
		back.arcWidth = 25
		kMeansCanvas.children.add( back )
	}

	private void initClusterPicker( ) {
		clustersPicker.items.addAll( 2..20 )
		clustersPicker.selectionModel.selectedItemProperty().addListener( [
				changed: { observableValue, Integer oldVal, Integer newVal ->
					onClusterCountChange( oldVal, newVal )
				}
		] as ChangeListener<Integer> );
		clustersPicker.selectionModel.selectFirst()
	}

	private void onClusterCountChange( Integer oldVal, Integer newVal ) {
		if ( !oldVal ) oldVal = 0
		if ( oldVal > newVal ) {
			( oldVal - newVal ).times {
				clusterColorsGroup.children.remove( clusterColorsGroup.children.size() - 1 )
			}
		} else {
			( oldVal..<newVal ).each { int index ->
				clusterColorsGroup.children.add( clusterColorPicker( index ) )
			}
		}
		buildKMeans( newVal )
	}

	private void buildKMeans( int k ) {
		def allSamples = [ ]
		clusterIndexById.keySet().each {
			allSamples.addAll( kMeans.store.getSamples( it ) )
		}
		clusterIndexById.clear()

		kMeans = new KMeans( k )

		kMeans.store = new MemoryClusterStore() {

			@Override
			void add( name, sample ) {
				super.add( name, sample )
				def index = clusterIndexById.get( name, clusterIndexById.size() )
				sample.fill = clusterColors[ index ]
			}
		}

		for ( Sample sample in allSamples ) {
			kMeans.classify( sample )
		}

	}

	private clusterColorPicker( int index ) {
		HBox box = new HBox( 5 )
		ColorPicker picker = new ColorPicker()
		picker.onAction = [
				handle: {
					println "Color changed: " + picker.value
					if ( clusterColors.contains( picker.value ) ) {
						picker.value = clusterColors[ index ]
						// show error msg
					} else {
						clusterColors[ index ] = picker.value
						def entry = clusterIndexById.entrySet().find {
							entry -> entry.value == index
						}
						println "Found Entry $entry"
						kMeans.store.getSamples( entry.key ).each {
							it.fill = picker.value
						}
					}
				}
		] as EventHandler
		picker.value = someColor( index )
		clusterColors[ index ] = picker.value
		box.children.add( new Label( "Cluster ${index + 1}" ) )
		box.children.add( picker )
		return box
	}

	void onClickCanvas( MouseEvent event ) {
		println( event.x + " " + event.y )
		def sample = new GuiSample( width: 10, height: 10, fill: Color.BLACK )
		sample.location = [ x: event.x - 5, y: event.y - 5 ]
		kMeansCanvas.children.add( sample )
		kMeans.classify( sample )
	}

	// this will produce up to 30 different colors
	private Color someColor( Integer index ) {
		def r = ( index * 30 ) % 255
		def g = ( index * 40 ) % 255
		def b = ( index * 50 ) % 255
		def color = [ r, g, b ] as Color
		if ( clusterColors.contains( color ) ) return someColor( ( index + 1 ) % 30 )
		return color
	}

}

class GuiSample extends Rectangle implements Sample {
	BigDecimal value

	def setLocation( location ) {
		this.x = location.x
		this.y = location.y
		value = x ** 2 + y ** 2
	}
}

Application.launch( KMeansGUI.class )
