package com.katafract.parkarmor.billing

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class BillingManager(
    context: Context,
    private val onPurchaseSuccess: (purchaseToken: String, productId: String) -> Unit,
    private val onPurchaseError: (message: String) -> Unit,
) : PurchasesUpdatedListener {

    private val _isPro = MutableStateFlow(false)
    val isPro = _isPro.asStateFlow()

    private var productDetails: ProductDetails? = null

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(this)
        .enablePendingPurchases(PendingPurchasesParams.newBuilder().enableOneTimeProducts().build())
        .build()

    companion object {
        const val PROD_PRO = "com.katafract.parkarmor.pro"
        const val PROD_TIP_SMALL = "com.katafract.parkarmor.tip.small"
    }

    fun connect() {
        billingClient.startConnection(object : BillingClientStateListener {
            override fun onBillingSetupFinished(result: BillingResult) {
                if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                    queryProducts()
                    checkExistingPurchases()
                }
            }
            override fun onBillingServiceDisconnected() {}
        })
    }

    private fun queryProducts() {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(PROD_PRO)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build(),
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(PROD_TIP_SMALL)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build(),
            ))
            .build()
        billingClient.queryProductDetailsAsync(params) { _, details ->
            productDetails = details.firstOrNull { it.productId == PROD_PRO }
        }
    }

    private fun checkExistingPurchases() {
        billingClient.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder()
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        ) { result, purchases ->
            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                _isPro.value = purchases.any { p ->
                    p.products.contains(PROD_PRO) && p.purchaseState == Purchase.PurchaseState.PURCHASED
                }
                purchases.filter { it.purchaseState == Purchase.PurchaseState.PURCHASED && !it.isAcknowledged }
                    .forEach { acknowledgePurchase(it) }
            }
        }
    }

    fun launchPurchase(activity: Activity, productId: String) {
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(listOf(
                QueryProductDetailsParams.Product.newBuilder()
                    .setProductId(productId)
                    .setProductType(BillingClient.ProductType.INAPP)
                    .build()
            ))
            .build()
        billingClient.queryProductDetailsAsync(params) { _, details ->
            val detail = details.firstOrNull() ?: run {
                onPurchaseError("Product not found")
                return@queryProductDetailsAsync
            }
            val flowParams = BillingFlowParams.newBuilder()
                .setProductDetailsParamsList(listOf(
                    BillingFlowParams.ProductDetailsParams.newBuilder()
                        .setProductDetails(detail)
                        .build()
                ))
                .build()
            billingClient.launchBillingFlow(activity, flowParams)
        }
    }

    fun restorePurchases() {
        checkExistingPurchases()
    }

    override fun onPurchasesUpdated(result: BillingResult, purchases: List<Purchase>?) {
        if (result.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                    acknowledgePurchase(purchase)
                    purchase.products.forEach { pid ->
                        onPurchaseSuccess(purchase.purchaseToken, pid)
                        if (pid == PROD_PRO) _isPro.value = true
                    }
                }
            }
        } else if (result.responseCode != BillingClient.BillingResponseCode.USER_CANCELED) {
            onPurchaseError("Purchase failed (${result.responseCode})")
        }
    }

    private fun acknowledgePurchase(purchase: Purchase) {
        if (!purchase.isAcknowledged) {
            val params = AcknowledgePurchaseParams.newBuilder()
                .setPurchaseToken(purchase.purchaseToken)
                .build()
            billingClient.acknowledgePurchase(params) {}
        }
    }

    fun disconnect() { billingClient.endConnection() }
}
