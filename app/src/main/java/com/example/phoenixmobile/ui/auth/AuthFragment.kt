package com.example.phoenixmobile.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.phoenixmobile.R

class AuthFragment : Fragment() {

    private val viewModel: AuthViewModel by viewModels()
    private var isLoginMode = true

    private lateinit var tvTitle: TextView
    private lateinit var etUsername: EditText
    private lateinit var etPassword: EditText
    private lateinit var etEmail: EditText
    private lateinit var etFullName: EditText
    private lateinit var btnSubmit: Button
    private lateinit var btnToggleMode: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var emailContainer: LinearLayout
    private lateinit var fullnameContainer: LinearLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_auth, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initViews(view)
        setupUI()
        observeViewModel()
    }

    private fun initViews(view: View) {
        tvTitle = view.findViewById(R.id.tv_title)
        etUsername = view.findViewById(R.id.et_username)
        etPassword = view.findViewById(R.id.et_password)
        etEmail = view.findViewById(R.id.et_email)
        etFullName = view.findViewById(R.id.et_full_name)
        btnSubmit = view.findViewById(R.id.btn_submit)
        btnToggleMode = view.findViewById(R.id.btn_toggle_mode)
        progressBar = view.findViewById(R.id.progress_bar)
        emailContainer = view.findViewById(R.id.email_container)
        fullnameContainer = view.findViewById(R.id.fullname_container)
    }

    private fun setupUI() {
        updateUI()

        btnSubmit.setOnClickListener {
            if (isLoginMode) {
                performLogin()
            } else {
                performRegister()
            }
        }

        btnToggleMode.setOnClickListener {
            isLoginMode = !isLoginMode
            updateUI()
            clearFields()
            viewModel.clearError()
        }
    }

    private fun updateUI() {
        if (isLoginMode) {
            tvTitle.text = "Вход"
            emailContainer.visibility = View.GONE
            fullnameContainer.visibility = View.GONE
            btnSubmit.text = "Войти"
            btnToggleMode.text = "Нет аккаунта? Зарегистрироваться"
        } else {
            tvTitle.text = "Регистрация"
            emailContainer.visibility = View.VISIBLE
            fullnameContainer.visibility = View.VISIBLE
            btnSubmit.text = "Зарегистрироваться"
            btnToggleMode.text = "Уже есть аккаунт? Войти"
        }
    }

    private fun clearFields() {
        etUsername.setText("")
        etPassword.setText("")
        etEmail.setText("")
        etFullName.setText("")
    }

    private fun performLogin() {
        val username = etUsername.text.toString().trim()
        val password = etPassword.text.toString()

        viewModel.login(username, password)
    }

    private fun performRegister() {
        val username = etUsername.text.toString().trim()
        val email = etEmail.text.toString().trim()
        val password = etPassword.text.toString()
        val fullName = etFullName.text.toString().trim().ifBlank { null }

        viewModel.register(username, email, password, fullName)
    }

    private fun observeViewModel() {
        viewModel.loading.observe(viewLifecycleOwner) { loading ->
            progressBar.visibility = if (loading) View.VISIBLE else View.GONE
            btnSubmit.isEnabled = !loading
            btnToggleMode.isEnabled = !loading
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (error != null) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
            }
        }

        viewModel.authSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                Toast.makeText(requireContext(), "Успешно!", Toast.LENGTH_SHORT).show()
                // Переходим к главному экрану
                try {
                    findNavController().navigate(R.id.navigation_mydevice)
                } catch (_: Exception) {
                    // Navigation error, ignore
                }
            }
        }
    }
}
