package com.example.trackinggym.utils

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.trackinggym.data.entities.Exercise
import com.example.trackinggym.data.entities.ExerciseLog
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object ImageExportUtil {

    fun shareRoutineImage(
        context: Context,
        routineName: String,
        dateMs: Long,
        data: Map<String, List<Pair<Exercise, ExerciseLog>>>
    ) {
        val bitmap = createBitmapFromData(routineName, dateMs, data)
        val uri = saveBitmapToCache(context, bitmap)
        if (uri != null) {
            shareImageUri(context, uri)
        }
    }

    private fun createBitmapFromData(
        routineName: String,
        dateMs: Long,
        data: Map<String, List<Pair<Exercise, ExerciseLog>>>
    ): Bitmap {
        // Calculate required height based on content
        var currentY = 100f
        val paint = Paint().apply {
            color = Color.BLACK
            textSize = 40f
            isAntiAlias = true
        }
        val titlePaint = Paint(paint).apply {
            textSize = 60f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val categoryPaint = Paint(paint).apply {
            textSize = 50f
            color = Color.DKGRAY
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        val setPaint = Paint(paint).apply {
            textSize = 35f
            color = Color.DKGRAY
        }

        val paddingX = 40f
        
        val dateStr = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date(dateMs))

        // Pre-calculate height
        var totalHeight = currentY + 80f // Title + Date + spacing
        
        data.forEach { (category, list) ->
            totalHeight += 80f // Category header
            list.forEach { (exercise, log) ->
                totalHeight += 60f // Exercise name
                log.sets.forEach { _ ->
                    totalHeight += 50f // Set line
                }
                totalHeight += 30f // Spacing after exercise
            }
            totalHeight += 40f // Spacing after category
        }
        
        totalHeight += 100f // Bottom padding

        val width = 1080
        val bitmap = Bitmap.createBitmap(width, totalHeight.toInt(), Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // Draw background
        canvas.drawColor(Color.WHITE)

        // Draw Title
        canvas.drawText("TrackingGym: $routineName", paddingX, currentY, titlePaint)
        currentY += 60f
        canvas.drawText(dateStr, paddingX, currentY, paint)
        currentY += 60f
        
        // Draw line separator
        paint.strokeWidth = 4f
        canvas.drawLine(paddingX, currentY, width - paddingX, currentY, paint)
        currentY += 40f

        data.forEach { (category, list) ->
            // Draw category
            canvas.drawText(category.uppercase(Locale.getDefault()), paddingX, currentY, categoryPaint)
            currentY += 20f
            canvas.drawLine(paddingX, currentY, width - paddingX, currentY, categoryPaint)
            currentY += 60f

            list.forEach { (exercise, log) ->
                canvas.drawText(exercise.name, paddingX + 20f, currentY, paint)
                currentY += 50f
                
                log.sets.forEach { setRecord ->
                    val rirText = if (setRecord.rir != null) " (RIR: ${setRecord.rir})" else ""
                    val setText = "Serie ${setRecord.setNumber}: ${setRecord.reps} x ${setRecord.weight} kg$rirText"
                    canvas.drawText(setText, paddingX + 60f, currentY, setPaint)
                    currentY += 50f
                }
                currentY += 30f
            }
            currentY += 40f
        }

        return bitmap
    }

    private fun saveBitmapToCache(context: Context, bitmap: Bitmap): Uri? {
        return try {
            val cachePath = File(context.cacheDir, "images")
            cachePath.mkdirs() // Create directory if needed
            val file = File(cachePath, "routine_export.png")
            val stream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
            stream.close()
            
            FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun shareImageUri(context: Context, uri: Uri) {
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "image/png"
            putExtra(Intent.EXTRA_STREAM, uri)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Compartir rutina"))
    }
}
