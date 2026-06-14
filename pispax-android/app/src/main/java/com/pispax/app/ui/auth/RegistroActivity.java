package com.pispax.app.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.widget.ImageButton;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.pispax.app.R;
import com.pispax.app.model.UsuarioDTO;
import com.pispax.app.model.request.RegistroRequest;
import com.pispax.app.network.RetrofitClient;
import com.pispax.app.util.Constants;
import com.pispax.app.util.SessionManager;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

// Formulario de creacion de cuenta. Llama a POST /api/auth/registro y vuelve al login.
public class RegistroActivity extends AppCompatActivity {

    // ── VISTAS (modificar estilo en activity_registro.xml) ───────────────────
    private TextInputEditText etNombre, etApellidos, etEmail, etPassword, etTelefono;
    private RadioGroup rgRol;
    private MaterialButton btnRegistro, btnVolver;
    private ImageButton btnToggleTema;
    private ProgressBar progress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_registro);

        bindVistas();

        // ── ACCION: volver al login ───────────────────────────────────────────
        btnVolver.setOnClickListener(v -> finish());
        findViewById(R.id.tv_ya_cuenta).setOnClickListener(v -> finish());

        // ── ACCION: toggle de tema ────────────────────────────────────────────
        btnToggleTema.setOnClickListener(v -> {
            boolean dark = !SessionManager.isDarkMode(this);
            SessionManager.setDarkMode(this, dark);
            AppCompatDelegate.setDefaultNightMode(
                    dark ? AppCompatDelegate.MODE_NIGHT_YES
                         : AppCompatDelegate.MODE_NIGHT_NO);
            recreate();
        });

        // ── ACCION: crear cuenta ──────────────────────────────────────────────
        btnRegistro.setOnClickListener(v -> intentarRegistro());
    }

    private void bindVistas() {
        etNombre      = findViewById(R.id.et_nombre);
        etApellidos   = findViewById(R.id.et_apellidos);
        etEmail       = findViewById(R.id.et_reg_email);
        etPassword    = findViewById(R.id.et_reg_password);
        etTelefono    = findViewById(R.id.et_telefono);
        rgRol         = findViewById(R.id.rg_rol);
        btnRegistro   = findViewById(R.id.btn_registro);
        btnVolver     = findViewById(R.id.btn_volver);
        btnToggleTema = findViewById(R.id.btn_toggle_tema);
        progress      = findViewById(R.id.progress_registro);
    }

    private void intentarRegistro() {
        String nombre    = texto(etNombre);
        String apellidos = texto(etApellidos);
        String email     = texto(etEmail);
        String password  = texto(etPassword);
        String telefono  = texto(etTelefono);

        String rol = (rgRol.getCheckedRadioButtonId() == R.id.rb_transportista)
                ? Constants.ROL_TRANSPORTISTA
                : Constants.ROL_CLIENTE;

        if (TextUtils.isEmpty(nombre) || TextUtils.isEmpty(apellidos)
                || TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Rellena todos los campos obligatorios", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.length() < 8) {
            Toast.makeText(this, "La contraseña debe tener al menos 8 caracteres", Toast.LENGTH_SHORT).show();
            return;
        }

        setUiCargando(true);

        RegistroRequest req = new RegistroRequest(nombre, apellidos, email, password,
                TextUtils.isEmpty(telefono) ? null : telefono, rol);

        RetrofitClient.getInstance(this).getApi()
                .registro(req)
                .enqueue(new Callback<UsuarioDTO>() {
                    @Override
                    public void onResponse(Call<UsuarioDTO> call, Response<UsuarioDTO> response) {
                        setUiCargando(false);
                        if (response.isSuccessful()) {
                            Toast.makeText(RegistroActivity.this,
                                    R.string.registro_ok, Toast.LENGTH_LONG).show();
                            finish(); // Vuelve al login para que el usuario entre con sus credenciales
                        } else if (response.code() == 400) {
                            Toast.makeText(RegistroActivity.this,
                                    "El email ya existe o los datos son incorrectos",
                                    Toast.LENGTH_LONG).show();
                        } else {
                            Toast.makeText(RegistroActivity.this,
                                    R.string.error_servidor, Toast.LENGTH_LONG).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<UsuarioDTO> call, Throwable t) {
                        setUiCargando(false);
                        Toast.makeText(RegistroActivity.this,
                                R.string.error_red, Toast.LENGTH_LONG).show();
                    }
                });
    }

    private void setUiCargando(boolean cargando) {
        progress.setVisibility(cargando ? View.VISIBLE : View.GONE);
        btnRegistro.setEnabled(!cargando);
    }

    private String texto(TextInputEditText et) {
        return et.getText() != null ? et.getText().toString().trim() : "";
    }
}
