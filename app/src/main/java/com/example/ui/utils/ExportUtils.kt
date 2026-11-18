package com.example.ui.utils
import com.example.ui.*

import com.example.ui.screens.auth.*
import com.example.ui.screens.home.*
import com.example.ui.screens.revision.*
import com.example.ui.screens.subject.*
import com.example.ui.screens.profile.*
import com.example.ui.screens.rewards.*
import com.example.ui.screens.settings.*
import com.example.ui.screens.admin.*
import com.example.ui.components.*
import com.example.ui.utils.*

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.data.AnalyticsMetrics
import com.example.data.UserProfile
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

fun exportReport(context: Context, format: String, metrics: AnalyticsMetrics): File? {
    try {
        val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
        val fileName = "Analytics_Report_$timestamp.$format"
        val cachePath = File(context.cacheDir, "reports")
        cachePath.mkdirs()
        val file = File(cachePath, fileName)

        if (format == "pdf") {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint()
            paint.color = Color.BLACK
            paint.textSize = 14f
            
            var y = 40f
            canvas.drawText("Analytics Report", 10f, y, paint)
            y += 30f
            paint.textSize = 10f
            canvas.drawText("Date: $timestamp", 10f, y, paint)
            y += 20f
            canvas.drawText("Syllabus Completion: ${metrics.overallCompletionPercentage}%", 10f, y, paint)
            y += 20f
            canvas.drawText("Revision Strength: ${metrics.productivityScore}%", 10f, y, paint)
            y += 20f
            canvas.drawText("Average Test Score: ${metrics.averageScore}%", 10f, y, paint)
            y += 20f
            canvas.drawText("Exam Readiness: ${metrics.readinessPercentage}%", 10f, y, paint)
            
            pdfDocument.finishPage(page)
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()
        } else if (format == "csv") {
            val sb = java.lang.StringBuilder()
            sb.append("Metric,Value\n")
            sb.append("Syllabus Completion,${metrics.overallCompletionPercentage}%\n")
            sb.append("Revision Strength,${metrics.productivityScore}%\n")
            sb.append("Average Test Score,${metrics.averageScore}%\n")
            sb.append("Exam Readiness,${metrics.readinessPercentage}%\n")
            
            sb.append("\nSubject,Completion,Study Time Mins\n")
            metrics.subjectProgress.forEach {
                sb.append("${it.name},${it.percentage}%,${it.studyTimeMins}%\n")
            }
            
            file.writeText(sb.toString())
        }

        if (file.exists() && file.length() > 0) {
            return file
        } else {
            return null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        return null
    }
}

fun generateStudyProgressReportPdf(context: Context, metrics: AnalyticsMetrics, userProfile: UserProfile?): File? {
    try {
        val fileName = "Study_Progress_Report.pdf"
        val cachePath = File(context.cacheDir, "reports")
        cachePath.mkdirs()
        val file = File(cachePath, fileName)

        val pdfDocument = PdfDocument()
        val pageWidth = 595
        val pageHeight = 842
        val pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas
        
        val paint = Paint().apply { color = Color.BLACK }
        val titlePaint = Paint().apply {
            color = Color.BLACK
            textSize = 24f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        val headerPaint = Paint().apply {
            color = Color.BLACK
            textSize = 16f
            isFakeBoldText = true
        }
        val normalPaint = Paint().apply {
            color = Color.DKGRAY
            textSize = 14f
        }
        
        var currentY = 50f
        val leftMargin = 50f
        val lineHeight = 25f
        
        fun checkPageBreak(requiredSpace: Float) {
            if (currentY + requiredSpace > pageHeight - 50f) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                currentY = 50f
            }
        }
        
        // Title
        canvas.drawText("STUDY PROGRESS REPORT", pageWidth / 2f, currentY, titlePaint)
        currentY += 40f
        
        // Basic Info
        val dateStr = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault()).format(Date())
        if (userProfile?.name?.isNotBlank() == true) {
            canvas.drawText("Student Name: ${userProfile.name}", leftMargin, currentY, normalPaint)
            currentY += lineHeight
        }
        canvas.drawText("Exam Name: ${metrics.targetExamName}", leftMargin, currentY, normalPaint)
        currentY += lineHeight
        canvas.drawText("Report Generation Date: $dateStr", leftMargin, currentY, normalPaint)
        currentY += 40f
        
        // Overall Progress
        checkPageBreak(100f)
        canvas.drawText("Overall Progress", leftMargin, currentY, headerPaint)
        currentY += lineHeight
        canvas.drawText("Syllabus Completion: ${metrics.overallProgressPercentage}%", leftMargin + 20f, currentY, normalPaint)
        currentY += lineHeight
        canvas.drawText("Total Topics Completed: ${metrics.completedTopics}", leftMargin + 20f, currentY, normalPaint)
        currentY += lineHeight
        canvas.drawText("Pending Topics: ${metrics.pendingTopics}", leftMargin + 20f, currentY, normalPaint)
        currentY += 40f
        
        // Subject-wise Progress
        checkPageBreak(50f + metrics.subjectProgress.size * lineHeight * 2)
        canvas.drawText("Subject-wise Progress", leftMargin, currentY, headerPaint)
        currentY += lineHeight
        if (metrics.subjectProgress.isEmpty()) {
            canvas.drawText("No subject data available.", leftMargin + 20f, currentY, normalPaint)
            currentY += lineHeight
        } else {
            metrics.subjectProgress.forEach { sp ->
                checkPageBreak(lineHeight * 2)
                canvas.drawText("• ${sp.name}: ${sp.completionPercentage}% Completed", leftMargin + 20f, currentY, normalPaint)
                currentY += lineHeight
                canvas.drawText("   Topics: ${sp.completedTopics}/${sp.totalTopics} | Pending: ${sp.pendingTopics} | Study Time: ${sp.studyTimeMins} mins", leftMargin + 20f, currentY, normalPaint)
                currentY += lineHeight
            }
        }
        currentY += 20f
        
        // Study Time Summary
        checkPageBreak(100f)
        canvas.drawText("Study Time Summary", leftMargin, currentY, headerPaint)
        currentY += lineHeight
        canvas.drawText("Total Study Time: ${metrics.totalStudyTimeStr}", leftMargin + 20f, currentY, normalPaint)
        currentY += lineHeight
        canvas.drawText("Deep Focus Time: ${metrics.deepFocusTimeStr}", leftMargin + 20f, currentY, normalPaint)
        currentY += lineHeight
        canvas.drawText("Average Session: ${metrics.averageSessionStr}", leftMargin + 20f, currentY, normalPaint)
        currentY += lineHeight
        canvas.drawText("Longest Session: ${metrics.longestSessionStr}", leftMargin + 20f, currentY, normalPaint)
        currentY += 40f
        
        // Revision Summary
        checkPageBreak(100f)
        canvas.drawText("Revision Summary", leftMargin, currentY, headerPaint)
        currentY += lineHeight
        canvas.drawText("Total Revisions: ${metrics.totalRevisions}", leftMargin + 20f, currentY, normalPaint)
        currentY += lineHeight
        canvas.drawText("Revision Strength / Productivity: ${metrics.productivityScore}%", leftMargin + 20f, currentY, normalPaint)
        currentY += lineHeight
        canvas.drawText("Retention Rate: ${metrics.retentionRate}%", leftMargin + 20f, currentY, normalPaint)
        currentY += 40f
        
        // Test Summary
        checkPageBreak(100f)
        canvas.drawText("Test Summary", leftMargin, currentY, headerPaint)
        currentY += lineHeight
        canvas.drawText("Average Test Score: ${metrics.averageScore}%", leftMargin + 20f, currentY, normalPaint)
        currentY += lineHeight
        canvas.drawText("Exam Readiness: ${metrics.readinessPercentage}%", leftMargin + 20f, currentY, normalPaint)
        currentY += lineHeight
        canvas.drawText("Expected Score Range: ${metrics.expectedScoreRange}", leftMargin + 20f, currentY, normalPaint)
        currentY += 40f
        
        // Achievement Summary
        checkPageBreak(100f)
        canvas.drawText("Achievement Summary", leftMargin, currentY, headerPaint)
        currentY += lineHeight
        canvas.drawText("Current Streak: ${metrics.currentStreak} Days", leftMargin + 20f, currentY, normalPaint)
        currentY += lineHeight
        canvas.drawText("Best Streak: ${metrics.bestStreak} Days", leftMargin + 20f, currentY, normalPaint)
        currentY += lineHeight
        canvas.drawText("Total Focus Sessions: ${metrics.focusSessions}", leftMargin + 20f, currentY, normalPaint)
        currentY += lineHeight
        canvas.drawText("Coins Earned: ${metrics.coinsEarned}", leftMargin + 20f, currentY, normalPaint)
        
        pdfDocument.finishPage(page)
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()

        if (file.exists() && file.length() > 0) {
            return file
        } else {
            Toast.makeText(context, "Failed to verify generated report file", Toast.LENGTH_SHORT).show()
            return null
        }
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Failed to generate report: ${e.localizedMessage}", Toast.LENGTH_SHORT).show()
        return null
    }
}

fun shareReportFile(context: Context, file: File, mimeType: String = "application/pdf") {
    try {
        val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = mimeType
            putExtra(Intent.EXTRA_STREAM, uri)
            putExtra(Intent.EXTRA_SUBJECT, "Study Progress Report")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(intent, "Share Study Progress Report"))
    } catch (e: Exception) {
        e.printStackTrace()
        Toast.makeText(context, "Failed to share report", Toast.LENGTH_SHORT).show()
    }
}

object pdfService {
    fun generateReport(context: Context, metrics: AnalyticsMetrics?, userProfile: UserProfile?) {
        if (metrics == null || metrics.subjectProgress.isEmpty()) {
            Toast.makeText(context, "No study data available", Toast.LENGTH_SHORT).show()
            return
        }
        generateStudyProgressReportPdf(context, metrics, userProfile)
    }
}

object PdfService {
    fun generateReport(context: Context, metrics: AnalyticsMetrics?, userProfile: UserProfile?) {
        if (metrics == null || metrics.subjectProgress.isEmpty()) {
            Toast.makeText(context, "No study data available", Toast.LENGTH_SHORT).show()
            return
        }
        generateStudyProgressReportPdf(context, metrics, userProfile)
    }
}

