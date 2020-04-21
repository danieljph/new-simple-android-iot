package id.recharge.commons.util

import android.content.Context
import com.imlaidian.laidianclient.managerUtils.SettingInfoManager
import id.recharge.new_simple_android_iot.BuildConfig
import okhttp3.Cache
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory

/**
 * @author Daniel Joi Partogi Hutapea
 */
object RetrofitUtils
{
    private const val CACHE_SIZE: Long = 10 * 1024 * 1024
    private const val CACHE_MAX_AGE = 5 // 5 seconds.
    private const val CACHE_MAX_STALE = 1 * 24 * 60 * 60 // 1 days.

    private fun isCachingAllowed(enableCache: Boolean = true, context: Context? = null): Boolean
    {
        return enableCache && context!=null
    }

    fun <T> createCachedMonolithService(service: Class<T>, context: Context): T
    {
        return createService(service, SettingInfoManager.monolithBaseUrl, null, true, context)
    }

    fun <T> createServerlessV2Service(service: Class<T>): T
    {
        return createService(service, SettingInfoManager.serverlessV2BaseUrl, SettingInfoManager.serverlessV2ApiKey, false, null)
    }

    private fun <T> createService(service: Class<T>, baseUrl: String, xApiKey: String?, enableCache: Boolean = false, context: Context? = null): T
    {
        val loggingInterceptor = HttpLoggingInterceptor().apply { level = HttpLoggingInterceptor.Level.BODY }

        val addHeadersInterceptor = Interceptor { chain ->
            val ongoing = chain.request().newBuilder()

            if(!xApiKey.isNullOrBlank())
            {
                ongoing.addHeader("x-api-key", xApiKey)
            }

            chain.proceed(ongoing.build())
        }

        val okHttpClientBuilder = OkHttpClient.Builder()

        if(isCachingAllowed(enableCache, context))
        {
            val cache = Cache(context!!.cacheDir, CACHE_SIZE)
            okHttpClientBuilder.cache(cache)
        }

        okHttpClientBuilder.addInterceptor(addHeadersInterceptor)

        @Suppress("ConstantConditionIf")
        if(BuildConfig.FLAVOR == "dev")
        {
            okHttpClientBuilder.addInterceptor(loggingInterceptor)
        }

        val client = okHttpClientBuilder.build()
        val retrofit = Retrofit.Builder().apply {
            baseUrl(baseUrl)
            addCallAdapterFactory(RxJava2CallAdapterFactory.create())
            addConverterFactory(ScalarsConverterFactory.create())
            addConverterFactory(GsonConverterFactory.create())
            client(client)
        }.build()

        return retrofit.create(service)
    }
}
