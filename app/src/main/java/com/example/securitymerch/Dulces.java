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

public class Dulces extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList = new ArrayList<>();
    private FirebaseFirestore firestore;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dulces);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        productAdapter = new ProductAdapter(productList);
        recyclerView.setAdapter(productAdapter);

        firestore = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Cargar productos de la categoría "Dulces" filtrados por el usuario actual
        loadUserProducts();

        // Configuración del botón flotante
        FloatingActionButton fabAddProduct = findViewById(R.id.fab_add_dulces);
        fabAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(Dulces.this, AddProductActivity.class);
            intent.putExtra("category", "Dulces"); // Pasar la categoría
            startActivity(intent);
        });

        // Configuración de la barra de búsqueda
        androidx.appcompat.widget.SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new androidx.appcompat.widget.SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                filterProducts(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterProducts(newText);
                return false;
            }
        });
    }

    private void loadUserProducts() {
        firestore.collection("categories").document("Dulces")
                .collection("products")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productList.clear();
                    productList.addAll(queryDocumentSnapshots.toObjects(Product.class));
                    productAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Log.e("Dulces", "Error al obtener productos", e));
    }

    private void filterProducts(String query) {
        List<Product> filteredList = new ArrayList<>();
        for (Product product : productList) {
            if (product.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(product);
            }
        }
        productAdapter.updateList(filteredList);
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadUserProducts();
    }
}
