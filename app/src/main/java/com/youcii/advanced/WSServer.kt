package com.youcii.advanced

import android.widget.Toast
import org.java_websocket.WebSocket
import org.java_websocket.drafts.Draft
import org.java_websocket.handshake.ClientHandshake
import org.java_websocket.server.WebSocketServer
import java.net.InetSocketAddress

/**
 * Created by jingdongwei on 2020/08/16.
 */
class WSServer @JvmOverloads constructor(
    address: InetSocketAddress?,
    decoderCount: Int = Runtime.getRuntime().availableProcessors(),
    drafts: MutableList<Draft>? = null,
    connectionsContainer: MutableCollection<WebSocket>? = HashSet()
) : WebSocketServer(address, decoderCount, drafts, connectionsContainer) {

    override fun onOpen(conn: WebSocket?, handshake: ClientHandshake?) {
        Toast.makeText(MyApplication.context, "onOpen", Toast.LENGTH_SHORT).show()
    }

    override fun onClose(conn: WebSocket?, code: Int, reason: String?, remote: Boolean) {
        Toast.makeText(MyApplication.context, "onClose: $code", Toast.LENGTH_SHORT).show()
    }

    override fun onMessage(conn: WebSocket?, message: String?) {
        Toast.makeText(MyApplication.context, "onMessage: $message", Toast.LENGTH_SHORT).show()
    }

    override fun onStart() {
        Toast.makeText(MyApplication.context, "onStart", Toast.LENGTH_SHORT).show()
    }

    override fun onError(conn: WebSocket?, ex: Exception?) {
        Toast.makeText(MyApplication.context, "onError: $ex", Toast.LENGTH_SHORT).show()
    }

}