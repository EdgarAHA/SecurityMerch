package com.example.securitymerch;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;

import com.example.securitymerch.models.Product;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.barcode.common.Barcode;

import java.util.List;
import java.util.UUID;

public class AddProductActivity extends AppCompatActivity {

    private EditText nameInput, quantityInput, barcodeInput;
    private ImageView productImage;
    private Uri imageUri;
    private String category;

    private FirebaseFirestore firestore;
    private StorageReference storageReference;

    private ActivityResultLauncher<Intent> barcodeScannerLauncher;
    private ActivityResultLauncher<String> imagePickerLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.add_product);

        // Inicializar Firestore y Storage
        firestore = FirebaseFirestore.getInstance();
        storageReference = FirebaseStorage.getInstance().getReference();

        // Capturar la categoría pasada desde el intent
        category = getIntent().getStringExtra("category");

        // Vincular vistas
        nameInput = findViewById(R.id.editText_product_name);
        quantityInput = findViewById(R.id.editText_quantity);
        barcodeInput = findViewById(R.id.editText_barcode);
        productImage = findViewById(R.id.imageView_product);
        Button uploadImageButton = findViewById(R.id.button_upload_image);
        Button saveButton = findViewById(R.id.button_save_product);
        Button scanBarcodeButton = findViewById(R.id.button_scan_barcode);

        // Configurar el escáner de código de barras
        setupBarcodeScanner();

        // Configurar el selector de imágenes
        setupImagePicker();

        // Seleccionar imagen
        uploadImageButton.setOnClickListener(v -> imagePickerLauncher.launch("image/*"));

        // Escanear código de barras
        scanBarcodeButton.setOnClickListener(v -> openBarcodeScanner());

        // Guardar producto
        saveButton.setOnClickListener(v -> saveProduct());
    }

    private void setupBarcodeScanner() {
        barcodeScannerLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                        Bundle extras = result.getData().getExtras();
                        if (extras != null) {
                            Bitmap bitmap = (Bitmap) extras.get("data");
                            if (bitmap != null) {
                                scanBarcodeFromBitmap(bitmap);
                            } else {
                                Toast.makeText(this, "No se pudo obtener la imagen", Toast.LENGTH_SHORT).show();
                            }
                        }
                    } else {
                        Toast.makeText(this, "Operación cancelada", Toast.LENGTH_SHORT).show();
                    }
                }
        );
    }

    private void openBarcodeScanner() {
        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            if (intent.resolveActivity(getPackageManager()) != null) {
                barcodeScannerLauncher.launch(intent);
            } else {
                Toast.makeText(this, "La cámara no está disponible", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al abrir la cámara", Toast.LENGTH_SHORT).show();
        }
    }

    private void scanBarcodeFromBitmap(Bitmap bitmap) {
        try {
            InputImage image = InputImage.fromBitmap(bitmap, 0);
            BarcodeScanner scanner = BarcodeScanning.getClient();

            scanner.process(image)
                    .addOnSuccessListener(this::handleBarcodeResult)
                    .addOnFailureListener(e -> {
                        e.printStackTrace();
                        Toast.makeText(this, "Error al escanear el código de barras", Toast.LENGTH_SHORT).show();
                    });
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al procesar la imagen", Toast.LENGTH_SHORT).show();
        }
    }

    private void handleBarcodeResult(List<Barcode> barcodes) {
        if (barcodes.size() > 0) {
            Barcode barcode = barcodes.get(0);
            barcodeInput.setText(barcode.getRawValue());
            Toast.makeText(this, "Código escaneado: " + barcode.getRawValue(), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "No se encontró ningún código de barras", Toast.LENGTH_SHORT).show();
        }
    }

    private void setupImagePicker() {
        imagePickerLauncher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imageUri = uri;
                        productImage.setImageURI(uri);
                    }
                });
    }

    private void saveProduct() {
        String name = nameInput.getText().toString();
        String quantity = quantityInput.getText().toString();
        String barcode = barcodeInput.getText().toString();

        if (name.isEmpty() || quantity.isEmpty() || imageUri == null) {
            Toast.makeText(this, "Por favor, complete todos los campos", Toast.LENGTH_SHORT).show();
            return;
        }

        // Subir imagen a Firebase Storage
        String imagePath = "products/" + UUID.randomUUID().toString();
        storageReference.child(imagePath).putFile(imageUri)
                .addOnSuccessListener(taskSnapshot -> storageReference.child(imagePath).getDownloadUrl()
                        .addOnSuccessListener(uri -> {
                            // Guardar datos en la subcolección correspondiente
                            Product product = new Product(name, Integer.parseInt(quantity), uri.toString(), barcode);
                            firestore.collection("categories").document(category)
                                    .collection("products").add(product)
                                    .addOnSuccessListener(documentReference -> {
                                        Toast.makeText(this, "Producto guardado exitosamente", Toast.LENGTH_SHORT).show();
                                        finish();
                                    })
                                    .addOnFailureListener(e -> Toast.makeText(this, "Error al guardar el producto", Toast.LENGTH_SHORT).show());
                        }))
                .addOnFailureListener(e -> Toast.makeText(this, "Error al subir la imagen", Toast.LENGTH_SHORT).show());
    }
}
