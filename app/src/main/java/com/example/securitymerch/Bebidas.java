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
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import java.util.ArrayList;
import java.util.List;

public class Bebidas extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList = new ArrayList<>();
    private FirebaseFirestore firestore;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bebidas);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        productAdapter = new ProductAdapter(productList);
        recyclerView.setAdapter(productAdapter);

        firestore = FirebaseFirestore.getInstance();

        // Cargar productos de la categoría "bebidas"
        firestore.collection("categories").document("Bebidas")
                .collection("products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productList.clear(); // Limpia la lista antes de agregar los productos
                    productList.addAll(queryDocumentSnapshots.toObjects(Product.class));
                    productAdapter.notifyDataSetChanged(); // Notifica cambios al adaptador
                })
                .addOnFailureListener(e -> {
                    // Manejo de errores
                    Log.e("Bebidas", "Error al obtener productos", e);
                });



        // Configuración del botón flotante
        FloatingActionButton fabAddProduct = findViewById(R.id.fab_add_bebida);
        fabAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(Bebidas.this, AddProductActivity.class);
            intent.putExtra("category", "Bebidas"); // Pasar la categoría
            startActivity(intent);
        });

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar productos de Firestore
        firestore.collection("categories").document("Bebidas")
                .collection("products")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productList.clear();
                    productList.addAll(queryDocumentSnapshots.toObjects(Product.class));
                    productAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("Bebidas", "Error al recargar productos", e));
    }

}
