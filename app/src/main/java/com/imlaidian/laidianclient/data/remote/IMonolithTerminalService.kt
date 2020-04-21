package com.imlaidian.laidianclient.data.remote

import android.content.Context
import com.imlaidian.laidianclient.data.remote.model.TerminalDetailBean
import id.recharge.commons.util.RetrofitUtils
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Query

/**
 * @author Muhammad Wahyudin
 */
interface IMonolithTerminalService
{
    @Headers("Content-Type: application/json")
    @POST("terminal/getMachineById")
    fun getTerminalDetail(@Query("TERMINAL_ID") terminalId: String): Single<Response<TerminalDetailBean.Response>>

    companion object Factory
    {
        fun create(context: Context): IMonolithTerminalService
        {
            return RetrofitUtils.createCachedMonolithService(IMonolithTerminalService::class.java, context)
        }
    }
}
