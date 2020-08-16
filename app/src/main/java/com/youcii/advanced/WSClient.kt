package com.youcii.advanced

import android.widget.Toast
import org.java_websocket.client.WebSocketClient
import org.java_websocket.drafts.Draft
import org.java_websocket.drafts.Draft_6455
import org.java_websocket.handshake.ServerHandshake
import java.lang.Exception
import java.net.URI

/**
 * Created by jingdongwei on 2020/08/16.
 */
class WSClient @JvmOverloads constructor(
    serverUri: URI,
    protocolDraft: Draft = Draft_6455(),
    httpHeaders: Map<String, String>? = null,
    connectTimeout: Int = 0
) : WebSocketClient(serverUri, protocolDraft, httpHeaders, connectTimeout) {

    override fun onOpen(handshakedata: ServerHandshake?) {
        Toast.makeText(MyApplication.context, "onOpen", Toast.LENGTH_SHORT).show()
    }

    override fun onClose(code: Int, reason: String?, remote: Boolean) {
        Toast.makeText(MyApplication.context, "onClose", Toast.LENGTH_SHORT).show()
    }

    override fun onMessage(message: String?) {
        Toast.makeText(MyApplication.context, "onMessage: $message", Toast.LENGTH_SHORT).show()
    }

    override fun onError(ex: Exception?) {
        Toast.makeText(MyApplication.context, "onError: $ex", Toast.LENGTH_SHORT).show()
    }

}