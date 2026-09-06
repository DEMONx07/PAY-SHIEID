package com.payshield.app.security.qr

import java.security.MessageDigest

class QrScanDebouncer(
    private val cooldownMs: Long = 1500L
) {
    private var lastPayloadHash: String? = null
    private var lastScanTimeMs: Long = 0L
    private var isScanLocked: Boolean = false

    @Synchronized
    fun shouldProcess(rawPayload: String): Boolean {
        if (isScanLocked) return false

        val currentTime = System.currentTimeMillis()
        val currentHash = computeHash(rawPayload)

        // Ignore identical payload if scanned within cooldown window
        if (currentHash == lastPayloadHash && (currentTime - lastScanTimeMs) < cooldownMs) {
            return false
        }

        lastPayloadHash = currentHash
        lastScanTimeMs = currentTime
        isScanLocked = true
        return true
    }

    @Synchronized
    fun unlockScan() {
        isScanLocked = false
    }

    @Synchronized
    fun reset() {
        lastPayloadHash = null
        lastScanTimeMs = 0L
        isScanLocked = false
    }

    private fun computeHash(input: String): String {
        return try {
            val bytes = MessageDigest.getInstance("SHA-256").digest(input.toByteArray())
            bytes.joinToString("") { "%02x".format(it) }
        } catch (_: Exception) {
            input.hashCode().toString()
        }
    }
}
