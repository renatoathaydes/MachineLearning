package com.athaydes.ml.gui

import com.athaydes.automaton.FXApp
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
	void testEverythingIsOnScreen( ) {
		sleep 2000
		assert FXApp.scene.lookup( '#clustersPicker' )
		//assert FXApp.scene.lookup( '#clusterColorsGroup' )
		assert FXApp.scene.lookup( '#kMeansCanvas' )
	}

}
