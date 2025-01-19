package com.example.eventmanagementapp.ui.profile

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.eventmanagementapp.*
import com.example.eventmanagementapp.R
import com.example.eventmanagementapp.databinding.FragmentProfileBinding
import com.example.eventmanagementapp.model.User_model
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class ProfileFragment : Fragment() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var database: DatabaseReference

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        firebaseAuth = FirebaseAuth.getInstance()
        database = FirebaseDatabase.getInstance().getReference()

        fetchUserDetails()
        setupActionButtons()
    }

    private fun displayUserDetails(user: User_model) {
        binding.apply {
            profileName.text = user.name
            profileEmail.text = user.email
        }
    }

    private fun fetchUserDetails() {
        val userId = firebaseAuth.currentUser?.uid
        if (userId != null) {
            database.child("Users").child(userId).addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val user = snapshot.getValue(User_model::class.java)
                    if (user != null) {
                        displayUserDetails(user)
                    } else {
                        Toast.makeText(requireContext(), "User details not found.", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(requireContext(), "Error fetching data: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            Toast.makeText(requireContext(), "User not logged in.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun setupActionButtons() {
        binding.apply {

            // Personal Information
            profile.setOnClickListener {
                val intent = Intent(requireActivity(), PersonalInformationActivity::class.java)
                startActivity(intent)
            }

            // Account Settings
            account.setOnClickListener {
                val intent = Intent(requireActivity(), AccountSettingsActivity::class.java)
                startActivity(intent)
            }

            // Event History
            history.setOnClickListener {
                val intent = Intent(requireActivity(), EventHistoryActivity::class.java)
                startActivity(intent)
            }

            // Organizer Features

            organiser.setOnClickListener {
                val intent = Intent(requireActivity(), OrganizerFeaturesActivity::class.java)
                startActivity(intent)
            }

            // Support and Feedback
            support.setOnClickListener {
                val intent = Intent(requireActivity(), SupportFeedbackActivity::class.java)
                startActivity(intent)
            }

            // Analytics and Insights
            analytics.setOnClickListener {
                val intent = Intent(requireActivity(), AnalyticsInsightsActivity::class.java)
                startActivity(intent)
            }

            // Logout
            logout.setOnClickListener {
                firebaseAuth.signOut()
                val intent = Intent(requireActivity(), LoginActivity::class.java)
                startActivity(intent)
                requireActivity().finish()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
