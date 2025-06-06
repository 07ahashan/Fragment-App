package com.anviam.fragmentapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.graphics.pdf.PdfDocument
import android.os.Bundle
import android.os.Environment
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.AppCompatButton
import androidx.core.content.ContextCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*

class ShortFragment : Fragment() {

    private var generateBtn: AppCompatButton? = null
    private var fuelManMapBtn: AppCompatButton? = null
    private var fuelManActivity: AppCompatButton ?= null
    private val STORAGE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE

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

    @SuppressLint("MissingInflatedId")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_short, container, false)
        generateBtn = view.findViewById(R.id.btn_generate_pdf)
        fuelManMapBtn = view.findViewById(R.id.btn_fuelman_map)
        fuelManActivity = view.findViewById(R.id.btn_fuelman_activity)
        generateBtn?.setOnClickListener {
            checkPermissionAndGeneratePdf()
        }
        fuelManMapBtn?.setOnClickListener {
            startActivity(Intent(requireActivity(), FuelManMapActivity::class.java))
        }

        fuelManActivity?.setOnClickListener {
            startActivity(Intent(requireActivity(), FuelManActivityOne::class.java))
        }
        return view
    }

    private fun checkPermissionAndGeneratePdf() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                STORAGE_PERMISSION
            ) == PackageManager.PERMISSION_GRANTED -> {
                generatePdf()
            }

            shouldShowRequestPermissionRationale(STORAGE_PERMISSION) -> {
                Toast.makeText(
                    requireContext(),
                    "Storage permission is needed to save the PDF",
                    Toast.LENGTH_LONG
                ).show()
                requestPermissionLauncher.launch(STORAGE_PERMISSION)
            }

            else -> {
                requestPermissionLauncher.launch(STORAGE_PERMISSION)
            }
        }
    }

    private fun generatePdf() {
        try {
            val document = PdfDocument()
            // Using A4 size
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = document.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint()

            // Draw company logo
            val logo = ContextCompat.getDrawable(requireContext(), R.drawable.tankspotter)
            logo?.let {
                val bitmap = (it as? BitmapDrawable)?.bitmap
                bitmap?.let { bmp ->
                    // Scale logo to appropriate size (e.g., 100x100 pixels)
                    val scaledBitmap = Bitmap.createScaledBitmap(bmp, 50, 50, true)
                    // Center the logo horizontally
                    val logoX = (pageInfo.pageWidth - scaledBitmap.width) / 2f - 25f
                    canvas.drawBitmap(scaledBitmap, logoX, 50f, paint)
                }
            }

            // Company Name
            paint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            paint.textSize = 16f
            paint.textAlign = Paint.Align.CENTER
            canvas.drawText("AEGIS INSURANCE", pageInfo.pageWidth / 2f, 180f, paint)

            // Title
            paint.textSize = 14f
            canvas.drawText("COMPANY POLICY AND PROCEDURES", pageInfo.pageWidth / 2f, 220f, paint)
            canvas.drawText("FOOTWEAR POLICY", pageInfo.pageWidth / 2f, 240f, paint)

            // Main content
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            paint.textSize = 12f
            paint.textAlign = Paint.Align.LEFT
            var yPosition = 280f
            val leftMargin = 50f
            val lineSpacing = 20f

            // Policy description
            val policyDesc =
                "Due to the nature of our work, slips, trips and falls are a major concern and can cause " +
                        "severe injury. In order to minimize the risk of an injury, we have developed this footwear " +
                        "policy for the benefit of our employees."

            // Draw wrapped text
            yPosition = drawWrappedText(
                canvas,
                policyDesc,
                leftMargin,
                yPosition,
                paint,
                pageInfo.pageWidth - 100f
            )
            yPosition += lineSpacing

            // Guidelines section
            paint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            canvas.drawText("Guidelines", leftMargin, yPosition, paint)
            yPosition += lineSpacing

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val guidelines = listOf(
                "• The employee's shoes shall be \"slip-resistant\".",
                "• The employee's shoes must be closed toe.",
                "• NO sandals or flip-flops"
            )

            guidelines.forEach { guideline ->
                canvas.drawText(guideline, leftMargin + 20f, yPosition, paint)
                yPosition += lineSpacing
            }

            yPosition += lineSpacing

            // Responsibilities section
            paint.typeface = Typeface.create(Typeface.DEFAULT_BOLD, Typeface.BOLD)
            canvas.drawText("Responsibilities", leftMargin, yPosition, paint)
            yPosition += lineSpacing

            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            val responsibilities = listOf(
                "• Wear the proper footwear as part of your daily uniform.",
                "• Inspect your shoes daily for cleanliness, presence of liquid or",
                "  contaminants wedged in the treads, and wear and tear."
            )

            responsibilities.forEach { responsibility ->
                canvas.drawText(responsibility, leftMargin + 20f, yPosition, paint)
                yPosition += lineSpacing
            }

            yPosition += lineSpacing

            // Footer text
            val footerText =
                "This policy has been established to minimize injury and promote a safe workplace. Your " +
                        "participation is essential to the success of our safety program and is also a condition of " +
                        "employment. If you have any questions, please let us know."

            yPosition = drawWrappedText(
                canvas,
                footerText,
                leftMargin,
                yPosition,
                paint,
                pageInfo.pageWidth - 100f
            )
            yPosition += lineSpacing * 2

            // Agreement text
            val agreementText =
                "I, Navjot kaur, on the date of 05/14/2025 have read the above policy and agree to all terms " +
                        "set forth. I understand and will comply with all provisions in the Footwear policy. I also " +
                        "understand that failure to comply will result in appropriate disciplinary procedures."

            yPosition = drawWrappedText(
                canvas,
                agreementText,
                leftMargin,
                yPosition,
                paint,
                pageInfo.pageWidth - 100f
            )
            yPosition += lineSpacing * 2

            // Witness Name
            canvas.drawText("Witness Name: BJBJB", leftMargin, yPosition, paint)
            yPosition += lineSpacing * 3

            // Signature section
            val signatureY = yPosition
            paint.textAlign = Paint.Align.CENTER

            // Driver's Signature
            canvas.drawLine(leftMargin + 50f, signatureY, leftMargin + 150f, signatureY, paint)
            canvas.drawText("Driver's Signature", leftMargin + 100f, signatureY + 20f, paint)

            // Witness Signature
            canvas.drawLine(
                pageInfo.pageWidth - 200f,
                signatureY,
                pageInfo.pageWidth - 100f,
                signatureY,
                paint
            )
            canvas.drawText("Witness Signature", pageInfo.pageWidth / 2f, signatureY + 20f, paint)

            // Date
            canvas.drawText("Date: 05-14-2025", pageInfo.pageWidth - 150f, signatureY + 20f, paint)

            document.finishPage(page)

            // Save the document
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val fileName = "FootwearPolicy_$timestamp.pdf"
            val filePath = File(
                requireContext().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS),
                fileName
            )

            try {
                document.writeTo(FileOutputStream(filePath))
                Toast.makeText(
                    requireContext(),
                    "PDF saved successfully at:\n${filePath.absolutePath}",
                    Toast.LENGTH_LONG
                ).show()
            } catch (e: IOException) {
                Toast.makeText(
                    requireContext(),
                    "Error saving PDF: ${e.message}",
                    Toast.LENGTH_LONG
                ).show()
            } finally {
                document.close()
            }
        } catch (e: Exception) {
            Toast.makeText(
                requireContext(),
                "Error generating PDF: ${e.message}",
                Toast.LENGTH_LONG
            ).show()
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

        words.forEach { word ->
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