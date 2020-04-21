package id.recharge.iot_core.model

import com.google.gson.annotations.SerializedName

/**
 * @author Daniel Joi Partogi Hutapea
 */
data class KeyPair(
    @SerializedName("PublicKey") var publicKey: String,
    @SerializedName("PrivateKey") var privateKey: String
)
