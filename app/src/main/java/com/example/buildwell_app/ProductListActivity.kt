package com.example.buildwell_app

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ListView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class ProductListActivity : AppCompatActivity() {

    private lateinit var initialMessageTextView: TextView
    private lateinit var listViewProducts: ListView
    private lateinit var btnDeleteInstructions: Button
    private lateinit var sharedPrefs: SharedPreferences

    private val SHARED_PREFS_NAME = "product_prefs"
    private val KEY_SCANNED_PRODUCTS = "scanned_products" // Key for the set of scanned products

    // Define your ALL possible products and their instructions (still needed to retrieve instructions)
    private val allProductInstructions = mapOf(
        "coffee-machine" to "Assembly Instructions for Coffee Machine:\n\n" +
                "1. Unpack all parts.\n" +
                "2. Attach the water reservoir.\n" +
                "3. Insert the filter.\n" +
                "4. Plug in and power on.\n" +
                "5. Brew your first cup!",
        "chair" to "Assembly Instructions for Chair:\n\n" +
                "1. Identify all parts.\n" +
                "2. Attach the legs to the seat.\n" +
                "3. Secure with provided screws.\n" +
                "4. Attach the backrest.\n" +
                "5. Tighten all bolts.",
        "table" to "Assembly Instructions for Table:\n\n" +
                "1. Place the tabletop upside down.\n" +
                "2. Attach the legs to the tabletop.\n" +
                "3. Flip the table upright.\n" +
                "4. Ensure it is stable."
    )

    // List to hold the names of products that have been scanned and will be displayed
    private val displayedProductNames = mutableListOf<String>()
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_list)

        initialMessageTextView = findViewById(R.id.productDetailsTextView)
        listViewProducts = findViewById(R.id.listViewProducts)
        btnDeleteInstructions = findViewById(R.id.btnDeleteInstructions)

        sharedPrefs = getSharedPreferences(SHARED_PREFS_NAME, Context.MODE_PRIVATE)

        // Load the scanned products from SharedPreferences and update the UI
        loadAndDisplayScannedProducts()

        // Set an item click listener for the ListView
        listViewProducts.setOnItemClickListener { parent, view, position, id ->
            val selectedProduct = displayedProductNames[position]
            val instructions = allProductInstructions[selectedProduct] // Get instructions from the full map

            if (instructions != null) {
                val intent = Intent(this, InstructionDetailActivity::class.java).apply {
                    putExtra("instructions", instructions)
                }
                startActivity(intent)
            } else {
                // This case should ideally not happen if product IDs are consistent
                // and the selected product is in the allProductInstructions map.
                initialMessageTextView.text = "Instructions not found for $selectedProduct."
            }
        }

        // Set up the delete button click listener
        btnDeleteInstructions.setOnClickListener {
            // Clear the set of scanned products in SharedPreferences
            with(sharedPrefs.edit()) {
                remove(KEY_SCANNED_PRODUCTS)
                apply()
            }
            // Clear the in-memory list and update the UI
            displayedProductNames.clear()
            updateProductListUI()
        }
    }

    // Function to load scanned products and update the UI
    private fun loadAndDisplayScannedProducts() {
        // Read the set of scanned product IDs from SharedPreferences
        val scannedProductsSet = sharedPrefs.getStringSet(KEY_SCANNED_PRODUCTS, emptySet()) ?: emptySet()

        // Clear the current displayed list
        displayedProductNames.clear()

        // Add only the names of scanned products to the displayed list
        displayedProductNames.addAll(scannedProductsSet)

        // Update the UI
        updateProductListUI()
    }


    // Function to update the ListView and initial message visibility
    private fun updateProductListUI() {
        // Initialize the adapter if not already, otherwise notify of data change
        if (!::adapter.isInitialized) {
            adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, displayedProductNames)
            listViewProducts.adapter = adapter
        } else {
            adapter.notifyDataSetChanged()
        }

        // Show/hide the ListView and initial message based on whether there are products to display
        if (displayedProductNames.isEmpty()) {
            listViewProducts.visibility = android.view.View.GONE
            initialMessageTextView.text = "No scanned products yet. Scan a QR code to add a product."
            initialMessageTextView.visibility = android.view.View.VISIBLE
        } else {
            listViewProducts.visibility = android.view.View.VISIBLE
            initialMessageTextView.visibility = android.view.View.GONE // Hide the initial message
        }
    }
}