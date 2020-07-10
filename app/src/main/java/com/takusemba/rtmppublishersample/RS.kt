package com.david.rs

import android.content.Intent
import android.content.pm.PackageManager.PERMISSION_DENIED
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

open class RS  private constructor(){

    companion object {
        private var rsRequestCode = 10000
        private const val RESULT_FRAGMENT_ID = "rsEmptyOnResultFragmentId"

        private var instance : RS? = null

            get() {
                if (field == null) {
                    field = RS()
                }
                return field
            }

        @Synchronized
        fun get() : RS{
            return instance!!
        }
        private fun getRsReqCode(): Int {
            rsRequestCode += 1
            return rsRequestCode
        }

        /**
         * activity跳转result粘贴
         */
        fun stickActivityResult(activity: AppCompatActivity,intent: Intent,onResult:OnActivityStickResult){
            val reqCode = getRsReqCode()
            var fragment = addFragmentToActivity(activity)
            if(fragment is RSEmptyOnResultFragment){
                    fragment.onActivityResultHook = {requestCode,resultCode,data->
                        if(reqCode == requestCode){
                            onResult(resultCode,data)
                        }
                    }

            }
            fragment?.startActivityForResult(intent, reqCode)

        }
        /**
         * activity请求权限
         */
        fun stickRequestPermission(activity: AppCompatActivity,permissions:Array<String>,onResult: OnPermissionStickResult){
            val reqCode = getRsReqCode()
            var fragment = addFragmentToActivity(activity)
            if(fragment is RSEmptyOnResultFragment) {
                fragment.onRequestPermissionsResultHook = { requestCode, ps, grantResults ->
                        if (reqCode == requestCode) {
                            val deniedIndex  = grantResults.find {
                                 it == PERMISSION_DENIED
                            }
                            onResult(deniedIndex == null ,ps, grantResults)
                        }
                    }
            }
            fragment?.requestPermissions(permissions, reqCode)

        }

        private fun addFragmentToActivity(activity: AppCompatActivity): Fragment? {
            var fragment = activity.supportFragmentManager.findFragmentByTag(RESULT_FRAGMENT_ID)
            if (fragment == null) {
                fragment = RSEmptyOnResultFragment()
                activity.supportFragmentManager.beginTransaction().add(fragment, RESULT_FRAGMENT_ID)
                    .commitAllowingStateLoss()
                activity.supportFragmentManager.executePendingTransactions()
            }
            return fragment
        }
    }





}

typealias OnPermissionStickResult = (isGrantAll:Boolean,permissions: Array<String?>,grantResults: IntArray) -> Unit

typealias OnActivityStickResult = (resultCode:Int, data:Intent?)->Unit

/**
 * AppCompatActivity内联startActivityStick函数，用于跳转获取返回数据Intent
 */
inline fun AppCompatActivity.startActivityStick(intent: Intent,
                                                      noinline onResult:OnActivityStickResult){
        RS.stickActivityResult(this,intent,onResult)
}
/**
 * AppCompatActivity内联requestPermissionStick函数，用于请求权限Result
 */
inline  fun AppCompatActivity.requestPermissionStick( permissions:Array<String>,noinline onResult: OnPermissionStickResult){
        RS.stickRequestPermission(this,permissions,onResult)
}

