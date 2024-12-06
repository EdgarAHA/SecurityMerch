package com.example.securitymerch.ui.gallery;

import android.content.Intent;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.securitymerch.DeleteProduct;
import com.example.securitymerch.R;
import com.example.securitymerch.adapters.ProductAdapter;
import com.example.securitymerch.models.Product;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.mlkit.vision.barcode.BarcodeScanner;
import com.google.mlkit.vision.barcode.BarcodeScanning;
import com.google.mlkit.vision.barcode.common.Barcode;
import com.google.mlkit.vision.common.InputImage;

import java.util.ArrayList;
import java.util.List;

public class GalleryFragment extends Fragment {

    private RecyclerView recyclerView;
    private ProductAdapter productAdapter;
    private List<Product> productList = new ArrayList<>();
    private FirebaseFirestore firestore;
    private FloatingActionButton fabScanDelete, fabOpenDeleteScreen;
    private TextView totalStockView;
    private String userId;
    private int totalStock = 0;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);

        recyclerView = root.findViewById(R.id.recycler_view_gallery);
        totalStockView = root.findViewById(R.id.text_total_stock);
        fabScanDelete = root.findViewById(R.id.fab_scan_delete);
        fabOpenDeleteScreen = root.findViewById(R.id.fab_open_delete_screen);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        productAdapter = new ProductAdapter(productList);
        recyclerView.setAdapter(productAdapter);

        firestore = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (userId != null) {
            loadUserInventory();
        } else {
            Toast.makeText(getContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show();
        }

        fabScanDelete.setOnClickListener(v -> openCameraToDelete());
        fabOpenDeleteScreen.setOnClickListener(v -> openDeleteScreen());

        return root;
    }

    private void openDeleteScreen() {
        Intent intent = new Intent(getActivity(), DeleteProduct.class);
        startActivity(intent);
    }

    private void loadUserInventory() {
        if (userId == null) {
            Log.e("GalleryFragment", "El userId es nulo. Asegúrate de que el usuario esté autenticado.");
            Toast.makeText(getContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collectionGroup("products")
                .whereEqualTo("userId", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty()) {
                        Log.d("GalleryFragment", "No se encontraron productos para este usuario.");
                        Toast.makeText(getContext(), "No tienes productos en el inventario.", Toast.LENGTH_SHORT).show();
                        productList.clear();
                        productAdapter.notifyDataSetChanged();
                        totalStockView.setText("Total de Productos: 0");
                        return;
                    }

                    productList.clear();
                    totalStock = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        Product product = document.toObject(Product.class);
                        if (product != null) {
                            productList.add(product);
                            totalStock += product.getQuantity();
                        }
                    }

                    productAdapter.notifyDataSetChanged();
                    totalStockView.setText("Total de Productos: " + totalStock);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Error al cargar el inventario.", Toast.LENGTH_SHORT).show();
                    Log.e("GalleryFragment", "Error al cargar el inventario", e);
                });
    }

    private void openCameraToDelete() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, 101);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 101 && resultCode == getActivity().RESULT_OK && data != null) {
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
                    .addOnSuccessListener(barcodes -> {
                        if (!barcodes.isEmpty()) {
                            String scannedCode = barcodes.get(0).getRawValue();
                            if (scannedCode != null) {
                                deleteProductByBarcode(scannedCode);
                            } else {
                                Toast.makeText(getContext(), "Código no válido.", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(getContext(), "No se detectó ningún código.", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(e -> Toast.makeText(getContext(), "Error al escanear el código.", Toast.LENGTH_SHORT).show());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void deleteProductByBarcode(String barcode) {
        if (barcode == null || barcode.isEmpty()) {
            Toast.makeText(getContext(), "El código es inválido.", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collectionGroup("products")
                .whereEqualTo("userId", userId)
                .whereEqualTo("barcode", barcode)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    if (!queryDocumentSnapshots.isEmpty()) {
                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                            Product product = document.toObject(Product.class);
                            if (product != null) {
                                int currentQuantity = product.getQuantity();
                                if (currentQuantity > 1) {
                                    int updatedQuantity = currentQuantity - 1;
                                    document.getReference().update("quantity", updatedQuantity)
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(getContext(), "Cantidad actualizada. Stock restante: " + updatedQuantity, Toast.LENGTH_SHORT).show();
                                                loadUserInventory();
                                            })
                                            .addOnFailureListener(e -> Log.e("GalleryFragment", "Error al actualizar el producto", e));
                                } else {
                                    document.getReference().delete()
                                            .addOnSuccessListener(aVoid -> {
                                                Toast.makeText(getContext(), "Producto eliminado.", Toast.LENGTH_SHORT).show();
                                                loadUserInventory();
                                            })
                                            .addOnFailureListener(e -> Log.e("GalleryFragment", "Error al eliminar el producto", e));
                                }
                            }
                        }
                    } else {
                        Toast.makeText(getContext(), "Producto no encontrado.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Log.e("GalleryFragment", "Error al buscar el producto", e));
    }
}
