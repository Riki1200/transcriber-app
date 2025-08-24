package com.romeodev.core.purchases

import com.romeodev.core.AppConstants
import com.revenuecat.purchases.kmp.LogLevel
import com.revenuecat.purchases.kmp.Purchases
import com.revenuecat.purchases.kmp.PurchasesConfiguration


fun initRevenueCat() {
    Purchases.logLevel = LogLevel.DEBUG
    Purchases.configure(
        PurchasesConfiguration.Builder(
            apiKey = AppConstants.REVENUE_CAT_API_KEY,
        ).build(),
    )
}
