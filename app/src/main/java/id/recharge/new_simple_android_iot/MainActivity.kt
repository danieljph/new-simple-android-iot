package id.recharge.new_simple_android_iot

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.imlaidian.laidianclient.utils.HyperlogUtils
import id.recharge.iot_core.AwsIotCore
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

                //if(it.cause is AWSIotTimeoutException)
                //{
                    HyperlogUtils.e(TAG, it, "Failed to initiate AwsIotCore. Retrying by deleting old certificate & private key and getting the new one...")
                    AwsIotCore.deleteOldKeyStore()
                    HyperlogUtils.i(TAG, "Deleting old SSL files has done.")
                    shouldRetry = true
                //}
                //else
                //{
                //    HyperlogUtils.i(TAG, "No need to retry AwsIotCore initiation because the exception is not instance of AWSIotTimeoutException.")
                //}

                return@retry shouldRetry
            }
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(
                {
                    updateInfo("IotCore initialized successfully.")
                },
                {
                    updateInfo("Failed to initiate IotCore.", it)
                }
            )
            .addTo(defaultCompositeDisposable)
    }

    private fun onAwsIotCoreInitProgressUpdate(message: String)
    {
        updateInfo(message)
    }

    private fun updateInfo(message: String, t: Throwable? = null)
    {
        if(t==null)
        {
            HyperlogUtils.i(TAG, message)
        }
        else
        {
            HyperlogUtils.e(TAG, t, message)
        }

        runOnUiThread {
            tvInfo.text = message
        }
    }
}
