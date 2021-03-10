package com.youcii.buildsrc

import com.android.build.gradle.AppExtension
import com.android.build.gradle.LibraryExtension
import com.youcii.buildsrc.transform.TestTransform
import org.gradle.api.Plugin
import org.gradle.api.Project

/**
 * Created by jingdongwei on 2021/02/19.
 */
class TransformPlugin : Plugin<Project> {
    override fun apply(target: Project) {
        // 第一种写法: 用于项目build.gradle注册插件
        target.subprojects { subProject ->
            subProject.afterEvaluate {
                val baseExtension = it.extensions.findByType(AppExtension::class.java) ?: it.extensions.findByType(LibraryExtension::class.java)
                baseExtension?.registerTransform(TestTransform())
            }
        }

        // 第二种写法: 在特定module的build.gradle注册
        // 如果使用target.afterEvaluate, 则要求 apply plugin: 'com.youcii.buildsrc.TransformPlugin' 先于 apply plugin: 'com.android.application'
        // target.afterEvaluate {
        //     val baseExtension = it.extensions.findByType(BaseExtension::class.java)
        //     baseExtension?.registerTransform(TestNonIncrementTransform())
        // }
        // 如果直接target.extensions, 则apply plugin: 'com.android.application'可以在apply plugin: 'com.youcii.buildsrc.TransformPlugin'之后
        // val baseExtension = target.extensions.findByType(BaseExtension::class.java)
        // baseExtension?.registerTransform(TestNonIncrementTransform())
    }
}