package com.example.securitymerch;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.securitymerch.ui.gallery.GalleryFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.List;

public class DeleteProduct extends AppCompatActivity {

    private EditText editTextQuantity, editTextBarcode;
    private Button buttonScanBarcode, buttonRemoveProduct;
    private ImageView imageViewProduct;

    private FirebaseFirestore firestore;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.delete_product);

        // Inicializar vistas
        editTextQuantity = findViewById(R.id.editText_quantity);
        editTextBarcode = findViewById(R.id.editText_barcode);
        buttonScanBarcode = findViewById(R.id.button_scan_barcode);
        buttonRemoveProduct = findViewById(R.id.button_remove_product);
        imageViewProduct = findViewById(R.id.imageView_product);

        // Inicializar Firestore y Auth
        firestore = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid()
                : null;

        if (userId == null) {
            Toast.makeText(this, "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Configurar botones
        buttonScanBarcode.setOnClickListener(v -> openBarcodeScanner());
        buttonRemoveProduct.setOnClickListener(v -> removeProduct());
    }

    private void openBarcodeScanner() {
        Intent intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 101);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == RESULT_OK && data != null) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            if (bitmap != null) {
                scanBarcodeFromBitmap(bitmap);
            }
        }
    }

    private void scanBarcodeFromBitmap(Bitmap bitmap) {
        try {
            InputImage image = InputImage.fromBitmap(bitmap, 0);
            BarcodeScanner scanner = BarcodeScanning.getClient();

            scanner.process(image)
                    .addOnSuccessListener(this::handleBarcodeResult)
                    .addOnFailureListener(e -> Toast.makeText(this, "Error al escanear el código", Toast.LENGTH_SHORT).show());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void handleBarcodeResult(List<Barcode> barcodes) {
        if (!barcodes.isEmpty()) {
            Barcode barcode = barcodes.get(0);
            editTextBarcode.setText(barcode.getRawValue());
            Toast.makeText(this, "Código escaneado: " + barcode.getRawValue(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No se detectó ningún código", Toast.LENGTH_SHORT).show();
        }
    }

    private void removeProduct() {
        String quantityStr = editTextQuantity.getText().toString().trim();
        String barcode = editTextBarcode.getText().toString().trim();

        if (TextUtils.isEmpty(quantityStr)) {
            Toast.makeText(this, "Por favor, ingresa la cantidad", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(barcode)) {
            Toast.makeText(this, "Por favor, ingresa el código de barras", Toast.LENGTH_SHORT).show();
            return;
        }

        int quantityToRemove;
        try {
            quantityToRemove = Integer.parseInt(quantityStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, "Cantidad inválida", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collectionGroup("products")
                .whereEqualTo("userId", userId)
                .whereEqualTo("barcode", barcode)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            int currentQuantity = document.getLong("quantity").intValue();
                            if (quantityToRemove > currentQuantity) {
                                Toast.makeText(this, "La cantidad a eliminar excede el stock actual", Toast.LENGTH_SHORT).show();
                                return;
                            }

                            int updatedQuantity = currentQuantity - quantityToRemove;
                            if (updatedQuantity > 0) {
                                // Actualizar la cantidad en Firestore
                                document.getReference().update("quantity", updatedQuantity)
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this, "Producto actualizado correctamente", Toast.LENGTH_SHORT).show();
                                            navigateToGallery();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Error al actualizar el producto", Toast.LENGTH_SHORT).show();
                                        });
                            } else {
                                // Eliminar el producto si la cantidad llega a 0
                                document.getReference().delete()
                                        .addOnSuccessListener(aVoid -> {
                                            Toast.makeText(this, "Producto eliminado correctamente", Toast.LENGTH_SHORT).show();
                                            navigateToGallery();
                                        })
                                        .addOnFailureListener(e -> {
                                            Toast.makeText(this, "Error al eliminar el producto", Toast.LENGTH_SHORT).show();
                                        });
                            }
                        }
                    } else {
                        Toast.makeText(this, "Producto no encontrado", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(this, "Error al buscar el producto", Toast.LENGTH_SHORT).show();
                });
    }

    private void navigateToGallery() {
        // Redirigir al `GalleryFragment` dentro de la actividad principal
        Intent intent = new Intent(this, GalleryFragment.class);
        intent.putExtra("navigateTo", "fragment_gallery");
        startActivity(intent);
        finish();
    }
}
