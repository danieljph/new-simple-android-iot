package com.amazonaws.services.iot.client.shadow

import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager
import com.amazonaws.mobileconnectors.iot.AWSIotMqttQos
import com.amazonaws.services.iot.client.AWSIotDeviceProperty
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.module.SimpleModule
import com.imlaidian.laidianclient.utils.HyperlogUtils
import id.recharge.iot_core.AwsIotCore
import timber.log.Timber
import java.io.IOException
import java.lang.reflect.Field
import java.nio.charset.Charset
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.logging.Level

/**
 * @author Daniel Joi Partogi Hutapea
 */
abstract class AbstractAwsIotDevice(private val mqttManager: AWSIotMqttManager, val thingName: String)
{
    companion object {
        private val TAG = AwsIotCore::class.java.simpleName
    }

    var reportInterval = 900000L // 900000 milliseconds = 15 minutes.
    private val executionService: ScheduledExecutorService = Executors.newScheduledThreadPool(1)
    private val topicShadowUpdate = "\$aws/things/$thingName/shadow/update"
    private val topicShadowUpdateDelta = "\$aws/things/$thingName/shadow/update/delta"

    val reportedProperties: Map<String, Field>
    val updatableProperties: Map<String, Field>
    val jsonObjectMapper: ObjectMapper

    init {
        reportedProperties = getDeviceProperties(enableReport = true, allowUpdate = false)
        updatableProperties = getDeviceProperties(false, allowUpdate = true)

        val module = SimpleModule()
        module.addSerializer(AbstractAwsIotDevice::class.java, AwsIotJsonSerializer())

        jsonObjectMapper = ObjectMapper()
        jsonObjectMapper.registerModule(module)
    }

    open fun getDeviceProperties(enableReport: Boolean, allowUpdate: Boolean): Map<String, Field>
    {
        val properties  = mutableMapOf<String, Field>()
        for(field in this.javaClass.declaredFields)
        {
            val annotation: AWSIotDeviceProperty = field.getAnnotation(AWSIotDeviceProperty::class.java)?: continue
            val propertyName = if (annotation.name.isNotEmpty()) annotation.name else field.name
            if(enableReport && annotation.enableReport || allowUpdate && annotation.allowUpdate)
            {
                properties[propertyName] = field
            }
        }
        return properties
    }

    fun startSubscribe()
    {
        mqttManager.subscribeToTopic(topicShadowUpdateDelta, AWSIotMqttQos.QOS0, ::onShadowUpdateDeltaReceived)
    }

    fun startSync()
    {
        executionService.scheduleAtFixedRate({
            HyperlogUtils.i(TAG, "Automatic send device report triggered. Sending report...")
            sendDeviceReport()
            HyperlogUtils.i(TAG, "Automatic sending report has done.")
        }, 0L, reportInterval, TimeUnit.MILLISECONDS)
    }

    private fun onShadowUpdateDeltaReceived(topic: String, data: ByteArray)
    {
        try
        {
            val payload = String(data, Charset.forName("UTF-8"))
            val rootNode = jsonObjectMapper.readTree(payload)

            if(!rootNode.isObject)
            {
                throw RuntimeException("Received invalid delta for device $thingName. Payload should be a JSON object.")
            }

            val node = rootNode["state"] ?: throw RuntimeException("Missing state field in delta for device $thingName.")
            val jsonState = node.toString()
            onShadowUpdate(jsonState)
        }
        catch(ex: Exception)
        {
            HyperlogUtils.e(TAG, ex, "Failed to execute delta.")
        }
    }

    protected open fun onShadowUpdate(jsonState: String)
    {
        try
        {
            // synchronized block to serialize device accesses
            synchronized(this)
            {
                AwsIotJsonDeserializer.deserialize(this, jsonState)
            }
        }
        catch(ex: Exception)
        {
            HyperlogUtils.e(TAG, ex, "Failed to apply delta to AwsIotDevice.")
        }
    }

    open fun onDeviceReport(): String?
    {
        var deviceReport: String? = null

        try
        {
            deviceReport = jsonObjectMapper.writeValueAsString(this)
            Timber.i("Device Report: $deviceReport")
        }
        catch(ex: Exception)
        {
            HyperlogUtils.e(TAG, ex, "Failed to generate device report.")
        }

        return deviceReport
    }

    open fun sendDeviceReport()
    {
        val jsonState = onDeviceReport()

        if(jsonState != null)
        {
            sendDeviceReport(jsonState)
        }
    }

    open fun sendDeviceReport(jsonState: String)
    {
        val payload = StringBuilder("{")
        payload.append("\"state\":{\"reported\":").append(jsonState).append("}}")

        try
        {
            HyperlogUtils.i(TAG, "Sending device report... ($topicShadowUpdate): $payload")
            mqttManager.publishString(payload.toString(), topicShadowUpdate, AWSIotMqttQos.QOS0)
            HyperlogUtils.i(TAG, "Sending device report has done.")
        }
        catch(ex: Exception)
        {
            HyperlogUtils.e(TAG, ex, "Failed to publish device report.")
        }
    }
}
