package com.example.pwmobilesdk

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import com.example.pwmobilesdk.databinding.ActivityMainBinding
import io.mpos.transactions.receipts.Receipt
import io.mpos.transactionprovider.TransactionProcessDetailsState
import io.mpos.transactionprovider.TransactionProcessDetails
import io.mpos.transactionprovider.TransactionProcess
import io.mpos.paymentdetails.DccInformation
import io.mpos.paymentdetails.ApplicationInformation
import io.mpos.android.shared.BitmapHelper
import android.graphics.Bitmap
import android.util.Log
import android.view.View
import android.widget.*
import io.mpos.transactionprovider.TransactionProcessWithRegistrationListener
import io.mpos.transactions.parameters.TransactionParameters
import io.mpos.accessories.AccessoryFamily
import io.mpos.accessories.parameters.AccessoryParameters
import io.mpos.provider.ProviderMode
import io.mpos.transactions.Currency
import io.mpos.transactions.Transaction
import java.math.BigDecimal
import java.util.*
import io.mpos.Mpos
import io.mpos.mock.DefaultMockConfiguration
import kotlin.collections.HashMap

class MainActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private var _process: TransactionProcess? = null

    private var _scrollViewTextLog: ScrollView? = null
    private var _textViewLog: TextView? = null
    private var _buttonStartTransaction: Button? = null
    private var _buttonAbortTransaction: Button? = null
    private var _buttonClearLog: Button? = null
    private var _inputAmount: EditText? = null
    private var _inputCurrency: Spinner? = null
    private var _inputTestAmount: Spinner? = null

    private var _testAmounts = HashMap<String, BigDecimal>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        this._scrollViewTextLog      = findViewById(R.id.textview_log_scroll_view)
        this._textViewLog            = findViewById( R.id.textview_log )
        this._buttonStartTransaction = findViewById( R.id.button_test_transaction )
        this._buttonAbortTransaction = findViewById( R.id.button_test_abort )
        this._buttonClearLog         = findViewById( R.id.button_clear_log )
        this._inputAmount            = findViewById( R.id.input_amount )
        this._inputCurrency          = findViewById( R.id.input_currency )
        this._inputTestAmount        = findViewById( R.id.input_test_amount )

        //setup currency spinner
        this._inputCurrency?.adapter = ArrayAdapter<Currency>(this, android.R.layout.simple_list_item_1, Currency.values())
        this._inputCurrency?.setSelection(Currency.EUR.ordinal)

        //setup test amount spinner
        setTestAmounts()

        val sortedAmountKeys = _testAmounts.keys.toMutableList().sorted()
        this._inputTestAmount?.adapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, sortedAmountKeys)

        val defaultIndex = sortedAmountKeys.indexOf("DEFAULT")
        this._inputTestAmount?.setSelection(defaultIndex)

        this._inputTestAmount?.onItemSelectedListener = this
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        Log.d( "payworks", "onOptionsItemSelected" )

        // if ( item.itemId == R.id.action_test_transaction )
        // {
        // }

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_test_transaction -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    fun clearLog() {
        _textViewLog?.text = ""
    }

    fun setTestAmounts() {
        val mockConf = DefaultMockConfiguration()
        val fields = mockConf.javaClass.declaredFields
        var amounts = HashMap<String, BigDecimal>()

        for (field in fields) {
            if (field.name.contains("AMOUNT_", ignoreCase = false)) {
                val amount = field.get(mockConf) as BigDecimal
                amounts[field.name.removePrefix("AMOUNT_")] = amount
            }
        }

        _testAmounts = amounts
    }

    /**
        Entire code from Payworks Dev Portal Mobile SDK Android Integration page.
        @see https://payworks.mpymnt.com/cp_int_pos_custom_overview/cp_int_pos_custom_integration.html
    */
    fun startTransaction() {
        // get and check amount
        val amount: String = _inputAmount?.text.toString()

        val decimalAmount: BigDecimal
        if (amount.isEmpty()) {
            logToConsole("No valid amount provided")
            return
        }
        decimalAmount = amount.toBigDecimal()

        val currency = _inputCurrency?.selectedItem as Currency

        // log new transaction start
        logToConsole("Starting new Transaction")

        // disable button 'start transaction'
        _buttonStartTransaction?.visibility = View.GONE

        // show but disable button 'cancel transaction'
        _buttonAbortTransaction?.visibility = View.VISIBLE
        _buttonAbortTransaction?.isEnabled  = false

        // For starting transaction in mocked mode use fallowing provider:
        val transactionProvider = Mpos.createTransactionProvider(
            this,
            ProviderMode.MOCK,
            "cbf4d153-e78a-4937-8ece-6b1ec948a2f9",
            "ZCMNdotEb3dLkRWabOxUsqe20hDq31ml"
        )

        /* When using the Bluetooth Miura, use the following parameters: */
        val accessoryParameters = AccessoryParameters.Builder(AccessoryFamily.MOCK)
            .mocked()
            .build()


        /* When using Verifone readers via WiFi or Ethernet, use the following parameters:
    AccessoryParameters accessoryParameters = new AccessoryParameters.Builder(AccessoryFamily.VERIFONE_VIPA)
                                                                     .tcp("192.168.254.123", 16107)
                                                                     .build();
    */

        val transactionParameters = TransactionParameters.Builder()
            .charge(decimalAmount, currency)
            .subject("Bouquet of Flowers")
            .customIdentifier("yourReferenceForTheTransaction")
            .build()
        val paymentProcess = transactionProvider.startTransaction(transactionParameters, accessoryParameters,
            object : TransactionProcessWithRegistrationListener {
                override fun onRegistered(
                    process: TransactionProcess?,
                    transaction: Transaction
                ) {
                    Log.d(
                        "mpos",
                        "transaction identifier is: " + transaction.getIdentifier()
                            .toString() + ". Store it in your backend so that you can always query its status."
                    )
                }

                override fun onStatusChanged(
                    process: TransactionProcess?,
                    transaction: Transaction?,
                    processDetails: TransactionProcessDetails
                ) {
                    var lineToLog = Arrays.toString(processDetails.information)

                    Log.d("mpos", "status changed: " + lineToLog)

                    _process = process

                    if (transaction != null) {
                        if (transaction.canBeAborted())
                        {
                            _buttonAbortTransaction?.isEnabled = true
                             lineToLog += ", abortable: " + transaction.canBeAborted()
                        }
                        else {
                            _buttonAbortTransaction?.isEnabled = false
                            lineToLog += ", abortable: " + transaction.canBeAborted()
                        }
                    }

                    logToConsole(lineToLog)
                }

                override fun onCustomerSignatureRequired(
                    process: TransactionProcess,
                    transaction: Transaction?
                ) {
                    // in a live app, this image comes from your signature screen
                    val conf = Bitmap.Config.ARGB_8888
                    val bm = Bitmap.createBitmap(1, 1, conf)
                    val signature = BitmapHelper.byteArrayFromBitmap(bm)
                    process.continueWithCustomerSignature(signature, true)
                }

                override fun onCustomerVerificationRequired(
                    process: TransactionProcess,
                    transaction: Transaction?
                ) {
                    // always return false here
                    process.continueWithCustomerIdentityVerified(false)
                }

                override fun onApplicationSelectionRequired(
                    process: TransactionProcess,
                    transaction: Transaction?,
                    applicationInformation: List<ApplicationInformation?>
                ) {
                    // This happens only for readers that don't support application selection on their screen
                    process.continueWithSelectedApplication(applicationInformation[0])
                }

                override fun onDccSelectionRequired(
                    transactionProcess: TransactionProcess,
                    transaction: Transaction?,
                    dccInformation: DccInformation?
                ) {

                    // This comes up if the DCC selection cannot be done on the terminal itself
                    transactionProcess.continueDccSelectionWithOriginalAmount()
                }

                override fun onCompleted(
                    process: TransactionProcess?,
                    transaction: Transaction,
                    processDetails: TransactionProcessDetails
                ) {
                    Log.d("mpos", "completed")

                    _buttonStartTransaction?.visibility = View.VISIBLE
                    _buttonAbortTransaction?.visibility = View.GONE

                    logToConsole("Transaction completed")

                    if (processDetails.state == TransactionProcessDetailsState.APPROVED) {
                        // print the merchant receipt
                        val merchantReceipt: Receipt = transaction.getMerchantReceipt()

                        // print a signature line if required
                        if (merchantReceipt.isSignatureLineRequired()) {
                            println("")
                            println("")
                            println("")
                            println("------ PLEASE SIGN HERE ------")
                        }

                        // ask the merchant, whether the shopper wants to have a receipt
                        val customerReceipt: Receipt = transaction.getCustomerReceipt()

                        // and close the checkout UI
                    } else {
                        // Allow your merchant to try another transaction
                    }
                }
            })
    }

    fun logToConsole(lineToLog: String) {
        _textViewLog?.append(lineToLog + "\n")
        scrollTextViewLogToBottom()
    }

    private fun scrollTextViewLogToBottom() {
        _scrollViewTextLog?.fullScroll(View.FOCUS_DOWN);
    }

    override fun onBackPressed() {
        Toast.makeText(
            this,
            "The back button is disabled during a transaction. Please use the 'abort' button to cancel the transaction.",
            Toast.LENGTH_LONG
        ).show()
    }

    fun abortTransaction() {

        Log.i( "mpos", "cancelTransaction Button pressed!" )

        this._process?.requestAbort()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, pos: Int, id: Long) {
        val selected = parent?.getItemAtPosition(pos)
        val amountString = _testAmounts[selected].toString()
        if (amountString != null) {
            _inputAmount?.setText(amountString)
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {
        _inputAmount?.setText("")
    }
}
