package moxy.compiler.presenterbinder

import com.squareup.kotlinpoet.ParameterizedTypeName
import com.squareup.kotlinpoet.TypeName
import com.squareup.kotlinpoet.asTypeName
import moxy.MvpProcessor
import javax.lang.model.type.TypeMirror

/**
 * Represents field with `@InjectPresenter` annotation.
 * `PresenterField` inner class will be generated based on data from this class.
 * [tag] and [presenterId] are annotation parameters.
 * [type] is field type.
 * [name] as field name.
 */
class TargetPresenterField constructor(
    val type: TypeMirror,
    val name: String,
    val tag: String?,
    val presenterId: String?
) {
    private val isParametrized = type.asTypeName() is ParameterizedTypeName
    val typeName: TypeName = if (isParametrized) {
        (type.asTypeName() as ParameterizedTypeName).rawType
    } else {
        type.asTypeName()
    }

    val generatedClassName: String get() = name.capitalize() + MvpProcessor.PRESENTER_BINDER_INNER_SUFFIX

    /**
     * `@ProvidePresenter` method name if it was found for this field
     */
    var presenterProviderMethodName: String? = null

    /**
     * `@ProvidePresenterTag` method name if it was found for this field
     */
    var presenterTagProviderMethodName: String? = null
}