package com.athaydes.ml.gui

import com.athaydes.ml.algorithms.KMeans
import com.athaydes.ml.algorithms.MemoryClusterStore
import com.athaydes.ml.algorithms.Sample
import javafx.application.Application
import javafx.beans.value.ChangeListener
import javafx.event.EventHandler
import javafx.geometry.Insets
import javafx.geometry.Pos
import javafx.scene.Group
import javafx.scene.Parent
import javafx.scene.Scene
import javafx.scene.control.*
import javafx.scene.input.MouseEvent
import javafx.scene.layout.VBox
import javafx.scene.paint.Color
import javafx.scene.paint.Paint
import javafx.scene.shape.Rectangle
import javafx.scene.text.Text
import javafx.stage.Stage

class KMeansGUI extends Application {

	final WIDTH = 500
	final HEIGHT = 500

	@Override
	public void start( Stage stage ) throws Exception {
		Parent root = KMeansPanel.create()
		stage.title = "K-Means Visual Test"
		stage.scene = new Scene( root, WIDTH, HEIGHT )
		stage.show()
	}

	static void main( args ) {
		Application.launch KMeansGUI
	}

}

class KMeansPanel extends VBox {
	final Group kMeansCanvas
	final MenuButton colorsMenuButton
	final ChoiceBox<Integer> clusterCountPicker
	final Map<String, Integer> clusterIndexById = [ : ]
	final List<Color> clusterColors = [ ]
	static final CANVAS_WIDTH = 300
	static final CANVAS_HEIGHT = 300

	KMeans kMeans

	static create( ) {
		def gui = new KMeansPanel()
		gui.initBackground()
		gui.initClusterPicker()
		return gui
	}

	KMeansPanel( ) {
		this.stylesheets.add 'com/athaydes/ml/gui/kmeans.css'
		this.padding = new Insets( 25, 25, 10, 25 )
		this.spacing = 10
		this.alignment = Pos.CENTER

		clusterCountPicker = new ChoiceBox( id: 'cluster-count-picker' )
		colorsMenuButton = new MenuButton( text: 'Change cluster colors', id: 'colors-menu-button' )
		kMeansCanvas = new Group( id: 'k-means-canvas' )

		children.setAll(
				text( 'K-Means Visual Test Tool', 'panel-title' ),
				text( 'Click on the area below to create data points' ),
				new ToolBar(
						text( 'Number of clusters:' ),
						clusterCountPicker,
						colorsMenuButton
				),
				kMeansCanvas
		)
	}

	static text( String text, String... styleClasses ) {
		def res = new Text( text )
		res.styleClass.addAll styleClasses
		return res
	}

	private void initBackground( ) {
		Rectangle back = new Rectangle( CANVAS_WIDTH, CANVAS_HEIGHT, Paint.valueOf( "#C0C0C0" ) )
		back.arcHeight = 25
		back.arcWidth = 25
		back.onMouseClicked = [ handle: this.&onClickCanvas ] as EventHandler
		kMeansCanvas.children.add( back )
	}

	private void initClusterPicker( ) {
		clusterCountPicker.items.addAll( 2..20 )
		clusterCountPicker.selectionModel.selectedItemProperty().addListener( [
				changed: { observableValue, Integer oldVal, Integer newVal ->
					onClusterCountChange( oldVal, newVal )
				}
		] as ChangeListener<Integer> );
		clusterCountPicker.selectionModel.selectFirst()
	}

	private void onClusterCountChange( Integer oldVal, Integer newVal ) {
		oldVal = oldVal ?: 0
		if ( oldVal > newVal ) {
			( oldVal - newVal ).times {
				colorsMenuButton.items.remove( colorsMenuButton.items.size() - 1 )
			}
		} else {
			( oldVal..<newVal ).each { int index ->
				colorsMenuButton.items.add( clusterColorPicker( index ) )
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
			void add( String name, Sample sample ) {
				super.add( name, sample )
				def index = clusterIndexById.get( name, clusterIndexById.size() )
				sample.fill = clusterColors[ index ]
			}
		}

		kMeans.classifyAll( allSamples )

	}

	private MenuItem clusterColorPicker( int index ) {
		ColorPicker picker = new ColorPicker( id: "cluster-color-picker-$index" )
		picker.onAction = [
				handle: {
					if ( clusterColors.contains( picker.value ) ) {
						picker.value = clusterColors[ index ]
						// show error msg
					} else {
						clusterColors[ index ] = picker.value
						def entry = clusterIndexById.entrySet().find {
							entry -> entry.value == index
						}
						if ( entry ) {
							kMeans.store.getSamples( entry.key ).each {
								it.fill = picker.value
							}
						}
					}
				}
		] as EventHandler
		picker.value = someColor( index )
		clusterColors[ index ] = picker.value
		return new MenuItem( "Cluster ${index + 1}", picker )
	}

	void onClickCanvas( MouseEvent event ) {
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
	static idCount = 0
	BigDecimal value

	GuiSample( ) {
		this.id = "gui-sample-${idCount++}"
	}

	def setLocation( location ) {
		this.x = location.x
		this.y = location.y
		value = x ** 2 + y ** 2
	}
}