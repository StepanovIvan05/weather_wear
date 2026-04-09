package com.stepanov_ivan.weatherwearadvisor.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.stepanov_ivan.weatherwearadvisor.R
import com.stepanov_ivan.weatherwearadvisor.databinding.FragmentProfileBinding
import com.stepanov_ivan.weatherwearadvisor.repository.auth.AuthRepositoryImpl

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private val auth = FirebaseAuth.getInstance()
    private val repository = AuthRepositoryImpl()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        val user = auth.currentUser
        binding.tvName.text = user?.displayName ?: "Пользователь"
        binding.tvEmail.text = user?.email ?: "Email не указан"

        binding.logoutBtn.setOnClickListener {
            repository.logout()
            findNavController().navigate(R.id.navigation_login)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
