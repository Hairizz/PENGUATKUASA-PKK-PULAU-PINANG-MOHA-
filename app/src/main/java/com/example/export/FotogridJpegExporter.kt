package com.example.export

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Typeface
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.example.data.model.OperationEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.max

data class ExportResult(
    val file: File,
    val contentUri: Uri,
    val width: Int,
    val height: Int
)

object FotogridJpegExporter {

    suspend fun generateAndSaveJpeg(
        context: Context,
        operation: OperationEntity
    ): ExportResult = withContext(Dispatchers.IO) {
        val canvasSize = 1200
        val bitmap = Bitmap.createBitmap(canvasSize, canvasSize, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)

        // Background
        canvas.drawColor(Color.WHITE)

        val headerHeight = 110f
        val footerHeight = 45f
        val gridTop = headerHeight
        val gridBottom = canvasSize - footerHeight

        // Draw Header
        drawHeader(canvas, canvasSize.toFloat(), headerHeight)

        // Draw Footer
        drawFooter(canvas, canvasSize.toFloat(), gridBottom, footerHeight)

        // 4 Grid layout coordinates
        val cellMargin = 6f
        val halfW = canvasSize / 2f
        val halfH = (gridTop + gridBottom) / 2f

        val rectGrid1 = RectF(cellMargin, gridTop + cellMargin, halfW - cellMargin, halfH - cellMargin)
        val rectGrid2 = RectF(halfW + cellMargin, gridTop + cellMargin, canvasSize - cellMargin, halfH - cellMargin)
        val rectGrid3 = RectF(cellMargin, halfH + cellMargin, halfW - cellMargin, gridBottom - cellMargin)
        val rectGrid4 = RectF(halfW + cellMargin, halfH + cellMargin, canvasSize - cellMargin, gridBottom - cellMargin)

        // Grid 1: Operation Details
        drawGrid1OperationDetails(canvas, rectGrid1, operation)

        // Grid 2: Photo 1
        drawPhotoGrid(
            context = context,
            canvas = canvas,
            bounds = rectGrid2,
            imageUriString = operation.imageUri1,
            label = operation.imageLabel1.ifBlank { "GRID 2: SASARAN / PREMIS" },
            slotNumber = 2
        )

        // Grid 3: Photo 2
        drawPhotoGrid(
            context = context,
            canvas = canvas,
            bounds = rectGrid3,
            imageUriString = operation.imageUri2,
            label = operation.imageLabel2.ifBlank { "GRID 3: PEMERIKSAAN / SASARAN" },
            slotNumber = 3
        )

        // Grid 4: Photo 3
        drawPhotoGrid(
            context = context,
            canvas = canvas,
            bounds = rectGrid4,
            imageUriString = operation.imageUri3,
            label = operation.imageLabel3.ifBlank { "GRID 4: EKSIBIT / BARANG RAMPASAN" },
            slotNumber = 4
        )

        // Draw separating lines
        val dividerPaint = Paint().apply {
            color = Color.parseColor("#102A54")
            strokeWidth = 4f
            style = Paint.Style.STROKE
        }
        canvas.drawLine(halfW, gridTop, halfW, gridBottom, dividerPaint)
        canvas.drawLine(0f, halfH, canvasSize.toFloat(), halfH, dividerPaint)

        // Save Bitmap as JPEG
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val sanitizedTitle = operation.title.replace(Regex("[^a-zA-Z0-9_]"), "_").take(25)
        val fileName = "FOTOGRID_${sanitizedTitle}_$timestamp.jpg"

        val picturesDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            ?: File(context.filesDir, "Pictures").apply { mkdirs() }
        val outputFile = File(picturesDir, fileName)

        FileOutputStream(outputFile).use { fos ->
            bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos)
            fos.flush()
        }

        // Also register in MediaStore if possible so user can see it in Gallery
        try {
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/KDNOps")
                    put(MediaStore.Images.Media.IS_PENDING, 1)
                }
            }
            val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) {
                context.contentResolver.openOutputStream(uri)?.use { os ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 95, os)
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    values.clear()
                    values.put(MediaStore.Images.Media.IS_PENDING, 0)
                    context.contentResolver.update(uri, values, null, null)
                }
            }
        } catch (_: Exception) {
            // Ignore if permission or scope restricts MediaStore insert, primary file is safe
        }

        val contentUri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            outputFile
        )

        ExportResult(
            file = outputFile,
            contentUri = contentUri,
            width = canvasSize,
            height = canvasSize
        )
    }

    private fun drawHeader(canvas: Canvas, width: Float, height: Float) {
        val bgPaint = Paint().apply {
            color = Color.parseColor("#0A192F")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, 0f, width, height, bgPaint)

        // Gold Accent Bar
        val goldPaint = Paint().apply {
            color = Color.parseColor("#D4AF37")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, height - 6f, width, height, goldPaint)

        // Title text
        val titlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFD700")
            textSize = 34f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("KEMENTERIAN DALAM NEGERI MALAYSIA", width / 2f, 44f, titlePaint)

        // Subtitle text
        val subPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 21f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("BAHAGIAN PENGUATKUASAAN & KAWALAN • FOTOGRID OPERASI", width / 2f, 78f, subPaint)

        val smallBadgePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#94A3B8")
            textSize = 15f
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("DOKUMEN RASMI TINDAKAN PENGUATKUASAAN", width / 2f, 98f, smallBadgePaint)
    }

    private fun drawFooter(canvas: Canvas, width: Float, top: Float, height: Float) {
        val bgPaint = Paint().apply {
            color = Color.parseColor("#09182E")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, top, width, top + height, bgPaint)

        val goldBar = Paint().apply {
            color = Color.parseColor("#D4AF37")
            style = Paint.Style.FILL
        }
        canvas.drawRect(0f, top, width, top + 4f, goldBar)

        val nowFormatted = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault()).format(Date())
        val footerPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#E2E8F0")
            textSize = 17f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(
            "DIJANA MELALUI SISTEM KDN OPS FOTOGRID • $nowFormatted • SULIT & RASMI",
            width / 2f,
            top + 28f,
            footerPaint
        )
    }

    private fun drawGrid1OperationDetails(canvas: Canvas, bounds: RectF, op: OperationEntity) {
        // Background for details
        val bgPaint = Paint().apply {
            color = Color.parseColor("#F8FAFC")
            style = Paint.Style.FILL
        }
        canvas.drawRect(bounds, bgPaint)

        // Border
        val borderPaint = Paint().apply {
            color = Color.parseColor("#CBD5E1")
            strokeWidth = 2f
            style = Paint.Style.STROKE
        }
        canvas.drawRect(bounds, borderPaint)

        // Header Tab
        val tabPaint = Paint().apply {
            color = Color.parseColor("#102A54")
            style = Paint.Style.FILL
        }
        val tabHeight = 44f
        canvas.drawRect(bounds.left, bounds.top, bounds.right, bounds.top + tabHeight, tabPaint)

        val tabTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#FFD700")
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText("GRID 1: BUTIR-BUTIR OPERASI", bounds.left + 16f, bounds.top + 29f, tabTextPaint)

        // Information fields
        var curY = bounds.top + tabHeight + 28f
        val lineSpacing = 32f

        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#475569")
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val valPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 19f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        }

        val boldValPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#0F172A")
            textSize = 20f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }

        val leftPad = bounds.left + 18f
        val maxTextWidth = bounds.width() - 36f

        // Operation Title
        canvas.drawText("OPERASI:", leftPad, curY, labelPaint)
        curY += 22f
        val truncatedTitle = truncate(op.title.ifBlank { "OPERASI KDN" }, 38)
        canvas.drawText(truncatedTitle, leftPad, curY, boldValPaint)
        curY += lineSpacing

        // No Rujukan
        canvas.drawText("NO. RUJUKAN:", leftPad, curY, labelPaint)
        curY += 22f
        canvas.drawText(op.referenceNumber.ifBlank { "TIADA" }, leftPad, curY, valPaint)
        curY += lineSpacing

        // Tarikh & Masa
        canvas.drawText("TARIKH & MASA:", leftPad, curY, labelPaint)
        curY += 22f
        val dateTimeStr = "${op.operationDate.ifBlank { "24/09/2026" }} • ${op.operationTime.ifBlank { "20:00" }}"
        canvas.drawText(dateTimeStr, leftPad, curY, valPaint)
        curY += lineSpacing

        // Lokasi
        canvas.drawText("LOKASI SERBUAN:", leftPad, curY, labelPaint)
        curY += 22f
        val locStr = truncate(op.location.ifBlank { "Lokasi belum dinyatakan" }, 42)
        canvas.drawText(locStr, leftPad, curY, valPaint)
        curY += lineSpacing

        // Pegawai Serbuan
        canvas.drawText("KETUA OPS / PEGAWAI:", leftPad, curY, labelPaint)
        curY += 22f
        canvas.drawText(truncate(op.officerName.ifBlank { "Pegawai Bertugas" }, 38), leftPad, curY, valPaint)
        curY += lineSpacing

        // Akta / Kesalahan
        canvas.drawText("AKTA / KATEGORI KES:", leftPad, curY, labelPaint)
        curY += 22f
        canvas.drawText(truncate(op.actCategory.ifBlank { "Akta Mesin Cetak & Kawalan KDN" }, 38), leftPad, curY, valPaint)
        curY += lineSpacing

        // Tangkapan / Rampasan
        if (op.suspectsCaught.isNotBlank()) {
            canvas.drawText("TANGKAPAN:", leftPad, curY, labelPaint)
            curY += 22f
            canvas.drawText(truncate(op.suspectsCaught, 40), leftPad, curY, boldValPaint)
            curY += lineSpacing
        }

        if (op.seizedItemsSummary.isNotBlank()) {
            canvas.drawText("RAMPASAN / EKSIBIT:", leftPad, curY, labelPaint)
            curY += 22f
            canvas.drawText(truncate(op.seizedItemsSummary, 40), leftPad, curY, boldValPaint)
            curY += lineSpacing
        }

        // Official Red Stamp / Verification Badge at bottom of Grid 1
        val stampRect = RectF(bounds.right - 190f, bounds.bottom - 75f, bounds.right - 14f, bounds.bottom - 12f)
        val stampBorder = Paint().apply {
            color = Color.parseColor("#B91C1C")
            strokeWidth = 2.5f
            style = Paint.Style.STROKE
        }
        canvas.drawRoundRect(stampRect, 8f, 8f, stampBorder)

        val stampText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#B91C1C")
            textSize = 14f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("PENGUATKUASAAN", stampRect.centerX(), stampRect.top + 22f, stampText)
        canvas.drawText("KDN MALAYSIA", stampRect.centerX(), stampRect.top + 40f, stampText)
        canvas.drawText("PENGESAHAN RASMI", stampRect.centerX(), stampRect.top + 56f, stampText)
    }

    private fun drawPhotoGrid(
        context: Context,
        canvas: Canvas,
        bounds: RectF,
        imageUriString: String?,
        label: String,
        slotNumber: Int
    ) {
        var bitmap: Bitmap? = null
        if (!imageUriString.isNullOrBlank()) {
            try {
                val uri = Uri.parse(imageUriString)
                bitmap = decodeSampledBitmapFromUri(context, uri, bounds.width().toInt(), bounds.height().toInt())
            } catch (_: Exception) {
                bitmap = null
            }
        }

        if (bitmap != null) {
            // Draw image cropped to fill bounds
            drawCenterCropBitmap(canvas, bitmap, bounds)
        } else {
            // Placeholder
            val phBg = Paint().apply {
                color = Color.parseColor("#1E293B")
                style = Paint.Style.FILL
            }
            canvas.drawRect(bounds, phBg)

            // Pattern lines
            val pLine = Paint().apply {
                color = Color.parseColor("#334155")
                strokeWidth = 2f
            }
            canvas.drawLine(bounds.left, bounds.top, bounds.right, bounds.bottom, pLine)
            canvas.drawLine(bounds.left, bounds.bottom, bounds.right, bounds.top, pLine)

            val pText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#94A3B8")
                textSize = 24f
                typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("[ FOTO OPERASI $slotNumber ]", bounds.centerX(), bounds.centerY() - 15f, pText)

            val pSub = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = Color.parseColor("#64748B")
                textSize = 16f
                textAlign = Paint.Align.CENTER
            }
            canvas.drawText("Pilih foto dalam editor", bounds.centerX(), bounds.centerY() + 18f, pSub)
        }

        // Label Ribbon at bottom of photo
        val ribbonHeight = 44f
        val ribbonBg = Paint().apply {
            color = Color.argb(210, 10, 25, 47)
            style = Paint.Style.FILL
        }
        canvas.drawRect(bounds.left, bounds.bottom - ribbonHeight, bounds.right, bounds.bottom, ribbonBg)

        val ribbonText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            textSize = 18f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        }
        canvas.drawText(label, bounds.left + 16f, bounds.bottom - 15f, ribbonText)

        // Slot tag top-right
        val tagBg = Paint().apply {
            color = Color.parseColor("#D4AF37")
            style = Paint.Style.FILL
        }
        val tagRect = RectF(bounds.right - 90f, bounds.top + 8f, bounds.right - 8f, bounds.top + 38f)
        canvas.drawRoundRect(tagRect, 6f, 6f, tagBg)

        val tagText = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.parseColor("#09182E")
            textSize = 16f
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText("GRID $slotNumber", tagRect.centerX(), tagRect.centerY() + 6f, tagText)
    }

    private fun drawCenterCropBitmap(canvas: Canvas, source: Bitmap, destRect: RectF) {
        val srcW = source.width
        val srcH = source.height
        val destW = destRect.width()
        val destH = destRect.height()

        val srcRatio = srcW.toFloat() / srcH.toFloat()
        val destRatio = destW / destH

        val cropRect = if (srcRatio > destRatio) {
            // Source is wider than dest
            val newW = (srcH * destRatio).toInt()
            val left = (srcW - newW) / 2
            Rect(left, 0, left + newW, srcH)
        } else {
            // Source is taller than dest
            val newH = (srcW / destRatio).toInt()
            val top = (srcH - newH) / 2
            Rect(0, top, srcW, top + newH)
        }

        canvas.drawBitmap(source, cropRect, destRect, Paint(Paint.FILTER_BITMAP_FLAG))
    }

    private fun decodeSampledBitmapFromUri(context: Context, uri: Uri, reqW: Int, reqH: Int): Bitmap? {
        var input: InputStream? = null
        return try {
            input = context.contentResolver.openInputStream(uri)
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            BitmapFactory.decodeStream(input, null, options)
            input?.close()

            // Calculate inSampleSize
            var inSampleSize = 1
            if (options.outHeight > reqH || options.outWidth > reqW) {
                val halfHeight = options.outHeight / 2
                val halfWidth = options.outWidth / 2
                while ((halfHeight / inSampleSize) >= reqH && (halfWidth / inSampleSize) >= reqW) {
                    inSampleSize *= 2
                }
            }

            input = context.contentResolver.openInputStream(uri)
            val decodeOptions = BitmapFactory.Options().apply {
                this.inSampleSize = max(1, inSampleSize)
            }
            BitmapFactory.decodeStream(input, null, decodeOptions)
        } catch (_: Exception) {
            null
        } finally {
            input?.close()
        }
    }

    private fun truncate(text: String, maxLen: Int): String {
        return if (text.length > maxLen) text.take(maxLen - 3) + "..." else text
    }
}
