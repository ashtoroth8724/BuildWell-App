package com.example.buildwell_app

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private val SHARED_PREFS_NAME = "product_prefs"
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
            val scannedProduct = "coffee-machine" // Simulate the scanned value

            // Get the current set of scanned products from SharedPreferences
            // Use a mutable copy to modify
            val scannedProductsSet = sharedPrefs.getStringSet(KEY_SCANNED_PRODUCTS, emptySet())?.toMutableSet() ?: mutableSetOf()


            // Add the new scanned product to the set
            scannedProductsSet.add(scannedProduct)

            // Save the updated set back to SharedPreferences
            with(sharedPrefs.edit()) {
                putStringSet(KEY_SCANNED_PRODUCTS, scannedProductsSet)
                apply() // Use apply() for asynchronous saving
            }

            // Navigate to the ProductListActivity
            val intent = Intent(this, ProductListActivity::class.java)
            startActivity(intent)
        }

        btnProductList.setOnClickListener {
            val intent = Intent(this, ProductListActivity::class.java)
            startActivity(intent)
        }
    }
}