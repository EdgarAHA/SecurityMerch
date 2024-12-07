package com.example.securitymerch.ui.reporte;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.fragment.app.Fragment;

import com.example.securitymerch.R;
import com.example.securitymerch.models.Product;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class ReporteFragment extends Fragment {

    private FirebaseFirestore firestore;
    private String userId;
    private Button btnGenerateReport, btnGenerateExcel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_reporte, container, false);

        btnGenerateReport = root.findViewById(R.id.btn_generate_report);
        btnGenerateExcel = root.findViewById(R.id.btn_generate_excel);

        // Inicializar Firebase
        FirebaseApp.initializeApp(requireContext());
        firestore = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        // Verificar y solicitar permisos de almacenamiento
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(requireActivity(), new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        btnGenerateReport.setOnClickListener(v -> generatePDF());
        btnGenerateExcel.setOnClickListener(v -> generateExcel());

        return root;
    }

    private void generatePDF() {
        if (userId == null) {
            Toast.makeText(getContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collectionGroup("products")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<Product> productList = new ArrayList<>();
                        for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Product product = document.toObject(Product.class);
                            productList.add(product);
                        }
                        createPDF(productList);
                    } else {
                        Toast.makeText(getContext(), "No hay productos en el inventario.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al obtener los productos.", Toast.LENGTH_SHORT).show();
                });
    }

    private void createPDF(List<Product> products) {
        // Crear una carpeta para guardar el PDF
        File pdfDirectory = new File(requireContext().getExternalFilesDir(null), "Reportes");
        if (!pdfDirectory.exists()) {
            pdfDirectory.mkdirs();
        }

        String fileName = "Reporte_Inventario.pdf";
        File pdfFile = new File(pdfDirectory, fileName);

        try (PdfWriter writer = new PdfWriter(new FileOutputStream(pdfFile))) {
            Document document = new Document(new com.itextpdf.kernel.pdf.PdfDocument(writer));

            // Configurar formato y zona horaria para la fecha
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
            dateFormat.setTimeZone(TimeZone.getDefault());
            String currentDate = dateFormat.format(new Date());

            // Agregar título y fecha
            document.add(new Paragraph("Reporte de Inventario").setFontSize(18).setBold().setMarginBottom(10));
            document.add(new Paragraph("Fecha: " + currentDate).setFontSize(12).setItalic().setMarginBottom(20));

            // Agregar los productos al PDF
            for (Product product : products) {
                document.add(new Paragraph("Nombre: " + product.getName()));
                document.add(new Paragraph("Cantidad: " + product.getQuantity()));
                document.add(new Paragraph("Código de Barras: " + product.getBarcode()));
                document.add(new Paragraph("\n"));
            }

            document.close();

            // Mostrar mensaje y abrir/compartir el PDF
            Toast.makeText(getContext(), "Reporte PDF generado", Toast.LENGTH_SHORT).show();
            openOrShareFile(pdfFile, "application/pdf");
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error al generar el PDF.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void generateExcel() {
        if (userId == null) {
            Toast.makeText(getContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collectionGroup("products")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        List<Product> productList = new ArrayList<>();
                        for (com.google.firebase.firestore.QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Product product = document.toObject(Product.class);
                            productList.add(product);
                        }
                        createExcel(productList);
                    } else {
                        Toast.makeText(getContext(), "No hay productos en el inventario.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al obtener los productos.", Toast.LENGTH_SHORT).show();
                });
    }

    private void createExcel(List<Product> products) {
        // Crear una carpeta para guardar el Excel
        File excelDirectory = new File(requireContext().getExternalFilesDir(null), "Reportes");
        if (!excelDirectory.exists()) {
            excelDirectory.mkdirs();
        }

        String fileName = "Reporte_Inventario.xlsx";
        File excelFile = new File(excelDirectory, fileName);

        XSSFWorkbook workbook = new XSSFWorkbook();
        XSSFSheet sheet = workbook.createSheet("Inventario");

        // Crear fila de encabezado
        Row headerRow = sheet.createRow(0);
        headerRow.createCell(0).setCellValue("Nombre");
        headerRow.createCell(1).setCellValue("Cantidad");
        headerRow.createCell(2).setCellValue("Código de Barras");

        // Agregar datos de los productos
        int rowNum = 1;
        for (Product product : products) {
            Row row = sheet.createRow(rowNum++);
            row.createCell(0).setCellValue(product.getName());
            row.createCell(1).setCellValue(product.getQuantity());
            row.createCell(2).setCellValue(product.getBarcode());
        }

        // Guardar el archivo Excel
        try (FileOutputStream fos = new FileOutputStream(excelFile)) {
            workbook.write(fos);
            workbook.close();

            // Mostrar mensaje y abrir/compartir el Excel
            Toast.makeText(getContext(), "Reporte Excel generado", Toast.LENGTH_SHORT).show();
            openOrShareFile(excelFile, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error al generar el Excel.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void openOrShareFile(File file, String mimeType) {
        Uri fileUri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".fileprovider", file);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(fileUri, mimeType);
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent chooser = Intent.createChooser(intent, "Abrir o compartir archivo");
        startActivity(chooser);
    }
}
