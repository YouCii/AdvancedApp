package com.youcii.buildsrc.transform

import com.android.build.gradle.BaseExtension
import com.youcii.buildsrc.base.BaseJavassistTransform
import javassist.CannotCompileException
import javassist.CodeConverter
import javassist.CtClass
import javassist.CtMethod
import javassist.expr.ExprEditor
import javassist.expr.MethodCall

/**
 * Created by jingdongwei on 2021/06/16.
 */
class TestJavassistTransform(extension: BaseExtension) : BaseJavassistTransform(extension) {

    override fun available(className: String): Boolean {
        return className.contains(Regex("com.youcii"))
    }

    override fun handleClass(targetClass: CtClass) {
        // 是否是Activity的子类
        if (!targetClass.subclassOf(classPool["android.app.Activity"])) {
            return
        }
        val onCreateMethods = targetClass.getDeclaredMethods("onCreate")
        for (onCreateMethod in onCreateMethods) {
            // 判断是否是onCreate生命周期方法
            val params = onCreateMethod.parameterTypes
            if (params.size != 1 || params[0] != classPool["android.os.Bundle"]) {
                continue
            }
            // 真正的处理
            try {
                case2(targetClass, onCreateMethod)
            } catch (e: CannotCompileException) {
                println("$name.handleClass CannotCompileException: $onCreateMethod")
            }
        }
    }

    /**
     * 插入到方法后面
     * 注意
     * 1. 只能插入java代码, 不可以是kotlin
     * 2. 必须是可用的表达式, 不能是编译不过的代码, 比如单个括号
     */
    private fun case1(onCreateMethod: CtMethod) {
        classPool.importPackage("android.widget.Toast")
        onCreateMethod.insertAfter(
            """
                showJavassistToast();
                Toast.makeText(this, JAVASSIST_SINGE_MSG, Toast.LENGTH_LONG).show();
            """
        )
    }

    /**
     * 整体catch
     *
     * 注意:
     * addCatch 必须在最后补充return, 否则注入报错 no basic block;
     */
    private fun case2(targetClass: CtClass, onCreateMethod: CtMethod) {
        classPool.importPackage("android.util.Log")
        onCreateMethod.addCatch(
            """
                Log.e("${targetClass.name}", "空指针了我擦:\n" + Log.getStackTraceString(${'$'}e));
                return;
            """, classPool.get("java.lang.NullPointerException")
        )
    }

    /**
     * 修改
     * https://github.com/woshikid/blog/issues/116
     * https://www.jianshu.com/p/b9b3ff0e1bf8
     */
    private fun case3(targetClass: CtClass, onCreateMethod: CtMethod) {
        // 修改方法
        onCreateMethod.instrument(CodeConverter())
        // converter.insertAfterMethod(origMethod, afterMethod);
        // converter.insertBeforeMethod(origMethod, beforeMethod);

        // 修改表达式
        val editor: ExprEditor = object : ExprEditor() {
            @Throws(CannotCompileException::class)
            override fun edit(m: MethodCall) {
                val call: String = m.className.toString() + "#" + m.methodName
                if (call == "java.io.PrintStream#print") {
                    m.replace("{ \$_ = \$proceed($1 + \"\\n\"); }")
                }
            }
        }
        onCreateMethod.instrument(editor)
        // public void edit(Handler h)
        // public void edit(Instanceof i)
        // public void edit(FieldAccess f)
    }

}