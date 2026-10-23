package eu.kanade.tachiyomi.ui.base.activity

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import eu.kanade.tachiyomi.R
import eu.kanade.tachiyomi.ui.base.delegate.SecureActivityDelegate
import eu.kanade.tachiyomi.ui.base.delegate.SecureActivityDelegateImpl
import eu.kanade.tachiyomi.util.system.prepareTabletUiContext
import mihon.app.di.appGraph

open class BaseActivity :
    AppCompatActivity(),
    SecureActivityDelegate by SecureActivityDelegateImpl() {

    override fun attachBaseContext(newBase: Context) {
        val uiPreferences = newBase.appGraph.uiPreferences
        super.attachBaseContext(newBase.prepareTabletUiContext(uiPreferences))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        // The manifest theme may be the splash theme, which isn't AppCompat on a relaunch
        setTheme(R.style.Theme_Tachiyomi)
        super.onCreate(savedInstanceState)
    }
}
