package com.sarvesh14.notifyme.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import com.sarvesh14.notifyme.Constants
import com.sarvesh14.notifyme.R
import com.sarvesh14.notifyme.databinding.FragmentHomeBinding
import com.sarvesh14.notifyme.services.NotificationListenerService

import java.util.*

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
            btnSelectLanguage.setOnClickListener {
                showChangeLang()
            }
            btnStopService.setOnClickListener { stopListenerService() }
            if(NotificationListenerService.isServiceRunning){
                Log.d(TAG, "Service running")
                btnStopService.isEnabled = true
                btnStartService.isEnabled = false
            }else{
                Log.d(TAG, "Service not running")
                btnStopService.isEnabled = false
                btnStartService.isEnabled = true
                btnMuteService.isEnabled = false
                btnUnmuteService.isEnabled = false
            }
            btnMuteService.setOnClickListener {
                muteService()
            }
            btnUnmuteService.setOnClickListener {
                unMuteService()
            }
        }
    }

    private fun unMuteService() {
        sendCommandToService(Constants.ACTION_UNMUTE_SERVICE)
        binding.btnMuteService.isEnabled = true
        binding.btnUnmuteService.isEnabled = false
    }

    private fun muteService() {
        sendCommandToService(Constants.ACTION_MUTE_SERVICE)
        binding.btnMuteService.isEnabled = false
        binding.btnUnmuteService.isEnabled = true
    }


    private fun showChangeLang() {
        val listItems = arrayOf("English", "हिन्दी", "मराठी")
        val mBuilder = AlertDialog.Builder(requireContext())
        mBuilder.setTitle("Choose Language")
        mBuilder.setSingleChoiceItems(listItems, 0) { dialog, langSelected ->
            if (langSelected == 0) {
                changeLanguage("en")
//                    recreate()
            } else if (langSelected == 1) {
                changeLanguage("hi")
//                    recreate()
            } else if (langSelected == 2) {
                changeLanguage("mr")
            }
            dialog.dismiss()
        }
        val mDialog = mBuilder.create()
        mDialog.show()
    }

    private fun changeLanguage(language: String) {

        Log.d(TAG, "changeLanguage to " + language)
        val locale = Locale(language)
//        Locale.setDefault(locale)
//        var config = Configuration()
//        config.locale = locale

//        currentLanguage = language
//        NotificationListener.currentLanguage = language
        binding.textViewLanguage.text = language.toString()
//        sendCommandToService(Constants.ACTION_CHANGE_LANGUAGE)
//        Log.d(TAG, "NotificationListener.language= " + NotificationListener.currentLanguage)
        val serviceIntent: Intent =
            Intent(requireContext(), NotificationListenerService::class.java).also {
                it.action = Constants.ACTION_CHANGE_LANGUAGE
                it.putExtra("langCode", language)
                activity?.startService(it)
            }

    }

    private fun startListenerService() {
        Log.d(TAG, "starting service")
        val serviceIntent = Intent(requireContext(), NotificationListenerService::class.java)
        serviceIntent.setAction(Constants.ACTION_START_OR_RESUME_SERVICE)
        activity?.startService(serviceIntent)
        binding.btnStartService.isEnabled = false
        binding.btnStopService.isEnabled = true
        binding.btnMuteService.isEnabled = true
    }

    private fun stopListenerService() {
        Log.d(TAG, "stopping service")
        val serviceIntent = Intent(requireContext(), NotificationListenerService::class.java)
        serviceIntent.setAction(Constants.ACTION_STOP_SERVICE)
        activity?.startService(serviceIntent)
        binding.btnStartService.isEnabled = true
        binding.btnStopService.isEnabled = false
        binding.btnMuteService.isEnabled = false
        binding.btnUnmuteService.isEnabled = false
    }

    private fun sendCommandToService(action:String){
        val serviceIntent:Intent = Intent(requireContext(), NotificationListenerService::class.java).also {
            it.action = action
            activity?.startService(it)
        }
    }
}