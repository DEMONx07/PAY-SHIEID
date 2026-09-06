package com.payshield.app.security.engine

import com.payshield.app.domain.model.RiskSignal

object SignalNormalizer {

    /**
     * Deduplicates and normalizes raw detected signals to ensure consistent scoring.
     */
    fun normalizeSignals(rawSignals: List<RiskSignal>): List<RiskSignal> {
        if (rawSignals.isEmpty()) return emptyList()

        val normalized = mutableListOf<RiskSignal>()
        val seenIds = mutableSetOf<String>()

        for (signal in rawSignals) {
            if (seenIds.contains(signal.id)) continue
            seenIds.add(signal.id)
            normalized.add(signal)
        }

        return normalized
    }
}
