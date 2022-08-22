package moxy.compiler.viewstate.entity

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import javax.lang.model.element.TypeElement

/**
 * Represents interface, that was used as view for at least one presenter.
 * `ViewState` will be generated based on data from this class.
 */
class ViewInterfaceInfo constructor(
    val element: TypeElement,
    val methods: List<ViewStateMethod>
) {
    val name: ClassName = element.asClassName()
    val typeVariables: List<TypeVariableName> = element.typeParameters.map { it.asTypeVariableName() }

    val nameWithTypeVariables: TypeName
        get() {
            return if (typeVariables.isEmpty()) {
                name
            } else {
                name.parameterizedBy(typeVariables)
            }
        }
}