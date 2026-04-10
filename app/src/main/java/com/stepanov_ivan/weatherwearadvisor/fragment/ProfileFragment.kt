package com.stepanov_ivan.weatherwearadvisor.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.stepanov_ivan.weatherwearadvisor.R
import com.stepanov_ivan.weatherwearadvisor.databinding.FragmentProfileBinding
import com.stepanov_ivan.weatherwearadvisor.di.AppContainer

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val authRepository = AppContainer.authRepository

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val user = object {
            val displayName = authRepository.getCurrentUserName()
            val email = authRepository.getCurrentUserEmail()
        }
        binding.tvName.text = user.displayName ?: "Пользователь"
        binding.tvEmail.text = user.email ?: "Email не указан"

        binding.logoutBtn.setOnClickListener {
            authRepository.logout()
            findNavController().navigate(R.id.navigation_login)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
