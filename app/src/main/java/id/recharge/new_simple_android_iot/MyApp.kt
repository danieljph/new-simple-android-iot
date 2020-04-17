package id.recharge.new_simple_android_iot

import androidx.multidex.MultiDexApplication
import timber.log.Timber

/**
 * @author Daniel Joi Partogi Hutapea
 */
class MyApp : MultiDexApplication()
{
    companion object {
        lateinit var instance: MyApp
    }

    override fun onCreate()
    {
        super.onCreate()
        instance = this
        Timber.plant(Timber.DebugTree())
    }
}
