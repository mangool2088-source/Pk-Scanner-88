package com.pakscanner.app

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.RectF
import android.view.MotionEvent
import android.view.ViewGroup
import android.app.AlertDialog
import android.graphics.Color
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.CheckBox
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.mlkit.documentscanner.GmsDocumentScannerOptions
import com.google.android.gms.mlkit.documentscanner.GmsDocumentScanning
import com.google.android.gms.mlkit.documentscanner.GmsDocumentScanningResult
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : AppCompatActivity() {
    private val pages = mutableListOf<Bitmap>()
    private val selected = mutableSetOf<Int>()
    private lateinit var root: LinearLayout
    private lateinit var list: LinearLayout
    private lateinit var status: TextView
    private var onScanResult: ((IntentSenderRequest) -> Unit)? = null

    private val scannerLauncher = registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val scan = GmsDocumentScanningResult.fromActivityResultIntent(result.data)
            scan?.pages?.forEach { page ->
                try {
                    contentResolver.openInputStream(page.imageUri)?.use { input ->
                        BitmapFactory.decodeStream(input)?.let { pages.add(it); selected.add(pages.lastIndex) }
                    }
                } catch (_: Exception) {}
            }
            refreshPages()
            toast(if (pages.isEmpty()) "اسکین میں کوئی صفحہ نہیں ملا" else "اسکین کے صفحات شامل ہوگئے")
        }
    }

    private val galleryLauncher = registerForActivityResult(ActivityResultContracts.OpenMultipleDocuments()) { uris ->
        uris.forEach { uri ->
            try {
                contentResolver.openInputStream(uri)?.use { input ->
                    BitmapFactory.decodeStream(input)?.let { pages.add(it); selected.add(pages.lastIndex) }
                }
            } catch (_: Exception) {}
        }
        refreshPages()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showHome()
    }

    private fun showHome() {
        root = column()
        root.setPadding(24, 32, 24, 24)
        root.gravity = Gravity.CENTER_HORIZONTAL
        root.addView(label("PAK Scanner", 30f, true).apply { setPadding(0, 30, 0, 30) })
        root.addView(actionButton("Scan Document") { showScanner() }, matchWidth())
        root.addView(space(16))
        root.addView(actionButton("Online Chat & Calls Setup") { showChatSetup() }, matchWidth())
        root.addView(space(20))
        root.addView(label("آسانی سے دستاویز اسکین کریں اور PDF بنائیں", 16f, false))
        setContentView(root)
    }

    private fun showScanner() {
        root = column()
        root.setPadding(16, 16, 16, 16)
        root.addView(label("Scan Document", 24f, true))
        val row1 = LinearLayout(this)
        row1.orientation = LinearLayout.HORIZONTAL
        row1.addView(actionButton("Camera Scan") { launchScanner() }, LinearLayout.LayoutParams(0, -2, 1f))
        row1.addView(actionButton("Gallery") { galleryLauncher.launch(arrayOf("image/*")) }, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(row1)
        val row2 = LinearLayout(this)
        row2.orientation = LinearLayout.HORIZONTAL
        row2.addView(actionButton("Select All") { selected.clear(); pages.indices.forEach { selected.add(it) }; refreshPages() }, LinearLayout.LayoutParams(0, -2, 1f))
        row2.addView(actionButton("Deselect") { selected.clear(); refreshPages() }, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(row2)
        val row3 = LinearLayout(this)
        row3.orientation = LinearLayout.HORIZONTAL
        row3.addView(actionButton("Black & White") { transformSelected(::toBlackWhite) }, LinearLayout.LayoutParams(0, -2, 1f))
        row3.addView(actionButton("Clean / Clear") { transformSelected(::enhance) }, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(row3)
        val row4 = LinearLayout(this)
        row4.orientation = LinearLayout.HORIZONTAL
        row4.addView(actionButton("Rotate") { transformSelected(::rotate) }, LinearLayout.LayoutParams(0, -2, 1f))
        row4.addView(actionButton("Delete") { deleteSelected() }, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(row4)
        root.addView(actionButton("Manual Crop (Drag Corners)") { manualCropSelected() }, matchWidth())
        val row5 = LinearLayout(this)
        row5.orientation = LinearLayout.HORIZONTAL
        row5.addView(actionButton("Save Images") { saveImages() }, LinearLayout.LayoutParams(0, -2, 1f))
        row5.addView(actionButton("Create One PDF") { createPdf() }, LinearLayout.LayoutParams(0, -2, 1f))
        root.addView(row5)
        root.addView(actionButton("Share PDF") { createPdf(true) }, matchWidth())
        status = label("کوئی صفحہ شامل نہیں", 14f, false)
        root.addView(status)
        val scroll = ScrollView(this)
        list = column()
        scroll.addView(list)
        root.addView(scroll, LinearLayout.LayoutParams(-1, 0, 1f))
        root.addView(actionButton("Back to Home") { showHome() }, matchWidth())
        setContentView(root)
        refreshPages()
    }

    private fun launchScanner() {
        val options = GmsDocumentScannerOptions.Builder()
            .setGalleryImportAllowed(false)
            .setPageLimit(50)
            .setResultFormats(GmsDocumentScannerOptions.RESULT_FORMAT_JPEG)
            .setScannerMode(GmsDocumentScannerOptions.SCANNER_MODE_FULL)
            .build()
        GmsDocumentScanning.getClient(options).getStartScanIntent(this)
            .addOnSuccessListener { sender -> scannerLauncher.launch(IntentSenderRequest.Builder(sender).build()) }
            .addOnFailureListener { e -> toast("Scanner شروع نہیں ہوا: ${e.localizedMessage ?: "خطا"}") }
    }

    private fun refreshPages() {
        if (!::list.isInitialized) return
        list.removeAllViews()
        pages.forEachIndexed { index, bitmap ->
            val row = LinearLayout(this)
            row.orientation = LinearLayout.HORIZONTAL
            row.gravity = Gravity.CENTER_VERTICAL
            val check = CheckBox(this)
            check.isChecked = selected.contains(index)
            check.setOnCheckedChangeListener { _, checked -> if (checked) selected.add(index) else selected.remove(index) }
            row.addView(check)
            val image = ImageView(this)
            image.setImageBitmap(bitmap)
            image.scaleType = ImageView.ScaleType.CENTER_INSIDE
            row.addView(image, LinearLayout.LayoutParams(120, 140))
            row.addView(label("Page ${index + 1}", 16f, true))
            list.addView(row)
            val divider = View(this)
            divider.setBackgroundColor(Color.LTGRAY)
            list.addView(divider, LinearLayout.LayoutParams(-1, 1))
        }
        status.text = "کل صفحات: ${pages.size} | منتخب: ${selected.size}"
    }

    private fun transformSelected(transform: (Bitmap) -> Bitmap) {
        if (selected.isEmpty()) { toast("پہلے صفحات منتخب کریں"); return }
        selected.toList().sorted().forEach { i -> if (i in pages.indices) pages[i] = transform(pages[i]) }
        refreshPages()
    }

    private fun toBlackWhite(src: Bitmap): Bitmap {
        val out = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        for (y in 0 until src.height) for (x in 0 until src.width) {
            val c = src.getPixel(x, y)
            val gray = (Color.red(c) * 0.299 + Color.green(c) * 0.587 + Color.blue(c) * 0.114).toInt()
            val value = if (gray > 165) 255 else 0
            out.setPixel(x, y, Color.rgb(value, value, value))
        }
        return out
    }

    private fun enhance(src: Bitmap): Bitmap {
        val out = Bitmap.createBitmap(src.width, src.height, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(out)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply { colorFilter = android.graphics.ColorMatrixColorFilter(
            android.graphics.ColorMatrix(floatArrayOf(1.25f,0f,0f,0f,-18f, 0f,1.25f,0f,0f,-18f, 0f,0f,1.25f,0f,-18f, 0f,0f,0f,1f,0f))
        ) }
        canvas.drawBitmap(src, 0f, 0f, paint)
        return out
    }

    private fun rotate(src: Bitmap): Bitmap {
        val matrix = Matrix().apply { postRotate(90f) }
        return Bitmap.createBitmap(src, 0, 0, src.width, src.height, matrix, true)
    }

    private fun deleteSelected() {
        if (selected.isEmpty()) { toast("پہلے صفحات منتخب کریں"); return }
        selected.sortedDescending().forEach { i -> if (i in pages.indices) pages.removeAt(i) }
        selected.clear()
        refreshPages()
    }

    private fun saveImages() {
        if (selected.isEmpty()) { toast("پہلے صفحات منتخب کریں"); return }
        var saved = 0
        selected.sorted().forEach { i ->
            val values = ContentValues().apply {
                put(MediaStore.Images.Media.DISPLAY_NAME, "PAK-Scanner-${System.currentTimeMillis()}-$i.jpg")
                put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
                put(MediaStore.Images.Media.RELATIVE_PATH, "Pictures/PAK Scanner")
            }
            val uri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            if (uri != null) contentResolver.openOutputStream(uri)?.use { pages[i].compress(Bitmap.CompressFormat.JPEG, 95, it); saved++ }
        }
        toast("$saved تصاویر گیلری میں محفوظ ہوگئیں")
    }

    private fun createPdf(shareAfter: Boolean = false) {
        if (selected.isEmpty()) { toast("پہلے صفحات منتخب کریں"); return }
        val doc = PdfDocument()
        selected.sorted().forEachIndexed { pageNo, index ->
            val bitmap = pages[index]
            val info = PdfDocument.PageInfo.Builder(bitmap.width.coerceAtLeast(1), bitmap.height.coerceAtLeast(1), pageNo + 1).create()
            val page = doc.startPage(info)
            page.canvas.drawBitmap(bitmap, 0f, 0f, Paint(Paint.FILTER_BITMAP_FLAG))
            doc.finishPage(page)
        }
        val filename = "PAK-Scanner-${SimpleDateFormat("yyyyMMdd-HHmmss", Locale.US).format(Date())}.pdf"
        val values = ContentValues().apply {
            put(MediaStore.Downloads.DISPLAY_NAME, filename)
            put(MediaStore.Downloads.MIME_TYPE, "application/pdf")
            put(MediaStore.Downloads.RELATIVE_PATH, "Download/PAK Scanner")
        }
        val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
        try {
            if (uri == null) throw Exception("فائل محفوظ نہیں ہو سکی")
            contentResolver.openOutputStream(uri)?.use { doc.writeTo(it) }
            doc.close()
            toast("ایک PDF محفوظ ہوگئی: $filename")
            if (shareAfter) {
                val intent = Intent(Intent.ACTION_SEND).apply {
                    type = "application/pdf"
                    putExtra(Intent.EXTRA_STREAM, uri)
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                }
                startActivity(Intent.createChooser(intent, "PDF Share کریں"))
            }
        } catch (e: Exception) {
            doc.close()
            toast("PDF بنانے میں مسئلہ: ${e.localizedMessage}")
        }
    }


    private fun manualCropSelected() {
        val index = selected.minOrNull()
        if (index == null || index !in pages.indices) {
            toast("پہلے ایک صفحہ منتخب کریں")
            return
        }
        val original = pages[index]
        val cropView = ManualCropView(this, original)
        val container = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(12, 8, 12, 8)
            addView(cropView, LinearLayout.LayoutParams(-1, 440))
            addView(TextView(this@MainActivity).apply {
                text = "چاروں کونوں کو انگلی سے گھسیٹ کر کاغذ کی حد مقرر کریں، پھر Apply دبائیں۔"
                textSize = 14f
                setPadding(4, 10, 4, 4)
            })
        }
        AlertDialog.Builder(this)
            .setTitle("Manual Crop")
            .setView(container)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Apply") { _, _ ->
                val cropped = cropView.getCroppedBitmap()
                if (cropped != null) {
                    pages[index] = cropped
                    refreshPages()
                    toast("صفحہ Crop ہوگیا")
                } else {
                    toast("Crop کی حد درست منتخب کریں")
                }
            }
            .show()
    }

    private class ManualCropView(context: android.content.Context, private val source: Bitmap) : View(context) {
        private val imageRect = RectF()
        private val cropRect = RectF()
        private val imagePaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
        private val shadePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = 0x88000000.toInt() }
        private val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.YELLOW
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }
        private val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.YELLOW; style = Paint.Style.FILL }
        private val handleRadius = 22f
        private var activeCorner = -1

        init {
            setLayerType(View.LAYER_TYPE_SOFTWARE, null)
        }

        override fun onDraw(canvas: Canvas) {
            super.onDraw(canvas)
            val scale = minOf(width.toFloat() / source.width, height.toFloat() / source.height)
            val drawW = source.width * scale
            val drawH = source.height * scale
            val left = (width - drawW) / 2f
            val top = (height - drawH) / 2f
            imageRect.set(left, top, left + drawW, top + drawH)
            if (cropRect.isEmpty) {
                cropRect.set(
                    imageRect.left + imageRect.width() * 0.08f,
                    imageRect.top + imageRect.height() * 0.08f,
                    imageRect.right - imageRect.width() * 0.08f,
                    imageRect.bottom - imageRect.height() * 0.08f
                )
            }
            canvas.drawColor(Color.DKGRAY)
            canvas.drawBitmap(source, null, imageRect, imagePaint)
            canvas.drawRect(imageRect.left, imageRect.top, imageRect.right, cropRect.top, shadePaint)
            canvas.drawRect(imageRect.left, cropRect.bottom, imageRect.right, imageRect.bottom, shadePaint)
            canvas.drawRect(imageRect.left, cropRect.top, cropRect.left, cropRect.bottom, shadePaint)
            canvas.drawRect(cropRect.right, cropRect.top, imageRect.right, cropRect.bottom, shadePaint)
            canvas.drawRect(cropRect, borderPaint)
            val points = arrayOf(
                cropRect.left to cropRect.top,
                cropRect.right to cropRect.top,
                cropRect.left to cropRect.bottom,
                cropRect.right to cropRect.bottom
            )
            points.forEach { (x, y) -> canvas.drawCircle(x, y, handleRadius, handlePaint) }
        }

        override fun onTouchEvent(event: MotionEvent): Boolean {
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val corners = arrayOf(
                        cropRect.left to cropRect.top,
                        cropRect.right to cropRect.top,
                        cropRect.left to cropRect.bottom,
                        cropRect.right to cropRect.bottom
                    )
                    activeCorner = corners.indices.minByOrNull { i ->
                        val dx = event.x - corners[i].first
                        val dy = event.y - corners[i].second
                        dx * dx + dy * dy
                    } ?: -1
                    val pair = if (activeCorner >= 0) corners[activeCorner] else null
                    if (pair == null || kotlin.math.hypot((event.x - pair.first).toDouble(), (event.y - pair.second).toDouble()) > handleRadius * 2.5) {
                        activeCorner = -1
                    }
                    return activeCorner >= 0
                }
                MotionEvent.ACTION_MOVE -> {
                    if (activeCorner < 0) return false
                    val minX = imageRect.left + 20f
                    val maxX = imageRect.right - 20f
                    val minY = imageRect.top + 20f
                    val maxY = imageRect.bottom - 20f
                    when (activeCorner) {
                        0 -> { cropRect.left = event.x.coerceIn(minX, cropRect.right - 40f); cropRect.top = event.y.coerceIn(minY, cropRect.bottom - 40f) }
                        1 -> { cropRect.right = event.x.coerceIn(cropRect.left + 40f, maxX); cropRect.top = event.y.coerceIn(minY, cropRect.bottom - 40f) }
                        2 -> { cropRect.left = event.x.coerceIn(minX, cropRect.right - 40f); cropRect.bottom = event.y.coerceIn(cropRect.top + 40f, maxY) }
                        3 -> { cropRect.right = event.x.coerceIn(cropRect.left + 40f, maxX); cropRect.bottom = event.y.coerceIn(cropRect.top + 40f, maxY) }
                    }
                    invalidate()
                    return true
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val handled = activeCorner >= 0
                    activeCorner = -1
                    return handled
                }
            }
            return true
        }

        fun getCroppedBitmap(): Bitmap? {
            if (imageRect.width() <= 0f || imageRect.height() <= 0f) return null
            val sx = source.width / imageRect.width()
            val sy = source.height / imageRect.height()
            val left = ((cropRect.left - imageRect.left) * sx).toInt().coerceIn(0, source.width - 1)
            val top = ((cropRect.top - imageRect.top) * sy).toInt().coerceIn(0, source.height - 1)
            val right = ((cropRect.right - imageRect.left) * sx).toInt().coerceIn(left + 1, source.width)
            val bottom = ((cropRect.bottom - imageRect.top) * sy).toInt().coerceIn(top + 1, source.height)
            if (right - left < 2 || bottom - top < 2) return null
            return Bitmap.createBitmap(source, left, top, right - left, bottom - top)
        }
    }

    private fun showChatSetup() {
        root = column()
        root.setPadding(24, 24, 24, 24)
        root.addView(label("Online Chat & Calls Setup", 24f, true))
        root.addView(label("یہ حصہ ابھی بنیادی سیٹ اپ اسکرین ہے۔ کسی چیٹ یا کال سروس کو فعال کرنے کے لیے اس سروس کی تفصیلات اور اجازت درکار ہوگی۔", 16f, false))
        root.addView(actionButton("Back to Home") { showHome() }, matchWidth())
        setContentView(root)
    }

    private fun column() = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
    private fun label(text: String, size: Float, bold: Boolean) = TextView(this).apply {
        this.text = text; textSize = size; setTextColor(Color.BLACK)
        if (bold) setTypeface(null, android.graphics.Typeface.BOLD)
        setPadding(8, 8, 8, 8)
    }
    private fun actionButton(text: String, action: () -> Unit) = Button(this).apply { this.text = text; setOnClickListener { action() } }
    private fun matchWidth() = LinearLayout.LayoutParams(-1, -2)
    private fun space(dp: Int) = View(this).apply { layoutParams = LinearLayout.LayoutParams(1, dp) }
    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}
