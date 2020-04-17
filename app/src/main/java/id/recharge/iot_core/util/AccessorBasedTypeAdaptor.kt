package id.recharge.iot_core.util

import com.google.gson.Gson
import com.google.gson.TypeAdapter
import com.google.gson.reflect.TypeToken
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonWriter
import java.io.IOException

/**
 * @author Daniel Joi Partogi Hutapea
 */
class AccessorBasedTypeAdaptor(private val gson: Gson) : TypeAdapter<Any>()
{
    @Throws(IOException::class)
    override fun write(out: JsonWriter, value: Any?)
    {
        out.beginObject()

        if(value != null)
        {
            val allDeclaredMethods = value::class.java.methods
            for(method in allDeclaredMethods)
            {
                val nonBooleanAccessor = method.name.startsWith("get")
                val booleanAccessor = method.name.startsWith("is")

                if((nonBooleanAccessor || booleanAccessor) && method.name != "getClass" && method.parameterTypes.isEmpty())
                {
                    try
                    {
                        var name = method.name.substring(if (nonBooleanAccessor) 3 else 2)
                        name = name[0].toLowerCase() + name.substring(1)
                        val returnValue = method.invoke(value)

                        if(returnValue != null)
                        {
                            val token: TypeToken<Any> = TypeToken.get(returnValue.javaClass)
                            val adapter = gson.getAdapter(token)
                            out.name(name)
                            adapter.write(out, returnValue)
                        }
                    }
                    catch(ex: Exception)
                    {
                        throw RuntimeException("Problem writing JSON:", ex)
                    }
                }
            }
        }

        out.endObject()
    }

    @Throws(IOException::class)
    override fun read(jsonReader: JsonReader): Any
    {
        throw UnsupportedOperationException("Only supports writes.")
    }
}
