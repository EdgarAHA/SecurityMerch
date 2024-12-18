package com.example.securitymerch;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import androidx.appcompat.widget.SearchView;

import com.example.securitymerch.adapters.ProductAdapter;
import com.example.securitymerch.models.Product;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;

public class Botanas extends AppCompatActivity {

    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList = new ArrayList<>();
    private FirebaseFirestore firestore;
    private String userId;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.botanas);

        recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        productAdapter = new ProductAdapter(productList);
        recyclerView.setAdapter(productAdapter);

        firestore = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Configurar barra de búsqueda
        SearchView searchView = findViewById(R.id.search_view);
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
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

        // Cargar productos de la categoría "Botanas" filtrados por el usuario actual
        loadUserProducts();

        // Configuración del botón flotante
        FloatingActionButton fabAddProduct = findViewById(R.id.fab_add_botanas);
        fabAddProduct.setOnClickListener(v -> {
            Intent intent = new Intent(Botanas.this, AddProductActivity.class);
            intent.putExtra("category", "Botanas"); // Pasar la categoría
            startActivity(intent);
        });
    }

    private void loadUserProducts() {
        firestore.collection("categories").document("Botanas")
                .collection("products")
                .whereEqualTo("userId", userId) // Filtrar por el usuario actual
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    productList.clear(); // Limpia la lista antes de agregar los productos
                    productList.addAll(queryDocumentSnapshots.toObjects(Product.class));
                    productAdapter.updateList(productList); // Notificar cambios al adaptador
                })
                .addOnFailureListener(e -> {
                    // Manejo de errores
                    Log.e("Botanas", "Error al obtener productos", e);
                });
    }

    private void filterProducts(String query) {
        List<Product> filteredList = new ArrayList<>();
        for (Product product : productList) {
            // Filtrar productos que contengan la consulta en el nombre
            if (product.getName().toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(product);
            }
        }
        productAdapter.updateList(filteredList); // Actualizar adaptador con la lista filtrada
    }

    @Override
    protected void onResume() {
        super.onResume();
        // Recargar productos de Firestore
        loadUserProducts();
    }
}
