package com.david.rs

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment

/**
 * 监听activity方法onActivityResult Fragment
 */
class RSEmptyOnResultFragment:Fragment() {

    lateinit var onActivityResultHook: (requestCode: Int, resultCode: Int, data: Intent?) -> Unit
    lateinit var onRequestPermissionsResultHook: ( requestCode: Int, permissions: Array<String?>,
                                                   grantResults: IntArray) -> Unit

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        retainInstance  = true
    }

    /**
     * 获取跳转回调
     */
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        onActivityResultHook(requestCode,resultCode,data)
    }

    /**
     * 请求权限回调
     */
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String?>,
        grantResults: IntArray
    ) {
        onRequestPermissionsResultHook(requestCode,permissions,grantResults)
    }

}