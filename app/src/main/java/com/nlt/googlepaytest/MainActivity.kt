package com.nlt.googlepaytest

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import com.google.android.gms.wallet.AutoResolveHelper
import com.google.android.gms.wallet.PaymentData
import com.google.android.gms.wallet.PaymentDataRequest
import com.google.android.gms.wallet.PaymentsClient
import org.json.JSONObject

class MainActivity : AppCompatActivity() {
    private lateinit var googlePaymentsClient: PaymentsClient
    private lateinit var btnPaymentByGoogle: Button

    companion object{
        const val REQUEST_CODE = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        googlePaymentsClient = GooglePaymentUtils.createGoogleApiClientForPay(applicationContext)
        //initialize Google Pay button
        btnPaymentByGoogle = findViewById(R.id.btnPaymentByGoogle)
        //add listener to Google Pay button
        btnPaymentByGoogle.setOnClickListener { requestPayment() }
        //check wethe we can display Google Pay button
        GooglePaymentUtils.checkIsReadyGooglePay(googlePaymentsClient){ btnPaymentByGoogle.visibility = if (it) View.VISIBLE else View.GONE}
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            REQUEST_CODE -> {
                when (resultCode) {
                    Activity.RESULT_OK -> {
                        if (data == null)
                            return
                        val paymentData: PaymentData? = PaymentData.getFromIntent(data)
                    }
                    Activity.RESULT_CANCELED -> {
                        //user canceled the payment
                    }
                    AutoResolveHelper.RESULT_ERROR -> {
                        if (data == null)
                            return
                        //Google will show error dialog, you no need to show something
                        val status = AutoResolveHelper.getStatusFromIntent(data)
                        Log.e("GOOGLE PAY", "Load payment data has failed with status: $status")
                    }
                    else -> { }
                }
                    btnPaymentByGoogle.isClickable = true
            }
            else -> { }
        }
    }

    private fun requestPayment() {
        val priceCents: String = computeItemPriceInCents()
        val paymentDataRequestJson: JSONObject? = GooglePaymentUtils.getPaymentDataRequest(priceCents)
        if (paymentDataRequestJson == null) {
            Log.e("RequestPayment", "Can't fetch payment data request")
            return
        }
        val request: PaymentDataRequest? = PaymentDataRequest.fromJson(paymentDataRequestJson.toString())
        if (request != null) {
            AutoResolveHelper.resolveTask(
                googlePaymentsClient.loadPaymentData(request), this, REQUEST_CODE)
        }
    }

    private fun setPayButtonVisibility(visible:Boolean){
        btnPaymentByGoogle.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun computeItemPriceInCents():String = "1000"
}