package com.example.buildwell_app

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

class ProductListActivity : AppCompatActivity() {

    private lateinit var productRecyclerView: RecyclerView
    private lateinit var productAdapter: ProductAdapter
    private lateinit var clearButton: Button
    private lateinit var noProductsFoundTextView: TextView // Declare the TextView
    private lateinit var sharedPrefs: SharedPreferences
    private val xmlProductParser = XmlProductParser()

    private val KEY_SCANNED_PRODUCTS = "scanned_products"
    private val TAG = "ProductListActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_product_list)

        sharedPrefs = getSharedPreferences("AppPreferences", Context.MODE_PRIVATE)

        productRecyclerView = findViewById(R.id.productListRecyclerView)
        productRecyclerView.layoutManager = LinearLayoutManager(this)

        clearButton = findViewById(R.id.clearScannedProductsButton)
        noProductsFoundTextView = findViewById(R.id.noProductsFoundTextView) // Find the TextView

        clearButton.setOnClickListener {
            clearScannedProducts()
        }

        loadScannedProducts()
    }

    private fun loadScannedProducts() {
        val scannedProductIds = sharedPrefs.getStringSet(KEY_SCANNED_PRODUCTS, emptySet()) ?: emptySet()
        val allProducts = getAllProductsFromXml(this)

        val scannedProducts = allProducts.filter { product ->
            scannedProductIds.contains(product.id)
        }

        // Update the adapter with the filtered list
        productAdapter = ProductAdapter(scannedProducts) { product ->
            val intent = Intent(this, InstructionDetailActivity::class.java)
            intent.putExtra("productId", product.id)
            startActivity(intent)
        }
        productRecyclerView.adapter = productAdapter

        // --- Logic to show/hide the TextView and RecyclerView ---
        if (scannedProducts.isEmpty()) {
            noProductsFoundTextView.visibility = View.VISIBLE
            productRecyclerView.visibility = View.GONE
            clearButton.isEnabled = false // Optionally disable the clear button if nothing to clear
        } else {
            noProductsFoundTextView.visibility = View.GONE
            productRecyclerView.visibility = View.VISIBLE
            clearButton.isEnabled = true // Enable the clear button if there are products
        }
        // --- End of logic ---
    }

    private fun clearScannedProducts() {
        val editor = sharedPrefs.edit()
        editor.remove(KEY_SCANNED_PRODUCTS)
        editor.apply()

        // Reload the product list to show an empty list and the "no products" message
        loadScannedProducts()
    }

    private fun getAllProductsFromXml(context: Context): List<Product> {
        return try {
            val inputStream = context.resources.openRawResource(R.raw.products)
            xmlProductParser.parse(inputStream)
        } catch (e: XmlPullParserException) {
            Log.e(TAG, "Error parsing XML", e)
            emptyList()
        } catch (e: IOException) {
            Log.e(TAG, "Error reading XML file", e)
            emptyList()
        } catch (e: Exception) {
            Log.e(TAG, "An unexpected error occurred", e)
            emptyList()
        }
    }

    // Simple data class to represent a product (ensure this is available)
    // data class Product(val id: String, val name: String, val instructions: List<ProductContent>)

    // Adapter for the RecyclerView (ensure this and its ViewHolder are available)
    class ProductAdapter(private val productList: List<Product>, private val onItemClick: (Product) -> Unit) :
        RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_product, parent, false)
            return ProductViewHolder(view, onItemClick)
        }

        override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
            val product = productList[position]
            holder.bind(product)
        }

        override fun getItemCount(): Int = productList.size

        class ProductViewHolder(itemView: View, private val onItemClick: (Product) -> Unit) :
            RecyclerView.ViewHolder(itemView) {

            private val productNameTextView: TextView = itemView.findViewById(R.id.productNameTextView)

            fun bind(product: Product) {
                productNameTextView.text = product.name
                itemView.setOnClickListener { onItemClick(product) }
            }
        }
    }
}