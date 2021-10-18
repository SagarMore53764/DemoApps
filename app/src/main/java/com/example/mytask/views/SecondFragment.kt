package com.example.mytask.views

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.mytask.databinding.FragmentSecondBinding
import com.google.firebase.FirebaseException
import com.google.firebase.FirebaseTooManyRequestsException
import com.google.firebase.auth.*
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import java.util.concurrent.TimeUnit

/**
 * A simple [Fragment] subclass as the second destination in the navigation.
 */
class SecondFragment : Fragment() {

    private lateinit var binding: FragmentSecondBinding
    private lateinit var auth: FirebaseAuth

    private var storedVerificationId: String? = ""
    private var resendToken: PhoneAuthProvider.ForceResendingToken? = null
    private lateinit var callbacks: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    private var mobileNo = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentSecondBinding.inflate(inflater, container, false)

        // Initialize Firebase Auth
        auth = Firebase.auth

        callbacks()

        binding.verifyBtn.setOnClickListener {

            if (!binding.otpEt.text.isNullOrBlank() && binding.otpEt.text?.length ?: 0 > 5) {

                signInWithPhoneAuthCredential(
                    PhoneAuthProvider.getCredential(
                        storedVerificationId ?: "", binding.otpEt.text.toString()
                    )
                )
            } else {
                Toast.makeText(requireContext(), "Please enter valid OTP", Toast.LENGTH_LONG).show()
            }

        }

        binding.resendOtpTxt.setOnClickListener {
            binding.resendOtpTxt.visibility = View.GONE
            binding.timerTxt.visibility = View.VISIBLE
            startTimer()
            if (resendToken != null) {
                resendVerificationCode(mobileNo, resendToken)
            }
        }

        startTimer()

        return binding.root

    }


    private fun callbacks() {

        // Initialize phone auth callbacks
        callbacks = object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {

            override fun onVerificationCompleted(credential: PhoneAuthCredential) {
                // This callback will be invoked in two situations:
                // 1 - Instant verification. In some cases the phone number can be instantly
                //     verified without needing to send or enter a verification code.
                // 2 - Auto-retrieval. On some devices Google Play services can automatically
                //     detect the incoming verification SMS and perform verification without
                //     user action.
                Log.d(TAG, "onVerificationCompleted:$credential")
                signInWithPhoneAuthCredential(credential)
            }

            override fun onVerificationFailed(e: FirebaseException) {
                // This callback is invoked in an invalid request for verification is made,
                // for instance if the the phone number format is not valid.
                Log.w(TAG, "onVerificationFailed", e)

                if (e is FirebaseAuthInvalidCredentialsException) {
                    // Invalid request
                } else if (e is FirebaseTooManyRequestsException) {
                    // The SMS quota for the project has been exceeded
                }

                // Show a message and update the UI
            }

            override fun onCodeSent(
                verificationId: String,
                token: PhoneAuthProvider.ForceResendingToken
            ) {
                // The SMS verification code has been sent to the provided phone number, we
                // now need to ask the user to enter the code and then construct a credential
                // by combining the code with a verification ID.
                Log.d(TAG, "onCodeSent:$verificationId")

                // Save verification ID and resending token so we can use them later
                storedVerificationId = verificationId
                resendToken = token
            }
        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (requireArguments() != null) {
            if (requireArguments().containsKey("MobileNo") && !requireArguments().getString("MobileNo")
                    .isNullOrBlank()
            ) {
                mobileNo = requireArguments().getString("MobileNo") ?: ""
                startPhoneNumberVerification(mobileNo)
            }
        }


    }


    private fun startPhoneNumberVerification(phoneNumber: String) {
        val options = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(requireActivity())                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
            .build()
        PhoneAuthProvider.verifyPhoneNumber(options)

        callbacks()
    }


    private fun resendVerificationCode(
        phoneNumber: String,
        token: PhoneAuthProvider.ForceResendingToken?
    ) {
        val optionsBuilder = PhoneAuthOptions.newBuilder(auth)
            .setPhoneNumber(phoneNumber)       // Phone number to verify
            .setTimeout(60L, TimeUnit.SECONDS) // Timeout and unit
            .setActivity(requireActivity())                 // Activity (for callback binding)
            .setCallbacks(callbacks)          // OnVerificationStateChangedCallbacks
        if (token != null) {
            optionsBuilder.setForceResendingToken(token) // callback's ForceResendingToken
        }
        PhoneAuthProvider.verifyPhoneNumber(optionsBuilder.build())
    }

    private fun signInWithPhoneAuthCredential(credential: PhoneAuthCredential) {
        auth.signInWithCredential(credential)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithCredential:success")

                    val user = task.result?.user
                    Toast.makeText(requireContext(), "SignIn In Successfully", Toast.LENGTH_LONG)
                        .show()
//                    findNavController().navigate(R.id.action_SecondFragment_to_FirstFragment)
                    updateUI(user)

                } else {
                    // Sign in failed, display a message and update the UI
                    Log.w(TAG, "signInWithCredential:failure", task.exception)
                    if (task.exception is FirebaseAuthInvalidCredentialsException) {
                        // The verification code entered was invalid
                    }
                    Toast.makeText(requireContext(), "SignIn Code Error", Toast.LENGTH_LONG).show()
                    // Update UI
                }
            }
    }

    private fun updateUI(user: FirebaseUser? = auth.currentUser) {

        val intent = Intent(requireContext(), DashboardActivity::class.java)
        requireActivity().startActivity(intent)
        requireActivity().finish()

    }

    override fun onStart() {
        super.onStart()
        // Check if user is signed in (non-null) and update UI accordingly.
        val currentUser = auth.currentUser
//        updateUI(currentUser)
    }


    companion object {
        private const val TAG = "PhoneAuthActivity"
        var otpVerificationTimer: Long = 0
        var otpCountDownTimer: CountDownTimer? = null
    }

    fun startTimer() {
        val timer = 60
        otpVerificationTimer = timer.toLong()

        if (otpCountDownTimer != null) {
            otpCountDownTimer?.cancel()
        }


        otpCountDownTimer = object : CountDownTimer((timer * 1000).toLong(), 1000) {
            override fun onTick(millis: Long) {
                otpVerificationTimer--
                val minuteSeconds = String.format(
                    "%02d:%02d",
                    (TimeUnit.MILLISECONDS.toMinutes(millis) -
                            TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(millis))),
                    (TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MINUTES.toSeconds(
                        TimeUnit.MILLISECONDS.toMinutes(millis)
                    ))
                )
                binding.timerTxt.text = minuteSeconds
            }

            override fun onFinish() {
                binding.timerTxt.visibility = View.GONE
                binding.resendOtpTxt.visibility = View.VISIBLE
            }

        }.start()


    }


}