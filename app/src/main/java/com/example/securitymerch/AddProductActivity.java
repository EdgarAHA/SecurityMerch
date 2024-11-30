package com.example.securitymerch;

import android.net.Uri;
import android.os.Bundle;
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

import java.util.UUID;

public class AddProductActivity extends AppCompatActivity {
    private EditText nameInput, quantityInput, barcodeInput;
    private ImageView productImage;
    private Uri imageUri;
    private String category;

    private FirebaseFirestore firestore;
    private StorageReference storageReference;

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

        // Seleccionar imagen
        ActivityResultLauncher<String> launcher = registerForActivityResult(
                new ActivityResultContracts.GetContent(),
                uri -> {
                    if (uri != null) {
                        imageUri = uri;
                        productImage.setImageURI(uri);
                    }
                });
        uploadImageButton.setOnClickListener(v -> launcher.launch("image/*"));

        // Guardar producto
        saveButton.setOnClickListener(v -> saveProduct());
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
