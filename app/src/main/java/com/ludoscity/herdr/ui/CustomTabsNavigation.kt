/*
 *     Copyright (c) 2020. f8full https://github.com/f8full
 *     Herdr is a privacy conscious multiplatform mobile data collector
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

//See: https://proandroiddev.com/add-chrome-custom-tabs-to-the-android-navigation-component-75092ce20c6a

package com.ludoscity.herdr.ui

import android.app.Activity
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.util.AttributeSet
import androidx.annotation.ColorRes
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.content.withStyledAttributes
import androidx.navigation.*
import androidx.navigation.fragment.NavHostFragment
import com.ludoscity.herdr.R

@Navigator.Name("customtab")
class CustomTabsNavigator(private val context: Context) :
    Navigator<CustomTabsNavigator.Destination>() {

    override fun createDestination(): Destination = Destination(this)

    override fun navigate(
        destination: Destination,
        args: Bundle?,
        navOptions: NavOptions?,
        navigatorExtras: Extras?
    ): NavDestination? {
        val builder = CustomTabsIntent.Builder()
        //builder.setToolbarColor(ContextCompat.getColor(context, destination.toolbarColor))
        //builder.setSecondaryToolbarColor(ContextCompat.getColor(context, destination.secondaryToolbarColor))

        val intent = builder.build()
        val url = args?.getString(URL_BUNDLE_KEY)
            ?: throw IllegalArgumentException("Need to specify url in Bundle")

        intent.launchUrl(context, Uri.parse(url))
        return null //do not add to the backstack managed by Chrome Custom Tabs
    }

    override fun popBackStack(): Boolean = true //managed by Chrome Custom Tabs

    @NavDestination.ClassType(Activity::class)
    class Destination(navigator: Navigator<out NavDestination>) : NavDestination(navigator) {

        @ColorRes
        var toolbarColor: Int = 0

        @ColorRes
        var secondaryToolbarColor: Int = 0

        override fun onInflate(context: Context, attrs: AttributeSet) {
            super.onInflate(context, attrs)

            context.withStyledAttributes(attrs, R.styleable.CustomTabsNavigator, 0, 0) {
                toolbarColor = getResourceId(R.styleable.CustomTabsNavigator_toolbarColor, 0)
                secondaryToolbarColor =
                    getResourceId(R.styleable.CustomTabsNavigator_secondaryToolbarColor, 0)
            }
        }
    }

    companion object {
        const val URL_BUNDLE_KEY = "url"
    }
}

@Suppress("unused")
//Used im XML --
class MyNavHostFragment : NavHostFragment() {
    override fun onCreateNavController(navController: NavController) {
        super.onCreateNavController(navController)

        context?.let {
            navController.navigatorProvider += CustomTabsNavigator(it)
        }
    }
}