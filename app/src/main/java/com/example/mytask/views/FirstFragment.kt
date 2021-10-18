package com.example.mytask.views

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.mytask.R
import com.example.mytask.databinding.FragmentFirstBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private lateinit var binding: FragmentFirstBinding
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding = FragmentFirstBinding.inflate(inflater, container, false)
        binding.countryCode.registerCarrierNumberEditText(binding.mobileNoEt)
        return binding.root

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.proceedBtn.setOnClickListener {
            if (!binding.mobileNoEt.text.isNullOrBlank()) {
                val bundle =
                    bundleOf("MobileNo" to binding.countryCode.fullNumberWithPlus.replace(" ", ""))
                findNavController().navigate(R.id.action_FirstFragment_to_SecondFragment, bundle)
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please enter valid mobile number",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

}