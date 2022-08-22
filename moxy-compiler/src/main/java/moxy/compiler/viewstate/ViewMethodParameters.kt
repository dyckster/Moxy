package moxy.compiler.viewstate

import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.asTypeName
import moxy.compiler.MvpCompiler
import javax.lang.model.element.ExecutableElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.ExecutableType
import javax.lang.model.type.TypeMirror

object ViewMethodParameters {

    fun createParameters(interfaceElement: DeclaredType, methodElement: ExecutableElement): List<ParameterSpec> {
        // typeUtils are used to work around generic views (https://github.com/Arello-Mobile/Moxy/issues/203)
        val executableType = MvpCompiler.typeUtils.asMemberOf(interfaceElement, methodElement) as ExecutableType
        val resolvedParameterTypes: List<TypeMirror> = executableType.parameterTypes

        return methodElement.parameters.zip(resolvedParameterTypes) { parameterElement, parameterType ->
            val type = parameterType.asTypeName()
            val name = parameterElement.simpleName.toString()
            ParameterSpec.builder(name,type)
                .addModifiers(KModifier.REIFIED)
                .build()
        }
    }
}