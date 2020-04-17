package id.recharge.iot_core.service

import id.recharge.commons.util.RetrofitUtils
import id.recharge.iot_core.model.PbProvisioningRequest
import id.recharge.iot_core.model.PbProvisioningResponse
import id.recharge.iot_core.model.SimpleResponse
import io.reactivex.Single
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.PUT
import retrofit2.http.Path

/**
 * @author Daniel Joi Partogi Hutapea
 */
interface PbProvisioningService
{
    @PUT("v1/iot/pb-provisioning/{terminalId}")
    fun doPbProvisioning(@Path("terminalId") terminalId: String, @Body reqBody: PbProvisioningRequest): Single<Response<SimpleResponse<PbProvisioningResponse>>>

    companion object Factory
    {
        fun create(): PbProvisioningService
        {
            return RetrofitUtils.createServerlessV2Service(PbProvisioningService::class.java)
        }
    }
}
