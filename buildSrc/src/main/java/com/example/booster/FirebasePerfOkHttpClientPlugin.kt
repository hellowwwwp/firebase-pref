package com.example.booster

import com.didiglobal.booster.kotlinx.touch
import com.didiglobal.booster.transform.TransformContext
import com.didiglobal.booster.transform.asm.ClassTransformer
import com.google.auto.service.AutoService
import org.objectweb.asm.Opcodes
import org.objectweb.asm.tree.ClassNode
import org.objectweb.asm.tree.InsnNode
import org.objectweb.asm.tree.MethodInsnNode
import org.objectweb.asm.tree.MethodNode
import java.io.PrintWriter

/**
 * @author: wangpan
 * @email: p.wang@aftership.com
 * @date: 2022/4/18
 */
@Suppress("unused")
@AutoService(ClassTransformer::class)
class FirebasePerfOkHttpClientPlugin : ClassTransformer {

    private lateinit var logger: PrintWriter

    override val name: String = "firebase-pref-okhttp"

    override fun onPreTransform(context: TransformContext) {
        this.logger = getReport(context, "report.txt").touch().printWriter()
    }

    override fun onPostTransform(context: TransformContext) {
        this.logger.close()
    }

    override fun transform(context: TransformContext, klass: ClassNode): ClassNode {
        if (klass.name != Retrofit2OkHttpCall) {
            return klass
        }
        klass.methods.iterator().forEach { methodNode ->
            logger.println("methodNode: ${methodNode.name}, ${methodNode.desc}")
            when {
                methodNode.isRetrofit2Execute() -> {
                    //处理 OkHttpCall 的 execute 方法
                    handleExecute(methodNode)
                }
                methodNode.isRetrofit2Enqueue() -> {
                    //处理 OkHttpCall 的 enqueue 方法
                    handleEnqueue(methodNode)
                }
            }
        }
        return klass
    }

    private fun handleEnqueue(methodNode: MethodNode) {
        //Okhttp3Call 的 enqueue 指令
        var callEnqueue: MethodInsnNode? = null
        //OkHttpCall 的 enqueue 方法中的 return 指令
        var lastReturn: InsnNode? = null
        methodNode.instructions.iterator().forEach {
            if (it is MethodInsnNode && it.isCallEnqueue()) {
                callEnqueue = it
            } else if (it is InsnNode && it.opcode == Opcodes.RETURN) {
                lastReturn = it
            }
        }
        if (callEnqueue == null || lastReturn == null) {
            throw IllegalAccessException("Okhttp3Call.enqueue or OkHttpCall.enqueue return not found")
        }
        //移除原有的 Okhttp3Call 的 enqueue 指令
        methodNode.instructions.remove(callEnqueue)
        //新建 FirebasePerfOkHttpClient 的 enqueue 指令
        val newCallEnqueue = MethodInsnNode(
            Opcodes.INVOKESTATIC,
            FirebasePerfOkHttpClient,
            "enqueue",
            "(L$Okhttp3Call;L$Okhttp3Callback;)V"
        )
        logger.println("handleEnqueue insert: ${newCallEnqueue.name}, ${newCallEnqueue.desc}")
        //插入到方法的指令列表中
        methodNode.instructions.insertBefore(lastReturn, newCallEnqueue)
    }

    private fun handleExecute(methodNode: MethodNode) {
        //Okhttp3Call 的 execute 指令
        var callExecute: MethodInsnNode? = null
        //OkHttpCall 的 execute 方法中的 parseResponse 指令
        var parseResponse: MethodInsnNode? = null
        methodNode.instructions.iterator().forEach {
            if (it is MethodInsnNode && it.isCallExecute()) {
                callExecute = it
            } else if (it is MethodInsnNode && it.isParseResponse()) {
                parseResponse = it
            }
        }
        if (callExecute == null || parseResponse == null) {
            throw IllegalAccessException("Okhttp3Call.execute or OkHttpCall.parseResponse not found")
        }
        //移除原有的 Okhttp3Call 的 execute 指令
        methodNode.instructions.remove(callExecute)
        //新建 FirebasePerfOkHttpClient 的 execute 指令
        val newCallExecute = MethodInsnNode(
            Opcodes.INVOKESTATIC,
            FirebasePerfOkHttpClient,
            "execute",
            "(L$Okhttp3Call;)L$Okhttp3Response;"
        )
        logger.println("handleExecute insert: ${newCallExecute.name}, ${newCallExecute.desc}")
        //插入到方法的指令列表中
        methodNode.instructions.insertBefore(parseResponse, newCallExecute)
    }

    private fun createMethodInsnNode(): MethodInsnNode {
        return MethodInsnNode(
            Opcodes.INVOKESTATIC,
            FirebasePerfOkHttpClient,
            "execute",
            "(L$Okhttp3Call;)L$Okhttp3Response;"
        )
    }

    private fun MethodNode.isRetrofit2Execute(): Boolean {
        return this.name == "execute" && this.desc == "()L$Retrofit2Response;"
    }

    private fun MethodNode.isRetrofit2Enqueue(): Boolean {
        return this.name == "enqueue" && this.desc == "(L$Retrofit2Callback;)V"
    }

    private fun MethodInsnNode.isParseResponse(): Boolean {
        return this.owner == Retrofit2OkHttpCall && this.name == "parseResponse" && this.desc == "(L$Okhttp3Response;)L$Retrofit2Response;"
    }

    private fun MethodInsnNode.isCallExecute(): Boolean {
        return this.owner == Okhttp3Call && this.name == "execute" && this.desc == "()L$Okhttp3Response;"
    }

    private fun MethodInsnNode.isCallEnqueue(): Boolean {
        return this.owner == Okhttp3Call && this.name == "enqueue" && this.desc == "(L$Okhttp3Callback;)V"
    }

    companion object {

        private const val Retrofit2OkHttpCall = "retrofit2/OkHttpCall"
        private const val Retrofit2Response = "retrofit2/Response"
        private const val Retrofit2Callback = "retrofit2/Callback"

        private const val Okhttp3Call = "okhttp3/Call"
        private const val Okhttp3Response = "okhttp3/Response"
        private const val Okhttp3Callback = "okhttp3/Callback"

        private const val FirebasePerfOkHttpClient = "com/example/firebase/pref/api/utils/FirebasePerfOkHttpClient"

    }

}