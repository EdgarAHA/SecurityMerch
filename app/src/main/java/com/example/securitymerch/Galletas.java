package com.example.securitymerch;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class Galletas extends AppCompatActivity {

    private RecyclerView recyclerView;
    private com.example.securitymerch.adapters.ProductAdapter adapter;
    private List<com.example.securitymerch.models.Product> productList;
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.galletas);

        // Inicializar Firestore
        firestore = FirebaseFirestore.getInstance();

        // Configurar RecyclerView
        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        productList = new ArrayList<>();
        adapter = new com.example.securitymerch.adapters.ProductAdapter(productList);
        recyclerView.setAdapter(adapter);

        // Cargar productos de la categoría "Bebidas"
        loadProducts();

        // Configuración del botón flotante
        FloatingActionButton fabAddProduct = findViewById(R.id.fab_add_galletas);
        fabAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(Galletas.this, AddProductActivity.class);
            intent.putExtra("category", "Galletas"); // Pasar la categoría
            startActivity(intent);
        });

    }

    private void loadProducts() {
        firestore.collection("products")
                .whereEqualTo("category", "Galletas")
                .orderBy("name", Query.Direction.ASCENDING)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productList.clear();
                    productList.addAll(queryDocumentSnapshots.toObjects(com.example.securitymerch.models.Product.class));
                    adapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> {
                    // Manejo de errores (opcional)
                });
    }
}
