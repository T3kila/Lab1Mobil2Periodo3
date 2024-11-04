package com.api.apicamara

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.api.apicamara.pages.CameraPage
import com.api.apicamara.pages.MainPage
import com.api.apicamara.routes.Routes

@Composable
fun SetupNavGraph(
    navController: NavHostController
){

    NavHost(
        navController = navController,
        startDestination = Routes.MainPage.route
    ){
        composable(
            route = Routes.MainPage.route
        ){

            MainPage(navController)
        }
        composable(
            route = Routes.CameraPage.route
        ){
            CameraPage(navController)
        }
    }
}