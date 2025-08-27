package com.romeodev.core.purchases.presentation.viewmodels

import androidx.lifecycle.ViewModel
import com.romeodev.core.datastore.common.CommonDataStore
import com.romeodev.core.utils.common.hoursToMillis
import com.romeodev.core.utils.logging.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

class PurchaseDialogViewModel(
    private val commonDataStore: CommonDataStore,
) : ViewModel() {
    private val _expirationMillis = MutableStateFlow(0L)
    val expirationMillis = _expirationMillis.asStateFlow()
    private val _showDiscountDialog = MutableStateFlow(false)
    val showDiscountDialog = _showDiscountDialog.asStateFlow()

    companion object {
        private const val TAG = "PurchaseDialogViewModel"
        private const val DEFAULT_EXPIRATION_HOUR = 1
    }

    private var getDiscountStatusJob: Job? = null

    init {
        getDiscountExpirationStatus()
    }

    @OptIn(ExperimentalTime::class)
    private fun getDiscountExpirationStatus() {
        getDiscountStatusJob?.cancel()
        getDiscountStatusJob = CoroutineScope(Dispatchers.IO).launch {
            Log.d(TAG, "Getting Discount Expiration Status")
            val currentMillis = Clock.System.now().toEpochMilliseconds()
            commonDataStore.installMillis.map { millis ->
                (millis) + hoursToMillis(hour = DEFAULT_EXPIRATION_HOUR)
            }.collect { millis ->
                val showDiscount = millis > currentMillis
                Log.d(
                    TAG,
                    "Exp Millis: $millis, current: $currentMillis, bool: $showDiscount"
                )
                _expirationMillis.update {
                    millis
                }
                _showDiscountDialog.update {
                    showDiscount
                }
            }

        }
    }

}



















