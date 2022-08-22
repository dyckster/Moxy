package moxy.compiler

import com.squareup.kotlinpoet.*
import javax.lang.model.element.AnnotationMirror
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import javax.lang.model.type.DeclaredType
import javax.lang.model.type.TypeMirror
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

/**
 * TypeMirror must be of kind TypeKind.DECLARED
 */
@OptIn(ExperimentalContracts::class)
fun TypeMirror.asTypeElement(): TypeElement {
    contract {
        returns() implies (this@asTypeElement is DeclaredType)
    }
    return (this as DeclaredType).asElement() as TypeElement
}

fun Element.asTypeElement() = this as TypeElement
fun Element.asDeclaredType() = this.asType() as DeclaredType

fun KClass<*>.subtypeWildcard(): WildcardTypeName = WildcardTypeName.consumerOf(java)
fun ClassName.supertypeWildcard(): WildcardTypeName = WildcardTypeName.consumerOf(this)

fun TypeSpec.toJavaFile(className: ClassName): FileSpec = toJavaFile(className.packageName)
fun TypeSpec.toJavaFile(packageName: String): FileSpec {
    return FileSpec.builder(packageName, this.toString())
        .indent("\t")
        .build()
}

fun <T : Annotation> Element.getAnnotationMirror(type: KClass<T>): AnnotationMirror? {
    return Util.getAnnotation(this, type.java.name)
}

// Pass property name, like StateStrategyType::value
fun AnnotationMirror.getValueAsString(property: KProperty1<*, *>): String? {
    return Util.getAnnotationValueAsString(this, property.name)
}

// Pass property name, like StateStrategyType::value
fun AnnotationMirror.getValueAsTypeMirror(property: KProperty1<*, *>): TypeMirror? {
    return Util.getAnnotationValueAsTypeMirror(this, property.name)
}

fun TypeMirror.getFullClassName(): String = Util.getFullClassName(this)

fun List<ParameterSpec>.equalsByType(other: List<ParameterSpec>): Boolean {
    return Util.equalsBy(this, other) { first, second ->
        first.type == second.type
    }
}