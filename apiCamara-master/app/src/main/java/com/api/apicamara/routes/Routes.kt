package com.api.apicamara.routes

const val ROOT_MAIN_PAGE = "main"
const val ROOT_CAMERA_PAGE = "camera"

sealed class Routes(
    val route: String
){
    object MainPage : Routes(route = ROOT_MAIN_PAGE)
    object CameraPage : Routes(route = ROOT_CAMERA_PAGE)
}