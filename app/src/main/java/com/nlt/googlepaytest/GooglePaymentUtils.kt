import android.content.Context
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.wallet.*

object GooglePaymentUtils {

    private val SUPPORTED_NETWORKS = arrayListOf(
        WalletConstants.CARD_NETWORK_OTHER,
        WalletConstants.CARD_NETWORK_VISA,
        WalletConstants.CARD_NETWORK_MASTERCARD
    )

    private val SUPPORTED_METHODS = arrayListOf(
        WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD,
        WalletConstants.PAYMENT_METHOD_CARD
    )

    fun createGoogleApiClientForPay(context: Context): PaymentsClient =
        Wallet.getPaymentsClient(
            context,
            Wallet.WalletOptions.Builder()
                .setEnvironment(WalletConstants.ENVIRONMENT_TEST)
                .setTheme(WalletConstants.THEME_LIGHT)
                .build()
        )

    fun checkIsReadyGooglePay(
        paymentsClient: PaymentsClient,
        callback: (res: Boolean) -> Unit
    ) {
        val isReadyRequest = IsReadyToPayRequest.newBuilder()
            .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_CARD)
            .addAllowedPaymentMethod(WalletConstants.PAYMENT_METHOD_TOKENIZED_CARD)
            .build()
        val task = paymentsClient.isReadyToPay(isReadyRequest)
        task.addOnCompleteListener {
            try {
                if (it.getResult(ApiException::class.java) == true)
                // можем показать кнопку оплаты, все хорошо
                    callback.invoke(true)
                else
                // должны спрятать кнопку оплаты
                    callback.invoke(false)
            } catch (e: ApiException) {
                e.printStackTrace()
                callback.invoke(false)
            }
        }
    }

    fun createPaymentDataRequest(price: String): PaymentDataRequest {
        val transaction = createTransaction(price)
        val request = generatePaymentRequest(transaction)
        return request
    }

    fun createTransaction(price: String): TransactionInfo =
        TransactionInfo.newBuilder()
            .setTotalPriceStatus(WalletConstants.TOTAL_PRICE_STATUS_FINAL)
            .setTotalPrice(price)
            .setCurrencyCode(/*CURRENCY_CODE*/ "USD")
            .build()

    private fun generatePaymentRequest(transactionInfo: TransactionInfo): PaymentDataRequest {
        val tokenParams = PaymentMethodTokenizationParameters
            .newBuilder()
            .setPaymentMethodTokenizationType(WalletConstants.PAYMENT_METHOD_TOKENIZATION_TYPE_DIRECT)
            .addParameter("gateway", "aciworldwide")
            .addParameter("gatewayMerchantId", "Shop name")
            .build()

        return PaymentDataRequest.newBuilder()
            .setPhoneNumberRequired(false)
            .setEmailRequired(true)
            .setShippingAddressRequired(true)
            .setTransactionInfo(transactionInfo)
            .addAllowedPaymentMethods(SUPPORTED_METHODS)
            .setCardRequirements(
                CardRequirements.newBuilder()
                    .addAllowedCardNetworks(SUPPORTED_NETWORKS)
                    .setAllowPrepaidCards(true)
                    .setBillingAddressRequired(true)
                    .setBillingAddressFormat(WalletConstants.BILLING_ADDRESS_FORMAT_FULL)
                    .build()
            )
            .setPaymentMethodTokenizationParameters(tokenParams)
            .setUiRequired(true)
            .build()
    }
}