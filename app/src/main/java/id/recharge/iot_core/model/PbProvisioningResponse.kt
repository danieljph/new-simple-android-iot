package id.recharge.iot_core.model

/**
 * @author Daniel Joi Partogi Hutapea
 */
data class PbProvisioningResponse(
    var createdThing: Thing,
    var createdKeysAndCertificate: KeysAndCertificate
)
