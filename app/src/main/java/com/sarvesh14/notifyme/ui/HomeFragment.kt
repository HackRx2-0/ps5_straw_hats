package com.sarvesh14.notifyme.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.sarvesh14.notifyme.Constants
import com.sarvesh14.notifyme.R
import com.sarvesh14.notifyme.databinding.FragmentHomeBinding
import com.sarvesh14.notifyme.services.NotificationListenerService

private const val TAG = "NotifyMeHomeFragment"
class HomeFragment : Fragment() {
    private lateinit var binding: FragmentHomeBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val fragmentBinding = FragmentHomeBinding.inflate(inflater, container, false)
        binding = fragmentBinding
        return fragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding?.apply {
            btnStartService.setOnClickListener { startListenerService() }
        }
    }

    private fun startListenerService() {
        Log.d(TAG, "starting service")
        val serviceIntent = Intent(requireContext(), NotificationListenerService::class.java)
        serviceIntent.setAction(Constants.ACTION_START_OR_RESUME_SERVICE)
        activity?.startService(serviceIntent)
    }

    private fun stopService(){

    }
}