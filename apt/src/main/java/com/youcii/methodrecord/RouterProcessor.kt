package com.youcii.methodrecord

import com.google.auto.service.AutoService
import com.squareup.javapoet.*
import java.io.IOException
import java.io.Writer
import java.util.*
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Modifier
import javax.lang.model.element.TypeElement
import kotlin.collections.LinkedHashSet


/**
 * Created by jingdongwei on 2020/06/04.
 *
 * 所有标注Router注解的类存储到某列表中
 * ps. 通过resources/META_INF/services进行注册, 这里可以用AutoService库进行自动处理(其实它的原理也是用的APT)
 */
@AutoService(Processor::class)
class RouterProcessor : AbstractProcessor() {

    private var generateContent = ""

    override fun process(typeElementSet: MutableSet<out TypeElement>, roundEnvironment: RoundEnvironment?): Boolean {
        roundEnvironment ?: return false

        if (roundEnvironment.processingOver()) {
            generateFileWithJavaPoet()
        } else {
            for (typeElement in typeElementSet) {
                val elements = roundEnvironment.getElementsAnnotatedWith(typeElement) ?: continue
                for (element in elements) {
                    // 使用Symbol.ClassSymbol必须引入/Library/Java/JavaVirtualMachines/jdk1.8.0_171.jdk/Contents/Home/lib/tools.jar
                    generateContent += "$element|"
                }
            }
        }
        return false
    }

    private fun generateFile() {
        try {
            val source = processingEnv.filer.createSourceFile(ROUTER_CLASS_NAME)
            val writer: Writer = source.openWriter()
            writer.write(
                "package com.youcii.advanced;\n" +
                        "\n" +
                        "/**\n" +
                        " * Created by APT on ${Date()}.\n" +
                        " */\n" +
                        "public class RouteList {\n" +
                        "\n" +
                        "    public static final String $ROUTER_FIELD_NAME = \"$generateContent\";\n" +
                        "\n" +
                        "}"
            )
            writer.flush()
            writer.close()
        } catch (ignore: IOException) {
            println("generateFile写入失败:$ignore")
        }
    }

    private fun generateFileWithJavaPoet() {
        val listField = FieldSpec.builder(String::class.java, ROUTER_FIELD_NAME)
            .addModifiers(Modifier.PUBLIC, Modifier.STATIC, Modifier.FINAL)
            .addJavadoc("存储Router列表,并用|分割\n")
            .initializer("\"$generateContent\"")
            .build()
        val resultClass = TypeSpec.classBuilder("RouteList")
            .addModifiers(Modifier.PUBLIC)
            .addJavadoc("Created by APT on ${Date()}.\n")
            .addField(listField)
            .build()
        val javaFile = JavaFile.builder("com.youcii.advanced", resultClass)
            .build()

        try {
            val source = processingEnv.filer.createSourceFile(ROUTER_CLASS_NAME)
            val writer: Writer = source.openWriter()
            javaFile.writeTo(writer)
            writer.flush()
            writer.close()
        } catch (ignore: IOException) {
            println("generateFileWithJavaPoet写入失败:$ignore")
        }
    }

    override fun getSupportedSourceVersion(): SourceVersion {
        return SourceVersion.latestSupported()
    }

    override fun getSupportedAnnotationTypes(): MutableSet<String> {
        return LinkedHashSet<String>().apply {
            add(Router::class.java.canonicalName)
        }
    }

    companion object {
        /**
         * 生成的类全名
         */
        const val ROUTER_CLASS_NAME = "com.youcii.advanced.RouteList"

        /**
         * 生成的类内数据存储变量名
         */
        const val ROUTER_FIELD_NAME = "list"
    }

}