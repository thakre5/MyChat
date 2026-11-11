package com.example.securemessenger.p2p

import android.content.Context
import org.webrtc.*
import java.nio.ByteBuffer

class WebRtcManager(
    private val context: Context,
    private val onMessageReceived: (String) -> Unit,
    private val onConnectionStateChanged: (PeerConnection.PeerConnectionState) -> Unit
) {

    private var peerConnectionFactory: PeerConnectionFactory? = null
    private var peerConnection: PeerConnection? = null
    private var dataChannel: DataChannel? = null

    init {
        // Initialize WebRTC globals
        val initializationOptions = PeerConnectionFactory.InitializationOptions.builder(context)
            .setEnableInternalTracer(true)
            .createInitializationOptions()
        PeerConnectionFactory.initialize(initializationOptions)

        val options = PeerConnectionFactory.Options()
        peerConnectionFactory = PeerConnectionFactory.builder()
            .setOptions(options)
            .createPeerConnectionFactory()
    }

    private val iceServers = listOf(
        // Public STUN servers to discover local/public IP endpoints (no data stored)
        PeerConnection.IceServer.builder("stun:stun.l.google.com:19302").create(),
        PeerConnection.IceServer.builder("stun:stun1.l.google.com:19302").create()
    )

    /**
     * Step A: Device A creates an outgoing Connection Offer.
     */
    fun createOffer(onOfferCreated: (String) -> Unit) {
        peerConnection = createPeerConnection() ?: return
        
        // Create a reliable data channel named "SecureChatChannel"
        val init = DataChannel.Init()
        dataChannel = peerConnection!!.createDataChannel("SecureChatChannel", init)
        setupDataChannelCallbacks()

        peerConnection!!.createOffer(object : SdpObserver {
            override and fun onCreateSuccess(sessionDescription: SessionDescription?) {
                peerConnection!!.setLocalDescription(object : SdpObserver {
                    override fun onSetSuccess() {
                        sessionDescription?.let { onOfferCreated(it.description) }
                    }
                    override fun onSetFailure(p0: String?) {}
                    override fun onCreateSuccess(p0: SessionDescription?) {}
                    override fun onCreateFailure(p0: String?) {}
                }, sessionDescription)
            }
            override fun onSetSuccess() {}
            override fun onCreateFailure(p0: String?) {}
            override fun onSetFailure(p0: String?) {}
        }, MediaConstraints())
    }

    /**
     * Step B: Device B accepts Device A's offer and generates an Answer.
     */
    fun acceptOffer(offerSdp: String, onAnswerCreated: (String) -> Unit) {
        peerConnection = createPeerConnection() ?: return
        
        peerConnection!!.setRemoteDescription(object : SdpObserver {
            override fun onSetSuccess() {
                peerConnection!!.createAnswer(object : SdpObserver {
                    override fun onCreateSuccess(sessionDescription: SessionDescription?) {
                        peerConnection!!.setLocalDescription(object : SdpObserver {
                            override fun onSetSuccess() {
                                sessionDescription?.let { onAnswerCreated(it.description) }
                            }
                            override fun onSetFailure(p0: String?) {}
                            override fun onCreateSuccess(p0: SessionDescription?) {}
                            override fun onCreateFailure(p0: String?) {}
                        }, sessionDescription)
                    }
                    override fun onSetFailure(p0: String?) {}
                    override fun onCreateFailure(p0: String?) {}
                    override fun onSetFailure(p0: String?) {}
                }, MediaConstraints())
            }
            override fun onSetFailure(p0: String?) {}
            override fun onCreateSuccess(p0: SessionDescription?) {}
            override fun onCreateFailure(p0: String?) {}
        }, SessionDescription(SessionDescription.Type.OFFER, offerSdp))
    }

    /**
     * Step C: Device A completes the handshake by applying Device B's Answer.
     */
    fun receiveAnswer(answerSdp: String) {
        peerConnection?.setRemoteDescription(object : SdpObserver {
            override fun onSetSuccess() {}
            override fun onSetFailure(p0: String?) {}
            override fun onCreateSuccess(p0: SessionDescription?) {}
            override fun onCreateFailure(p0: String?) {}
        }, SessionDescription(SessionDescription.Type.ANSWER, answerSdp))
    }

    private fun createPeerConnection(): PeerConnection? {
        val rtcConfig = PeerConnection.RTCConfiguration(iceServers)
        return peerConnectionFactory?.createPeerConnection(rtcConfig, object : PeerConnection.Observer {
            override fun onSignalingChange(p0: PeerConnection.SignalingState?) {}
            override fun onIceConnectionChange(state: PeerConnection.IceConnectionState?) {
                if (state == PeerConnection.IceConnectionState.CONNECTED) {
                    onConnectionStateChanged(PeerConnection.PeerConnectionState.CONNECTED)
                }
            }
            override fun onIceConnectionReceivingChange(p0: Boolean) {}
            override fun onIceGatheringChange(p0: PeerConnection.IceGatheringState?) {}
            override fun onIceCandidate(candidate: IceCandidate?) {
                // In a full implementation, pass ICE candidates via QR code or ephemeral relay
            }
            override fun onRemoveIceCandidates(p0: Array<out IceCandidate>?) {}
            override fun onAddStream(p0: MediaStream?) {}
            override fun onRemoveStream(p0: MediaStream?) {}
            override fun onDataChannel(dc: DataChannel) {
                // Invoked on Device B when the data channel opens
                dataChannel = dc
                setupDataChannelCallbacks()
            }
            override fun onRenegotiationNeeded() {}
            override fun onAddTrack(p0: RtpReceiver?, p1: Array<out MediaStream?>?) {}
        })
    }

    private fun setupDataChannelCallbacks() {
        dataChannel?.registerObserver(object : DataChannel.Observer {
            override fun onBufferedAmountChange(p0: Long) {}
            override fun onStateChange() {}
            override fun onMessage(buffer: DataChannel.Buffer) {
                val data = buffer.data
                val bytes = ByteArray(data.remaining())
                data.get(bytes)
                val messageStr = String(bytes, Charsets.UTF_8)
                onMessageReceived(messageStr)
            }
        })
    }

    /**
     * Sends an encrypted ciphertext message directly over the peer-to-peer data channel.
     */
    fun sendDirectMessage(cipherText: String) {
        val buffer = DataChannel.Buffer(ByteBuffer.wrap(cipherText.toByteArray(Charsets.UTF_8)), false)
        dataChannel?.send(buffer)
    }

    fun close() {
        dataChannel?.dispose()
        peerConnection?.dispose()
        peerConnectionFactory?.dispose()
    }
}
