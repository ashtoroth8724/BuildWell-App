package com.example.buildwell_app

import android.content.Context
import android.util.Xml
import androidx.compose.foundation.layout.add
import androidx.compose.ui.semantics.text
import org.xmlpull.v1.XmlPullParser
import org.xmlpull.v1.XmlPullParserException
import java.io.IOException
import java.io.InputStream

class XmlProductParser {

    private val ns: String? = null // We don't use namespaces

    @Throws(XmlPullParserException::class, IOException::class)
    fun parse(inputStream: InputStream): List<Product> {
        inputStream.use { inputStream ->
            val parser: XmlPullParser = Xml.newPullParser()
            parser.setFeature(XmlPullParser.FEATURE_PROCESS_NAMESPACES, false)
            parser.setInput(inputStream, null)
            parser.nextTag()
            return readProducts(parser)
        }
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readProducts(parser: XmlPullParser): List<Product> {
        val products = mutableListOf<Product>()

        parser.require(XmlPullParser.START_TAG, ns, "products")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            // Starts by looking for the entry tag
            if (parser.name == "product") {
                products.add(readProduct(parser))
            } else {
                skip(parser)
            }
        }
        return products
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readProduct(parser: XmlPullParser): Product {
        parser.require(XmlPullParser.START_TAG, ns, "product")
        var productId: String = ""
        var productName: String = ""
        var instructions: List<ProductContent> = emptyList()

        productId = parser.getAttributeValue(ns, "id")

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "name" -> productName = readText(parser, "name")
                "instructions" -> instructions = readInstructions(parser)
                else -> skip(parser)
            }
        }
        // Return a new Product object including the instructions list
        return Product(productId, productName, instructions)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readInstructions(parser: XmlPullParser): List<ProductContent> {
        val instructions = mutableListOf<ProductContent>()

        parser.require(XmlPullParser.START_TAG, ns, "instructions")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "step" -> instructions.add(readStep(parser))
                else -> skip(parser)
            }
        }
        return instructions
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readStep(parser: XmlPullParser): ProductContent {
        parser.require(XmlPullParser.START_TAG, ns, "step")
        val stepType = parser.getAttributeValue(ns, "type")
        var productContent: ProductContent? = null

        when (stepType) {
            "text" -> {
                val text = readText(parser, "step")
                productContent = ProductContent.TextInstruction(text)
            }
            "video" -> {
                val videoId = parser.getAttributeValue(ns, "videoId")
                // Optionally read the text content for a video title
                val videoTitle = readText(parser, "step") // Read the text within the step tag
                productContent = ProductContent.YoutubeVideo(videoId) // We'll stick to videoId for now
            }
            else -> skip(parser) // Skip unknown step types
        }

        parser.require(XmlPullParser.END_TAG, ns, "step")
        return productContent ?: throw XmlPullParserException("Invalid step type or missing content")
    }


    @Throws(IOException::class, XmlPullParserException::class)
    private fun readText(parser: XmlPullParser, tagName: String): String {
        parser.require(XmlPullParser.START_TAG, ns, tagName)
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
        parser.require(XmlPullParser.END_TAG, ns, tagName)
        return result
    }


    @Throws(XmlPullParserException::class, IOException::class)
    private fun skip(parser: XmlPullParser) {
        if (parser.eventType != XmlPullParser.START_TAG) {
            throw IllegalStateException()
        }
        var depth = 1
        while (depth != 0) {
            when (parser.next()) {
                XmlPullParser.END_TAG -> depth--
                XmlPullParser.START_TAG -> depth++
            }
        }
    }
}