package knote.script

import kastree.ast.Node
import kastree.ast.psi.Converter
import mu.KotlinLogging
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtScript
import org.jetbrains.kotlin.utils.addToStdlib.firstIsInstanceOrNull

open class KNConverter : Converter() {
    private val logger = KotlinLogging.logger {}
    open fun convertScript(v: KtFile): Node.Script {
        val ktScript = v.declarations.firstIsInstanceOrNull<KtScript>()!!
        with(ktScript) {
            logger.info("blockExpression: $blockExpression ${blockExpression::class}")
            blockExpression.statements.forEach { expression ->
                logger.info("   expression: $expression ${expression::class}")
                logger.info("   ${expression.text}")
            }
            logger.info("kotlinScriptDefinition: $kotlinScriptDefinition ${kotlinScriptDefinition::class}")
        }
        return Node.Script(
            anns = convertAnnotationSets(v),
            pkg = v.packageDirective?.takeIf { it.packageNames.isNotEmpty() }?.let(::convertPackage),
            imports = v.importDirectives.map(::convertImport),
//                exprs = v.blockExpressionsOrSingle().map{ convertExpr(it) }.toList()
            exprs = ktScript.blockExpression.statements.map(::convertExpr)
        ).map(v)
    }


//    override fun convertExpr(v: KtExpression): Node.Expr = when (v) {
//        is KtScriptInitializer -> convertScriptInitializer(v)
//        else -> super.convertExpr(v)
//    }
//
//    private fun convertScriptInitializer(v: KtScriptInitializer): Node.Expr {
//
//    }

    companion object : KNConverter()
}
