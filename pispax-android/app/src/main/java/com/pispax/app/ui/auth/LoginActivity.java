package com.pispax.app.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.pispax.app.R;
import com.pispax.app.model.LoginResponse;
import com.pispax.app.model.request.LoginRequest;
import com.pispax.app.network.RetrofitClient;
import com.pispax.app.ui.cliente.HomeClienteActivity;
import com.pispax.app.ui.transportista.HomeTransportistaActivity;
import com.pispax.app.util.Constants;
import com.pispax.app.util.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Formulario de login. Llama a POST /api/auth/login y redirige al Home segun el rol.
public class LoginActivity extends AppCompatActivity {

    // ── VISTAS (modificar estilo en activity_login.xml) ──────────────────────
    private TextInputEditText etEmail, etPassword;
    private TextInputLayout tilEmail, tilPassword;
    private MaterialButton btnLogin, btnToggleTema;
    private TextView tvRegistro;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        bindVistas();
        configurarToggleTema();

        // ── ACCION: boton ENTRAR ──────────────────────────────────────────────
        btnLogin.setOnClickListener(v -> intentarLogin());

        // ── ACCION: enlace de REGISTRO ────────────────────────────────────────
        tvRegistro.setOnClickListener(v ->
                startActivity(new Intent(this, RegistroActivity.class)));
    }

    private void bindVistas() {
        etEmail         = findViewById(R.id.et_email);
        etPassword      = findViewById(R.id.et_password);
        tilEmail        = findViewById(R.id.til_email);
        tilPassword     = findViewById(R.id.til_password);
        btnLogin        = findViewById(R.id.btn_login);
        btnToggleTema   = findViewById(R.id.btn_toggle_tema);
        tvRegistro      = findViewById(R.id.tv_registro);
        progress        = findViewById(R.id.progress_login);
    }

    // ── Toggle claro/oscuro: cambia icono, guarda preferencia y recrea ────────
    private void configurarToggleTema() {
        actualizarIconoTema();
        btnToggleTema.setOnClickListener(v -> {
            boolean dark = !SessionManager.isDarkMode(this);
            SessionManager.setDarkMode(this, dark);
            AppCompatDelegate.setDefaultNightMode(
                    dark ? AppCompatDelegate.MODE_NIGHT_YES
                         : AppCompatDelegate.MODE_NIGHT_NO);
            recreate();
        });
    }

    // Cambia el icono del boton toggle segun el modo actual
    private void actualizarIconoTema() {
        boolean dark = SessionManager.isDarkMode(this);
        btnToggleTema.setIconResource(dark ? R.drawable.ic_light_mode : R.drawable.ic_dark_mode);
    }

    // ── Logica de login ───────────────────────────────────────────────────────
    private void intentarLogin() {
        String email    = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString() : "";

        tilEmail.setError(null);
        tilPassword.setError(null);

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, R.string.login_error_campos, Toast.LENGTH_SHORT).show();
            return;
        }

        setUiCargando(true);

        RetrofitClient.getInstance(this).getApi()
                .login(new LoginRequest(email, password))
                .enqueue(new Callback<LoginResponse>() {
                    @Override
                    public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                        setUiCargando(false);
                        if (response.isSuccessful() && response.body() != null) {
                            onLoginExito(response.body());
                        } else {
                            // 401 → credenciales incorrectas
                            Toast.makeText(LoginActivity.this,
                                    R.string.login_error_credenciales, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<LoginResponse> call, Throwable t) {
                        setUiCargando(false);
                        Toast.makeText(LoginActivity.this,
                                R.string.error_red, Toast.LENGTH_LONG).show();
                    }
                });
    }

    // Guarda la sesion y navega al Home correcto segun el rol
    private void onLoginExito(LoginResponse loginResponse) {
        SessionManager.guardarSesion(
                this,
                loginResponse.getToken(),
                loginResponse.getUsuario().getId(),
                loginResponse.getUsuario().getRol(),
                loginResponse.getUsuario().getNombreCompleto()
        );

        Intent destino;
        if (Constants.ROL_TRANSPORTISTA.equals(loginResponse.getUsuario().getRol())) {
            destino = new Intent(this, HomeTransportistaActivity.class);
        } else {
            destino = new Intent(this, HomeClienteActivity.class);
        }

        startActivity(destino);
        finish();
    }

    // Muestra/oculta el indicador de carga y deshabilita el boton
    private void setUiCargando(boolean cargando) {
        progress.setVisibility(cargando ? View.VISIBLE : View.GONE);
        btnLogin.setEnabled(!cargando);
    }
}
