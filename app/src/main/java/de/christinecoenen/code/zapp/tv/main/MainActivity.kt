package de.christinecoenen.code.zapp.tv.main

import android.os.Bundle
import androidx.fragment.app.FragmentActivity
import de.christinecoenen.code.zapp.R

class MainActivity : FragmentActivity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		setContentView(R.layout.tv_activity_main)
	}

	override fun onBackPressed() {
		val mainFragment =
			supportFragmentManager.findFragmentById(R.id.main_fragment) as MainFragment

		// give MainFragment the change to hadle back presses by itself
		val backPressedHandled = mainFragment.onBackPressed()

		if (!backPressedHandled) {
			super.onBackPressed()
		}
	}
}
