package com.example.securitymerch;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class MainActivity extends AppCompatActivity {

    private EditText editTextUsername, editTextPassword;
    private Button buttonLogin;
    private TextView textViewSignup, textViewForgotPassword;
    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Vincular vistas
        editTextUsername = findViewById(R.id.editTextUsername);
        editTextPassword = findViewById(R.id.editTextPassword);
        buttonLogin = findViewById(R.id.buttonLogin);
        textViewSignup = findViewById(R.id.textViewSignup);
        textViewForgotPassword = findViewById(R.id.textViewForgotPassword);

        // Inicializar Firebase Auth
        mAuth = FirebaseAuth.getInstance();

        // Configurar el botón de inicio de sesión
        buttonLogin.setOnClickListener(v -> loginUser());

        // Configurar enlace para registrarse
        textViewSignup.setOnClickListener(v -> {
            // Intent para abrir la actividad de registro
            startActivity(new Intent(MainActivity.this, Registro.class));
        });

        // Configurar enlace para recuperar contraseña
        textViewForgotPassword.setOnClickListener(v -> {
            // Intent para abrir la actividad de cambiar contraseña
            startActivity(new Intent(MainActivity.this, CambiarContraseña.class));
        });
    }

    private void loginUser() {
        String email = editTextUsername.getText().toString();
        String password = editTextPassword.getText().toString();

        if (TextUtils.isEmpty(email)) {
            editTextUsername.setError("Ingrese su correo");
            return;
        }
        if (TextUtils.isEmpty(password)) {
            editTextPassword.setError("Ingrese su contraseña");
            return;
        }

        // Autenticar usuario con Firebase
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        Toast.makeText(MainActivity.this, "Login exitoso", Toast.LENGTH_SHORT).show();
                        // Redirigir a la pantalla principal
                        startActivity(new Intent(MainActivity.this, MainActivity2.class));
                        finish();
                    } else {
                        Toast.makeText(MainActivity.this, "Error de autenticación", Toast.LENGTH_SHORT).show();
                    }
                });
    }
}
