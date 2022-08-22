package moxy.compiler.viewstate

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.TypeSpec.Builder
import com.squareup.kotlinpoet.jvm.throws
import moxy.MvpProcessor
import moxy.compiler.*
import moxy.compiler.Util.decapitalizeString
import moxy.compiler.viewstate.entity.ViewInterfaceInfo
import moxy.compiler.viewstate.entity.ViewStateMethod
import moxy.viewstate.MvpViewState
import moxy.viewstate.ViewCommand
import javax.lang.model.type.DeclaredType

class ViewStateClassGenerator : JavaFilesGenerator<ViewInterfaceInfo> {

    override fun generate(viewInterfaceInfo: ViewInterfaceInfo): List<FileSpec> {
        val viewName = viewInterfaceInfo.name
        val nameWithTypeVariables = viewInterfaceInfo.nameWithTypeVariables
        val viewInterfaceType = viewInterfaceInfo.element.asDeclaredType()

        val typeName = Util.getSimpleClassName(viewInterfaceInfo.element) + MvpProcessor.VIEW_STATE_SUFFIX
        val classBuilder: Builder = TypeSpec.classBuilder(typeName)
            .addOriginatingElement(viewInterfaceInfo.element)
            .addModifiers(KModifier.PUBLIC)
            .superclass(MvpViewState::class.asClassName().parameterizedBy(nameWithTypeVariables))
            .addSuperinterface(nameWithTypeVariables)
            .addTypeVariables(viewInterfaceInfo.typeVariables)

        for (method in viewInterfaceInfo.methods) {
            val commandClass = generateCommandClass(method, nameWithTypeVariables)
            classBuilder.addType(commandClass)
            classBuilder.addFunction(generateMethod(viewInterfaceType, method, nameWithTypeVariables, commandClass))
        }

        return listOf(classBuilder.build().toJavaFile(viewName))
    }

    private fun generateCommandClass(
        method: ViewStateMethod,
        viewTypeName: TypeName
    ): TypeSpec {
        val applyMethod: FunSpec = FunSpec.builder("apply")
            .addAnnotation(Override::class.java)
            .addModifiers(KModifier.PUBLIC)
            .addParameter("mvpView", viewTypeName)
            .throws(method.exceptions)
            .addStatement("mvpView.$1L($2L)", method.name, method.argumentsString)
            .build()

        val classBuilder = TypeSpec.classBuilder(method.commandClassName)
            .addModifiers(KModifier.PUBLIC) // TODO: private and static
            .addTypeVariables(method.typeVariables)
            .superclass(ViewCommand::class.asClassName().parameterizedBy(viewTypeName))
            .addFunction(generateCommandConstructor(method))
            .addFunction(applyMethod)

        for (parameter in method.parameters) {
            // TODO: private field
            classBuilder.addProperty(parameter.name,parameter.type, KModifier.PUBLIC, KModifier.FINAL)
        }
        return classBuilder.build()
    }

    private fun generateMethod(
        enclosingType: DeclaredType,
        method: ViewStateMethod,
        viewTypeName: TypeName,
        commandClass: TypeSpec
    ): FunSpec {
        // TODO: val commandFieldName = "$cmd";

        var commandFieldName: String = decapitalizeString(method.commandClassName)
        var iterationVariableName = "view"

        // Add salt if contains argument with same name
        while (method.argumentsString.contains(commandFieldName)) {
            commandFieldName += commandFieldName.hashCode() % 10
        }
        while (method.argumentsString.contains(iterationVariableName)) {
            iterationVariableName += iterationVariableName.hashCode() % 10
        }

        return FunSpec.overriding(method.element, enclosingType, MvpCompiler.typeUtils)
            .addStatement("$1N $2L = new $1N($3L)", commandClass, commandFieldName, method.argumentsString)
            .addStatement("this.viewCommands.beforeApply($1L)", commandFieldName)
            .addCode("\n")
            .beginControlFlow("if (hasNotView())")
            .addStatement("return")
            .endControlFlow()
            .addCode("\n")
            .beginControlFlow("for ($1T $iterationVariableName : this.views)", viewTypeName)
            .addStatement("$iterationVariableName.$1L($2L)", method.name, method.argumentsString)
            .endControlFlow()
            .addCode("\n")
            .addStatement("this.viewCommands.afterApply($1L)", commandFieldName)
            .build()
    }

    private fun generateCommandConstructor(method: ViewStateMethod): FunSpec {
        val parameters: List<ParameterSpec> = method.parameters

        val builder: FunSpec.Builder = FunSpec.constructorBuilder()
            .addParameters(parameters)
            .addStatement("super($1S, $2T.class)", method.strategy.tag, method.strategy.strategyClass)

        if (parameters.isNotEmpty()) builder.addCode("\n")

        for (parameter in parameters) {
            builder.addStatement("this.$1N = $1N", parameter)
        }

        return builder.build()
    }
}