package com.example.securitymerch;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;

import java.util.ArrayList;
import java.util.List;

public class Legumbres extends AppCompatActivity {
    private RecyclerView recyclerView;
    private com.example.securitymerch.adapters.ProductAdapter productAdapter;
    private List<com.example.securitymerch.models.Product> productList = new ArrayList<>();
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.legumbres);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        productAdapter = new com.example.securitymerch.adapters.ProductAdapter(productList);
        recyclerView.setAdapter(productAdapter);

        firestore = FirebaseFirestore.getInstance();

        // Cargar productos de la categoría "bebidas"
        firestore.collection("categories").document("Legumbres")
                .collection("products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productList.clear(); // Limpia la lista antes de agregar los productos
                    productList.addAll(queryDocumentSnapshots.toObjects(com.example.securitymerch.models.Product.class));
                    productAdapter.notifyDataSetChanged(); // Notifica cambios al adaptador
                })
                .addOnFailureListener(e -> {
                    // Manejo de errores
                    Log.e("Legumbres", "Error al obtener productos", e);
                });

        // Configuración del botón flotante
        FloatingActionButton fabAddProduct = findViewById(R.id.fab_add_legumbres);
        fabAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(Legumbres.this, AddProductActivity.class);
            intent.putExtra("category", "Legumbres"); // Pasar la categoría
            startActivity(intent);
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar productos de Firestore
        firestore.collection("categories").document("Legumbres")
                .collection("products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productList.clear();
                    productList.addAll(queryDocumentSnapshots.toObjects(com.example.securitymerch.models.Product.class));
                    productAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("Legumbres", "Error al recargar productos", e));
    }

}
