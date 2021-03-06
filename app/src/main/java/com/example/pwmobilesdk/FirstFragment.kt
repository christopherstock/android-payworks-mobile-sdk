package com.example.pwmobilesdk

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.pwmobilesdk.databinding.FragmentFirstBinding

/**
 * A simple [Fragment] subclass as the default destination in the navigation.
 */
class FirstFragment : Fragment() {

    private var _binding: FragmentFirstBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentFirstBinding.inflate(inflater, container, false)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.buttonTestTransaction.setOnClickListener {

            ( activity as MainActivity ).startTransaction()
        }

        binding.buttonTestAbort.setOnClickListener {
            ( activity as MainActivity ).abortTransaction()
        }

        binding.buttonClearLog.setOnClickListener {
            ( activity as MainActivity ).clearLog()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}