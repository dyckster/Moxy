package moxy.compiler.viewstateprovider

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import moxy.MvpProcessor
import moxy.MvpView
import moxy.ViewStateProvider
import moxy.compiler.*
import moxy.viewstate.MvpViewState

class ViewStateProviderClassGenerator : JavaFilesGenerator<PresenterInfo?> {

    override fun generate(presenterInfo: PresenterInfo?): List<FileSpec> {
        if (presenterInfo == null) {
            return emptyList()
        }

        var className = presenterInfo.name.simpleName + MvpProcessor.VIEW_STATE_PROVIDER_SUFFIX
        var enclosingClass = presenterInfo.name.enclosingClassName()

        while (enclosingClass != null) {
            className = "${enclosingClass.simpleName}$$className"
            enclosingClass = enclosingClass.enclosingClassName()
        }

        val typeSpec = TypeSpec
            .classBuilder(className)
            .addOriginatingElement(presenterInfo.element)
            .addModifiers(KModifier.PUBLIC)
            .superclass(ViewStateProvider::class)
            .addFunction(presenterInfo.generateGetViewStateMethod())
            .build()
        return listOf(typeSpec.toJavaFile(presenterInfo.name))
    }

    private fun PresenterInfo.generateGetViewStateMethod() = generateGetViewStateMethod(name, viewStateName)

    private fun generateGetViewStateMethod(
        presenter: ClassName,
        viewState: ClassName?
    ): FunSpec {
        return FunSpec.builder("getViewState")
            .addModifiers(KModifier.PUBLIC,KModifier.OVERRIDE)
            .returns(MvpViewState::class.asClassName().parameterizedBy(MvpView::class.subtypeWildcard()))
            .apply {
                if (viewState == null) {
                    addStatement("throw RuntimeException($1S)", presenter.reflectionName() + " should have view")
                } else {
                    returns(viewState)
                }
            }
            .build()
    }
}