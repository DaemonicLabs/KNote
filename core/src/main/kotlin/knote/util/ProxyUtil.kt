package knote.util

import mu.KLogging
import org.jetbrains.kotlin.utils.addToStdlib.cast
import java.lang.reflect.Proxy
import kotlin.reflect.full.declaredFunctions

object ProxyUtil : KLogging() {
    fun <T> createProxy(value: Any, clazz: Class<T>): T {
        val loader = clazz.classLoader
        val interfaces = arrayOf(clazz)
        val proxyResult = Proxy.newProxyInstance(
            loader, interfaces
        ) { proxy, method, args ->
            val method_name = method.name
            val classes = method.parameterTypes
            println("calling: $method_name")
            println("parameterTypes: $classes")
            println("args: $args")
            println("this: $value")
//            value::class.declaredFunctions.forEach {
//                println("method: ${it.name}")
//                println("method.parameters: ${it.parameters}")
//            }
            val methodRef = value::class.declaredFunctions.find { it.name == method.name }!!
            if(args == null) {
                methodRef.call(value).also {
                    println("result: $it")
                    println("result: ${it!!::class}")
                }.cast()
//                        method.invoke(paramResult)
            } else {
                methodRef.call(value, *args).also {
                    println("result: $it")
                    println("result: ${it!!::class}")
                }.cast()
//                        method.invoke(paramResult, args)
            }
        }
        return clazz.cast(proxyResult)
    }
}