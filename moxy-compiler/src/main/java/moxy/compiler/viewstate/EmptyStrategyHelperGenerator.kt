package moxy.compiler.viewstate

import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.TypeSpec
import moxy.compiler.toJavaFile
import moxy.compiler.viewstate.entity.MigrationMethod

object EmptyStrategyHelperGenerator {

    /**
     * @param migrationMethods non-empty list of methods
     *
     * @return File with references to the files which are in need of refactoring
     */
    @JvmStatic
    fun generate(migrationMethods: List<MigrationMethod>): FileSpec {

        val exampleView = migrationMethods[0].viewInterface.simpleName

        val classBuilder = TypeSpec
            .classBuilder("EmptyStrategyHelper")
            .addModifiers(KModifier.PUBLIC, KModifier.FINAL)

        val javaDoc = """
            This class is generated because 'enableEmptyStrategyHelper' compiler option is set to true.
            
            
            All view methods must have a strategy. Please, add the @StateStrategyType annotation
            to the methods listed below. You can also set this annotation directly to the View interface.
            
            Do not pay attention to compilation errors like
            'error: $exampleView is abstract; cannot be instantiated'
            
            Just use your IDE to navigate to the methods and set the required strategy for them.
            After you've fixed all the methods, you can remove 'enableEmptyStrategyHelper' option
            for the current module.
            
        """.trimIndent() // leave blank line above for a nice generated javadoc


        classBuilder.addKdoc(javaDoc)

        val methodSpecBuilder = FunSpec.builder("getViewStateProviders")
            .addModifiers(KModifier.PUBLIC, KModifier.OVERRIDE)

        methodSpecBuilder.addComment("If you are using Intellij IDEA or Android Studio, use Go to declaration (Ctrl/⌘+B or Ctrl/⌘+Click)")
        methodSpecBuilder.addComment("to navigate to '${migrationMethods.first().method.simpleName}()'")

        for ((viewInterface, method) in migrationMethods) {
            val statement = "new %s().%s()".format(viewInterface.qualifiedName, method.simpleName)
            methodSpecBuilder.addStatement(statement)
        }

        classBuilder.addFunction(methodSpecBuilder.build())

        return classBuilder.build().toJavaFile("moxy")
    }
}
