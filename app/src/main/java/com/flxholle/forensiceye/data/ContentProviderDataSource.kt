package com.flxholle.forensiceye.data

import android.annotation.SuppressLint
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.net.Uri
import android.util.Log
import java.io.File
import java.io.IOException
import java.io.OutputStreamWriter

/**
 * Abstract class representing a data source that interacts with a content provider.
 *
 * @param context The context to use for accessing the content provider.
 * @param enabled A flag indicating whether the data source is enabled.
 */
abstract class ContentProviderDataSource(context: Context, enabled: Boolean = true) :
    DataSource(context, enabled) {

    /**
     * Writes data from the content provider to a file.
     *
     * @return True if the data was written successfully, false otherwise.
     */
    override fun writeToFileInternal(): Boolean {
        var ret = true
        for (uri in getURIs()) {
            val r = queryAndWriteToCsv(context, uri)
            ret = ret && r
        }
        return ret
    }

    /**
     * Abstract method to get the URIs of the content providers to query.
     *
     * @return A list of URIs.
     */
    abstract fun getURIs(): List<Uri>

    /**
     * Queries a content provider and writes the result to a CSV file.
     *
     * @param contentResolver The ContentResolver to query.
     * @param fileUri The Uri of the file where the CSV data will be written.
     * @param uri The Uri of the content provider to query.
     * @param projection The list of columns to return. Passing null will return all columns.
     * @param selection A filter declaring which rows to return, formatted as an SQL WHERE clause (excluding the WHERE itself). Passing null will return all rows.
     * @param selectionArgs You may include ?s in selection, which will be replaced by the values from selectionArgs, in the order that they appear in the selection.
     * @param sortOrder How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself). Passing null will use the default sort order.
     * @return True if the CSV file was created successfully, false otherwise.
     */
    @SuppressLint("Range") // getColumnIndex returns -1 if the column is not found
    private fun queryAndWriteToCsv(
        contentResolver: ContentResolver,
        fileUri: Uri,
        uri: Uri,
        projection: Array<String>? = null,
        selection: String? = null,
        selectionArgs: Array<String>? = null,
        sortOrder: String? = null
    ): Boolean {

        val cursor: Cursor
        try {
            val cursorTmp: Cursor? =
                contentResolver.query(uri, projection, selection, selectionArgs, sortOrder)
            if (cursorTmp == null) {
                Log.e("CSVWriter", "Return of null querying content provider $uri")
                return false
            } else {
                cursor = cursorTmp
            }
        } catch (e: Exception) {
            Log.e("CSVWriter", "Exception querying content provider $uri", e)
            return false
        }

        Log.d("CSVWriter", "Query successful at content provider $uri")

        var retValue = true

        try {
            contentResolver.openOutputStream(fileUri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    // Write the header
                    val columns =
                        cursor.columnNames?.toList() ?: (projection?.toList() ?: emptyList())
                    if (columns.isEmpty()) {
                        throw Exception("Columns are empty")
                    }
                    val header = columns.joinToString(",")
                    writer.append(header).append("\n")

                    // Write the data
                    cursor.use {
                        while (it.moveToNext()) {
                            val row = columns.joinToString(",") { column ->
                                it.getString(it.getColumnIndex(column)) ?: ""
                            }
                            writer.append(row).append("\n")
                        }
                    }
                }
            }
            Log.d("CSVWriter", "CSV file created successfully at $fileUri")
        } catch (e: IOException) {
            retValue = false
            Log.e("CSVWriter", "Error writing CSV file at $fileUri", e)
        } finally {
            cursor.close()
        }
        return retValue
    }

    /**
     * Queries a content provider and writes the result to a CSV file in the external files directory.
     * The filename is generated based on the content provider's authority and path.
     *
     * @param context The context to use for accessing the external files directory.
     * @param uri The Uri of the content provider to query.
     * @param projection The list of columns to return. Passing null will return all columns.
     * @param selection A filter declaring which rows to return, formatted as an SQL WHERE clause (excluding the WHERE itself). Passing null will return all rows.
     * @param selectionArgs You may include ?s in selection, which will be replaced by the values from selectionArgs, in the order that they appear in the selection.
     * @param sortOrder How to order the rows, formatted as an SQL ORDER BY clause (excluding the ORDER BY itself). Passing null will use the default sort order.
     * @return True if the CSV file was created successfully, false otherwise.
     */
    private fun queryAndWriteToCsv(
        context: Context,
        uri: Uri,
        projection: Array<String>? = null,
        selection: String? = null,
        selectionArgs: Array<String>? = null,
        sortOrder: String? = null
    ): Boolean {
        val authority = uri.authority?.replace(".", "_") ?: "unknown"
        val path = uri.pathSegments.joinToString(separator = "_")
        val filename = authority + "_" + path + ".csv"

        val externalFile = File(context.getExternalFilesDir(null), filename)

        return queryAndWriteToCsv(
            context.contentResolver,
            Uri.fromFile(externalFile),
            uri,
            projection,
            selection,
            selectionArgs,
            sortOrder
        )
    }
}