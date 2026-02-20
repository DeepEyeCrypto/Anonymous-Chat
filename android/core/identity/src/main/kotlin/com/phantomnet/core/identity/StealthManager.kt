package com.phantomnet.core.identity

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.util.Log

/**
 * Manages the visibility and disguise of the application icon.
 * Uses Android Activity Aliases to switch identities in the launcher.
 */
object StealthManager {
    private const val TAG = "StealthManager"
    
    // Alias fully qualified names
    private const val MAIN_ALIAS = "com.phantomnet.app.MainAlias"
    private const val CALC_ALIAS = "com.phantomnet.app.CalcAlias"
    private const val SYSTEM_ALIAS = "com.phantomnet.app.SystemAlias"

    enum class AliasMode {
        NORMAL,
        CALCULATOR,
        SYSTEM
    }

    /**
     * Set the active launcher alias.
     * Note: Changing aliases may kill the app process on some Android versions.
     */
    fun setAlias(context: Context, mode: AliasMode) {
        val pm = context.packageManager
        val packageName = context.packageName

        val mainComp = ComponentName(packageName, MAIN_ALIAS)
        val calcComp = ComponentName(packageName, CALC_ALIAS)
        val systemComp = ComponentName(packageName, SYSTEM_ALIAS)

        Log.i(TAG, "Switching Alias to: $mode")

        try {
            // Disable all first (but keep track of target)
            val comps = mapOf(
                AliasMode.NORMAL to mainComp,
                AliasMode.CALCULATOR to calcComp,
                AliasMode.SYSTEM to systemComp
            )

            comps.forEach { (m, comp) ->
                val state = if (m == mode) {
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                } else {
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                }
                
                pm.setComponentEnabledSetting(
                    comp,
                    state,
                    PackageManager.DONT_KILL_APP
                )
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to set launcher alias", e)
        }
    }
}
