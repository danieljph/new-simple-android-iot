package id.recharge.iot_core

/**
 * @author Daniel Joi Partogi Hutapea
 */
class AwsIotCoreConnectException : RuntimeException
{
    constructor(message: String, ex: Throwable?): super(message, ex)
    constructor(message: String): super(message)
    constructor(ex: Throwable): super(ex)
}
