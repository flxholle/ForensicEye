package com.flxholle.forensiceye.data

import android.content.Context
import android.net.Uri
import android.util.Log
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter

/**
 * Abstract class representing a key-value data source.
 * This class provides functionality to write key-value pairs to a JSON file.
 *
 * @param context The context of the application.
 * @param enabled A flag indicating whether the data source is enabled.
 */
abstract class KeyValueDataSource(context: Context, enabled: Boolean = true) :
    DataSource(context, enabled) {

    /**
     * Writes the content map to a file in JSON format.
     *
     * @return True if the file was written successfully, false otherwise.
     */
    override fun writeToFileInternal(): Boolean {
        val content = getContentMap()
        if (content.isEmpty()) {
            Log.e("KeyValueWriter", "No content to write/error for ${getFilename()}")
            return false
        }

        val json = mapToJson(content)

        val externalFile = File(context.getExternalFilesDir(null), getFilename())
        val fileUri = Uri.fromFile(externalFile)
        try {
            context.contentResolver.openOutputStream(fileUri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    writer.append(json.toString(4))
                }
            }
            Log.d("KeyValueWriter", "File created successfully at $fileUri")
        } catch (e: IOException) {
            Log.e("KeyValueWriter", "Error writing file at $fileUri", e)
            return false
        }
        return true
    }

    /**
     * Abstract method to get the content map to be written to the file.
     *
     * @return A map containing the key-value pairs.
     */
    abstract fun getContentMap(): Map<String, Any>

    /**
     * Abstract method to get the filename for the file to be written.
     *
     * @return The filename as a string.
     */
    abstract fun getFilename(): String

    /**
     * Converts a map to a JSON object.
     *
     * @param map The map to be converted.
     * @return The resulting JSON object.
     */
    private fun mapToJson(map: Map<String, Any?>): JSONObject {
        val jsonObject = JSONObject()

        for ((key, value) in map) {
            when (value) {
                is Map<*, *> -> jsonObject.put(
                    key,
                    mapToJson(value as Map<String, Any?>)
                ) // Recursively convert maps
                is List<*> -> jsonObject.put(
                    key,
                    listToJsonArray(value)
                ) // Convert lists to JSON arrays
                is Array<*> -> jsonObject.put(
                    key,
                    listToJsonArray(value.toList())
                ) // Convert arrays to JSON arrays
                else -> jsonObject.put(key, value?.toString() ?: "null")
            }
        }

        return jsonObject
    }

    /**
     * Converts a list to a JSON array.
     *
     * @param list The list to be converted.
     * @return The resulting JSON array.
     */
    private fun listToJsonArray(list: List<*>): JSONArray {
        val jsonArray = JSONArray()

        for (item in list) {
            when (item) {
                is Map<*, *> -> jsonArray.put(mapToJson(item as Map<String, Any>)) // Recursively convert maps
                is List<*> -> jsonArray.put(listToJsonArray(item)) // Recursively convert nested lists
                is Array<*> -> jsonArray.put(listToJsonArray(item.toList())) // Recursively convert nested arrays
                else -> jsonArray.put(item?.toString() ?: "null")
            }
        }

        return jsonArray
    }
}