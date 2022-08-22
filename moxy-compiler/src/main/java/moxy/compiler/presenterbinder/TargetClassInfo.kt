package moxy.compiler.presenterbinder

import com.squareup.kotlinpoet.ClassName
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement

/**
 * Represents class into which presenters will be injected.
 * `PresenterBinder` will be generated based on data from this class.
 * [superPresenterBinder] will be set if any parent class also requires injection.
 */
class TargetClassInfo(
    val element: Element,
    val name: ClassName,
    val fields: List<TargetPresenterField>,
    val superPresenterBinder: TypeElement?
)