package id.recharge.iot_core

import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager
import com.amazonaws.services.iot.client.AWSIotDevice
import com.amazonaws.services.iot.client.AWSIotDeviceProperty
import com.imlaidian.laidianclient.managerUtils.SettingInfoManager
import com.imlaidian.laidianclient.utils.HyperlogUtils
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

/**
 * @author Daniel Joi Partogi Hutapea
 */
class RcDevice(mqttManager: AWSIotMqttManager, thingName: String) : AWSIotDevice(mqttManager, thingName)
{
    companion object {
        private val TAG = RcDevice::class.java.simpleName
        private val ISO_8601_SDF = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }
    }

    private var hasChanges = false

    override fun onDeviceReport(): String?
    {
        HyperlogUtils.i(TAG, "AwsIotCore preparing device report...")
        val onDeviceReportResult =  super.onDeviceReport()
        HyperlogUtils.i(TAG, "AwsIotCore preparing device report has done.")
        return onDeviceReportResult
    }

    override fun onShadowUpdate(jsonState: String)
    {
        super.onShadowUpdate(jsonState)
        if(hasChanges)
        {
            HyperlogUtils.i(TAG, "State is changed after shadow updated. Sending device report...")
            sendDeviceReport()
            HyperlogUtils.i(TAG, "Sending device report has done.")
            hasChanges = false
        }
    }

    @field:AWSIotDeviceProperty var id: String = SettingInfoManager.terminalId

    @field:AWSIotDeviceProperty var terminalName: String = SettingInfoManager.terminalName
        set(desiredValue)
        {
            HyperlogUtils.e("RcDevice", "Dapat desired value nih: $desiredValue")
            field = desiredValue
        }

    @field:AWSIotDeviceProperty var slotInfoList: List<SlotInfo>? = null
        get() {
            val arrayOfSlotInfo = ArrayList<SlotInfo>()

            try
            {
                val channelInfoListCache = listOf("1", "2", "3")

                for(operatorChannelStatusModel in channelInfoListCache)
                {
                    val slotInfo = SlotInfo()
                    slotInfo.v = "V-$operatorChannelStatusModel"
                    slotInfo.pos = operatorChannelStatusModel.toIntOrNull()
                    slotInfo.vp = 50 + operatorChannelStatusModel.toInt()
                    slotInfo.ct = 5
                    slotInfo.cv = 6
                    arrayOfSlotInfo.add(slotInfo)
                }
            }
            catch(ex: Exception)
            {
                HyperlogUtils.e("RcDevice", ex, "Failed to get Channel Info list from LaidianCoreApi.")
            }

            return arrayOfSlotInfo
        }
}
