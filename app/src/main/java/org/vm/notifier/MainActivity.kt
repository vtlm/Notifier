package org.vm.notifier

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.annotation.RequiresExtension
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import org.vm.notifier.ui.theme.NotifierTheme
import timber.log.Timber
import java.security.Permission
import kotlin.system.exitProcess

val NOTIFICATION_ID = 0x357;


class MainActivity : ComponentActivity() {


    private var _statusString = MutableStateFlow("")
    private val statusString = _statusString.asStateFlow()

    private var _permissionsGranted = MutableStateFlow(false)
    private val permissionsGranted = _permissionsGranted.asStateFlow()

    private var isReCheckPermissions = false

    private val neededPermissions:  MutableList<String> = mutableListOf()

    @RequiresApi(Build.VERSION_CODES.Q)
    @RequiresExtension(extension = Build.VERSION_CODES.R, version = 1)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Create the NotificationChannel.
            val name = getString(R.string.channel_name)
            val descriptionText = getString(R.string.channel_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val CHANNEL_ID = "SECURITY_NOTIFY"
            val mChannel = NotificationChannel(CHANNEL_ID, name, importance)
            mChannel.description = descriptionText
            // Register the channel with the system. You can't change the importance
            // or other notification behaviors after this.
            val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(mChannel)
        }

        checkPermissions()

        enableEdgeToEdge()
        setContent {
            NotifierTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Box(modifier = Modifier.padding(innerPadding)) {
                        MainU(permissionsGranted.collectAsState().value)
                    }
                }
            }
        }
    }


    //todo: to coroutine
    @RequiresApi(Build.VERSION_CODES.Q)
    private val reCheckPermissions =
        registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()) { result ->
            //Log.d("DPR","in afterPermissions $result")
//            checkPermissions()
//            triggerRestart(this)
            if(result.resultCode == RESULT_OK){
                result.data?.data?.also {
                    Timber.tag("DPR").d("afterPermissions")
                }
            }
        }


    // Register the permissions callback, which handles the user's response to the
// system permissions dialog. Save the return value, an instance of
// ActivityResultLauncher. You can use either a val, as shown in this snippet,
// or a lateinit var in your onAttach() or onCreate() method.
    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) { permissions ->
            neededPermissions.clear()
            _permissionsGranted.value = true
            permissions.entries.forEach {
                Timber.tag("DEBUG").i("${it.key} = ${it.value}")
                if (it.value) {
                    println("Successful......")

                }else{
                    neededPermissions += it.key
                    _permissionsGranted.value = false
                }
            }

            if(_permissionsGranted.value){
//                mediaViewModel.setUserScrollPos(0)
//                lifecycle.addObserver(mediaViewModel)
            }

            _statusString.value = ""
        }


    @RequiresApi(Build.VERSION_CODES.TIRAMISU)
    private fun checkPermissions(){

        _statusString.value = resources.getString(R.string.checking_permissions)

        val requiredPermissions: MutableList<String> = mutableListOf()

        checkIsPermissionGranted(android.Manifest.permission.POST_NOTIFICATIONS, requiredPermissions)
//        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
//            checkIsPermissionGranted(android.Manifest.permission.READ_EXTERNAL_STORAGE, requiredPermissions)
//        }
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            checkIsPermissionGranted(android.Manifest.permission.READ_MEDIA_AUDIO, requiredPermissions)
//        }
//        requiredPermissions += android.Manifest.permission.POST_NOTIFICATIONS;
        if(requiredPermissions.isNotEmpty()) {
            requestPermissionLauncher.launch(requiredPermissions.toTypedArray())
        }else{
            _statusString.value = ""
            _permissionsGranted.value = true
        }

    }

    private fun checkIsPermissionGranted(permission: String, collector: MutableList<String>){
        val permissionRequest = ContextCompat.checkSelfPermission(applicationContext, permission)
        if(permissionRequest == PackageManager.PERMISSION_DENIED){
            collector += permission
        }
    }

//    fun triggerRestart(context: Activity) {
//        val intent = Intent(context, MainActivity::class.java)
//        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//        context.startActivity(intent)
//        context.finish()
//        Runtime.getRuntime().exit(0)
//    }

    @RequiresExtension(extension = Build.VERSION_CODES.R, version = 1)
    @RequiresApi(Build.VERSION_CODES.Q)
    @Composable
    fun MainU(permissionsGranted: Boolean){
        ShowProgressOrContent(statusString.collectAsState().value) {
            when(permissionsGranted) {
                false -> {
                    RequestPermissionsScreen()
                }
                true -> {
                    //Nav()
                    MainScreen()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    @Composable
    fun RequestPermissionsScreen(){
        Column {
            Row(Modifier.weight(1f)) {
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.background)
                ) {

                    Text(
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = MaterialTheme.typography.headlineSmall.fontSize,
                        text = resources.getString(R.string.wait_for_permissions)
                    )
                }
            }
            Row(Modifier.weight(1f)){
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.background)
                ) {
                    Column {
                        neededPermissions.forEachIndexed{ ind, str ->
                            val permissionInfo = applicationContext.packageManager.getPermissionInfo(str, PackageManager.GET_META_DATA)
                            val description = permissionInfo.loadDescription(applicationContext.packageManager)
                            Text(text = "${ind + 1}. ${description.toString()}",
                                color = MaterialTheme.colorScheme.onBackground,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                            )
                        }
                    }
                }

            }
            Row(Modifier.weight(1f)){
                Box(
                    contentAlignment = Alignment.Center,
                    modifier = Modifier
                        .fillMaxSize()
                        .background(color = MaterialTheme.colorScheme.background)
                ) {

                    Column(Modifier.fillMaxWidth(),
//                    Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally){
                        Button(onClick = {
                            isReCheckPermissions = true
//                                checkPermissions()
                            reCheckPermissions.launch(Intent().apply {
                                action = Settings.ACTION_APPLICATION_DETAILS_SETTINGS
                                data = Uri.fromParts("package", packageName, null)
                            })
                        },
                            Modifier.fillMaxWidth(0.9f)
                                .padding(vertical = 4.dp)) {
                            Text(text = resources.getString(R.string.Go_To_Settings))
                        }
                        Button(onClick = {
//                                finishAffinity();
                            finishAndRemoveTask()
                            exitProcess(0)
                        },
                            modifier = Modifier.padding(vertical = 4.dp))
                        { Text(text = resources.getString(R.string.Exit))}
                    }
                }

            }
        }

    }

    @Composable
    fun MainScreen() {
        Column {
            Text("Main screen")
            Button(onClick = {
                var builder = NotificationCompat.Builder(
                    applicationContext,
                    applicationContext.getString(R.string.channel_id)
                )
                    .setSmallIcon(R.drawable.outline_error_24)
                    .setContentTitle("My notification")
                    .setContentText("Much longer text that cannot fit one line...")
                    .setStyle(
                        NotificationCompat.BigTextStyle()
                            .bigText("Much longer text that cannot fit one line...")
                    )
                    .setPriority(NotificationCompat.PRIORITY_HIGH)

                with(NotificationManagerCompat.from(applicationContext)) {

                    val req = ActivityCompat.checkSelfPermission(
                        applicationContext,
                        Manifest.permission.POST_NOTIFICATIONS
                    )

                    if ( req != PackageManager.PERMISSION_GRANTED
                    ) {
                        // TODO: Consider calling
                        // ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        // public fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>,
                        //                                        grantResults: IntArray)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.

                        return@with
                    }
                    // notificationId is a unique int for each notification that you must define.
                    notify(NOTIFICATION_ID, builder.build())
                }

            }) {
                Text("Notify")
            }
        }

    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    NotifierTheme {
        Greeting("Android")
    }
}