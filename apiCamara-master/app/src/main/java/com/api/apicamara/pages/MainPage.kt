package com.api.apicamara.pages

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Icon
import android.net.Uri
import android.os.Build
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ButtonElevation
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.api.apicamara.R
import com.api.apicamara.routes.Routes
import com.api.apicamara.ui.theme.ApiCamaraTheme

@Composable
fun MainPage(
    navController: NavHostController
){
    val context = LocalContext.current
    var permission by remember { mutableStateOf(false) }

    val requestPermissionsLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        permissions ->
        permission = permissions[Manifest.permission.READ_MEDIA_IMAGES] == true &&
                permissions[Manifest.permission.READ_MEDIA_VIDEO] == true &&
                permissions[Manifest.permission.CAMERA] == true

        if(!permission){
            Toast.makeText(
                context,
                "Se denego los permisos o limito el acceso",
                Toast.LENGTH_LONG).show()
        }
    }

    LaunchedEffect(Unit) {
        VerifyPermissions(
            context,
            onSuccess = {
                permission = !permission
            },
            onFailed = {
                Toast.makeText(
                    context,
                    "Se necesita que el usuario acepte los permisos",
                    Toast.LENGTH_LONG)
                    .show()
            }
        )
    }

    Scaffold(
        topBar = { MainTopBar(context) }
    ) {
        paddingValues ->

        Column(
            modifier = Modifier
                .padding(
                    top = paddingValues.calculateTopPadding(),
                    bottom = paddingValues.calculateBottomPadding(),
                    start = 12.dp,
                    end = 12.dp
                )
                .fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if(permission){
                MainBodyPermissionsGranted {
                    navController.navigate(Routes.CameraPage.route)
                }
            }else{
                MainBodyPermissionsFailed {
                    requestPermissionsLauncher.launch(
                        arrayOf(
                            Manifest.permission.READ_MEDIA_IMAGES,
                            Manifest.permission.READ_MEDIA_VIDEO,
                            Manifest.permission.CAMERA
                        )
                    )
                }
            }
        }

    }
}

@Preview(showBackground = true)
@Composable
fun MainPagePreview(){
    ApiCamaraTheme(dynamicColor = false) {
        MainPage(rememberNavController())
    }
}

//-------------------------------------------------------[TOP BAR]
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(
    context: Context
){

    TopAppBar(
        title = {
            Text(
                text = "Api Camara",
                fontSize = 30.sp,
                fontWeight = FontWeight.W400
            )
        },
        navigationIcon = {
            IconButton(
                onClick = {
                    (context as? Activity)?.finishAffinity()
                }
            ) {
                Icon(
                    imageVector = Icons.Filled.Close,
                    contentDescription = "close app"
                )
            }
        }
    )

}

//-------------------------------------------------------[BODY]

@Composable
fun MainBodyPermissionsFailed(
    onClick: () -> Unit
){
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(25.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ElevatedButton(
            modifier = Modifier.fillMaxWidth(0.75f),
            onClick = onClick,
            colors = ButtonDefaults.elevatedButtonColors(
                contentColor = Color.Black,
                containerColor = colorResource(R.color.btnAccesoCamara)
            )
        ) {
            Text(
                text = "Obtener acceso a la camara",
                fontSize = 22.sp,
                fontWeight = FontWeight.W500,
                textAlign = TextAlign.Center
            )
        }
        Text(
            text = stringResource(R.string.notaAccesoLimitado),
            fontSize = 13.sp,
            fontWeight = FontWeight.W300,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun MainBodyPermissionsGranted(
    onClick: () -> Unit
){
    ElevatedButton(
        modifier = Modifier.fillMaxWidth(0.75f),
        onClick = onClick,
        colors = ButtonDefaults.elevatedButtonColors(
            contentColor = Color.Black,
            containerColor = colorResource(R.color.btnAccesoCamara)
        )
    ) {
        Text(
            text = "Visualizador de contenido",
            fontSize = 22.sp,
            fontWeight = FontWeight.W500,
            textAlign = TextAlign.Center
        )

    }
}


//-------------------------------------------------------[PERMISSIONS]

fun VerifyPermissions(context: Context, onSuccess: () -> Unit, onFailed: () -> Unit){

    if(ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED
        || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.READ_MEDIA_VIDEO) != PackageManager.PERMISSION_GRANTED
        || ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED){
        onFailed()
    }else{
        onSuccess()
    }

}
