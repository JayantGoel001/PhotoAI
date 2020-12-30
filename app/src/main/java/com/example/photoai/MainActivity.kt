@file:Suppress("DEPRECATION")

package com.example.photoai

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import com.example.photoai.databinding.ActivityMainBinding
import com.google.android.material.chip.Chip
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.text.FirebaseVisionText

class MainActivity : AppCompatActivity() {
    private lateinit var binding : ActivityMainBinding
    private var isText = false
    private lateinit var imageView: ImageView
    private lateinit var switch:SwitchCompat
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        switch = findViewById(R.id.switch1)
        imageView = findViewById(R.id.imageView)
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
            tags.map {
                Chip(this,null,R.style.Widget_MaterialComponents_Chip_Choice).apply { text = it.text }
            }.forEach {
                binding.chipGroup.addView(it)
            }
        }.addOnFailureListener{
            Toast.makeText(this,it.message.toString(),Toast.LENGTH_LONG).show()
        }
    }

    private fun getImageFromData(data:Intent?):Bitmap?{
        val selectedImage = data?.data
        return MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImage)
    }

    @SuppressLint("SetTextI18n")
    private fun startTextRecognizing(bitmap: Bitmap){
        if (binding.imageView.drawable!=null){
            val image = FirebaseVisionImage.fromBitmap(bitmap)
            val detector = FirebaseVision.getInstance().cloudTextRecognizer
            detector.processImage(image).addOnSuccessListener {
                processTextBlock(it!!)
            }.addOnFailureListener {
                binding.textView.text = "Failed"
            }
        }else{
            Toast.makeText(this,"Failed",Toast.LENGTH_LONG).show()
        }
    }

    private fun processTextBlock(result: FirebaseVisionText) {
        binding.chipGroup.removeAllViews()
        result.textBlocks.map {
            Chip(this,null,R.style.Widget_MaterialComponents_Chip_Choice).apply { text = it.text }
        }.forEach {
            binding.chipGroup.addView(it)
        }
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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (resultCode==Activity.RESULT_OK && requestCode== IMAGE_PICK_CODE){
            val bitmap = getImageFromData(data)
            bitmap.apply {
                imageView.setImageBitmap(this)
                if (!isText){
                    if (bitmap != null) {
                        processImageTagging(bitmap)
                    }
                }else{
                    if (bitmap != null) {
                        startTextRecognizing(bitmap)
                    }
                }
            }
        }
        super.onActivityResult(requestCode, resultCode, data)

    }
}