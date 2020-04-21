package com.imlaidian.laidianclient.utils

import timber.log.Timber

/**
 * @author Daniel Joi Partogi Hutapea
 */
object HyperlogUtils
{
    fun d(tag: String, message: String)
    {
        Timber.tag(tag).d(message)
    }

    fun i(tag: String, message: String)
    {
        Timber.tag(tag).i(message)
    }

    fun w(tag: String, message: String)
    {
        Timber.tag(tag).w(message)
    }

    fun e(tag: String, message: String)
    {
        Timber.tag(tag).e(message)
    }

    fun e(tag: String, throwable: Throwable, message: String)
    {
        Timber.tag(tag).e(throwable, message)
    }
}