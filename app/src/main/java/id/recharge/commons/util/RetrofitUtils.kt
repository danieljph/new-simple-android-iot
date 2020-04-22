package id.recharge.commons.util

import android.content.Context
import com.imlaidian.laidianclient.managerUtils.SettingInfoManager
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

    fun <T> createServerlessService(service: Class<T>): T
    {
        return createService(service, SettingInfoManager.serverlessBaseUrl, SettingInfoManager.serverlessApiKey, false, null)
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
        okHttpClientBuilder.addInterceptor(loggingInterceptor)

        // 1) Fix SSL issue? No
        /*
        val trustManagerFactory: TrustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm())
        trustManagerFactory.init(null as KeyStore?)
        val trustManagers: Array<TrustManager> = trustManagerFactory.trustManagers
        check(!(trustManagers.size != 1 || trustManagers[0] !is X509TrustManager))
        {
            ("Unexpected default trust managers:" + trustManagers.contentToString())
        }
        val trustManager: X509TrustManager = trustManagers[0] as X509TrustManager
        val sslContext: SSLContext = SSLContext.getInstance("TLSv1.2")
        sslContext.init(null, arrayOf<TrustManager>(trustManager), null)
        val sslSocketFactory: SSLSocketFactory = sslContext.socketFactory

        okHttpClientBuilder.sslSocketFactory(sslSocketFactory, trustManager)
         */

        // 2) Fix SSL issue? No
        /*
        val requireTls12 = ConnectionSpec.Builder(ConnectionSpec.MODERN_TLS)
            .tlsVersions(TlsVersion.TLS_1_2)
            .build()

        okHttpClientBuilder.connectionSpecs(arrayListOf(requireTls12))
         */

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
