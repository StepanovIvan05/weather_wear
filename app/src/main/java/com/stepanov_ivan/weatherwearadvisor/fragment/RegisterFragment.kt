package com.stepanov_ivan.weatherwearadvisor.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.stepanov_ivan.weatherwearadvisor.R
import com.stepanov_ivan.weatherwearadvisor.databinding.FragmentRegisterBinding
import com.stepanov_ivan.weatherwearadvisor.utils.Resource
import com.stepanov_ivan.weatherwearadvisor.viewmodel.AuthViewModel
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {

    private var _binding: FragmentRegisterBinding? = null
    private val binding get() = _binding!!
    private val viewModel: AuthViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        observeViewModel()
    }

    private fun setupListeners() {
        binding.registerBtn.setOnClickListener {
            val name = binding.nameInput.text.toString().trim()
            val email = binding.emailInput.text.toString().trim()
            val password = binding.passwordInput.text.toString().trim()
            val confirmPass = binding.confirmPasswordInput.text.toString().trim()
            
            viewModel.register(name, email, password, confirmPass)
        }

        binding.loginLink.setOnClickListener {
            findNavController().navigate(R.id.navigation_login)
        }
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.authState.collect { resource ->
                    when (resource) {
                        is Resource.Loading -> {
                            binding.registerBtn.isEnabled = false
                        }
                        is Resource.Success -> {
                            Toast.makeText(requireContext(), "Регистрация успешна!", Toast.LENGTH_SHORT).show()
                            // Используем action_register_to_home для очистки стека
                            findNavController().navigate(R.id.action_register_to_home)
                        }
                        is Resource.Error -> {
                            binding.registerBtn.isEnabled = true
                            Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                            viewModel.resetState()
                        }
                        null -> {}
                    }
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
