package dji.sampleV5.aircraft.views.base

import androidx.appcompat.widget.Toolbar

interface BaseActivityContract {
    fun onBackPressed()
    fun onChangeActionBar(toolbar: Toolbar, isDisplayHomeAsUpEnabled: Boolean)
    fun onChangeBottomNavigationVisibility(isVisible: Boolean)
}