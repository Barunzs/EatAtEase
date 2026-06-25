package com.shop.eatatease.ui.profile

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.google.firebase.Firebase
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.shop.eatatease.LoginActivity
import com.shop.eatatease.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    companion object {
        private const val PREFS_NAME = "profile_prefs"
        private const val KEY_PHOTO_URI = "photo_uri"
    }

    // ─── Image picker launcher ───────────────────────────────────
    private val pickImageLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                // Persist permission so URI survives restarts
                try {
                    requireContext().contentResolver.takePersistableUriPermission(
                        uri, Intent.FLAG_GRANT_READ_URI_PERMISSION
                    )
                } catch (_: Exception) { /* permission not grantable – ignore */ }

                savePhotoUri(uri)
                applyPhotoUri(uri)
            }
        }

    // ─── Lifecycle ───────────────────────────────────────────────
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val auth = Firebase.auth
        val user = auth.currentUser

        // ── Populate hero header ──────────────────────────────────
        if (user != null) {
            val phone = user.phoneNumber
            val email = user.email
            val displayName = user.displayName

            // Name shown in header
            val heroName = when {
                !displayName.isNullOrEmpty() -> displayName
                !phone.isNullOrEmpty() -> phone
                !email.isNullOrEmpty() -> email.substringBefore("@").replaceFirstChar { it.uppercase() }
                else -> "User"
            }
            binding.textProfileName.text = heroName

            val subLabel = when {
                !phone.isNullOrEmpty() -> "Phone Verified Account"
                !email.isNullOrEmpty() -> email
                else -> "Signed in"
            }
            binding.textProfileIdentifier.text = subLabel

            // Phone row
            if (!phone.isNullOrEmpty()) {
                binding.rowPhone.visibility = View.VISIBLE
                binding.dividerPhone.visibility = View.VISIBLE
                binding.textProfilePhone.text = phone
            }

            // Email row
            if (!email.isNullOrEmpty()) {
                binding.rowEmail.visibility = View.VISIBLE
                binding.textProfileEmail.text = email
            }

            // Auth method
            binding.textProfileAuthMethod.text = when {
                !phone.isNullOrEmpty() -> "Phone (OTP)"
                !email.isNullOrEmpty() -> "Email / Password"
                else -> "Anonymous"
            }

            // Name edit display
            val nameLabel = if (!displayName.isNullOrEmpty()) displayName else "Tap ✏️ to add your name"
            binding.textDisplayName.text = nameLabel
            binding.editNameInput.setText(displayName ?: "")
        }

        // ── Restore saved profile photo ───────────────────────────
        loadSavedPhoto()

        // ── Change photo ─────────────────────────────────────────
        binding.btnChangePhoto.setOnClickListener {
            pickImageLauncher.launch("image/*")
        }

        // ── Name editing toggle ───────────────────────────────────
        binding.btnEditName.setOnClickListener {
            showNameEditMode()
        }

        binding.btnCancelName.setOnClickListener {
            hideNameEditMode()
        }

        binding.btnSaveName.setOnClickListener {
            val newName = binding.editNameInput.text.toString().trim()
            if (newName.isEmpty()) {
                binding.editNameInput.error = "Name cannot be empty"
                return@setOnClickListener
            }
            saveName(newName)
        }

        // ── Logout ────────────────────────────────────────────────
        binding.btnProfileLogout.setOnClickListener {
            auth.signOut()
            val intent = Intent(requireContext(), LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }

    // ─── Name editing helpers ─────────────────────────────────────
    private fun showNameEditMode() {
        binding.rowNameDisplay.visibility = View.GONE
        binding.rowNameEdit.visibility = View.VISIBLE
        binding.editNameInput.requestFocus()
        binding.editNameInput.setSelection(binding.editNameInput.text?.length ?: 0)
        showKeyboard(binding.editNameInput)
    }

    private fun hideNameEditMode() {
        binding.rowNameDisplay.visibility = View.VISIBLE
        binding.rowNameEdit.visibility = View.GONE
        hideKeyboard()
    }

    private fun saveName(name: String) {
        val user = Firebase.auth.currentUser ?: return
        binding.btnSaveName.isEnabled = false
        binding.btnSaveName.text = "Saving…"

        val profileUpdates = UserProfileChangeRequest.Builder()
            .setDisplayName(name)
            .build()

        user.updateProfile(profileUpdates)
            .addOnCompleteListener { task ->
                binding.btnSaveName.isEnabled = true
                binding.btnSaveName.text = "Save"
                if (task.isSuccessful) {
                    binding.textDisplayName.text = name
                    binding.textProfileName.text = name
                    hideNameEditMode()
                    Toast.makeText(requireContext(), "Name updated!", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Failed: ${task.exception?.localizedMessage}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
    }

    // ─── Photo helpers ────────────────────────────────────────────
    private fun applyPhotoUri(uri: Uri) {
        try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            if (bitmap != null) {
                // Clear tint so the actual image shows through
                binding.imgProfilePhoto.setImageBitmap(bitmap)
                binding.imgProfilePhoto.imageTintList = null
                binding.imgProfilePhoto.setPadding(0, 0, 0, 0)
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Could not load image", Toast.LENGTH_SHORT).show()
        }
    }

    private fun savePhotoUri(uri: Uri) {
        requireContext()
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit()
            .putString(KEY_PHOTO_URI, uri.toString())
            .apply()
    }

    private fun loadSavedPhoto() {
        val uriString = requireContext()
            .getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_PHOTO_URI, null)
        if (!uriString.isNullOrEmpty()) {
            applyPhotoUri(Uri.parse(uriString))
        }
    }

    // ─── Keyboard helpers ─────────────────────────────────────────
    private fun showKeyboard(view: View) {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showSoftInput(view, InputMethodManager.SHOW_IMPLICIT)
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
