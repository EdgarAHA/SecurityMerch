package com.example.securitymerch.ui.gallery;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
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
    private TextView totalStockView, minStockValueView;
    private SeekBar minStockSeekBar;
    private String userId;
    private int totalStock = 0;
    private int minStockThreshold = 10; // Valor inicial del stock mínimo

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_gallery, container, false);

        // Inicializar vistas
        recyclerView = root.findViewById(R.id.recycler_view_gallery);
        totalStockView = root.findViewById(R.id.text_total_stock);
        fabScanDelete = root.findViewById(R.id.fab_scan_delete);
        fabOpenDeleteScreen = root.findViewById(R.id.fab_open_delete_screen);
        minStockSeekBar = root.findViewById(R.id.seekBar_min_stock);
        minStockValueView = root.findViewById(R.id.text_min_stock_value);

        // Configurar RecyclerView
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        productAdapter = new ProductAdapter(productList);
        recyclerView.setAdapter(productAdapter);

        // Inicializar Firebase
        firestore = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser() != null
                ? FirebaseAuth.getInstance().getCurrentUser().getUid() : null;

        if (userId != null) {
            loadUserInventory();
        } else {
            Toast.makeText(getContext(), "Usuario no autenticado", Toast.LENGTH_SHORT).show();
        }

        // Configurar SeekBar para el rango de stock mínimo
        minStockSeekBar.setProgress(minStockThreshold);
        minStockValueView.setText(String.valueOf(minStockThreshold));
        minStockSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                minStockThreshold = progress;
                minStockValueView.setText(String.valueOf(progress));
                checkLowStockProducts(); // Revisar productos con stock bajo
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        // Configurar botones flotantes
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
                    checkLowStockProducts(); // Revisar productos con stock bajo
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

    private void checkLowStockProducts() {
        for (Product product : productList) {
            if (product.getQuantity() <= minStockThreshold) {
                sendLowStockNotification(product);
            }
        }
    }

    private void sendLowStockNotification(Product product) {
        NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
        String channelId = "low_stock_channel";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId, "Productos con bajo stock", NotificationManager.IMPORTANCE_HIGH);
            notificationManager.createNotificationChannel(channel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), channelId)
                .setSmallIcon(android.R.drawable.ic_dialog_alert) // Ícono integrado
                .setContentTitle("Producto con bajo stock")
                .setContentText("El producto \"" + product.getName() + "\" tiene un stock de " + product.getQuantity() + ".")
                .setPriority(NotificationCompat.PRIORITY_HIGH);

        notificationManager.notify(product.getBarcode().hashCode(), builder.build());
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserInventory(); // Asegurar que el inventario se actualice al regresar al fragmento.
    }
}
