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
        val id = parser.getAttributeValue(null, "id") ?: ""
        var name = ""
        var instructions: List<ProductContent> = emptyList()

        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) {
                continue
            }
            when (parser.name) {
                "name" -> name = readTag(parser, "name")
                "instructions" -> instructions = readInstructions(parser)
                else -> skip(parser)
            }
        }
        return Product(id, name, instructions)
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readInstructions(parser: XmlPullParser): List<ProductContent> {
        val instructions = mutableListOf<ProductContent>()
        parser.require(XmlPullParser.START_TAG, ns, "instructions")
        while (parser.next() != XmlPullParser.END_TAG) {
            if (parser.eventType != XmlPullParser.START_TAG) continue
            if (parser.name == "step") {
                val type = parser.getAttributeValue(null, "type")
                val videoId = parser.getAttributeValue(null, "videoId") // Optional
                val text = readText(parser)

                when (type) {
                    "text" -> instructions.add(ProductContent.TextInstruction(text))
                    "title" -> instructions.add(ProductContent.TitleInstruction(text))
                    "video" -> {
                        if (videoId != null) {
                            instructions.add(ProductContent.YoutubeVideo(videoId))
                        }
                    }
                    else -> skip(parser)
                }
            } else {
                skip(parser)
            }
        }
        return instructions
    }

    @Throws(XmlPullParserException::class, IOException::class)
    private fun readTag(parser: XmlPullParser, tagName: String): String {
        parser.require(XmlPullParser.START_TAG, ns, tagName)
        val result = readText(parser)
        parser.require(XmlPullParser.END_TAG, ns, tagName)
        return result
    }

    @Throws(IOException::class, XmlPullParserException::class)
    private fun readText(parser: XmlPullParser): String {
        var result = ""
        if (parser.next() == XmlPullParser.TEXT) {
            result = parser.text
            parser.nextTag()
        }
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