package com.example.securitymerch;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;

public class CambiarContraseña extends AppCompatActivity {

    private EditText etEmail;
    private Button btnResetPassword;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cambiarcontra);

        // Inicializar Firebase Auth
        auth = FirebaseAuth.getInstance();

        // Referencias a los elementos del diseño
        etEmail = findViewById(R.id.etEmail);
        btnResetPassword = findViewById(R.id.btnResetPassword);

        // Configurar botón para enviar la recuperación
        btnResetPassword.setOnClickListener(v -> resetPassword());
    }

    private void resetPassword() {
        String email = etEmail.getText().toString().trim();

        // Verificar que el campo no esté vacío
        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Por favor, ingrese su correo electrónico", Toast.LENGTH_SHORT).show();
            return;
        }

        // Enviar solicitud de recuperación de contraseña
        auth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        Toast.makeText(this, "Instrucciones enviadas a su correo", Toast.LENGTH_SHORT).show();
                        Log.d("CambiarContraseña", "Correo enviado a: " + email);
                        finish();
                    } else {
                        Toast.makeText(this, "Error al enviar las instrucciones", Toast.LENGTH_SHORT).show();
                        Log.e("CambiarContraseña", "Error: ", task.getException());
                    }
                });
    }
}
