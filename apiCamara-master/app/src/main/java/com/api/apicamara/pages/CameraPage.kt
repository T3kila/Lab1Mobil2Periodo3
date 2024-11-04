package com.api.apicamara.pages

import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import coil.compose.AsyncImage
import com.api.apicamara.R
import com.api.apicamara.routes.Routes
import com.api.apicamara.ui.theme.ApiCamaraTheme
import java.io.File
import java.text.SimpleDateFormat
import java.util.Locale

private const val FILENAME_FORMAT = "yyyy-MM-dd-HH-mm-ss-SSS"

@Composable
fun CameraPage(navController: NavHostController) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    Scaffold(
        topBar = { CameraTopBar(navController) }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CameraBody(context, lifecycleOwner)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CameraTopBar(navController: NavHostController) {
    TopAppBar(
        title = {
            Text(
                text = "Visualizador",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        },
        navigationIcon = {
            IconButton(onClick = { navController.navigate(Routes.MainPage.route) }) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "return home",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
    )
}

@Composable
fun CameraBody(context: Context, lifecycleOwner: LifecycleOwner) {
    val previewView = remember { PreviewView(context) }
    val imageCapture = remember { ImageCapture.Builder().build() }
    val cameraProviderFuture = remember { ProcessCameraProvider.getInstance(context) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }
    var imageFile by remember { mutableStateOf<File?>(null) }
    var sharePicture by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {
        if (imageUri == null) {
            AndroidView(
                factory = { previewView },
                modifier = Modifier.fillMaxSize()
            )

            LaunchedEffect(cameraProviderFuture) {
                val cameraProvider = cameraProviderFuture.get()
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
                try {
                    cameraProvider.unbindAll()
                    cameraProvider.bindToLifecycle(lifecycleOwner, cameraSelector, preview, imageCapture)
                } catch (e: Exception) {
                    Toast.makeText(context, "No se pudo vincular la cámara", Toast.LENGTH_SHORT).show()
                }
            }

            FloatingActionButton(
                onClick = {
                    takePicture(
                        context, imageCapture,
                        onImageCaptured = { uri -> imageUri = uri },
                        onImageFileCapture = { file -> imageFile = file },
                        onError = { Toast.makeText(context, "Error al tomar la foto", Toast.LENGTH_SHORT).show() }
                    )
                },
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(24.dp),
                shape = CircleShape,
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = Icons.Default.PhotoCamera,
                    contentDescription = "Take Photo",
                    tint = Color.White
                )
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp, vertical = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                AsyncImage(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1f)
                        .padding(8.dp),
                    model = imageUri,
                    contentDescription = "current picture"
                )

                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Button(
                        onClick = {
                            savePicture(context, imageUri)
                            sharePicture = true
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                    ) {
                        Text("Guardar")
                    }

                    Button(
                        onClick = { imageUri = null; sharePicture = false },
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("Repetir")
                    }
                }

                Button(
                    onClick = {
                        if (sharePicture) {
                            sharePicture(context, imageFile)
                        } else {
                            Toast.makeText(context, "Guarde la foto antes de compartir", Toast.LENGTH_SHORT).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Text("Compartir")
                }
            }
        }
    }
}

fun takePicture(
    context: Context,
    imageCapture: ImageCapture,
    onImageFileCapture: (File) -> Unit,
    onImageCaptured: (Uri) -> Unit,
    onError: (Exception) -> Unit
) {
    val photoFile = File(
        getOutputDir(context),
        SimpleDateFormat(FILENAME_FORMAT, Locale.US).format(System.currentTimeMillis()) + ".jpg"
    )
    onImageFileCapture(photoFile)
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
    imageCapture.takePicture(
        outputOptions,
        ContextCompat.getMainExecutor(context),
        object : ImageCapture.OnImageSavedCallback {
            override fun onError(exception: ImageCaptureException) { onError(exception) }
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photoFile)
                onImageCaptured(savedUri)
                Log.d("Foto tomada:", "$savedUri")
            }
        }
    )
}

fun getOutputDir(context: Context): File {
    val mediaDir = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)?.let {
        File(it, context.getString(R.string.app_name)).apply { mkdirs() }
    }
    return mediaDir ?: context.filesDir
}

fun savePicture(context: Context, imageUri: Uri?) {
    imageUri?.let {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, "IMG_${System.currentTimeMillis()}.jpg")
            put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_PICTURES)
        }
        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues)
        resolver.openOutputStream(uri!!).use { outputStream ->
            resolver.openInputStream(imageUri)?.copyTo(outputStream!!)
        }
        Toast.makeText(context, "Foto guardada en fotos", Toast.LENGTH_SHORT).show()
    } ?: Toast.makeText(context, "Problema al cargar la foto", Toast.LENGTH_SHORT).show()
}

fun sharePicture(context: Context, imageFile: File?) {
    imageFile?.let {
        val imageUri = FileProvider.getUriForFile(context, "com.api.apicamara.fileprovider", it)
        val shareIntent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_STREAM, imageUri)
            type = "image/jpeg"
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }
        context.startActivity(Intent.createChooser(shareIntent, "Compartir Imagen"))
    } ?: Toast.makeText(context, "No hay imagen para compartir", Toast.LENGTH_SHORT).show()
}
