package com.example.mytask.views

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mytask.adapters.ProductsListAdapter
import com.example.mytask.databinding.ActivityDashboard2Binding
import com.example.mytask.model.ProductDTO
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.database.ktx.getValue
import com.google.firebase.ktx.Firebase

class DashboardActivity : AppCompatActivity() {

    private var TAG = "DashboardActivity"
    private lateinit var binding: ActivityDashboard2Binding
    private lateinit var productsListAdapter: ProductsListAdapter
    private val database = Firebase.database
    val myRef = database.getReference("Products")
    private var productArrayList = arrayListOf<ProductDTO>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityDashboard2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)
        productsListAdapter = ProductsListAdapter()
        binding.content.productsRecyclerView.adapter = productsListAdapter


        fetchData()

        binding.profileImg.setOnClickListener {
            val intent = Intent(this, ProfileActivity::class.java)
            startActivity(intent)
        }


    }


    private fun fetchData() {

        // Read from the database
        myRef.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                // This method is called once with the initial value and again
                // whenever data at this location is updated.
                productArrayList.clear()

                val value = dataSnapshot.getValue<ArrayList<ProductDTO>>()
                Log.d(TAG, "Value is: $value")

                productArrayList = value ?: arrayListOf()
                productsListAdapter.arrayList = productArrayList

            }

            override fun onCancelled(error: DatabaseError) {
                // Failed to read value
                Log.w(TAG, "Failed to read value.", error.toException())
            }
        })
    }

    private var isBackPressed = false
    override fun onBackPressed() {

        Handler().postDelayed({
            isBackPressed = false
        }, 2000)

        if (isBackPressed) {
//                super.onBackPressed()
            finish()
        } else {
            isBackPressed = true
            Toast.makeText(this, "Press back again to Exit", Toast.LENGTH_SHORT).show()
        }
    }
}