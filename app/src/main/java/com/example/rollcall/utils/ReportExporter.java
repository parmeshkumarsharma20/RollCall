package com.example.rollcall.utils;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.pdf.PdfDocument;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;

import androidx.core.content.FileProvider;

import com.example.rollcall.model.AttendanceRecord;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ReportExporter {

    public static void exportToCSV(Context context, String title, List<AttendanceRecord> records) {
        try {
            File exportDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            if (exportDir != null && !exportDir.exists()) {
                exportDir.mkdirs();
            }

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            File csvFile = new File(exportDir, "RollCall_Report_" + timeStamp + ".csv");
            FileWriter writer = new FileWriter(csvFile);

            // Write CSV Header
            writer.append("Record ID,Student Name,Roll Number,Subject ID,Marked At,Distance (m),Status\n");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US);
            for (AttendanceRecord r : records) {
                String dateStr = sdf.format(new Date(r.getMarkedAt() > 0 ? r.getMarkedAt() : System.currentTimeMillis()));
                writer.append(String.format(Locale.US, "%s,%s,%s,%s,%s,%.1f,%s\n",
                        r.getRecordId() != null ? r.getRecordId() : "N/A",
                        r.getStudentName() != null ? r.getStudentName() : "Student",
                        r.getRollNumber() != null ? r.getRollNumber() : "N/A",
                        r.getSubjectId() != null ? r.getSubjectId() : "N/A",
                        dateStr,
                        r.getDistanceMeters(),
                        r.getStatus() != null ? r.getStatus() : "PRESENT"
                ));
            }

            writer.flush();
            writer.close();

            shareFile(context, csvFile, "text/csv");
            Toast.makeText(context, "CSV Report generated successfully!", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            Toast.makeText(context, "Error exporting CSV: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    public static void exportToPDF(Context context, String title, List<AttendanceRecord> records) {
        PdfDocument document = new PdfDocument();
        PdfDocument.PageInfo pageInfo = new PdfDocument.PageInfo.Builder(595, 842, 1).create(); // A4 Size
        PdfDocument.Page page = document.startPage(pageInfo);

        Canvas canvas = page.getCanvas();
        Paint paint = new Paint();

        // Header Background (Deep Blue)
        paint.setColor(Color.parseColor("#172B75"));
        canvas.drawRect(0, 0, 595, 90, paint);

        // Header Title
        paint.setColor(Color.WHITE);
        paint.setTextSize(22);
        paint.setFakeBoldText(true);
        canvas.drawText("RollCall - Attendance Report", 30, 45, paint);

        paint.setTextSize(12);
        paint.setFakeBoldText(false);
        String dateStr = new SimpleDateFormat("dd MMM yyyy, HH:mm", Locale.US).format(new Date());
        canvas.drawText("Generated: " + dateStr + " • " + title, 30, 70, paint);

        // Table Header
        int y = 130;
        paint.setColor(Color.parseColor("#2457D6"));
        paint.setTextSize(13);
        paint.setFakeBoldText(true);
        canvas.drawText("Student Name", 30, y, paint);
        canvas.drawText("Roll No", 200, y, paint);
        canvas.drawText("Date & Time", 320, y, paint);
        canvas.drawText("Dist (m)", 470, y, paint);
        canvas.drawText("Status", 530, y, paint);

        // Divider
        paint.setColor(Color.parseColor("#E2E8F0"));
        paint.setStrokeWidth(1);
        canvas.drawLine(30, y + 8, 565, y + 8, paint);

        // Records Rows
        y += 30;
        paint.setColor(Color.parseColor("#172554"));
        paint.setTextSize(12);
        paint.setFakeBoldText(false);

        SimpleDateFormat recordDateFormat = new SimpleDateFormat("dd/MM HH:mm", Locale.US);

        for (int i = 0; i < Math.min(records.size(), 22); i++) {
            AttendanceRecord r = records.get(i);
            String name = r.getStudentName() != null ? r.getStudentName() : "Student";
            if (name.length() > 22) name = name.substring(0, 20) + "..";

            canvas.drawText(name, 30, y, paint);
            canvas.drawText(r.getRollNumber() != null ? r.getRollNumber() : "N/A", 200, y, paint);
            canvas.drawText(recordDateFormat.format(new Date(r.getMarkedAt() > 0 ? r.getMarkedAt() : System.currentTimeMillis())), 320, y, paint);
            canvas.drawText(String.format(Locale.US, "%.1fm", r.getDistanceMeters()), 470, y, paint);

            paint.setColor(Color.parseColor("PRESENT".equalsIgnoreCase(r.getStatus()) ? "#20B875" : "#E5484D"));
            paint.setFakeBoldText(true);
            canvas.drawText(r.getStatus() != null ? r.getStatus() : "PRESENT", 530, y, paint);

            paint.setColor(Color.parseColor("#172554"));
            paint.setFakeBoldText(false);

            y += 26;
        }

        // Footer
        paint.setColor(Color.parseColor("#64748B"));
        paint.setTextSize(10);
        canvas.drawText("RollCall Smart QR-Based Attendance System • Scan. Verify. Attend.", 30, 810, paint);

        document.finishPage(page);

        try {
            File exportDir = context.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
            if (exportDir != null && !exportDir.exists()) {
                exportDir.mkdirs();
            }

            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(new Date());
            File pdfFile = new File(exportDir, "RollCall_Report_" + timeStamp + ".pdf");
            FileOutputStream fos = new FileOutputStream(pdfFile);
            document.writeTo(fos);
            document.close();
            fos.close();

            shareFile(context, pdfFile, "application/pdf");
            Toast.makeText(context, "PDF Report generated successfully!", Toast.LENGTH_SHORT).show();

        } catch (IOException e) {
            Toast.makeText(context, "Error exporting PDF: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private static void shareFile(Context context, File file, String mimeType) {
        try {
            Uri fileUri = FileProvider.getUriForFile(context, context.getPackageName() + ".fileprovider", file);
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType(mimeType);
            intent.putExtra(Intent.EXTRA_STREAM, fileUri);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            context.startActivity(Intent.createChooser(intent, "Share Attendance Report"));
        } catch (Exception e) {
            Toast.makeText(context, "File saved at: " + file.getAbsolutePath(), Toast.LENGTH_LONG).show();
        }
    }
}
