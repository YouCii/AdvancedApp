package com.youcii.buildsrc.transform

import com.youcii.buildsrc.base.BaseAsmTransform
import org.objectweb.asm.ClassVisitor
import org.objectweb.asm.ClassWriter
import org.objectweb.asm.MethodVisitor
import org.objectweb.asm.Opcodes

/**
 * Created by jingdongwei on 2021/03/02.
 */
class TestAsmTransform : BaseAsmTransform() {

    override fun getClassVisitor(classWriter: ClassWriter): ClassVisitor {
        return object : ClassVisitor(Opcodes.ASM5, classWriter) {
            private var className: String? = ""

            override fun visit(version: Int, access: Int, name: String?, signature: String?, superName: String?, interfaces: Array<out String>?) {
                super.visit(version, access, name, signature, superName, interfaces)
                className = name
            }

            override fun visitMethod(access: Int, name: String?, desc: String?, signature: String?, exceptions: Array<out String>?): MethodVisitor {
                val methodVisitor = super.visitMethod(access, name, desc, signature, exceptions)
                return when {
                    className?.contains("Activity") != true -> methodVisitor
                    "onCreate" != name -> methodVisitor
                    else -> object : MethodVisitor(Opcodes.ASM5, methodVisitor) {
                        // 访问方法操作指令
                        override fun visitMethodInsn(opcode: Int, owner: String?, name: String?, desc: String?, itf: Boolean) {
                            super.visitMethodInsn(opcode, owner, name, desc, itf)
                            mv.visitVarInsn(Opcodes.ALOAD, 0)
                            mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "com/youcii/advanced/MainActivity", "showASMToast", "()V", false)
                        }
                    }
                }
            }

        }
    }

}