package moxy.compiler.viewstateprovider;

import com.squareup.javapoet.ClassName;
import javax.lang.model.element.TypeElement;

class PresenterInfo {

    private final ClassName name;

    private final ClassName viewStateName;

    private final TypeElement element;

    PresenterInfo(TypeElement name, String viewStateName) {
        this.name = ClassName.get(name);
        this.viewStateName = ClassName.bestGuess(viewStateName);
        this.element = name;
    }

    ClassName getName() {
        return name;
    }

    ClassName getViewStateName() {
        return viewStateName;
    }

    TypeElement getElement() {
        return element;
    }
}
