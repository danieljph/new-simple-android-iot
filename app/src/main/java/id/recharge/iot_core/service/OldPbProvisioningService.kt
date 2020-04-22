package id.recharge.iot_core.service

import id.recharge.commons.util.RetrofitUtils
import id.recharge.iot_core.model.PbProvisioningResponse
import id.recharge.iot_core.model.SimpleResponse
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * @author Daniel Joi Partogi Hutapea
 */
interface OldPbProvisioningService
{
    @PUT("pb/provisioning/{terminalId}")
    fun doPbProvisioning(@Path("terminalId") terminalId: String): Single<Response<SimpleResponse<PbProvisioningResponse>>>

    companion object Factory
    {
        fun create(): OldPbProvisioningService
        {
            return RetrofitUtils.createServerlessService(OldPbProvisioningService::class.java)
        }
    }
}
