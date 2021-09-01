package com.example.pwmobilesdk

import android.app.AlertDialog
import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import android.view.Menu
import android.view.MenuItem
import com.example.pwmobilesdk.databinding.ActivityMainBinding
import android.widget.Toast
import io.mpos.transactions.receipts.Receipt
import io.mpos.transactionprovider.TransactionProcessDetailsState
import io.mpos.transactionprovider.TransactionProcessDetails
import io.mpos.transactionprovider.TransactionProcess
import io.mpos.paymentdetails.DccInformation
import io.mpos.paymentdetails.ApplicationInformation
import io.mpos.android.shared.BitmapHelper
import android.graphics.Bitmap
import android.util.Log
import io.mpos.transactionprovider.TransactionProcessWithRegistrationListener
import io.mpos.transactions.parameters.TransactionParameters
import io.mpos.accessories.AccessoryFamily
import io.mpos.accessories.parameters.AccessoryParameters
import io.mpos.provider.ProviderMode
import io.mpos.Mpos.createTransactionProvider
import io.mpos.transactions.Currency
import io.mpos.transactions.Transaction
import java.math.BigDecimal
import java.util.*
import android.content.DialogInterface




class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)

        val navController = findNavController(R.id.nav_host_fragment_content_main)
        appBarConfiguration = AppBarConfiguration(navController.graph)
        setupActionBarWithNavController(navController, appBarConfiguration)

        binding.fab.setOnClickListener { view ->
            Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).show()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        Log.d( "payworks", "onOptionsItemSelected" )

        if ( item.itemId == R.id.action_settings )
        {
            Log.d( "payworks", "actionSettings" )

            Toast.makeText( this, "Let's do a test transaction ..", Toast.LENGTH_LONG ).show()

            Log.d( "payworks", "Toast - shown!" )

            val builder = AlertDialog.Builder( this )
/*
            val items = mutableListOf( "1", "2", "3" );
*/
            builder.setTitle("Fire Transaction?")
            builder.setMessage("Ready to fire a test transaction ?")
            builder.setPositiveButton( "Go!") {
                dialog, which -> Log.d( "payworks", "Positive Button clicked!" )

                Log.d( "payworks", "Perform Payworks Test Transaction .." )
                this.transaction()
            }
            builder.setNegativeButton( "Not now..") {
                dialog, which -> Log.d( "payworks", "Negative Button clicked!" )
            }

            builder.create().show()
        }

        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_settings -> true
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration)
                || super.onSupportNavigateUp()
    }

    fun transaction() {
        val transactionProvider = createTransactionProvider(
            this, ProviderMode.TEST,
            "cbf4d153-e78a-4937-8ece-6b1ec948a2f9",
            "ZCMNdotEb3dLkRWabOxUsqe20hDq31ml"
        )

        /* For starting transaction in mocked mode use fallowing provider:
    final TransactionProvider transactionProvider = Mpos.createTransactionProvider(this,
            ProviderMode.MOCK,
            "merchantIdentifier",
            "merchantSecretKey");
    */


        /* When using the Bluetooth Miura, use the following parameters: */
        val accessoryParameters = AccessoryParameters.Builder(AccessoryFamily.MIURA_MPI)
            .bluetooth()
            .build()


        /* When using Verifone readers via WiFi or Ethernet, use the following parameters:
    AccessoryParameters accessoryParameters = new AccessoryParameters.Builder(AccessoryFamily.VERIFONE_VIPA)
                                                                     .tcp("192.168.254.123", 16107)
                                                                     .build();
    */
        val transactionParameters = TransactionParameters.Builder()
            .charge(BigDecimal("5.00"), Currency.EUR)
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
                    Log.d("mpos", "status changed: " + Arrays.toString(processDetails.information))
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

    override fun onBackPressed() {
        Toast.makeText(
            this,
            "The back button is disabled during a transaction. Please use the 'abort' button to cancel the transaction.",
            Toast.LENGTH_LONG
        ).show()
    }
}
