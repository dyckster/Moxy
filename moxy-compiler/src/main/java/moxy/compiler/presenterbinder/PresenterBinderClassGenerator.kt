package moxy.compiler.presenterbinder

import com.squareup.kotlinpoet.*
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import moxy.MvpPresenter
import moxy.MvpProcessor
import moxy.PresenterBinder
import moxy.compiler.JavaFilesGenerator
import moxy.compiler.Util
import moxy.compiler.asTypeElement
import moxy.compiler.subtypeWildcard
import moxy.compiler.supertypeWildcard
import moxy.compiler.toJavaFile
import moxy.presenter.PresenterField
import java.util.*
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement

/**
 * 18.12.2015
 *
 * Generates PresenterBinder for a class annotated with @InjectPresenters
 */
class PresenterBinderClassGenerator : JavaFilesGenerator<TargetClassInfo> {

    override fun generate(targetClassInfo: TargetClassInfo): List<FileSpec> {
        val targetClassName = targetClassInfo.name
        val fields = targetClassInfo.fields
        val superPresenterBinder = targetClassInfo.superPresenterBinder

        val containerSimpleName = targetClassName.simpleNames.joinToString("$")

        val classBuilder = TypeSpec
            .classBuilder(containerSimpleName + MvpProcessor.PRESENTER_BINDER_SUFFIX)
            .addOriginatingElement(targetClassInfo.element)
            .addModifiers(KModifier.PUBLIC)
            .superclass(PresenterBinder::class.asClassName().parameterizedBy(targetClassName))

        for (field in fields) {
            classBuilder.addType(generatePresenterBinderClass(field, targetClassName))
        }

        classBuilder.addFunction(
            generateGetPresentersMethod(
                fields,
                targetClassName,
                superPresenterBinder
            )
        )

        return listOf(classBuilder.build().toJavaFile(targetClassName))
    }

    private fun generateGetPresentersMethod(
        fields: List<TargetPresenterField>,
        containerClassName: ClassName,
        superPresenterBinder: TypeElement?
    ): FunSpec {

        val builder = FunSpec.builder("getPresenterFields")
            .addAnnotation(Override::class.java)
            .addModifiers(KModifier.PUBLIC)
            .returns(
                List::class.asClassName().parameterizedBy(
                    PresenterField::class.asClassName().parameterizedBy(
                        containerClassName.supertypeWildcard()
                    )
                )
            )

        builder.addStatement(
            "$1T<$2T<$3T>> presenters = new $4T<>($5L)",
            List::class.java,
            PresenterField::class.java,
            containerClassName.supertypeWildcard(),
            ArrayList::class.java,
            fields.size
        )

        for (field in fields) {
            builder.addStatement("presenters.add(new $1L())", field.generatedClassName)
        }

        if (superPresenterBinder != null) {
            builder.addStatement(
                "presenters.addAll(new $1L().getPresenterFields())",
                superPresenterBinder.qualifiedName.toString() + MvpProcessor.PRESENTER_BINDER_SUFFIX
            )
        }

        builder.addStatement("return presenters")

        return builder.build()
    }

    private fun generatePresenterBinderClass(
        field: TargetPresenterField,
        targetClassName: ClassName
    ): TypeSpec {

        val tag = field.tag ?: field.name

        return TypeSpec.classBuilder(field.generatedClassName)
            .addModifiers(KModifier.PUBLIC)
            .superclass(PresenterField::class.asClassName().parameterizedBy(targetClassName))
            .addFunction(generatePresenterBinderConstructor(field, tag))
            .addFunction(generateBindMethod(field, targetClassName))
            .addFunction(generateProvidePresenterMethod(field, targetClassName))
            .addOptionalMethod(generateGetTagMethod(field.presenterTagProviderMethodName, targetClassName))
            .build()
    }

    private fun generateGetTagMethod(
        tagProviderMethodName: String?,
        targetClassName: ClassName
    ): FunSpec? {
        tagProviderMethodName ?: return null
        return FunSpec.builder("getTag")
            .addAnnotation(Override::class.java)
            .addModifiers(KModifier.PUBLIC)
            .returns(String::class.java)
            .addParameter("delegated",targetClassName)
            .addStatement("return String.valueOf(delegated.$1L())", tagProviderMethodName)
            .build()
    }

    private fun generatePresenterBinderConstructor(
        field: TargetPresenterField,
        tag: String
    ): FunSpec {
        return FunSpec.constructorBuilder()
            .addModifiers(KModifier.PUBLIC)
            .addStatement("super($1S, $2S, $3T.class)", tag, field.presenterId.toString(), field.typeName)
            .build()
    }

    private fun generateBindMethod(
        field: TargetPresenterField,
        targetClassName: ClassName
    ): FunSpec {
        return FunSpec.builder("bind")
            .addAnnotation(Override::class.java)
            .addModifiers(KModifier.PUBLIC)
            .addParameter("target",targetClassName)
            .addParameter("presenter", MvpPresenter::class.java)
            .addStatement("target.$1L = ($2T) presenter", field.name, field.typeName)
            .build()
    }

    private fun generateProvidePresenterMethod(
        field: TargetPresenterField,
        targetClassName: ClassName
    ): FunSpec {
        val builder: FunSpec.Builder =
            FunSpec.builder("providePresenter")
                .addAnnotation(Override::class.java)
                .addModifiers(KModifier.PUBLIC)
                .returns(MvpPresenter::class.asClassName().parameterizedBy(Any::class.subtypeWildcard()))
                .addParameter("delegated", targetClassName)

        if (field.presenterProviderMethodName != null) {
            builder.addStatement("return delegated.$1L()", field.presenterProviderMethodName!!)
        } else {
            val hasEmptyConstructor = Util.hasEmptyConstructor(field.type.asTypeElement())

            if (hasEmptyConstructor) {
                builder.addStatement("return new $1T()", field.typeName)
            } else {
                builder.addStatement(
                    "throw new $1T($2S + $3S)",
                    IllegalStateException::class.java,
                    field.type,
                    " hasn't got a default constructor. You can apply @ProvidePresenter to a method which will "
                            + "construct Presenter. Otherwise you can add empty constructor to presenter."
                )
            }
        }

        return builder.build()
    }

    private fun TypeSpec.Builder.addOptionalMethod(methodSpec: FunSpec?): TypeSpec.Builder {
        return if (methodSpec != null) {
            addFunction(methodSpec)
        } else {
            this
        }
    }
}