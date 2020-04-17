package com.amazonaws.services.iot.client

import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager
import com.amazonaws.services.iot.client.shadow.AbstractAwsIotDevice
import id.recharge.iot_core.AwsIotCore

/**
 * @author Daniel Joi Partogi Hutapea
 */
open class AWSIotDevice(mqttManager: AWSIotMqttManager, thingName: String) : AbstractAwsIotDevice(mqttManager, thingName)
{
    companion object {
        @Transient private val TAG = AwsIotCore::class.java.simpleName
    }
}
