package com.example.sendsms

import android.app.Activity
import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Build
import com.yanzhenjie.permission.AndPermission


object KotlinPermissionUtils {

    /**
     * 权限提示
     */
    fun showPermission(
        mContext: Activity, titleContent: String,
        permissionStr: Array<String>
        , onGrantedListener: () -> Unit
    ) {
        AndPermission.with(mContext).runtime().permission(permissionStr)
            .onGranted { permissions ->
                onGrantedListener()
            }
            .onDenied { permissions ->
                val builder = AlertDialog.Builder(mContext)
                builder.setTitle("权限提示")
                builder.setMessage(titleContent) //提示内容
                builder.setPositiveButton("确定")
                { dialog: DialogInterface, which: Int ->
                  getAppDetailSettingIntent(
                        mContext
                    )
                }
                builder.setNegativeButton("取消") { dialog: DialogInterface, which: Int -> }
                val dialog = builder.create()
                dialog.show()
            }
            .start()
    }

    /**
     * 权限提示
     */
    fun showPermission(
        mContext: Activity, titleName: String,
        titleContent: String,
        permissionStr: Array<String>
        , onGrantedListener: () -> Unit
    ) {
        AndPermission.with(mContext).runtime().permission(permissionStr)
            .onGranted { permissions ->
                onGrantedListener()
            }
            .onDenied { permissions ->
                val builder = AlertDialog.Builder(mContext)
                builder.setTitle(titleName)
                builder.setMessage(titleContent) //提示内容
                builder.setPositiveButton("确定")
                { dialog: DialogInterface, which: Int ->
                   getAppDetailSettingIntent(
                        mContext
                    )
                }
                builder.setNegativeButton("取消") { dialog: DialogInterface, which: Int -> }
                val dialog = builder.create()
                dialog.show()
            }
            .start()
    }


    /**
     * 跳转到权限设置界面
     */
    fun getAppDetailSettingIntent(context: Activity) {

        // vivo 点击设置图标>加速白名单>我的app
        //      点击软件管理>软件管理权限>软件>我的app>信任该软件
        var appIntent = context.packageManager.getLaunchIntentForPackage("com.iqoo.secure")
        if (appIntent != null) {
            context.startActivity(appIntent)

            return
        }

        // oppo 点击设置图标>应用权限管理>按应用程序管理>我的app>我信任该应用
        //      点击权限隐私>自启动管理>我的app
        appIntent = context.packageManager.getLaunchIntentForPackage("com.oppo.safe")
        if (appIntent != null) {
            context.startActivity(appIntent)

            return
        }

        val intent = Intent()
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        if (Build.VERSION.SDK_INT >= 9) {
            intent.action = "android.settings.APPLICATION_DETAILS_SETTINGS"
            intent.data = (Uri.fromParts("package", context.packageName, null))
        } else if (Build.VERSION.SDK_INT <= 8) {
            intent.action = Intent.ACTION_VIEW
            intent.setClassName("com.android.settings", "com.android.settings.InstalledAppDetails")
            intent.putExtra("com.android.settings.ApplicationPkgName", context.packageName)
        }
        context.startActivity(intent)
    }
}