package com.youcii.buildsrc.base

/**
 * Created by jingdongwei on 2021/02/20.
 * 增量编辑基类Transform
 */
abstract class BaseIncrementTransform : BaseTransform() {

    /**
     * 支持增量编译处理, 需要判断Input.status
     * 参考: https://www.jianshu.com/p/37a5e058830a
     */
    override fun isIncremental(): Boolean {
        return true
    }

}