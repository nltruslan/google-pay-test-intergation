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
        btnPaymentByGoogle = findViewById(R.id.btnPaymentByGoogle)
        setPayButtonClickListener()
        GooglePaymentUtils.checkIsReadyGooglePay(googlePaymentsClient, this::setPayButtonVisibility)
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
                        // Пользователь нажал назад,
                        // когда был показан диалог google pay
                        // если показывали загрузку или что-то еще,
                        // можете отменить здесь
                    }
                    AutoResolveHelper.RESULT_ERROR -> {
                        if (data == null)
                            return

                        // Гугл сам покажет диалог ошибки.
                        // Можете вывести логи и спрятать загрузку,
                        // если показывали
                        val status = AutoResolveHelper.getStatusFromIntent(data)
                        Log.e("GOOGLE PAY", "Load payment data has failed with status: $status")
                    }
                    else -> { }
                }
            }
            else -> { }
        }
    }

    private fun setPayButtonClickListener(){
        btnPaymentByGoogle.setOnClickListener {
            val price: String = computeItemPrice()
            val request: PaymentDataRequest = GooglePaymentUtils.createPaymentDataRequest(price)
            AutoResolveHelper.resolveTask<PaymentData>(googlePaymentsClient.loadPaymentData(request), this, REQUEST_CODE)
        }
    }

    private fun setPayButtonVisibility(visible:Boolean){
        btnPaymentByGoogle.visibility = if (visible) View.VISIBLE else View.GONE
    }

    private fun computeItemPrice():String = "10.00"
}