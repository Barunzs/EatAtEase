package com.shop.eatatease

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.Firebase
import com.google.firebase.FirebaseException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.shop.eatatease.BuildConfig
import com.shop.eatatease.databinding.ActivityLoginBinding
import java.util.concurrent.TimeUnit

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var auth: FirebaseAuth
    private var isPasswordVisible = false
    private var isOtpMode = false
    private var isOtpSent = false
    private var resendTimer: CountDownTimer? = null
    private var storedVerificationId: String? = null
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        auth = Firebase.auth

        // Disable app verification for debug builds (emulators/CI).
        // This bypasses Play Integrity + reCAPTCHA — NEVER use in release builds.
        if (BuildConfig.DEBUG) {
            auth.firebaseAuthSettings.setAppVerificationDisabledForTesting(true)
        }

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupAnimations()
        setupPasswordToggle()
        setupSignInButton()
        setupSignUpLink()
        setupForgotPassword()
        setupModeToggle()
        setupOtpFlow()
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            navigateToMain()
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Entry Animation
    // ═══════════════════════════════════════════════════════════════

    /**
     * Animate the login card sliding up and fading in on entry.
     */
    private fun setupAnimations() {
        binding.loginCard.alpha = 0f
        binding.loginCard.translationY = 80f
        binding.loginCard.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(600)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()
    }

    // ═══════════════════════════════════════════════════════════════
    // Email/Password Mode
    // ═══════════════════════════════════════════════════════════════

    /**
     * Toggle password visibility with the eye icon.
     */
    private fun setupPasswordToggle() {
        binding.btnTogglePassword.setOnClickListener {
            isPasswordVisible = !isPasswordVisible
            if (isPasswordVisible) {
                binding.editPassword.transformationMethod =
                    HideReturnsTransformationMethod.getInstance()
                binding.btnTogglePassword.setImageResource(R.drawable.ic_visibility_on)
            } else {
                binding.editPassword.transformationMethod =
                    PasswordTransformationMethod.getInstance()
                binding.btnTogglePassword.setImageResource(R.drawable.ic_visibility_off)
            }
            // Keep cursor at the end
            binding.editPassword.setSelection(binding.editPassword.text?.length ?: 0)
        }
    }

    /**
     * Handle sign in button click — validates fields, shows progress,
     * and navigates to MainActivity on success.
     */
    private fun setupSignInButton() {
        binding.btnSignIn.setOnClickListener {
            val email = binding.editEmail.text.toString().trim()
            val password = binding.editPassword.text.toString().trim()

            // Basic validation
            if (email.isEmpty()) {
                binding.editEmail.error = getString(R.string.login_error_email_required)
                binding.editEmail.requestFocus()
                return@setOnClickListener
            }

            if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                binding.editEmail.error = getString(R.string.login_error_email_invalid)
                binding.editEmail.requestFocus()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                binding.editPassword.error = getString(R.string.login_error_password_required)
                binding.editPassword.requestFocus()
                return@setOnClickListener
            }

            if (password.length < 6) {
                binding.editPassword.error = getString(R.string.login_error_password_short)
                binding.editPassword.requestFocus()
                return@setOnClickListener
            }

            // Show progress, disable button
            setLoading(true)

            // Hook up Firebase Auth sign-in
            auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    setLoading(false)
                    if (task.isSuccessful) {
                        navigateToMain()
                    } else {
                        Toast.makeText(this, "Authentication failed: ${task.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                    }
                }
        }
    }

    private fun setupSignUpLink() {
        binding.textSignUp.setOnClickListener {
            // TODO: Navigate to Sign Up screen
            Toast.makeText(this, R.string.login_sign_up_coming_soon, Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupForgotPassword() {
        binding.textForgotPassword.setOnClickListener {
            // TODO: Navigate to Forgot Password screen
            Toast.makeText(this, R.string.login_forgot_password_coming_soon, Toast.LENGTH_SHORT)
                .show()
        }
    }

    // ═══════════════════════════════════════════════════════════════
    // Mode Toggle (Email ↔ OTP)
    // ═══════════════════════════════════════════════════════════════

    /**
     * Set up the toggle button to switch between Email/Password and OTP login modes.
     */
    private fun setupModeToggle() {
        binding.btnToggleLoginMode.setOnClickListener {
            isOtpMode = !isOtpMode
            if (isOtpMode) {
                switchToOtpMode()
            } else {
                switchToEmailMode()
            }
        }
    }

    private fun switchToOtpMode() {
        // Animate out email section, animate in OTP section
        binding.emailPasswordSection.animate()
            .alpha(0f)
            .setDuration(200)
            .withEndAction {
                binding.emailPasswordSection.visibility = View.GONE
                binding.otpSection.visibility = View.VISIBLE
                binding.otpSection.alpha = 0f
                binding.otpSection.animate()
                    .alpha(1f)
                    .setDuration(250)
                    .start()
            }
            .start()

        // Update toggle button text and icon
        binding.btnToggleLoginMode.text = getString(R.string.login_switch_to_email)
        binding.btnToggleLoginMode.setIconResource(R.drawable.ic_email)

        // Update subtitle
        binding.loginSubtitle.text = getString(R.string.login_phone_label)

        // Reset OTP state
        isOtpSent = false
        binding.otpInputSection.visibility = View.GONE
        binding.editPhone.text?.clear()
        binding.editOtp.text?.clear()
    }

    private fun switchToEmailMode() {
        // Animate out OTP section, animate in email section
        binding.otpSection.animate()
            .alpha(0f)
            .setDuration(200)
            .withEndAction {
                binding.otpSection.visibility = View.GONE
                binding.emailPasswordSection.visibility = View.VISIBLE
                binding.emailPasswordSection.alpha = 0f
                binding.emailPasswordSection.animate()
                    .alpha(1f)
                    .setDuration(250)
                    .start()
            }
            .start()

        // Update toggle button text and icon
        binding.btnToggleLoginMode.text = getString(R.string.login_switch_to_otp)
        binding.btnToggleLoginMode.setIconResource(R.drawable.ic_phone)

        // Restore subtitle
        binding.loginSubtitle.text = getString(R.string.login_subtitle)

        // Cancel any running resend timer
        resendTimer?.cancel()
    }

    // ═══════════════════════════════════════════════════════════════
    // OTP Flow
    // ═══════════════════════════════════════════════════════════════

    /**
     * Set up the OTP flow: Send OTP → show OTP input → Verify OTP.
     */
    private fun setupOtpFlow() {
        // Send OTP
        binding.btnSendOtp.setOnClickListener {
            val phone = binding.editPhone.text.toString().trim()

            if (phone.isEmpty()) {
                binding.editPhone.error = getString(R.string.login_error_phone_required)
                binding.editPhone.requestFocus()
                return@setOnClickListener
            }

            if (phone.length < 10) {
                binding.editPhone.error = getString(R.string.login_error_phone_invalid)
                binding.editPhone.requestFocus()
                return@setOnClickListener
            }

            var phoneNumber = phone
            if (!phoneNumber.startsWith("+")) {
                if (phoneNumber.length == 10) {
                    phoneNumber = "+91$phoneNumber"
                } else {
                    binding.editPhone.error = "Include country code (e.g. +91)"
                    binding.editPhone.requestFocus()
                    return@setOnClickListener
                }
            }

            setLoading(true)
            sendVerificationCode(phoneNumber, false)
        }

        // Verify OTP
        binding.btnVerifyOtp.setOnClickListener {
            val otp = binding.editOtp.text.toString().trim()

            if (otp.isEmpty()) {
                binding.editOtp.error = getString(R.string.login_error_otp_required)
                binding.editOtp.requestFocus()
                return@setOnClickListener
            }

            if (otp.length != 6) {
                binding.editOtp.error = getString(R.string.login_error_otp_invalid)
                binding.editOtp.requestFocus()
                return@setOnClickListener
            }

            val verificationId = storedVerificationId
            if (verificationId == null) {
                Toast.makeText(this, "Verification ID is missing. Send OTP again.", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            setLoading(true)
            val credential = PhoneAuthProvider.getCredential(verificationId, otp)
            signInWithPhoneAuthCredential(credential)
        }

        // Resend OTP
        binding.textResendOtp.setOnClickListener {
            if (binding.textResendOtp.isEnabled) {
                val phone = binding.editPhone.text.toString().trim()
                if (phone.isEmpty()) {
                    binding.editPhone.error = getString(R.string.login_error_phone_required)
                    binding.editPhone.requestFocus()
                    return@setOnClickListener
                }
                var phoneNumber = phone
                if (!phoneNumber.startsWith("+")) {
                    if (phoneNumber.length == 10) {
                        phoneNumber = "+91$phoneNumber"
                    } else {
                        binding.editPhone.error = "Include country code (e.g. +91)"
                        binding.editPhone.requestFocus()
                        return@setOnClickListener
                    }
                }
                setLoading(true)
                sendVerificationCode(phoneNumber, true)
            }
        }
    }

    private fun sendVerificationCode(phone: String, isResend: Boolean) {
        val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phone)
            .setTimeout(60L, TimeUnit.SECONDS)
            .setActivity(this)
            .setCallbacks(object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                    signInWithPhoneAuthCredential(credential)
                }

                override fun onVerificationFailed(e: FirebaseException) {
                    setLoading(false)
                    Toast.makeText(this@LoginActivity, "Verification failed: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                }

                override fun onCodeSent(
                    verificationId: String,
                    token: PhoneAuthProvider.ForceResendingToken
                ) {
                    storedVerificationId = verificationId
                    resendToken = token
                    setLoading(false)
                    onOtpSent(phone)
                    if (isResend) {
                        Toast.makeText(this@LoginActivity, "OTP resent!", Toast.LENGTH_SHORT).show()
                    }
                }
            })

        if (isResend && resendToken != null) {
            optionsBuilder.setForceResendingToken(resendToken!!)
        }

        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    navigateToMain()
                } else {
                    setLoading(false)
                    Toast.makeText(this, "Sign-in failed: ${task.exception?.localizedMessage}", Toast.LENGTH_LONG).show()
                }
            }
    }

    /**
     * Called after OTP is successfully sent. Shows the OTP input section
     * and starts a 30-second resend cooldown timer.
     */
    private fun onOtpSent(phone: String) {
        isOtpSent = true

        // Show OTP input section with animation
        binding.otpInputSection.visibility = View.VISIBLE
        binding.otpInputSection.alpha = 0f
        binding.otpInputSection.translationY = 20f
        binding.otpInputSection.animate()
            .alpha(1f)
            .translationY(0f)
            .setDuration(300)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .start()

        // Show confirmation message
        binding.otpSentMessage.text = getString(R.string.login_otp_sent, phone)

        // Disable Send OTP button (already sent)
        binding.btnSendOtp.isEnabled = false
        binding.btnSendOtp.alpha = 0.5f
        binding.btnSendOtp.text = "OTP Sent ✓"

        // Start resend cooldown timer (30 seconds)
        startResendTimer()

        // Focus the OTP input
        binding.editOtp.requestFocus()
    }

    /**
     * 30-second countdown timer for the "Resend OTP" link.
     */
    private fun startResendTimer() {
        binding.textResendOtp.isEnabled = false
        binding.textResendOtp.alpha = 0.5f

        resendTimer?.cancel()
        resendTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = millisUntilFinished / 1000
                binding.textResendOtp.text = "Resend in ${seconds}s"
            }

            override fun onFinish() {
                binding.textResendOtp.text = getString(R.string.login_resend_otp)
                binding.textResendOtp.isEnabled = true
                binding.textResendOtp.alpha = 1.0f

                // Re-enable Send OTP button too
                binding.btnSendOtp.isEnabled = true
                binding.btnSendOtp.alpha = 1.0f
                binding.btnSendOtp.text = getString(R.string.login_send_otp)
            }
        }.start()
    }

    // ═══════════════════════════════════════════════════════════════
    // Shared Utilities
    // ═══════════════════════════════════════════════════════════════

    /**
     * Toggle loading state — shows/hides progress bar and disables the active button.
     */
    private fun setLoading(loading: Boolean) {
        binding.loginProgress.visibility = if (loading) View.VISIBLE else View.GONE

        if (isOtpMode) {
            if (isOtpSent) {
                binding.btnVerifyOtp.isEnabled = !loading
                binding.btnVerifyOtp.alpha = if (loading) 0.6f else 1.0f
            } else {
                binding.btnSendOtp.isEnabled = !loading
                binding.btnSendOtp.alpha = if (loading) 0.6f else 1.0f
            }
        } else {
            binding.btnSignIn.isEnabled = !loading
            binding.btnSignIn.alpha = if (loading) 0.6f else 1.0f
        }
    }

    /**
     * Navigate to the main app screen and finish the login activity.
     */
    private fun navigateToMain() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        resendTimer?.cancel()
    }
}
