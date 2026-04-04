package com.stepanov_ivan.weatherwearadvisor.fragment

import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.stepanov_ivan.weatherwearadvisor.R
import com.stepanov_ivan.weatherwearadvisor.adapter.ClothingAdapter
import com.stepanov_ivan.weatherwearadvisor.data.AppDatabase
import com.stepanov_ivan.weatherwearadvisor.databinding.DialogAddClothingBinding
import com.stepanov_ivan.weatherwearadvisor.databinding.FragmentWardrobeBinding
import com.stepanov_ivan.weatherwearadvisor.model.ClothingItem
import com.stepanov_ivan.weatherwearadvisor.repository.wardrobe.WardrobeRepository
import com.stepanov_ivan.weatherwearadvisor.utils.Resource
import com.stepanov_ivan.weatherwearadvisor.viewmodel.WardrobeViewModel
import kotlinx.coroutines.launch

class WardrobeFragment : Fragment() {

    private var _binding: FragmentWardrobeBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: WardrobeViewModel
    private val auth = FirebaseAuth.getInstance()
    
    private var selectedImageUri: Uri? = null
    private var currentDialogBinding: DialogAddClothingBinding? = null

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            selectedImageUri = uri
            currentDialogBinding?.let {
                Glide.with(this).load(uri).into(it.ivSelectedPhoto)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWardrobeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupViewModel()
        setupRecycler()
        setupListeners()
        observeViewModel()
    }

    private fun setupViewModel() {
        val dao = AppDatabase.getDatabase(requireContext()).wardrobeDao()
        val repository = WardrobeRepository(dao)
        val factory = object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return WardrobeViewModel(repository) as T
            }
        }
        viewModel = ViewModelProvider(this, factory)[WardrobeViewModel::class.java]
        
        auth.currentUser?.uid?.let { viewModel.getItems(it) }
    }

    private fun setupRecycler() {
        binding.itemsRecycler.layoutManager = GridLayoutManager(requireContext(), 2)
    }

    private fun setupListeners() {
        binding.btnAddClothing.setOnClickListener {
            showAddEditDialog(null)
        }
    }

    private fun showAddEditDialog(item: ClothingItem?) {
        selectedImageUri = item?.photoUri?.let { Uri.parse(it) }
        val dialogBinding = DialogAddClothingBinding.inflate(layoutInflater)
        currentDialogBinding = dialogBinding
        
        val isEdit = item != null
        if (isEdit) {
            dialogBinding.etName.setText(item?.name)
            dialogBinding.etMinTemp.setText(item?.minTemp.toString())
            dialogBinding.etMaxTemp.setText(item?.maxTemp.toString())
            dialogBinding.btnAdd.text = "Обновить"
            Glide.with(this).load(selectedImageUri).into(dialogBinding.ivSelectedPhoto)
        }

        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogBinding.root)
            .create()

        dialogBinding.btnSelectPhoto.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        dialogBinding.btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialogBinding.btnAdd.setOnClickListener {
            val name = dialogBinding.etName.text.toString()
            val minTemp = dialogBinding.etMinTemp.text.toString().toIntOrNull() ?: 0
            val maxTemp = dialogBinding.etMaxTemp.text.toString().toIntOrNull() ?: 0
            val photoUri = selectedImageUri?.toString() ?: ""
            val userId = auth.currentUser?.uid ?: ""

            if (name.isBlank()) {
                Toast.makeText(requireContext(), "Введите название", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (isEdit) {
                val updatedItem = item!!.copy(
                    name = name,
                    photoUri = photoUri,
                    minTemp = minTemp,
                    maxTemp = maxTemp
                )
                viewModel.updateItem(updatedItem)
            } else {
                viewModel.addItem(name, photoUri, minTemp, maxTemp, userId)
            }
            dialog.dismiss()
        }
        
        dialog.setOnDismissListener {
            currentDialogBinding = null
        }
        
        dialog.show()
    }

    private fun showDeleteConfirmation(item: ClothingItem) {
        AlertDialog.Builder(requireContext())
            .setTitle("Удалить вещь?")
            .setMessage("Вы уверены, что хотите удалить '${item.name}'?")
            .setPositiveButton("Удалить") { _, _ ->
                viewModel.deleteItem(item)
            }
            .setNegativeButton("Отмена", null)
            .show()
    }

    private fun observeViewModel() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.itemsState.collect { resource ->
                when (resource) {
                    is Resource.Loading -> { }
                    is Resource.Success -> {
                        binding.itemsRecycler.adapter = ClothingAdapter(
                            items = resource.data,
                            onItemClick = { item -> showAddEditDialog(item) },
                            onItemLongClick = { item -> showDeleteConfirmation(item) }
                        )
                    }
                    is Resource.Error -> {
                        Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.operationResult.collect { resource ->
                if (resource is Resource.Error) {
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
