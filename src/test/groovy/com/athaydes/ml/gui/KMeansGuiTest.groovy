package com.athaydes.ml.gui

import com.athaydes.automaton.FXApp
import com.athaydes.automaton.FXAutomaton
import com.athaydes.automaton.Speed
import org.junit.Before
import org.junit.Test

/**
 *
 * User: Renato
 */
class KMeansGuiTest {

	@Before
	void setup( ) {
		FXApp.startApp( new KMeansGUI() )
	}

	@Test
	void testSamplesShowOnScreenWithDifferentColors( ) {
		sleep 2000
		assert FXApp.scene.lookup( '#cluster-count-picker' )

		def canvas = FXApp.scene.lookup( '#k-means-canvas' )
		assert canvas

		FXAutomaton.user.moveTo( canvas ).moveBy( -50, -50 )
		def moveLeft = true
		5.times {
			5.times { FXAutomaton.user.click().moveBy( moveLeft ? 20 : -20, 0 ) }
			FXAutomaton.user.click().moveBy( 0, 20 )
			moveLeft = !moveLeft
		}

		def samples = FXApp.scene.root.lookupAll( 'GuiSample' )
		assert samples.size() == 30

		def color1 = samples[ 0 ].fill
		assert samples.findAll { it.fill == color1 }.size() ==
				samples.findAll { it.fill != color1 }.size()

	}

	@Test
	void testCanChangeColorBeforeAddingSamples( ) {
		sleep 500
		def colorsMenuButton = FXApp.scene.lookup( '#colors-menu-button' )
		assert colorsMenuButton

		FXAutomaton.user.clickOn( colorsMenuButton ).pause( 250 )
				.clickOn( colorsMenuButton.items[ 0 ].graphic )
				.moveBy( 50, 100 ).click()
				.clickOn( colorsMenuButton ).pause( 250 )

		def canvas = FXApp.scene.lookup( '#k-means-canvas' )
		assert canvas

		FXAutomaton.user.moveTo( canvas ).click().moveBy( 50, 0 ).click().pause( 500 )

		def samples = FXApp.scene.root.lookupAll( 'GuiSample' )
		assert samples.size() == 2
		assert samples[ 0 ].fill != samples[ 1 ].fill
	}

	@Test( timeout = 8000L )
	void performanceTest( ) {
		sleep 2000
		def canvas = FXApp.scene.lookup( '#k-means-canvas' )
		assert canvas

		def VF = Speed.VERY_FAST
		def center = FXAutomaton.centerOf( canvas )
		FXAutomaton.user.moveTo(
				( center.x - KMeansPanel.CANVAS_WIDTH / 2 ) as int,
				( center.y - KMeansPanel.CANVAS_HEIGHT / 2 ) as int,
				VF )
				.moveBy( 5, 5, VF )

		def moveLeft = true
		20.times {
			20.times { FXAutomaton.user.click().moveBy( moveLeft ? 5 : -5, 0, VF ) }
			FXAutomaton.user.click().moveBy( 0, 5, VF )
			moveLeft = !moveLeft
		}
	}

}
