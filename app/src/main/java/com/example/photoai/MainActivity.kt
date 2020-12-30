package com.example.photoai

import android.content.Intent
import android.graphics.Bitmap
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import com.example.photoai.databinding.ActivityMainBinding
import com.google.android.material.chip.Chip
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.mlkit.vision.common.InputImage

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private var isText = false
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val switch = findViewById<SwitchCompat>(R.id.switch1)
        switch.setOnClickListener{
           isText = switch.isChecked
        }
        binding.fab.setOnClickListener {
            pickImage()
        }

    }

    private fun processImageTagging(bitmap: Bitmap){
        val visionImage = FirebaseVisionImage.fromBitmap(bitmap)
        FirebaseVision.getInstance().cloudImageLabeler.processImage(visionImage).addOnSuccessListener { tags ->
            binding.chipGroup.removeAllViews()
            tags.sortByDescending { it.confidence }
            for (it in tags){
                Chip(this,null,R.style.Widget_MaterialComponents_Chip_Choice).apply {
                    text = this.text
                    binding.chipGroup.addView(this)
                }
            }
        }.addOnFailureListener{
            Toast.makeText(this,it.message.toString(),Toast.LENGTH_LONG).show()
        }
    }

    private fun getImageFromData(data:Intent):Bitmap{

    }

    private fun pickImage() {
        val intent = Intent().apply {
            action = Intent.ACTION_PICK
            type = "image/*"
        }
        startActivityForResult(Intent.createChooser(intent,"Select Picture"),IMAGE_PICK_CODE)
    }
    companion object{
        private var IMAGE_PICK_CODE = 108
    }
}