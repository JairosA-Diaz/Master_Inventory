package com.example.equipoOcho.view.fragment

import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.equipoOcho.R
import com.example.equipoOcho.utils.SessionManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException

class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var auth: FirebaseAuth

    private lateinit var tilEmail: TextInputLayout
    private lateinit var tilPassword: TextInputLayout
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var tvPasswordError: TextView
    private lateinit var btnLogin: MaterialButton
    private lateinit var tvRegister: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        auth = FirebaseAuth.getInstance()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tilEmail = view.findViewById(R.id.tilEmail)
        tilPassword = view.findViewById(R.id.tilPassword)
        etEmail = view.findViewById(R.id.etEmail)
        etPassword = view.findViewById(R.id.etPassword)
        tvPasswordError = view.findViewById(R.id.tvPasswordError)
        btnLogin = view.findViewById(R.id.btnLogin)
        tvRegister = view.findViewById(R.id.tvRegister)

        btnLogin.isEnabled = false
        tvRegister.isEnabled = false
        tvRegister.isClickable = false

        val watcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable?) {
                validatePasswordLength()
                updateButtonsState()
            }
        }

        etEmail.addTextChangedListener(watcher)
        etPassword.addTextChangedListener(watcher)

        // LOGIN
        btnLogin.setOnClickListener {
            val email = etEmail.text?.toString()?.trim().orEmpty()
            val password = etPassword.text?.toString()?.trim().orEmpty()

            android.util.Log.d("LoginFragment", "INTENTANDO LOGIN: email='$email', password='$password'")

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(requireContext(), "Ingresa correo y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isPasswordValid(password)) {
                Toast.makeText(
                    requireContext(),
                    "La contraseña debe tener entre 6 y 10 dígitos y solo números",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            login(email, password)
        }

        // REGISTRO
        tvRegister.setOnClickListener {
            val email = etEmail.text?.toString()?.trim().orEmpty()
            val password = etPassword.text?.toString()?.trim().orEmpty()

            if (email.isBlank() || password.isBlank()) {
                Toast.makeText(requireContext(), "Ingresa correo y contraseña", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (!isPasswordValid(password)) {
                Toast.makeText(
                    requireContext(),
                    "La contraseña debe tener entre 6 y 10 dígitos y solo números",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            register(email, password)
        }
    }

    // ---------- Validaciones UI ----------

    private fun validatePasswordLength() {
        val password = etPassword.text?.toString().orEmpty()
        val hasSomething = password.isNotEmpty()

        val white = ContextCompat.getColor(requireContext(), android.R.color.white)
        val red = ContextCompat.getColor(requireContext(), android.R.color.holo_red_light)

        if (hasSomething && password.length < 6) {
            tvPasswordError.visibility = View.VISIBLE
            tilPassword.boxStrokeColor = red
        } else {
            tvPasswordError.visibility = View.GONE
            tilPassword.boxStrokeColor = white
        }
    }

    private fun isPasswordValid(password: String): Boolean {
        // OJO: aquí debía ser 6..10 (rango), NO 6.10
        return password.length in 6..10 && password.all { it.isDigit() }
    }

    private fun updateButtonsState() {
        val emailFilled = !etEmail.text.isNullOrBlank()
        val passwordText = etPassword.text?.toString().orEmpty()
        val enabled = emailFilled && passwordText.isNotBlank()

        val white = ContextCompat.getColor(requireContext(), android.R.color.white)
        val grey = android.graphics.Color.parseColor("#9EA1A1")

        btnLogin.isEnabled = enabled
        tvRegister.isEnabled = enabled
        tvRegister.isClickable = enabled

        if (enabled) {
            btnLogin.alpha = 1f
            btnLogin.setTypeface(null, Typeface.BOLD)
            tvRegister.setTypeface(null, Typeface.BOLD)
            tvRegister.setTextColor(white)
        } else {
            btnLogin.alpha = 0.6f
            btnLogin.setTypeface(null, Typeface.NORMAL)
            tvRegister.setTypeface(null, Typeface.NORMAL)
            tvRegister.setTextColor(grey)
        }
    }

    // ---------- Firebase + navegación ----------

    private fun login(email: String, password: String) {
        android.util.Log.d("LoginFragment", "LLAMANDO A FirebaseAuth.signIn")

        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                android.util.Log.d("LoginFragment", "onComplete login, success=${task.isSuccessful}")

                if (task.isSuccessful) {
                    val user = task.result?.user
                    android.util.Log.d(
                        "LoginFragment",
                        "Login OK, uid=${user?.uid}, email=${user?.email}"
                    )
                    Toast.makeText(requireContext(), "Login correcto", Toast.LENGTH_SHORT).show()
                    SessionManager.setSessionActive(requireContext(), true)
                    findNavController().navigate(R.id.homeInventoryFragment)
                } else {
                    val e = task.exception
                    val errorCode = if (e is FirebaseAuthException) e.errorCode else "SIN_CODIGO"

                    android.util.Log.e(
                        "LoginFragment",
                        "Error al hacer login. code=$errorCode, msg=${e?.localizedMessage}",
                        e
                    )

                    Toast.makeText(
                        requireContext(),
                        "Login incorrecto: $errorCode",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    private fun register(email: String, password: String) {
        android.util.Log.d("LoginFragment", "REGISTRO: creando usuario $email")

        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                android.util.Log.d("LoginFragment", "onComplete register, success=${task.isSuccessful}")

                if (task.isSuccessful) {
                    val user = task.result?.user
                    android.util.Log.d(
                        "LoginFragment",
                        "Registro OK, uid=${user?.uid}, email=${user?.email}"
                    )
                    Toast.makeText(requireContext(), "Registro correcto", Toast.LENGTH_SHORT).show()
                    SessionManager.setSessionActive(requireContext(), true)
                    findNavController().navigate(R.id.homeInventoryFragment)
                } else {
                    val e = task.exception
                    val errorCode = if (e is FirebaseAuthException) e.errorCode else "SIN_CODIGO"

                    android.util.Log.e(
                        "LoginFragment",
                        "Error al registrar usuario. code=$errorCode, msg=${e?.localizedMessage}",
                        e
                    )

                    Toast.makeText(
                        requireContext(),
                        "Error en el registro: $errorCode",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }
}