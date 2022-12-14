package dev.mgmix.beautyganservingapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ContentResolver
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.Point
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.gun0912.tedpermission.PermissionListener
import com.gun0912.tedpermission.TedPermission
import gun0912.tedbottompicker.TedRxBottomPicker
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.layout_result.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

class MainActivity : AppCompatActivity() {


    var test = ""
    var path = ""
    val api = Provider.api
    val dialog: Dialog by lazy {
        Dialog(this, R.style.AppTheme).apply {
            requestWindowFeature(Window.FEATURE_NO_TITLE)
            setContentView(R.layout.layout_result)
        }
    }

    @SuppressLint("CheckResult")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        var srcPart: MultipartBody.Part? = null
        var refPart: MultipartBody.Part? = null

        // Add Permission
        setPermission()

        if(File("/storage/emulated/0/DCIM/beauty_1.jpg").exists()) File("/storage/emulated/0/DCIM/beauty_1.jpg").delete()
        if(File("/storage/emulated/0/DCIM/beauty_2.jpg").exists()) File("/storage/emulated/0/DCIM/beauty_2.jpg").delete()
        if(File("/storage/emulated/0/DCIM/beauty_3.jpg").exists()) File("/storage/emulated/0/DCIM/beauty_3.jpg").delete()


        srcButton.setOnClickListener {
            TedRxBottomPicker.with(this)
                .show()
                .subscribe({
                    // Show SrcImage
                    Log.d("TEST", " $it: ");
                    Glide.with(this)
                        .load(it)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(srcImage)
                    // Store Uri Path
                    val file = File(it.path.toString())
                    Log.e("test",it.path.toString())
                    val requestBody = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
                    srcPart = MultipartBody.Part.createFormData("srcImage", file.name, requestBody)

                }) {
                    it.printStackTrace()
                }
        }
        radio_group.setOnCheckedChangeListener { group, checkedId ->
            when (checkedId) {
                R.id.beauty_1 -> test = "beauty_1";

                R.id.beauty_2 -> test = "beauty_2";

                R.id.beauty_3 -> test = "beauty_3";
            }
            val resources: Resources = this.resources
            if(test.equals("beauty_1")) {
                path = "/storage/emulated/0/DCIM/beauty_1.jpg"
                if(bitmapToFile(BitmapFactory.decodeResource(resources, R.drawable.beauty_1), "/storage/emulated/0/DCIM/beauty_1.jpg").exists()){
                    Log.e("Telechips","beauty_1")
                }
            }
            else if(test.equals("beauty_2")){
                path = "/storage/emulated/0/DCIM/beauty_2.jpg"
                if(bitmapToFile(BitmapFactory.decodeResource(resources, R.drawable.beauty_2), "/storage/emulated/0/DCIM/beauty_2.jpg").exists()){
                    Log.e("Telechips","beauty_2")
                }
            }
            else if(test.equals("beauty_3")){
                path = "/storage/emulated/0/DCIM/beauty_3.jpg"
                if(bitmapToFile(BitmapFactory.decodeResource(resources, R.drawable.beauty_3), "/storage/emulated/0/DCIM/beauty_3.jpg").exists()){
                    Log.e("Telechips","beauty_3")
                }
            }
        }

        execute.setOnClickListener {
            // TODO API Call and Popup Loading View and

            val file = File(path)
            val requestBody = file.asRequestBody("multipart/form-data".toMediaTypeOrNull())
            refPart = MultipartBody.Part.createFormData("refImage", file.name, requestBody)

            if (srcPart == null || refPart == null) {
                Toast.makeText(this, "????????? ???????????????", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            progressBar.visibility = View.VISIBLE
            api.uploadImage(
                srcPart ?: return@setOnClickListener, refPart ?: return@setOnClickListener
            )
                .observeOn(AndroidSchedulers.mainThread())
                .map {
                    executeDialog(it.path)
                }
                .doOnSuccess { progressBar.visibility = View.INVISIBLE }
                .doOnError { progressBar.visibility = View.INVISIBLE }
                .subscribeOn(Schedulers.io())
                .subscribe({}) {
                    it.printStackTrace()
                }
        }
        if(File("/storage/emulated/0/DCIM/beauty_1.jpg").exists()) File("/storage/emulated/0/DCIM/beauty_1.jpg").delete()
        if(File("/storage/emulated/0/DCIM/beauty_2.jpg").exists()) File("/storage/emulated/0/DCIM/beauty_2.jpg").delete()
        if(File("/storage/emulated/0/DCIM/beauty_3.jpg").exists()) File("/storage/emulated/0/DCIM/beauty_3.jpg").delete()
    }

    fun bitmapToFile(bitmap: Bitmap, path: String): File{
        var file = File(path)
        var out: OutputStream? = null
        try{
            file.createNewFile()
            out = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out)
        }finally{
            out?.close()
        }
        return file
    }

    private fun setPermission() {
        val permissionListener: PermissionListener = object : PermissionListener {
            override fun onPermissionGranted() {

            }

            override fun onPermissionDenied(deniedPermissions: MutableList<String>?) {
                finish()
            }
        }

        TedPermission.with(this)
            .setPermissionListener(permissionListener)
            .setDeniedMessage("?????? ?????? ??? ???????????? ???????????? [??????] ???????????? ?????? ????????? ???????????????.")
            .setPermissions(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
            .check()
    }

    private fun executeDialog(path: String) {
        dialog.apply {
            window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            Glide.with(this@MainActivity)
                .load(Provider.baseUrl + "/image/" + path)
                .apply(RequestOptions.bitmapTransform(RoundedCorners(16)))
                .skipMemoryCache(true)
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .into(image)

            val size = Point()
            windowManager.defaultDisplay.getSize(size)
            window?.setLayout(size.x, size.y)
            show()

            close.setOnClickListener {
                dialog.dismiss()
            }

        }
    }

}
