package com.takusemba.rtmppublishersample

import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.david.rs.requestPermissionStick

class TestActivity :AppCompatActivity(){


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)
        requestPermissionStick(permissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO,
            Manifest.permission.VIBRATE, Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_NETWORK_STATE)){ isGrantAll, permissions, grantResults ->
            if(isGrantAll){

            }

        }
        findViewById<Button>(R.id.btn1).setOnClickListener {
            val rmtp = findViewById<EditText>(R.id.etInput).text.toString()
            if(rmtp.isNullOrEmpty()){
                Toast.makeText(this,"请输入rtmp地址",Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startActivity(Intent(this,MainActivity::class.java).putExtra("url",rmtp))
        }
    }
}