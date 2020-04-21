package id.recharge.commons.util

import android.content.Context
import android.media.AudioManager
import id.recharge.new_simple_android_iot.MyApp

/**
 * @author Daniel Joi Partogi Hutapea
 */
object AudioManagerUtils
{
    private fun getAudioManager(): AudioManager
    {
        return MyApp.instance.applicationContext.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    fun getStreamMaxVolume(streamType: Int): Int
    {
        return getAudioManager().getStreamMaxVolume(streamType)
    }

    fun getStreamVolume(streamType: Int): Int
    {
        return getAudioManager().getStreamVolume(streamType)
    }

    fun setStreamVolume(streamType: Int, index: Int, flags: Int)
    {
        getAudioManager().setStreamVolume(streamType, index, flags)
    }
}
