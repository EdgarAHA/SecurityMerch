package com.example.securitymerch;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securitymerch.adapters.ProductAdapter;
import com.example.securitymerch.models.Product;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class Galletas extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList = new ArrayList<>();
    private FirebaseFirestore firestore;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.galletas);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        productAdapter = new ProductAdapter(productList);
        recyclerView.setAdapter(productAdapter);

        firestore = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Cargar productos de la categoría "Galletas" filtrados por el usuario actual
        loadUserProducts();

        // Configuración del botón flotante
        FloatingActionButton fabAddProduct = findViewById(R.id.fab_add_galletas);
        fabAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(Galletas.this, AddProductActivity.class);
            intent.putExtra("category", "Galletas"); // Pasar la categoría
            startActivity(intent);
        });
    }

    private void loadUserProducts() {
        firestore.collection("categories").document("Galletas")
                .collection("products")
                .whereEqualTo("userId", userId) // Filtrar por el usuario actual
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productList.clear(); // Limpia la lista antes de agregar los productos
                    productList.addAll(queryDocumentSnapshots.toObjects(Product.class));
                    productAdapter.notifyDataSetChanged(); // Notifica cambios al adaptador
                })
                .addOnFailureListener(e -> {
                    // Manejo de errores
                    Log.e("Galletas", "Error al obtener productos", e);
                });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar productos de Firestore
        loadUserProducts();
    }
}
