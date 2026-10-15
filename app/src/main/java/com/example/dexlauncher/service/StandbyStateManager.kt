package com.example.dexlauncher.service

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

object StandbyStateManager {

    // Starts on Standby mode by default when launcher opens first time
    private val _isStandbyActive = MutableStateFlow(true)
    val isStandbyActive: StateFlow<Boolean> = _isStandbyActive.asStateFlow()

    fun resetToStandby() {
        _isStandbyActive.value = true
    }

    fun unlockToDesktop() {
        _isStandbyActive.value = false
    }

    fun onHomeButtonPressed() {
        // As required: "Nhấn nút Home từ app khác chỉ về Desktop, KHÔNG reset Standby."
        // Once unlocked, pressing home always keeps Desktop active!
        // Standby is only reset when SCREEN_OFF occurs.
    }
}
