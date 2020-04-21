package id.recharge.new_simple_android_iot

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.imlaidian.laidianclient.data.remote.IMonolithTerminalService
import com.imlaidian.laidianclient.utils.HyperlogUtils
import id.recharge.iot_core.AwsIotCore
import id.recharge.iot_core.AwsIotCoreConnectException
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.rxkotlin.addTo
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*

/**
 * @author Daniel Joi Partogi Hutapea
 */
class MainActivity : AppCompatActivity()
{
    private val TAG = AwsIotCore::class.java.simpleName
    private val defaultCompositeDisposable = CompositeDisposable()

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        initAwsIotCore()

        btnCallReChargeMonolithApi.setOnClickListener {
            logInfo("Calling getTerminalDetail on IMonolithTerminalService.")
            IMonolithTerminalService.create(this)
                .getTerminalDetail("000030000004")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        logInfo("Terminal Info: ${Gson().toJson(it.body())}")
                    },
                    {
                        logInfo("Error when calling getTerminalDetail on IMonolithTerminalService. Error:\n${Log.getStackTraceString(it)}")
                    }
                )
        }

        btnCallReChargeAwsLambdaApi.setOnClickListener {
            AwsIotCore.doPbProvisioning(::onAwsIotCoreInitProgressUpdate)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    {
                        logInfo("PB Provisioning Result: ${Gson().toJson(it.data)}")
                    },
                    {
                        logInfo("Error when calling doPbProvisioning on AwsIotCore. Error:\n${Log.getStackTraceString(it)}")
                    }
                )
        }

        btnClearLog.setOnClickListener { tvInfo.text = "Log Info:\n" }
    }

    private fun initAwsIotCore()
    {
        AwsIotCore.init(::onAwsIotCoreInitProgressUpdate)
            .retry(1) {
                /*
                  Make sure this retry procedure only occurred once.
                  We don't want the terminal keep deleting and calling PB Provisioning multiple times.
                 */
                var shouldRetry = false

                if(it.cause is AwsIotCoreConnectException)
                {
                    HyperlogUtils.e(TAG, it, "Failed to initiate AwsIotCore. Retrying by deleting old KeyStore and getting the new one...")
                    AwsIotCore.deleteOldKeyStore()
                    HyperlogUtils.i(TAG, "Deleting old KeyStore file has done.")
                    shouldRetry = true
                }
                else
                {
                    HyperlogUtils.i(TAG, "No need to retry AwsIotCore initiation because the exception is not instance of AwsIotCoreConnectException.")
                }

                return@retry shouldRetry
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    logInfo("IotCore initialized successfully.")
                },
                {
                    logInfo("Failed to initiate IotCore. Error:\n${Log.getStackTraceString(it)}")
                }
            )
            .addTo(defaultCompositeDisposable)
    }

    private fun onAwsIotCoreInitProgressUpdate(message: String)
    {
        logInfo(message)
    }

    private fun logInfo(message: String)
    {
        HyperlogUtils.i(TAG, message)
        tvInfo.append(message+'\n')
    }
}
