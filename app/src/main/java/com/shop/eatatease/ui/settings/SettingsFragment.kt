package com.shop.eatatease.ui.settings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.shop.eatatease.LoginActivity
import com.shop.eatatease.databinding.FragmentSettingsBinding

class SettingsFragment : Fragment() {

    private var _binding: FragmentSettingsBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSettingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val auth = Firebase.auth
        val currentUser = auth.currentUser

        if (currentUser != null) {
            val userDetail = if (!currentUser.phoneNumber.isNullOrEmpty()) {
                "Logged in via Phone:\n${currentUser.phoneNumber}"
            } else if (!currentUser.email.isNullOrEmpty()) {
                "Logged in via Email:\n${currentUser.email}"
            } else {
                "Logged in as Guest"
            }
            binding.textSettings.text = userDetail
        } else {
            binding.textSettings.text = "Not logged in"
        }

        binding.btnLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}