package id.recharge.iot_core.util

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.TypeAdapterFactory
import com.google.gson.reflect.TypeToken

/**
 * @author Daniel Joi Partogi Hutapea
 */
class AccessorBasedTypeAdapterFactory : TypeAdapterFactory
{
    @Suppress("UNCHECKED_CAST")
    override fun <T : Any?> create(gson: Gson?, type: TypeToken<T>?): TypeAdapter<T>
    {
        return AccessorBasedTypeAdaptor(gson!!) as TypeAdapter<T>
    }
}
