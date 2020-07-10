package com.takusemba.rtmppublishersample

import android.Manifest
import android.opengl.GLSurfaceView
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.david.rs.requestPermissionStick
import com.takusemba.rtmppublisher.Publisher
import com.takusemba.rtmppublisher.PublisherListener

class MainActivity : AppCompatActivity(), PublisherListener {

    private  var publisher: Publisher? = null
    private lateinit var glView: GLSurfaceView
    private lateinit var container: RelativeLayout
    private lateinit var publishButton: Button
    private lateinit var cameraButton: ImageView
    private lateinit var label: TextView

    private var url = if(Build.MODEL == "MIX 2S") "rtmp://push.guotailimin.com/live/111?auth_key=1594295755946-0-0-5c7859f8023537168238f20b434f2ed5" else "rtmp://push.guotailimin.com/live/111?auth_key=1594295755946-0-0-5c7859f8023537168238f20b434f2ed5"
    private val handler = Handler()
    private var thread: Thread? = null
    private var isCounting = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        glView = findViewById(R.id.surface_view)
        container = findViewById(R.id.container)
        publishButton = findViewById(R.id.toggle_publish)
        cameraButton = findViewById(R.id.toggle_camera)
        label = findViewById(R.id.live_label)
        url = intent.getStringExtra("url")?:"rtmp://push.guotailimin.com/live/111?auth_key=1594295755946-0-0-5c7859f8023537168238f20b434f2ed5"
        requestPermissionStick(permissions = arrayOf(
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.RECORD_AUDIO,
            Manifest.permission.VIBRATE, Manifest.permission.CAMERA,
            Manifest.permission.ACCESS_NETWORK_STATE)){ isGrantAll, permissions, grantResults ->
           if(isGrantAll){

           }

        }
        if (url.isBlank()) {
            Toast.makeText(this, R.string.error_empty_url, Toast.LENGTH_SHORT)
                    .apply { setGravity(Gravity.CENTER, 0, 0) }
                    .run { show() }
        } else {
            publisher = Publisher.Builder(this)
                    .setGlView(glView)
                .setPath( Environment.getExternalStorageDirectory()
                        .toString() + "/1.mp4")
               // .setUrl(url)
                    .setSize(Publisher.Builder.DEFAULT_WIDTH, Publisher.Builder.DEFAULT_HEIGHT)
                    .setAudioBitrate(Publisher.Builder.DEFAULT_AUDIO_BITRATE)
                    .setVideoBitrate(Publisher.Builder.DEFAULT_VIDEO_BITRATE)
                    .setCameraMode(Publisher.Builder.DEFAULT_MODE)
                    .setListener(this)
                    .build()

            publishButton.setOnClickListener {
                if (publisher!!.isPublishing) {
                    publisher?.stopPublishing()
                } else {
                    publisher?.startPublishing()
                }
            }

            cameraButton.setOnClickListener {
                publisher?.switchCamera()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if (url.isNotBlank() && publisher !=null) {
            updateControls()
        }
    }

    override fun onRemoteStarted() {
        Toast.makeText(this, R.string.started_publishing, Toast.LENGTH_SHORT)
                .apply { setGravity(Gravity.CENTER, 0, 0) }
                .run { show() }
        updateControls()
        startCounting()
    }

    override fun onRemoteStopped() {
        Toast.makeText(this, R.string.stopped_publishing, Toast.LENGTH_SHORT)
                .apply { setGravity(Gravity.CENTER, 0, 0) }
                .run { show() }
        updateControls()
        stopCounting()
    }

    override fun onDisconnected() {
        Toast.makeText(this, R.string.disconnected_publishing, Toast.LENGTH_SHORT)
                .apply { setGravity(Gravity.CENTER, 0, 0) }
                .run { show() }
        updateControls()
        stopCounting()
    }

    override fun onLocalStarted() {
        Toast.makeText(this, R.string.started_publishing, Toast.LENGTH_SHORT)
            .apply { setGravity(Gravity.CENTER, 0, 0) }
            .run { show() }
        updateControls()
        startCounting()
    }

    override fun onLocalStopped() {
        updateControls()
        stopCounting()
    }

    override fun onFailedToConnect() {
        Toast.makeText(this, R.string.failed_publishing, Toast.LENGTH_SHORT)
                .apply { setGravity(Gravity.CENTER, 0, 0) }
                .run { show() }
        updateControls()
        stopCounting()
    }

    private fun updateControls() {
        publishButton.text = getString(if (publisher!!.isPublishing) R.string.stop_publishing else R.string.start_publishing)
    }

    private fun startCounting() {
        isCounting = true
        label.text = getString(R.string.publishing_label, 0L.format(), 0L.format())
        label.visibility = View.VISIBLE
        val startedAt = System.currentTimeMillis()
        var updatedAt = System.currentTimeMillis()
        thread = Thread {
            while (isCounting) {
                if (System.currentTimeMillis() - updatedAt > 1000) {
                    updatedAt = System.currentTimeMillis()
                    handler.post {
                        val diff = System.currentTimeMillis() - startedAt
                        val second = diff / 1000 % 60
                        val min = diff / 1000 / 60
                        label.text = getString(R.string.publishing_label, min.format(), second.format())
                    }
                }
            }
        }
        thread?.start()
    }

    private fun stopCounting() {
        isCounting = false
        label.text = ""
        label.visibility = View.GONE
        thread?.interrupt()
    }

    private fun Long.format(): String {
        return String.format("%02d", this)
    }
}