package com.polaris.filmo.core.renderer

interface RenderFrameCapturer {
    fun captureCurrentFrame(callback: (Result<CapturedFrame>) -> Unit)
}
