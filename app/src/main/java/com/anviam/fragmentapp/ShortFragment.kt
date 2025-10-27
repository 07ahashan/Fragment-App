package com.anviam.fragmentapp

import android.content.Intent
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.anviam.fragmentapp.adapter.NewsAdapter
import com.anviam.fragmentapp.model.NewsApiResponse
import com.anviam.fragmentapp.model.ResultsItem
import com.anviam.fragmentapp.model.NewsApiInterface
import com.anviam.fragmentapp.model.RetrofitInstance
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class ShortFragment : Fragment() {

    private var generateBtn: AppCompatButton? = null
    private var fuelManMapBtn: AppCompatButton? = null
    private var fuelManActivity: AppCompatButton? = null
    private var newsAdapter: NewsAdapter? = null
    private var recyclerView: RecyclerView? = null

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            generatePdf()
        } else {
            Toast.makeText(
                requireContext(),
                "Storage permission is required to generate PDF",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_short, container, false)

        generateBtn = view.findViewById(R.id.btn_generate_pdf)
        fuelManMapBtn = view.findViewById(R.id.btn_fuelman_map)
        fuelManActivity = view.findViewById(R.id.btn_fuelman_activity)
        recyclerView = view.findViewById(R.id.rv_news)
        newsAdapter = NewsAdapter()
        recyclerView?.layoutManager = LinearLayoutManager(activity)
        recyclerView?.adapter = newsAdapter

        generateBtn?.setOnClickListener { generatePdf() } // No need to ask for permission on scoped storage
        fuelManMapBtn?.setOnClickListener {
            startActivity(Intent(requireContext(), FuelManMapActivity::class.java))
        }
        fuelManActivity?.setOnClickListener {
            startActivity(Intent(requireContext(), FuelManActivityOne::class.java))
        }

        fetchNews()
        return view
    }

    private fun fetchNews() {
        val apiService = RetrofitInstance.api.create(NewsApiInterface::class.java)
        val call = apiService.getTopNews(NewsApiInterface.API_KEY)
        call.enqueue(object : Callback<NewsApiResponse> {
            override fun onResponse(call: Call<NewsApiResponse>, response: Response<NewsApiResponse>) {
                if (response.isSuccessful) {
                    val articles = response.body()?.articles?.results?.filterNotNull() ?: emptyList()
                    newsAdapter?.setNews(articles)
                } else {
                    Toast.makeText(requireContext(), "Failed to load news", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<NewsApiResponse>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun generatePdf() {
        try {
            val document = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint()

            // Draw logo
            val logo = ContextCompat.getDrawable(requireContext(), R.drawable.tankspotter)
            logo?.let {
                val bitmap = (it as? BitmapDrawable)?.bitmap
                bitmap?.let { bmp ->
                    val scaledBitmap = Bitmap.createScaledBitmap(bmp, 50, 50, true)
                    val logoX = (pageInfo.pageWidth - scaledBitmap.width) / 2f - 25f
                    canvas.drawBitmap(scaledBitmap, logoX, 50f, paint)
                }
            }

            // Title
            paint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            paint.textSize = 16f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("AEGIS INSURANCE", pageInfo.pageWidth / 2f, 180f, paint)

            paint.textSize = 14f
            canvas.drawText("COMPANY POLICY AND PROCEDURES", pageInfo.pageWidth / 2f, 220f, paint)
            canvas.drawText("FOOTWEAR POLICY", pageInfo.pageWidth / 2f, 240f, paint)

            paint.textSize = 12f
            paint.textAlign = Paint.Align.LEFT
            paint.typeface = Typeface.DEFAULT
            val leftMargin = 50f
            var y = 280f
            val lineSpacing = 20f

            val desc = "Due to the nature of our work, slips, trips and falls are a major concern..."
            y = drawWrappedText(canvas, desc, leftMargin, y, paint, pageInfo.pageWidth - 100f)
            y += lineSpacing

            paint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            canvas.drawText("Guidelines", leftMargin, y, paint)
            y += lineSpacing

            paint.typeface = Typeface.DEFAULT
            val guidelines = listOf(
                "• The employee's shoes shall be \"slip-resistant\".",
                "• The employee's shoes must be closed toe.",
                "• NO sandals or flip-flops"
            )
            guidelines.forEach { canvas.drawText(it, leftMargin + 20f, y, paint).also { y += lineSpacing } }

            y += lineSpacing
            paint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            canvas.drawText("Responsibilities", leftMargin, y, paint)
            y += lineSpacing

            paint.typeface = Typeface.DEFAULT
            val responsibilities = listOf(
                "• Wear the proper footwear as part of your daily uniform.",
                "• Inspect your shoes daily for cleanliness, presence of liquid or",
                "  contaminants wedged in the treads, and wear and tear."
            )
            responsibilities.forEach { canvas.drawText(it, leftMargin + 20f, y, paint).also { y += lineSpacing } }

            y += lineSpacing

            val footer =
                "This policy has been established to minimize injury and promote a safe workplace..."
            y = drawWrappedText(canvas, footer, leftMargin, y, paint, pageInfo.pageWidth - 100f)
            y += lineSpacing * 2

            val agreement =
                "I, Navjot Kaur, on the date of 05/14/2025 have read the above policy..."
            y = drawWrappedText(canvas, agreement, leftMargin, y, paint, pageInfo.pageWidth - 100f)
            y += lineSpacing * 2

            canvas.drawText("Witness Name: BJBJB", leftMargin, y, paint)
            y += lineSpacing * 3

            paint.textAlign = Paint.Align.CENTER
            canvas.drawLine(leftMargin + 50f, y, leftMargin + 150f, y, paint)
            canvas.drawText("Driver's Signature", leftMargin + 100f, y + 20f, paint)

            canvas.drawLine(pageInfo.pageWidth - 200f, y, pageInfo.pageWidth - 100f, y, paint)
            canvas.drawText("Witness Signature", pageInfo.pageWidth / 2f, y + 20f, paint)

            canvas.drawText("Date: 05-14-2025", pageInfo.pageWidth - 150f, y + 20f, paint)

            document.finishPage(page)

            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "FootwearPolicy_$timestamp.pdf"
            val filePath = File(requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)

            document.writeTo(FileOutputStream(filePath))
            Toast.makeText(requireContext(), "PDF saved at: ${filePath.absolutePath}", Toast.LENGTH_LONG).show()

            document.close()
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Error generating PDF: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun drawWrappedText(
        canvas: Canvas,
        text: String,
        x: Float,
        y: Float,
        paint: Paint,
        maxWidth: Float
    ): Float {
        var currentY = y
        val lineHeight = paint.fontSpacing
        val words = text.split(" ")
        var currentLine = StringBuilder()

        for (word in words) {
            val testLine = if (currentLine.isEmpty()) word else "${currentLine} $word"
            if (paint.measureText(testLine) <= maxWidth) {
                currentLine.append(if (currentLine.isEmpty()) word else " $word")
            } else {
                canvas.drawText(currentLine.toString(), x, currentY, paint)
                currentY += lineHeight
                currentLine = StringBuilder(word)
            }
        }

        if (currentLine.isNotEmpty()) {
            canvas.drawText(currentLine.toString(), x, currentY, paint)
            currentY += lineHeight
        }

        return currentY
    }
}
