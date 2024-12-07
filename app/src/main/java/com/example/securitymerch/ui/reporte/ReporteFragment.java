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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReporteFragment extends Fragment {

    private FirebaseFirestore firestore;
    private String userId;
    private Button btnGenerateReport;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_reporte, container, false);

        btnGenerateReport = root.findViewById(R.id.btn_generate_report);

        // Inicializar Firebase (si no está inicializado)
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
            document.add(new Paragraph("Reporte de Inventario").setFontSize(18).setBold().setMarginBottom(20));

            for (Product product : products) {
                document.add(new Paragraph("Nombre: " + product.getName()));
                document.add(new Paragraph("Cantidad: " + product.getQuantity()));
                document.add(new Paragraph("Código de Barras: " + product.getBarcode()));
                document.add(new Paragraph("\n"));
            }

            document.close();

            // Mostrar mensaje y abrir/compartir el PDF
            Toast.makeText(getContext(), "Reporte PDF generado", Toast.LENGTH_SHORT).show();
            openOrSharePDF(pdfFile);
        } catch (IOException e) {
            Toast.makeText(getContext(), "Error al generar el PDF.", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

    private void openOrSharePDF(File pdfFile) {
        Uri pdfUri = FileProvider.getUriForFile(requireContext(), requireContext().getPackageName() + ".fileprovider", pdfFile);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(pdfUri, "application/pdf");
        intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY | Intent.FLAG_GRANT_READ_URI_PERMISSION);

        Intent chooser = Intent.createChooser(intent, "Abrir o compartir PDF");
        startActivity(chooser);
    }
}
