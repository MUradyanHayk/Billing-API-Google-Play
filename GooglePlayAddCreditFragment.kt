package com.beint.zangi.screens

import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Handler
import android.view.*
import androidx.fragment.app.Fragment
import com.android.billingclient.api.*
import com.android.billingclient.api.BillingFlowParams.ProductDetailsParams
import com.beint.zangi.core.utils.Log
import com.beint.zangi.screens.settings.premium.UploadSubscriptionData


class GooglePlayAddCreditFragment : Fragment() {

    companion object {
        val TAG: String = "GooglePlayAddCreditFragment"
    }

    private var billingClient: BillingClient? = null
    private val skuList = ArrayList<String>()

    private val sku_25 = "com.esim.connect_25"
    private val sku_10 = "com.esim.connect_10"
    private val sku_50 = "com.esim.connect_50"

    var productList: List<QueryProductDetailsParams.Product> = listOf()

    var handler:Handler? = null
    lateinit var view: GooglePlayAddCreditView
    var balanceText: String? = null
    var productDetailsList = mutableListOf<ProductDetails>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
//        arguments?.let {
//            balanceText = it.getString("balance")
//        }

//
//        billingClient = BillingClient.newBuilder(context!!)
//            .enablePendingPurchases()
//            .setListener { billingResult, list ->
//                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && list != null) {
//                    for (purchase in list) {
//                        verifyPurchase(purchase)
//                    }
//                }
//            }.build()

        //start the connection after initializing the billing client

        //start the connection after initializing the billing client
//        connectGooglePlayBilling()

    }

    fun connectGooglePlayBilling() {
        billingClient?.startConnection(object : BillingClientStateListener {
            override fun onBillingServiceDisconnected() {
                connectGooglePlayBilling()
            }

            override fun onBillingSetupFinished(billingResult: BillingResult) {
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                    showProducts()
                }
            }
        })
    }

    @SuppressLint("SetTextI18n")
    fun showProducts() {
        productList = listOf( //Product 1
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(sku_25)
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(sku_10)
                .setProductType(BillingClient.ProductType.INAPP)
                .build(),
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(sku_50)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        val params = QueryProductDetailsParams.newBuilder()
            .setProductList(productList)
            .build()
        billingClient?.queryProductDetailsAsync(params) { billingResult, list ->

            productDetailsList.clear()
            productDetailsList.addAll(list)
            handler?.postDelayed({
                for (i in list.indices) {
                    view.buttonsList[i]?.text = list[i].oneTimePurchaseOfferDetails?.formattedPrice ?: ""
                    view.buttonsList[i]?.hideProgressBar()
                    view.showAddButton()
                }
            }, 1000)
        }
    }

    fun launchPurchaseFlow(productDetails: ProductDetails?) {
        val productDetailsParamsList: List<ProductDetailsParams> = listOf(
            ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails!!)
                .build()
        )
        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()
        val billingResult: BillingResult? = billingClient?.launchBillingFlow(activity!!, billingFlowParams)
//        billingClient?.showInAppMessages(activity!!,billingFlowParams ,object :InAppMessageResponseListener{
//            override fun onInAppMessageResponse(p0: InAppMessageResult) {
//
//            }
//        })
    }

    fun verifyPurchase(purchase: Purchase) {
        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()
        val listener = ConsumeResponseListener { billingResult: BillingResult, s: String? ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                givePurchase(purchase)
            }
        }
        billingClient?.consumeAsync(consumeParams, listener)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        super.onCreateView(inflater, container, savedInstanceState)
        view = GooglePlayAddCreditView(context!!)

        handler = Handler()

        view.addButton?.setOnClickListener {

            for(i in view.buttonsList.indices){
                if (view.buttonsList[i]?.isChecked == true) {
                    launchPurchaseFlow(productDetailsList[i])
                    return@setOnClickListener
                }
            }
        }


        billingClient = BillingClient.newBuilder(context!!)
            .enablePendingPurchases()
            .setListener { billingResult, list ->
                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && list != null) {
                    for (purchase in list) {
                        verifyPurchase(purchase)
                    }
                }
            }.build()

        //start the connection after initializing the billing client

        //start the connection after initializing the billing client
        connectGooglePlayBilling()
        return view
    }

    override fun onResume() {
        super.onResume()
        billingClient?.queryPurchasesAsync(
            QueryPurchasesParams.newBuilder().setProductType(BillingClient.ProductType.INAPP).build(),
            { billingResult, list ->
                if (billingResult.getResponseCode() === BillingClient.BillingResponseCode.OK) {
                    for (purchase in list) {
                        if (purchase.purchaseState === Purchase.PurchaseState.PURCHASED && !purchase.isAcknowledged) {
                            verifyPurchase(purchase)
                        }
                    }
                }
            }
        )
    }

    @SuppressLint("SetTextI18n")
    fun givePurchase(purchase: Purchase) {
        UploadSubscriptionData(purchase.packageName, purchase.purchaseToken, purchase.products[0]).execute()

//        Log.d("TestINAPP", purchase.products[0])
//        Log.d("TestINAPP", purchase.quantity.toString() + " Quantity")
//        for (i in 0 until productIds.size()) {
//            if (purchase.products[0].equals(productIds.get(i))) {
//                Log.d(TAG, "Balance " + prefs.getInt("coins", 0).toString() + " Coins")
//                Log.d(TAG, "Allocating " + coins.get(i).toString() + " Coins")
//
//                //set coins
//                prefs.setInt("coins", coins.get(i) + prefs.getInt("coins", 0))
//                Log.d(TAG, "New Balance " + prefs.getInt("coins", 0).toString() + " Coins")
//
//                //Update UI
//                txt_coins.setText(prefs.getInt("coins", 0).toString() + "")
//            }
//        }
    }
}