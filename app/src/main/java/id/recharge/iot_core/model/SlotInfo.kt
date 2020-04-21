package id.recharge.iot_core.model

/**
 * @author Daniel Joi Partogi Hutapea
 */
class SlotInfo(
    var v: String? = null, // PowerBank ID
    var pos: Int? = null, // Slot Position
    var vp: Int? = null, // Battery Percentage
    var ct: Int? = null, // Channel Type
    var cv: Int? = null // Channel Version
)
