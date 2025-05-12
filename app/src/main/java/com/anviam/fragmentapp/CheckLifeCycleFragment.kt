package com.anviam.fragmentapp

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup


class CheckLifeCycleFragment : Fragment() {

        override fun onAttach(context: Context) {
            super.onAttach(context)
            Log.d("FragmentLifecycle", "onAttach called")
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            Log.d("FragmentLifecycle", "onCreate called")
        }

        override fun onCreateView(
            inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?
        ): View? {
            Log.d("FragmentLifecycle", "onCreateView called")
            return inflater.inflate(R.layout.fragment_check_life_cycle, container, false)
        }

        override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
            Log.d("FragmentLifecycle", "onViewCreated called")
        }

        override fun onStart() {
            super.onStart()
            Log.d("FragmentLifecycle", "onStart called")
        }

        override fun onResume() {
            super.onResume()
            Log.d("FragmentLifecycle", "onResume called")
        }

        override fun onPause() {
            super.onPause()
            Log.d("FragmentLifecycle", "onPause called")
        }

        override fun onStop() {
            super.onStop()
            Log.d("FragmentLifecycle", "onStop called")
        }

        override fun onDestroyView() {
            super.onDestroyView()
            Log.d("FragmentLifecycle", "onDestroyView called")
        }

        override fun onDestroy() {
            super.onDestroy()
            Log.d("FragmentLifecycle", "onDestroy called")
        }

        override fun onDetach() {
            super.onDetach()
            Log.d("FragmentLifecycle", "onDetach called")
        }
}