package com.si.gymmanager.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.si.gymmanager.screens.DetailScreen
import com.si.gymmanager.screens.HomeScreen
import com.si.gymmanager.viewmodel.ViewModel

@Composable
fun App() {

    val navController = rememberNavController()
    val viewModel: ViewModel = hiltViewModel()
    NavHost(navController, startDestination = Routes.DetailEntryScreen){
        composable<Routes.HomeScreen> {
            HomeScreen()
        }
        composable<Routes.DetailEntryScreen> {
            DetailScreen(
                navController,
                viewModel
            )
        }
    }

}