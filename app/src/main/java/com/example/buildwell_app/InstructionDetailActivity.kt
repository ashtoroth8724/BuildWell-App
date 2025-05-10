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
}


class InstructionDetailActivity : AppCompatActivity() {

    private lateinit var contentRecyclerView: RecyclerView
    private lateinit var contentAdapter: ProductContentAdapter
    private val xmlProductParser = XmlProductParser() // Instance of the parser


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_instruction_detail)

        contentRecyclerView = findViewById(R.id.contentRecyclerView)
        contentRecyclerView.layoutManager = LinearLayoutManager(this)
        contentAdapter = ProductContentAdapter(lifecycle) // Pass lifecycle to adapter
        contentRecyclerView.adapter = contentAdapter

        // Get the product ID from the intent
        val productId = intent.getStringExtra("productId")

        if (productId != null) {
            // Fetch product data from XML
            val product = getProductFromXml(this, productId)

            if (product != null) {
                // Set the activity title to the product name
                title = product.name
                contentAdapter.submitList(product.instructions) // Submit the list to the adapter
            } else {
                // Handle product not found
                title = "Product Not Found"
                contentAdapter.submitList(listOf(ProductContent.TextInstruction("Product instructions not found for ID: $productId.")))
            }
        } else {
            // Handle invalid product information
            title = "Invalid Product"
            contentAdapter.submitList(listOf(ProductContent.TextInstruction("Invalid product information provided.")))
        }
    }

    // Function to get a specific product from the XML data
    private fun getProductFromXml(context: Context, productId: String): Product? { // Return type now correctly refers to the top-level Product
        try {
            // Open the XML resource file
            val inputStream = context.resources.openRawResource(R.raw.products) // Assuming products.xml is in res/raw
            val products = xmlProductParser.parse(inputStream) // Assuming parse returns List<Product>
            // Find the product with the matching ID
            return products.find { it.id == productId }
        } catch (e: XmlPullParserException) {
            Log.e("InstructionDetail", "Error parsing XML", e)
            // Handle parsing error (e.g., show an error message)
            return null
        } catch (e: IOException) {
            Log.e("InstructionDetail", "Error reading XML file", e)
            // Handle file reading error
            return null
        } catch (e: Exception) {
            Log.e("InstructionDetail", "An unexpected error occurred", e)
            // Handle any other unexpected errors
            return null
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        // The YouTubePlayerViews within the RecyclerView items will be released by the adapter
        // when the RecyclerView or its items are destroyed.
    }
}

// Adapter to handle different content types in the RecyclerView
class ProductContentAdapter(private val lifecycle: Lifecycle) :
    ListAdapter<ProductContent, RecyclerView.ViewHolder>(ProductContentDiffCallback()) { // Now uses the top-level ProductContent

    private val VIEW_TYPE_TEXT = 1
    private val VIEW_TYPE_VIDEO = 2

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ProductContent.TextInstruction -> VIEW_TYPE_TEXT // Now uses the top-level ProductContent
            is ProductContent.YoutubeVideo -> VIEW_TYPE_VIDEO // Now uses the top-level ProductContent
        }
    }

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
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is TextInstructionViewHolder -> holder.bind(getItem(position) as ProductContent.TextInstruction) // Now uses the top-level ProductContent
            is YoutubeVideoViewHolder -> holder.bind(getItem(position) as ProductContent.YoutubeVideo) // Now uses the top-level ProductContent
        }
    }

    // Ensure YouTubePlayerViews are properly released when items are detached
    override fun onViewDetachedFromWindow(holder: RecyclerView.ViewHolder) {
        super.onViewDetachedFromWindow(holder)
        if (holder is YoutubeVideoViewHolder) {
            holder.releasePlayer()
        }
    }

    // ViewHolder for text instructions
    class TextInstructionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        // Assuming you have a TextView with ID instructionTextView in item_text_instruction.xml
        private val instructionTextView: TextView = itemView.findViewById(R.id.instructionTextView)

        fun bind(textInstruction: ProductContent.TextInstruction) { // Now uses the top-level ProductContent
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

                override fun onCurrentSecond(youTubePlayer: YouTubePlayer, second: Float) {
                    // Optional: Implement if you need to track playback progress
                }

                override fun onVideoDuration(youTubePlayer: YouTubePlayer, duration: Float) {
                    // Optional: Implement if you need to know the video duration
                }

                override fun onVideoLoadedFraction(youTubePlayer: YouTubePlayer, loadedFraction: Float) {
                    // Optional: Implement if you need to track loading progress
                }

                override fun onVideoId(youTubePlayer: YouTubePlayer, videoId: String) {
                    // Optional: Called when a video is loaded with its ID
                }


                // ... other AbstractYouTubePlayerListener methods if needed
            })
        }

        fun bind(youtubeVideo: ProductContent.YoutubeVideo) {
            currentVideoId = youtubeVideo.videoId
            // If the player is already ready, cue the video
            youTubePlayer?.cueVideo(currentVideoId!!, 0f) // Use cueVideo to load without autoplaying initially
        }

        fun releasePlayer() {
            youtubePlayerView.release()
        }
    }
    // DiffUtil callback for ProductContent
    class ProductContentDiffCallback : DiffUtil.ItemCallback<ProductContent>() {
        override fun areItemsTheSame(oldItem: ProductContent, newItem: ProductContent): Boolean {
            // Assuming you have a unique identifier for each content item if needed for more complex scenarios.
            // For now, we can compare based on content type and core identifier (text or videoId).
            return when {
                oldItem is ProductContent.TextInstruction && newItem is ProductContent.TextInstruction -> oldItem.text == newItem.text
                oldItem is ProductContent.YoutubeVideo && newItem is ProductContent.YoutubeVideo -> oldItem.videoId == newItem.videoId
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