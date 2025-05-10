package com.example.buildwell_app

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.ui.semantics.text
import androidx.lifecycle.Lifecycle
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.YouTubePlayer
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.listeners.AbstractYouTubePlayerListener
import com.pierfrancescosoffritti.androidyoutubeplayer.core.player.views.YouTubePlayerView
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException

// Define a data class to hold product information - MOVED HERE
data class Product(
    val id: String,
    val name: String,
    val instructions: List<ProductContent>
)

// Define a sealed class for content types - Keep this at the top level too
sealed class ProductContent {
    data class TextInstruction(val text: String) : ProductContent()
    data class YoutubeVideo(val videoId: String) : ProductContent()
    data class TitleInstruction(val title: String) : ProductContent()
}


class InstructionDetailActivity : AppCompatActivity() {

    private lateinit var contentRecyclerView: RecyclerView
    private lateinit var contentAdapter: ProductContentAdapter
    private lateinit var instructionTitleTextView: TextView // Assuming you are using a custom title TextView
    private val xmlProductParser = XmlProductParser()

    private val TAG = "InstructionDetail" // Define a TAG for logging

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instruction_detail)

        // Assuming you are using a custom title TextView
        instructionTitleTextView = findViewById(R.id.instructionTitleTextView)

        contentRecyclerView = findViewById(R.id.contentRecyclerView)
        contentRecyclerView.layoutManager = LinearLayoutManager(this)
        contentAdapter = ProductContentAdapter(lifecycle)
        contentRecyclerView.adapter = contentAdapter

        // Get the product ID from the intent
        val productId = intent.getStringExtra("productId")

        Log.d(TAG, "Received product ID from Intent: $productId") // Log the received ID

        if (productId != null) {
            // Fetch product data from XML
            val product = getProductFromXml(this, productId)

            if (product != null) {
                Log.d(TAG, "Product found: ${product.name}") // Log if product is found
                // Set the text of the custom title TextView
                instructionTitleTextView.text = product.name
                contentAdapter.submitList(product.instructions)
            } else {
                Log.w(TAG, "Product not found for ID: $productId") // Log if product is NOT found
                // Handle product not found - set a default text in the TextView
                instructionTitleTextView.text = "Product Not Found"
                contentAdapter.submitList(listOf(ProductContent.TextInstruction("Product instructions not found for ID: $productId.")))
            }
        } else {
            Log.e(TAG, "Invalid product ID received from Intent.") // Log if ID is null
            // Handle invalid product information - set a default text in the TextView
            instructionTitleTextView.text = "Invalid Product"
            contentAdapter.submitList(listOf(ProductContent.TextInstruction("Invalid product information provided.")))
        }
    }

    private fun getProductFromXml(context: Context, productId: String): Product? {
        Log.d(TAG, "Attempting to fetch product from XML with ID: $productId") // Log the start of fetching
        return try {
            val inputStream = context.resources.openRawResource(R.raw.products)
            Log.d(TAG, "Successfully opened products.xml raw resource.") // Log resource opening success

            val products = xmlProductParser.parse(inputStream)
            Log.d(TAG, "Finished parsing XML. Found ${products.size} products.") // Log after parsing

            val foundProduct = products.find { it.id == productId }
            if (foundProduct != null) {
                Log.d(TAG, "Found product in parsed list with matching ID: ${foundProduct.id}") // Log if product is found in the list
            } else {
                Log.w(TAG, "Did not find product with ID '$productId' in the parsed list.") // Log if product is not found in the list
                // Optional: Log the IDs of the products that were found to see what's available
                Log.d(TAG, "Available product IDs in XML: ${products.map { it.id }}")
            }
            foundProduct

        } catch (e: XmlPullParserException) {
            Log.e(TAG, "XML Parsing Error: ${e.message}", e) // Log parsing errors
            null
        } catch (e: IOException) {
            Log.e(TAG, "IO Error reading XML file: ${e.message}", e) // Log file reading errors
            null
        } catch (e: Exception) {
            Log.e(TAG, "An unexpected error occurred during XML fetching: ${e.message}", e) // Log other errors
            null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // ... (your existing onDestroy)
    }

}

// Adapter to handle different content types in the RecyclerView
// Adapter to handle different content types in the RecyclerView
class ProductContentAdapter(private val lifecycle: Lifecycle) :
    ListAdapter<ProductContent, RecyclerView.ViewHolder>(ProductContentDiffCallback()) {

    // Define unique view types for each content type
    private val VIEW_TYPE_TEXT = 1
    private val VIEW_TYPE_VIDEO = 2
    private val VIEW_TYPE_TITLE = 3 // New view type for titles


    // Determine the view type based on the item's class
    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ProductContent.TextInstruction -> VIEW_TYPE_TEXT
            is ProductContent.YoutubeVideo -> VIEW_TYPE_VIDEO
            is ProductContent.TitleInstruction -> VIEW_TYPE_TITLE // Return the new view type
            // Add cases for other content types if you have them (e.g., ImageInstruction)
            // is ProductContent.ImageInstruction -> VIEW_TYPE_IMAGE
        }
    }

    // Create the appropriate ViewHolder based on the view type
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            VIEW_TYPE_TEXT -> {
                val view = inflater.inflate(R.layout.item_text_instruction, parent, false)
                TextInstructionViewHolder(view)
            }
            VIEW_TYPE_VIDEO -> {
                val view = inflater.inflate(R.layout.item_youtube_video, parent, false)
                YoutubeVideoViewHolder(view, lifecycle) // Pass lifecycle to the ViewHolder
            }
            VIEW_TYPE_TITLE -> { // Handle the new title view type
                val view = inflater.inflate(R.layout.item_text_title, parent, false) // Inflate the new layout
                TitleInstructionViewHolder(view) // Create the new TitleInstructionViewHolder
            }
            // Add cases for other content types (e.g., ImageInstruction)
            // VIEW_TYPE_IMAGE -> {
            //     val view = inflater.inflate(R.layout.item_image_instruction, parent, false)
            //     ImageInstructionViewHolder(view)
            // }
            else -> throw IllegalArgumentException("Invalid view type: $viewType")
        }
    }

    // Bind the data to the correct ViewHolder
    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TextInstructionViewHolder -> holder.bind(getItem(position) as ProductContent.TextInstruction)
            is YoutubeVideoViewHolder -> holder.bind(getItem(position) as ProductContent.YoutubeVideo)
            is TitleInstructionViewHolder -> holder.bind(getItem(position) as ProductContent.TitleInstruction) // Bind data for the new title type
            // Add cases for other content types
            // is ImageInstructionViewHolder -> holder.bind(getItem(position) as ProductContent.ImageInstruction)
        }
    }

    // Ensure YouTubePlayerViews are properly released when items are detached
    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        if (holder is YoutubeVideoViewHolder) {
            holder.releasePlayer()
        }
    }

    // Ensure YouTubePlayerViews are properly released when items are recycled
    override fun onViewRecycled(holder: RecyclerView.ViewHolder) {
        super.onViewRecycled(holder)
        if (holder is YoutubeVideoViewHolder) {
            // Optional: Reset the player or state if needed upon recycling
            // holder.resetPlayerState() // You might need to add this method to YoutubeVideoViewHolder
        }
    }


    // ViewHolder for text instructions
    class TextInstructionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Assuming you have a TextView with ID instructionTextView in item_text_instruction.xml
        private val instructionTextView: TextView = itemView.findViewById(R.id.instructionTextView)

        fun bind(textInstruction: ProductContent.TextInstruction) {
            instructionTextView.text = textInstruction.text
        }
    }

    // ViewHolder for YouTube videos
    class YoutubeVideoViewHolder(itemView: View, private val lifecycle: Lifecycle) : RecyclerView.ViewHolder(itemView) {
        // Assuming you have a YouTubePlayerView with ID youtube_player_view_item in item_youtube_video.xml
        private val youtubePlayerView: YouTubePlayerView = itemView.findViewById(R.id.youtube_player_view_item)
        private var currentVideoId: String? = null
        private var youTubePlayer: YouTubePlayer? = null

        init {
            // Add observer to the YouTubePlayerView's lifecycle
            lifecycle.addObserver(youtubePlayerView)

            // Initialize the YouTube player
            youtubePlayerView.addYouTubePlayerListener(object : AbstractYouTubePlayerListener() {
                override fun onReady(player: YouTubePlayer) {
                    youTubePlayer = player
                    currentVideoId?.let { videoId ->
                        player.cueVideo(videoId, 0f) // or loadVideo(videoId, 0f) for autoplay
                    }
                }

                // ... other AbstractYouTubePlayerListener methods you might need
                // (onCurrentSecond, onVideoDuration, onVideoLoadedFraction, onVideoId, etc.)
            })
        }

        fun bind(youtubeVideo: ProductContent.YoutubeVideo) {
            currentVideoId = youtubeVideo.videoId
            // If the player is already ready, cue the video
            // Check if youTubePlayer is not null before calling cueVideo
            if (youTubePlayer != null && currentVideoId != null) {
                youTubePlayer?.cueVideo(currentVideoId!!, 0f) // Use cueVideo to load without autoplaying initially
            }
        }

        fun releasePlayer() {
            youtubePlayerView.release()
        }
    }

    // New ViewHolder for the title
    class TitleInstructionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Assuming you have a TextView with ID titleTextView in item_text_title.xml
        private val titleTextView: TextView = itemView.findViewById(R.id.titleTextView)

        fun bind(titleInstruction: ProductContent.TitleInstruction) {
            titleTextView.text = titleInstruction.title
        }
    }

    // DiffUtil callback for ProductContent
    class ProductContentDiffCallback : DiffUtil.ItemCallback<ProductContent>() {
        override fun areItemsTheSame(oldItem: ProductContent, newItem: ProductContent): Boolean {
            // Compare items based on their unique properties (if they had them)
            // For simplicity, we can compare based on type and core content for now.
            return when {
                oldItem is ProductContent.TextInstruction && newItem is ProductContent.TextInstruction -> oldItem.text == newItem.text
                oldItem is ProductContent.YoutubeVideo && newItem is ProductContent.YoutubeVideo -> oldItem.videoId == newItem.videoId
                oldItem is ProductContent.TitleInstruction && newItem is ProductContent.TitleInstruction -> oldItem.title == newItem.title // Compare titles
                // Add cases for other content types
                // oldItem is ProductContent.ImageInstruction && newItem is ProductContent.ImageInstruction -> oldItem.imageUrl == newItem.imageUrl
                else -> false // Different types are never the same item
            }
        }

        @SuppressLint("DiffUtilEquals")
        override fun areContentsTheSame(oldItem: ProductContent, newItem: ProductContent): Boolean {
            // For data classes, the default equals implementation checks all properties.
            // This is sufficient for our ProductContent data classes.
            return oldItem == newItem
        }
    }
}