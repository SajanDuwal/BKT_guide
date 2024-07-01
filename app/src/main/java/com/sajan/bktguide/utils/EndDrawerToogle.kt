package com.sajan.bktguide.utils

import androidx.drawerlayout.widget.DrawerLayout
import androidx.core.view.GravityCompat
import androidx.appcompat.widget.AppCompatImageButton
import androidx.appcompat.graphics.drawable.DrawerArrowDrawable
import android.app.Activity
import android.view.View
import androidx.appcompat.app.ActionBar
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import com.sajan.bktguide.R


class EndDrawerToggle(
    activity: Activity, private val drawerLayout: DrawerLayout, toolbar: Toolbar,
    openDrawerContentDescRes: Int, closeDrawerContentDescRes: Int
) : DrawerLayout.DrawerListener {
    private val arrowDrawable: DrawerArrowDrawable
    private val toggleButton: AppCompatImageButton
    private val openDrawerContentDesc: String?
    private val closeDrawerContentDesc: String

    init {
        this.openDrawerContentDesc = activity.getString(openDrawerContentDescRes)
        this.closeDrawerContentDesc = activity.getString(closeDrawerContentDescRes)

        arrowDrawable = DrawerArrowDrawable(toolbar.context)
        arrowDrawable.direction = DrawerArrowDrawable.ARROW_DIRECTION_END
        arrowDrawable.color = ContextCompat.getColor(activity, R.color.colorPrimaryDark)

        toggleButton = AppCompatImageButton(
            toolbar.context, null,
            R.attr.toolbarNavigationButtonStyle
        )
        toolbar.addView(toggleButton, ActionBar.LayoutParams(GravityCompat.END))
        toggleButton.setImageDrawable(arrowDrawable)
        toggleButton.setOnClickListener { toggle() }
    }

    fun syncState() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            setPosition(1f)
        } else {
            setPosition(0f)
        }
    }

    private fun toggle() {
        if (drawerLayout.isDrawerOpen(GravityCompat.END)) {
            drawerLayout.closeDrawer(GravityCompat.END)
        } else {
            drawerLayout.openDrawer(GravityCompat.END)
        }
    }

    private fun setPosition(position: Float) {
        if (position == 1f) {
            arrowDrawable.setVerticalMirror(true)
            toggleButton.contentDescription = closeDrawerContentDesc
        } else if (position == 0f) {
            arrowDrawable.setVerticalMirror(false)
            toggleButton.contentDescription = openDrawerContentDesc
        }
        arrowDrawable.progress = position
    }

    override fun onDrawerSlide(drawerView: View, slideOffset: Float) {
        setPosition(1f.coerceAtMost(0f.coerceAtLeast(slideOffset)))
    }

    override fun onDrawerOpened(drawerView: View) {
        setPosition(1f)
    }

    override fun onDrawerClosed(drawerView: View) {
        setPosition(0f)
    }

    override fun onDrawerStateChanged(newState: Int) {}
}