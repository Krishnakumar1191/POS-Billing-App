package com.example.posapp.utils

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import android.widget.Toast
import androidx.core.content.FileProvider
import com.example.posapp.data.ShopProfile
import com.example.posapp.data.entities.CashTransaction
import com.example.posapp.data.entities.Order
import com.example.posapp.data.entities.Product
import java.io.File
import java.io.FileOutputStream
import java.io.FileWriter
import java.text.SimpleDateFormat
import java.util.*

object ReportExporter {

    // Ensures report files are saved specifically to: Internal Storage -> Documents -> POSAPP -> Reports
    private fun getReportsDirectory(): File {
        val documentsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        val reportsDir = File(documentsDir, "POSAPP/Reports")
        if (!reportsDir.exists()) {
            reportsDir.mkdirs()
        }
        return reportsDir
    }

    private fun openOrShareFile(context: Context, file: File, mimeType: String) {
        // Always notify user where the file was saved on device
        Toast.makeText(context, "Saved to Documents/POSAPP/Reports/${file.name}", Toast.LENGTH_LONG).show()

        try {
            val uri: Uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
            val intent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(uri, mimeType)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            val chooser = Intent.createChooser(intent, "Open Report")
            chooser.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)

            // Explicitly grant read permission to all matching PDF/Excel reader apps
            val resInfoList = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
            for (resolveInfo in resInfoList) {
                val packageName = resolveInfo.activityInfo.packageName
                context.grantUriPermission(packageName, uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            context.startActivity(chooser)
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(context, "Cannot open viewer: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    // --- 1. SALE REPORT PDF ---
    fun exportSaleReportPdf(context: Context, profile: ShopProfile, orders: List<Order>, dateRangeText: String) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create() // A4 Size
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }
        var currentY = 50f

        // Shop Header (Fetched from My Profile)
        paint.color = Color.parseColor("#800000") // Theme Maroon
        paint.textSize = 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        val shopTitle = profile.name.ifBlank { "POS BILLING" }
        canvas.drawText(shopTitle, 297.5f, currentY, paint)

        currentY += 18f
        paint.color = Color.parseColor("#555555")
        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT
        if (profile.address.isNotBlank()) {
            canvas.drawText(profile.address, 297.5f, currentY, paint)
            currentY += 14f
        }

        val contactStr = "Phone: ${profile.phone.ifBlank { "N/A" }} | Email: ${profile.email.ifBlank { "N/A" }}"
        canvas.drawText(contactStr, 297.5f, currentY, paint)
        currentY += 16f

        // Horizontal Divider Line
        paint.color = Color.parseColor("#800000")
        paint.strokeWidth = 2f
        canvas.drawLine(36f, currentY, 559f, currentY, paint)
        currentY += 24f

        // Report Hero Title
        paint.color = Color.parseColor("#2E7D32") // Emerald Green
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("SALE REPORT", 297.5f, currentY, paint)

        currentY += 16f
        paint.color = Color.parseColor("#666666")
        paint.textSize = 11f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Period: $dateRangeText", 297.5f, currentY, paint)
        currentY += 30f

        // Summary Box Card
        val totalRevenue = orders.sumOf { it.totalAmount }
        paint.color = Color.parseColor("#F4F6F9")
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(36f, currentY, 559f, currentY + 50f, 12f, 12f, paint)

        paint.color = Color.parseColor("#1F2937")
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("TOTAL SALES: ${orders.size}", 60f, currentY + 30f, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("TOTAL REVENUE: Rs. ${"%.2f".format(totalRevenue)}", 535f, currentY + 30f, paint)

        currentY += 70f

        // Table Header
        paint.color = Color.parseColor("#800000")
        paint.style = Paint.Style.FILL
        canvas.drawRect(36f, currentY, 559f, currentY + 24f, paint)

        paint.color = Color.WHITE
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.LEFT

        canvas.drawText("SR #", 46f, currentY + 16f, paint)
        canvas.drawText("RECEIPT NO", 90f, currentY + 16f, paint)
        canvas.drawText("DATE", 220f, currentY + 16f, paint)
        canvas.drawText("CUSTOMER", 330f, currentY + 16f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("AMOUNT (Rs)", 549f, currentY + 16f, paint)

        currentY += 24f

        // Table Rows
        var srNo = 1
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())

        orders.forEach { order ->
            if (currentY > 780f) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                currentY = 50f
            }

            // Alternating row background
            if (srNo % 2 == 0) {
                paint.color = Color.parseColor("#F9FAFB")
                paint.style = Paint.Style.FILL
                canvas.drawRect(36f, currentY, 559f, currentY + 22f, paint)
            }

            paint.color = Color.parseColor("#1F2937")
            paint.textSize = 9f
            paint.typeface = Typeface.DEFAULT
            paint.textAlign = Paint.Align.LEFT

            canvas.drawText("$srNo", 46f, currentY + 15f, paint)
            canvas.drawText(order.orderNumber, 90f, currentY + 15f, paint)
            canvas.drawText(dateFormat.format(order.timestamp), 220f, currentY + 15f, paint)
            canvas.drawText(order.customerName?.take(20) ?: "Walk-in", 330f, currentY + 15f, paint)

            paint.textAlign = Paint.Align.RIGHT
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("%.2f".format(order.totalAmount), 549f, currentY + 15f, paint)

            currentY += 22f
            srNo++
        }

        pdfDocument.finishPage(page)

        // Save PDF to Documents/POSAPP/Reports/
        val fileName = "Sale_Report_${System.currentTimeMillis()}.pdf"
        val file = File(getReportsDirectory(), fileName)
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()

        openOrShareFile(context, file, "application/pdf")
    }

    // --- 2. SALE REPORT EXCEL (.csv) ---
    fun exportSaleReportExcel(context: Context, profile: ShopProfile, orders: List<Order>, dateRangeText: String) {
        val fileName = "Sale_Report_${System.currentTimeMillis()}.csv"
        val file = File(getReportsDirectory(), fileName)
        val writer = FileWriter(file)

        writer.append("SHOP NAME,${profile.name.ifBlank { "POS BILLING" }}\n")
        writer.append("ADDRESS,${profile.address.replace(",", " ") }\n")
        writer.append("CONTACT,${profile.phone} | ${profile.email}\n")
        writer.append("REPORT,Sale Report\n")
        writer.append("PERIOD,$dateRangeText\n\n")

        writer.append("SR No,Receipt No,Date,Customer Name,Phone,Total Amount (INR)\n")

        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        var srNo = 1
        var totalAmount = 0.0

        orders.forEach { order ->
            writer.append("$srNo,")
            writer.append("${order.orderNumber},")
            writer.append("${dateFormat.format(order.timestamp)},")
            writer.append("\"${order.customerName ?: "Walk-in"}\",")
            writer.append("\"${order.customerPhone ?: ""}\",")
            writer.append("${"%.2f".format(order.totalAmount)}\n")
            totalAmount += order.totalAmount
            srNo++
        }

        writer.append("\nTOTAL,,,,,$totalAmount\n")
        writer.flush()
        writer.close()

        openOrShareFile(context, file, "text/csv")
    }

    // --- 3. EXPENSE REPORT PDF ---
    fun exportExpenseReportPdf(context: Context, profile: ShopProfile, expenses: List<CashTransaction>, dateRangeText: String) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }
        var currentY = 50f

        paint.color = Color.parseColor("#800000")
        paint.textSize = 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(profile.name.ifBlank { "POS BILLING" }, 297.5f, currentY, paint)

        currentY += 18f
        paint.color = Color.parseColor("#555555")
        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT
        if (profile.address.isNotBlank()) {
            canvas.drawText(profile.address, 297.5f, currentY, paint)
            currentY += 14f
        }

        val contactStr = "Phone: ${profile.phone.ifBlank { "N/A" }} | Email: ${profile.email.ifBlank { "N/A" }}"
        canvas.drawText(contactStr, 297.5f, currentY, paint)
        currentY += 16f

        paint.color = Color.parseColor("#800000")
        paint.strokeWidth = 2f
        canvas.drawLine(36f, currentY, 559f, currentY, paint)
        currentY += 24f

        paint.color = Color.parseColor("#C62828") // Red
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("EXPENSE REPORT", 297.5f, currentY, paint)

        currentY += 16f
        paint.color = Color.parseColor("#666666")
        paint.textSize = 11f
        paint.typeface = Typeface.DEFAULT
        canvas.drawText("Period: $dateRangeText", 297.5f, currentY, paint)
        currentY += 30f

        val totalExpense = expenses.sumOf { it.amount }
        paint.color = Color.parseColor("#F4F6F9")
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(36f, currentY, 559f, currentY + 50f, 12f, 12f, paint)

        paint.color = Color.parseColor("#1F2937")
        paint.textSize = 12f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("TOTAL TRANSACTIONS: ${expenses.size}", 60f, currentY + 30f, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("TOTAL EXPENSE: Rs. ${"%.2f".format(totalExpense)}", 535f, currentY + 30f, paint)

        currentY += 70f

        paint.color = Color.parseColor("#800000")
        paint.style = Paint.Style.FILL
        canvas.drawRect(36f, currentY, 559f, currentY + 24f, paint)

        paint.color = Color.WHITE
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.LEFT

        canvas.drawText("SR #", 46f, currentY + 16f, paint)
        canvas.drawText("REASON / CATEGORY", 90f, currentY + 16f, paint)
        canvas.drawText("DATE", 350f, currentY + 16f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("AMOUNT (Rs)", 549f, currentY + 16f, paint)

        currentY += 24f

        var srNo = 1
        val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())

        expenses.forEach { tx ->
            if (currentY > 780f) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                currentY = 50f
            }

            if (srNo % 2 == 0) {
                paint.color = Color.parseColor("#F9FAFB")
                paint.style = Paint.Style.FILL
                canvas.drawRect(36f, currentY, 559f, currentY + 22f, paint)
            }

            paint.color = Color.parseColor("#1F2937")
            paint.textSize = 9f
            paint.typeface = Typeface.DEFAULT
            paint.textAlign = Paint.Align.LEFT

            canvas.drawText("$srNo", 46f, currentY + 15f, paint)
            canvas.drawText(tx.reason.take(35), 90f, currentY + 15f, paint)
            canvas.drawText(dateFormat.format(tx.timestamp), 350f, currentY + 15f, paint)

            paint.textAlign = Paint.Align.RIGHT
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("%.2f".format(tx.amount), 549f, currentY + 15f, paint)

            currentY += 22f
            srNo++
        }

        pdfDocument.finishPage(page)

        val fileName = "Expense_Report_${System.currentTimeMillis()}.pdf"
        val file = File(getReportsDirectory(), fileName)
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()

        openOrShareFile(context, file, "application/pdf")
    }

    // --- 4. EXPENSE REPORT EXCEL (.csv) ---
    fun exportExpenseReportExcel(context: Context, profile: ShopProfile, expenses: List<CashTransaction>, dateRangeText: String) {
        val fileName = "Expense_Report_${System.currentTimeMillis()}.csv"
        val file = File(getReportsDirectory(), fileName)
        val writer = FileWriter(file)

        writer.append("SHOP NAME,${profile.name.ifBlank { "POS BILLING" }}\n")
        writer.append("ADDRESS,${profile.address.replace(",", " ")}\n")
        writer.append("CONTACT,${profile.phone} | ${profile.email}\n")
        writer.append("REPORT,Expense Report\n")
        writer.append("PERIOD,$dateRangeText\n\n")

        writer.append("SR No,Reason / Category,Type,Date,Amount (INR)\n")

        val dateFormat = SimpleDateFormat("dd/MM/yyyy hh:mm a", Locale.getDefault())
        var srNo = 1
        var totalAmount = 0.0

        expenses.forEach { tx ->
            writer.append("$srNo,")
            writer.append("\"${tx.reason.replace(",", " ")}\",")
            writer.append("${tx.type},")
            writer.append("${dateFormat.format(tx.timestamp)},")
            writer.append("${"%.2f".format(tx.amount)}\n")
            totalAmount += tx.amount
            srNo++
        }

        writer.append("\nTOTAL,,,,$totalAmount\n")
        writer.flush()
        writer.close()

        openOrShareFile(context, file, "text/csv")
    }

    // --- 5. STOCK SUMMARY REPORT PDF ---
    fun exportStockReportPdf(context: Context, profile: ShopProfile, products: List<Product>) {
        val pdfDocument = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
        var page = pdfDocument.startPage(pageInfo)
        var canvas = page.canvas

        val paint = Paint().apply { isAntiAlias = true }
        var currentY = 50f

        paint.color = Color.parseColor("#800000")
        paint.textSize = 22f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.CENTER
        canvas.drawText(profile.name.ifBlank { "POS BILLING" }, 297.5f, currentY, paint)

        currentY += 18f
        paint.color = Color.parseColor("#555555")
        paint.textSize = 10f
        paint.typeface = Typeface.DEFAULT
        if (profile.address.isNotBlank()) {
            canvas.drawText(profile.address, 297.5f, currentY, paint)
            currentY += 14f
        }

        val contactStr = "Phone: ${profile.phone.ifBlank { "N/A" }} | Email: ${profile.email.ifBlank { "N/A" }}"
        canvas.drawText(contactStr, 297.5f, currentY, paint)
        currentY += 16f

        paint.color = Color.parseColor("#800000")
        paint.strokeWidth = 2f
        canvas.drawLine(36f, currentY, 559f, currentY, paint)
        currentY += 24f

        paint.color = Color.parseColor("#1565C0") // Blue
        paint.textSize = 18f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        canvas.drawText("STOCK SUMMARY REPORT", 297.5f, currentY, paint)

        currentY += 30f

        val totalValuation = products.sumOf { it.stock * it.costPrice }
        val totalQty = products.sumOf { it.stock }

        paint.color = Color.parseColor("#F4F6F9")
        paint.style = Paint.Style.FILL
        canvas.drawRoundRect(36f, currentY, 559f, currentY + 50f, 12f, 12f, paint)

        paint.color = Color.parseColor("#1F2937")
        paint.textSize = 11f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.LEFT
        canvas.drawText("TOTAL PRODUCTS: ${products.size} | QTY: ${"%.0f".format(totalQty)}", 50f, currentY + 30f, paint)

        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("STOCK VALUATION: Rs. ${"%.2f".format(totalValuation)}", 545f, currentY + 30f, paint)

        currentY += 70f

        paint.color = Color.parseColor("#800000")
        paint.style = Paint.Style.FILL
        canvas.drawRect(36f, currentY, 559f, currentY + 24f, paint)

        paint.color = Color.WHITE
        paint.textSize = 10f
        paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        paint.textAlign = Paint.Align.LEFT

        canvas.drawText("SR #", 46f, currentY + 16f, paint)
        canvas.drawText("PRODUCT NAME", 90f, currentY + 16f, paint)
        canvas.drawText("STOCK QTY", 280f, currentY + 16f, paint)
        canvas.drawText("COST (Rs)", 380f, currentY + 16f, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText("TOTAL VALUATION (Rs)", 549f, currentY + 16f, paint)

        currentY += 24f

        var srNo = 1
        products.forEach { prod ->
            if (currentY > 780f) {
                pdfDocument.finishPage(page)
                page = pdfDocument.startPage(pageInfo)
                canvas = page.canvas
                currentY = 50f
            }

            if (srNo % 2 == 0) {
                paint.color = Color.parseColor("#F9FAFB")
                paint.style = Paint.Style.FILL
                canvas.drawRect(36f, currentY, 559f, currentY + 22f, paint)
            }

            paint.color = Color.parseColor("#1F2937")
            paint.textSize = 9f
            paint.typeface = Typeface.DEFAULT
            paint.textAlign = Paint.Align.LEFT

            canvas.drawText("$srNo", 46f, currentY + 15f, paint)
            canvas.drawText(prod.name.take(25), 90f, currentY + 15f, paint)
            canvas.drawText("%.0f".format(prod.stock), 280f, currentY + 15f, paint)
            canvas.drawText("%.2f".format(prod.costPrice), 380f, currentY + 15f, paint)

            paint.textAlign = Paint.Align.RIGHT
            paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            canvas.drawText("%.2f".format(prod.stock * prod.costPrice), 549f, currentY + 15f, paint)

            currentY += 22f
            srNo++
        }

        pdfDocument.finishPage(page)

        val fileName = "Stock_Report_${System.currentTimeMillis()}.pdf"
        val file = File(getReportsDirectory(), fileName)
        pdfDocument.writeTo(FileOutputStream(file))
        pdfDocument.close()

        openOrShareFile(context, file, "application/pdf")
    }

    // --- 6. STOCK SUMMARY REPORT EXCEL (.csv) ---
    fun exportStockReportExcel(context: Context, profile: ShopProfile, products: List<Product>) {
        val fileName = "Stock_Report_${System.currentTimeMillis()}.csv"
        val file = File(getReportsDirectory(), fileName)
        val writer = FileWriter(file)

        writer.append("SHOP NAME,${profile.name.ifBlank { "POS BILLING" }}\n")
        writer.append("ADDRESS,${profile.address.replace(",", " ")}\n")
        writer.append("CONTACT,${profile.phone} | ${profile.email}\n")
        writer.append("REPORT,Stock Summary Report\n\n")

        writer.append("SR No,Product Name,Barcode,Stock Qty,Cost Price (INR),Selling Price (INR),Valuation (INR)\n")

        var srNo = 1
        var totalValuation = 0.0

        products.forEach { prod ->
            val valuation = prod.stock * prod.costPrice
            writer.append("$srNo,")
            writer.append("\"${prod.name.replace(",", " ")}\",")
            writer.append("\"${prod.barcode ?: ""}\",")
            writer.append("${"%.0f".format(prod.stock)},")
            writer.append("${"%.2f".format(prod.costPrice)},")
            writer.append("${"%.2f".format(prod.price)},")
            writer.append("${"%.2f".format(valuation)}\n")
            totalValuation += valuation
            srNo++
        }

        writer.append("\nTOTAL VALUATION,,,,,,$totalValuation\n")
        writer.flush()
        writer.close()

        openOrShareFile(context, file, "text/csv")
    }
}