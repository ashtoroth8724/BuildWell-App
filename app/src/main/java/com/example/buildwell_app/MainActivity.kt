package com.example.buildwell_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val SHARED_PREFS_NAME = "AppPreferences"
    private val KEY_SCANNED_PRODUCTS = "scanned_products" // Key for the set of scanned products

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnMarketplace: Button = findViewById(R.id.btnMarketplace)
        val btnScanProduct: Button = findViewById(R.id.btnScanProduct)
        val btnProductList: Button = findViewById(R.id.btnProductList)

        val sharedPrefs = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)

        btnMarketplace.setOnClickListener {
            val intent = Intent(this, MarketplaceActivity::class.java)
            startActivity(intent)
        }

        btnScanProduct.setOnClickListener {
            // Simulate QR code scan for a coffee machine.
            // In a real app, this would come from your QR scanner result.
            val simulatedScannedProductId = "coffee-machine" // Simulate the scanned product ID

            // Get the current set of scanned products from SharedPreferences
            // Use a mutable copy to modify
            val scannedProductsSet = sharedPrefs.getStringSet(KEY_SCANNED_PRODUCTS, emptySet())?.toMutableSet() ?: mutableSetOf()

            // Add the new scanned product ID to the set
            scannedProductsSet.add(simulatedScannedProductId)

            // Save the updated set back to SharedPreferences
            with(sharedPrefs.edit()) {
                putStringSet(KEY_SCANNED_PRODUCTS, scannedProductsSet)
                apply() // Use apply() for asynchronous saving
                Log.d("MainActivity", "Saving scanned product ID: $simulatedScannedProductId")
            }

            // Navigate to the InstructionDetailActivity
            val intent = Intent(this, InstructionDetailActivity::class.java)

            // Pass the simulated product ID as an extra to InstructionDetailActivity
            intent.putExtra("productId", simulatedScannedProductId)

            startActivity(intent)

            // Optionally, you might want to finish MainActivity if you don't want it
            // on the back stack after scanning and viewing instructions.
            // finish()
        }

        btnProductList.setOnClickListener {
            val intent = Intent(this, ProductListActivity::class.java)
            startActivity(intent)
        }
    }
}