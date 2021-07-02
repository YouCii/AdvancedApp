package com.youcii.methodrecord

import com.google.auto.service.AutoService
import com.sun.source.util.Trees
import com.sun.tools.javac.code.Flags
import com.sun.tools.javac.processing.JavacProcessingEnvironment
import com.sun.tools.javac.tree.JCTree
import com.sun.tools.javac.tree.JCTree.*
import com.sun.tools.javac.tree.TreeMaker
import com.sun.tools.javac.tree.TreeTranslator
import com.sun.tools.javac.util.List
import com.sun.tools.javac.util.Name
import com.sun.tools.javac.util.Names
import java.util.*
import javax.annotation.processing.AbstractProcessor
import javax.annotation.processing.ProcessingEnvironment
import javax.annotation.processing.Processor
import javax.annotation.processing.RoundEnvironment
import javax.lang.model.SourceVersion
import javax.lang.model.element.Element
import javax.lang.model.element.TypeElement
import kotlin.collections.LinkedHashSet

/**
 * Created by jingdongwei on 2021/06/30.
 *
 * 利用APT+AST修改现有逻辑
 * 使用com.sun包必须引入..javasdk/Contents/Home/lib/tools.jar
 */
@AutoService(Processor::class)
class ASTProcessor : AbstractProcessor() {

    private lateinit var trees: Trees
    private lateinit var treeMaker: TreeMaker
    private lateinit var names: Names

    override fun init(processingEnv: ProcessingEnvironment?) {
        super.init(processingEnv)
        if (processingEnv is JavacProcessingEnvironment) {
            trees = Trees.instance(processingEnv)
            treeMaker = TreeMaker.instance(processingEnv.context)
            names = Names.instance(processingEnv.context)
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

    override fun process(typeElementSet: MutableSet<out TypeElement>, roundEnvironment: RoundEnvironment?): Boolean {
        roundEnvironment ?: return false

        for (typeElement in typeElementSet) {
            val elements = roundEnvironment.getElementsAnnotatedWith(typeElement) ?: continue
            for (element in elements) {
                handleElement(element)
            }
        }
        return false
    }

    /**
     * 使用AST处理单个Element
     */
    private fun handleElement(element: Element) {
        val jcTree = trees.getTree(element) as JCTree? ?: return
        jcTree.accept(myVisitor)
    }

    private val myVisitor = object : TreeTranslator() {
        /**
         * 类定义
         */
        override fun visitClassDef(tree: JCClassDecl?) {
            super.visitClassDef(tree)
            tree ?: return

            for (jcTree in tree.defs) {
                // 声明的参数
                if (jcTree is JCVariableDecl) {
                    tree.defs = tree.defs.append(makeGetterMethod(jcTree))
                }
            }
        }
    }

    /**
     * 构建get方法
     */
    private fun makeGetterMethod(variable: JCVariableDecl): JCMethodDecl? {
        // this
        val ident = treeMaker.Ident(names.fromString("this"))
        // this.xx
        val select = treeMaker.Select(ident, variable.name)
        // return this.xxx
        val jcStatement: JCStatement = treeMaker.Return(select)
        // 把整个表达式塞到代码块里
        val jcBlock = treeMaker.Block(0, List.nil<JCStatement?>().append(jcStatement))

        return treeMaker.MethodDef(
            treeMaker.Modifiers(Flags.PUBLIC.toLong()), //public
            getterMethodName(variable),   // getXxx
            variable.vartype,             // return 类型
            List.nil<JCTypeParameter>(),  // 泛型参数列表
            List.nil<JCVariableDecl>(),   // 参数列表
            List.nil<JCExpression>(),     // 异常抛出列表
            jcBlock,                      // 代码块
            null
        )
    }

    private fun getterMethodName(variable: JCVariableDecl): Name {
        val varName = variable.name.toString()
        return names.fromString(
            "get" + varName.substring(0, 1).toUpperCase(Locale.getDefault()) + varName.substring(1, varName.length)
        )
    }

}