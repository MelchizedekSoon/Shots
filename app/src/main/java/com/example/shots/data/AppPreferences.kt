package com.example.shots.data

import android.app.AlertDialog
import android.content.Context
import android.content.SharedPreferences

object AppPreferences {
    private const val PREFS_NAME = "MyAppPrefs"
    private const val KEY_VERIFICATION_DIALOG_SHOWN = "verificationDialogShown"
    private const val KEY_USER_LOGGED_IN = "userLoggedIn"

    fun isFirstTimeLaunch(context: Context): Boolean {
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getBoolean(KEY_VERIFICATION_DIALOG_SHOWN, true) && prefs.getBoolean(
            KEY_USER_LOGGED_IN,
            false
        )
    }

    fun setVerificationDialogShown(context: Context) {
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_VERIFICATION_DIALOG_SHOWN, false).apply()
    }

    fun setUserLoggedIn(context: Context, isLoggedIn: Boolean) {
        val prefs: SharedPreferences =
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().putBoolean(KEY_USER_LOGGED_IN, isLoggedIn).apply()
    }

    fun showVerificationDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Verification Required")
        builder.setMessage("You can view profiles but you'll need to complete add a little bit more info before you can interact with others!")
        builder.setPositiveButton("OK") { dialog, _ ->
            // Handle the OK button click if needed
            dialog.dismiss()
        }
        val dialog = builder.create()
        dialog.show()
    }

    fun showVerificationDialogIfNeeded(context: Context) {
        if (AppPreferences.isFirstTimeLaunch(context)) {
            showVerificationDialog(context)
            AppPreferences.setVerificationDialogShown(context)
        }
    }
}
