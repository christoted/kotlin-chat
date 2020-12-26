package com.example.kotlinchatii.Util

import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat

class Permission {

    fun isContactOk(context: Context):Boolean {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED

    }

    fun requestContactPermission(context: Context):Boolean {
        return ContextCompat.checkSelfPermission(context, android.Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED

    }

}