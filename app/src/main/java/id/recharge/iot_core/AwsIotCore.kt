package id.recharge.iot_core

import android.annotation.SuppressLint
import android.os.Environment
import com.amazonaws.mobileconnectors.iot.AWSIotKeystoreHelper
import com.amazonaws.mobileconnectors.iot.AWSIotMqttClientStatusCallback
import com.amazonaws.mobileconnectors.iot.AWSIotMqttManager
import com.imlaidian.laidianclient.managerUtils.SettingInfoManager
import com.imlaidian.laidianclient.utils.HyperlogUtils
import id.recharge.commons.util.FileUtils
import id.recharge.iot_core.model.PbProvisioningRequest
import id.recharge.iot_core.model.PbProvisioningResponse
import id.recharge.iot_core.model.RcDevice
import id.recharge.iot_core.model.SimpleResponse
import id.recharge.iot_core.service.PbProvisioningService
import io.reactivex.Single
import io.reactivex.SingleEmitter
import io.reactivex.schedulers.Schedulers
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit

/**
 * @author Daniel Joi Partogi Hutapea
 */
typealias OnInitProgressUpdate = (message: String) -> Unit

object AwsIotCore
{
    private val TAG = AwsIotCore::class.java.simpleName
    private const val AWS_CONNECT_TIMEOUT_IN_MILLISECONDS = 30_000L

    private val thingName = SettingInfoManager.terminalId
    private val certId = thingName
    private val keystoreName = "$thingName.jks"
    private val keystorePassword = thingName
    @Suppress("DEPRECATION") val keystorePath = "${Environment.getExternalStorageDirectory().absolutePath}/LaidianClient/AWS/ssl"
    private val keystoreFullPath = "$keystorePath/$keystoreName"
    private val certificatePathname = "$keystorePath/$thingName-certificate.pem.crt"
    private val privateKeyPathname = "$keystorePath/$thingName-private.pem.key"

    private lateinit var mqttManager: AWSIotMqttManager

    fun init(onInitProgressUpdate: OnInitProgressUpdate): Single<Unit>
    {
        return Single.create { emitter ->
            migrateCertificateAndPrivateKeyToKeyStore()

            if(FileUtils.checkFileExists(keystoreFullPath))
            {
                onInitProgressUpdate("KeyStore file exist. No need to call PB Provisioning Service.")
                connect(emitter, onInitProgressUpdate)
            }
            else
            {
                onInitProgressUpdate("KeyStore file not found.")
                doPbProvisioning(onInitProgressUpdate)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .subscribe(
                        {
                            connect(emitter, onInitProgressUpdate)
                        },
                        {
                            emitter.onError(it)
                        }
                    )
            }
        }
    }

    private fun connect(emitter: SingleEmitter<Unit>, onInitProgressUpdate: OnInitProgressUpdate)
    {
        onInitProgressUpdate("Reading KeyStore...")
        val clientKeyStore = AWSIotKeystoreHelper.getIotKeystore(certId, keystorePath, keystoreName, keystorePassword)
        onInitProgressUpdate("Reading KeyStore has done.")

        onInitProgressUpdate("Creating IotMqttManager...")

        if(::mqttManager.isInitialized)
        {
            try
            {
                onInitProgressUpdate("Disconnecting old IotMqttManager...")
                mqttManager.maxAutoReconnectAttempts = 1
                mqttManager.disconnect()
                onInitProgressUpdate("Disconnecting old IotMqttManager has done.")
            }
            catch(ex: Exception)
            {
                HyperlogUtils.e(TAG, ex, "Failed to disconnect old IotMqttManager.")
            }
        }
        mqttManager = AWSIotMqttManager(thingName, SettingInfoManager.awsIotCoreClientEndpoint)
        mqttManager.maxAutoReconnectAttempts = -1 // -1 = Retry forever.
        onInitProgressUpdate("Creating IotMqttManager has done.")

        var isConnectDone = false
        val countDownLatch = CountDownLatch(1)
        var connectException: Throwable? = null

        onInitProgressUpdate("IotMqttManager connecting...")
        mqttManager.connect(clientKeyStore) { status, throwable ->
            if(throwable != null)
            {
                if(isConnectDone)
                {
                    HyperlogUtils.e(TAG, throwable, "AWSIotMqttManager failed to connect.")
                }
                else
                {
                    connectException = throwable
                    countDownLatch.countDown()
                }
            }
            else
            {
                when(status)
                {
                    AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Connecting -> HyperlogUtils.i(TAG, "Connecting...")
                    AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Connected ->
                    {
                        HyperlogUtils.i(TAG, "Connected.")

                        if(!isConnectDone)
                        {
                            countDownLatch.countDown()
                        }
                    }
                    AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.Reconnecting -> HyperlogUtils.i(TAG, "Reconnecting...")
                    AWSIotMqttClientStatusCallback.AWSIotMqttClientStatus.ConnectionLost -> HyperlogUtils.i(TAG, "Connection lost.")
                    else -> HyperlogUtils.i(TAG, "AWSIotMqttManager 'connect' status: $status")
                }
            }
        }

        val isTimeout = !countDownLatch.await(AWS_CONNECT_TIMEOUT_IN_MILLISECONDS, TimeUnit.MILLISECONDS)
        isConnectDone = true

        if(isTimeout)
        {
            connectException?.also {
                emitter.onError(AWSIotMqttManagerConnectException("IotMqttManager failed to connect to IOT Core.", it))
            }?: run {
                emitter.onError(AWSIotMqttManagerConnectException("IotMqttManager failed to connect to IOT Core after waiting for ${AWS_CONNECT_TIMEOUT_IN_MILLISECONDS}ms."))
            }
        }
        else
        {
            onInitProgressUpdate("IotMqttManager connected.")

            onInitProgressUpdate("Creating new RcDevice...")
            val rcDevice = RcDevice(mqttManager, thingName)
            onInitProgressUpdate("Creating new RcDevice has done.")

            onInitProgressUpdate("Configuring RcDevice...")
            rcDevice.reportInterval = SettingInfoManager.awsIotCoreShadowReportIntervalInMilliseconds
            onInitProgressUpdate("Set RcDevice.reportInterval = ${rcDevice.reportInterval}ms.")
            rcDevice.startSubscribe()
            onInitProgressUpdate("RcDevice started subscribing Shadow's topics.")
            rcDevice.startSync()
            onInitProgressUpdate("RcDevice started sync.")
            onInitProgressUpdate("Configuring RcDevice has done.")
            emitter.onSuccess(Unit)
        }
    }

    @SuppressLint("CheckResult")
    private fun doPbProvisioning(onInitProgressUpdate: OnInitProgressUpdate): Single<SimpleResponse<PbProvisioningResponse>>
    {
        onInitProgressUpdate("Calling PB Provisioning service...")

        return Single.create { emitter ->
            PbProvisioningService.create()
                .doPbProvisioning(thingName, PbProvisioningRequest(SettingInfoManager.thingTypeName))
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe(
                    {
                        if(it.isSuccessful)
                        {
                            try
                            {
                                val pbProvisioningSimpleResponse = it.body()
                                val certificatePem = pbProvisioningSimpleResponse?.data?.createdKeysAndCertificate?.certificatePem
                                val privateKeyPem = pbProvisioningSimpleResponse?.data?.createdKeysAndCertificate?.keyPair?.privateKey

                                if(certificatePem!=null && privateKeyPem!=null)
                                {
                                    FileUtils.deleteIfExists("$keystorePath/$keystoreName")
                                    onInitProgressUpdate("Saving Certificate and Private Key in KeyStore...")
                                    AWSIotKeystoreHelper.saveCertificateAndPrivateKey(certId, certificatePem, privateKeyPem, keystorePath, keystoreName, keystorePassword)
                                    onInitProgressUpdate("Saving Certificate and Private Key in KeyStore has done.")
                                    emitter.onSuccess(pbProvisioningSimpleResponse)
                                }
                                else
                                {
                                    emitter.onError(RuntimeException("Certificate or Private Key not found in PB Provisioning response."))
                                }
                            }
                            catch(ex: Exception)
                            {
                                emitter.onError(ex)
                            }
                        }
                        else
                        {
                            emitter.onError(RuntimeException("Calling PB Provisioning service return an error. Error message: ${it.errorBody()?.string()}"))
                        }
                    },
                    {
                        emitter.onError(it)
                    }
                )
        }
    }

    fun deleteOldKeyStore()
    {
        FileUtils.deleteIfExists(keystoreFullPath)
    }

    private fun migrateCertificateAndPrivateKeyToKeyStore()
    {
        try
        {
            HyperlogUtils.i(TAG, "Migrating Certificate & Private Key files to KeyStore...")

            if(FileUtils.checkFileExists(keystoreFullPath))
            {
                HyperlogUtils.i(TAG, "KeyStore file exist. No need to do the migration.")
                deleteOldSslFiles()
            }
            else if(FileUtils.checkFileExists(certificatePathname) && FileUtils.checkFileExists(privateKeyPathname))
            {
                val certificatePem = FileUtils.read(certificatePathname)
                val privateKeyPem = FileUtils.read(privateKeyPathname)
                HyperlogUtils.i(TAG, "Saving Certificate and Private Key to KeyStore...")
                AWSIotKeystoreHelper.saveCertificateAndPrivateKey(certId, certificatePem, privateKeyPem, keystorePath, keystoreName, keystorePassword)
                HyperlogUtils.i(TAG, "Saving Certificate and Private Key to KeyStore has done.")
                deleteOldSslFiles()
            }
            else
            {
                HyperlogUtils.i(TAG, "Certificate or Private Key files not exist. No need to do the migration.")
            }

            HyperlogUtils.i(TAG, "Migrating Certificate & Private Key files to KeyStore has done.")
        }
        catch(ex: Exception)
        {
            HyperlogUtils.e(TAG, ex, "Failed to migrate Certificate & Private Key files to KeyStore.")
            deleteOldSslFiles()
        }
    }

    private fun deleteOldSslFiles()
    {
        HyperlogUtils.i(TAG, "Deleting old SSL files...")
        FileUtils.deleteIfExists(certificatePathname)
        FileUtils.deleteIfExists(privateKeyPathname)
        HyperlogUtils.i(TAG, "Deleting old SSL files has done.")
    }
}
