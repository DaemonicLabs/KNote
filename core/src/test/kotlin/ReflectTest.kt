import kotlin.reflect.KParameter

object ReflectTest{
    @JvmStatic
    fun main(args: Array<String>) {
        val arg = Argument("value")
        val funRef = Reflector::testFun

        val reflector = Reflector()
        val params = funRef.parameters.associate { parameter ->
            when (parameter.kind) {
                KParameter.Kind.INSTANCE -> {
                    return@associate parameter to reflector
                }
                else -> {
                }
            }
            parameter to arg
        }
        funRef.callBy(params)
    }
}

class Reflector {

    fun testFun(arg: IArg): String {
        println("calling testFun($arg)")
        return "test($arg)"
    }

}
interface IArg
data class Argument(val value: String) : IArg