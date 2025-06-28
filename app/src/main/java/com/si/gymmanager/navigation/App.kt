package com.si.gymmanager.navigation

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.si.gymmanager.screens.DetailScreen
import com.si.gymmanager.screens.HomeScreen
import com.si.gymmanager.utils.NavigationTransitionUtil
import com.si.gymmanager.viewmodel.ViewModel

@Composable
fun App() {

    val navController = rememberNavController()
    val viewModel: ViewModel = hiltViewModel()
    NavHost(navController, startDestination = Routes.HomeScreen){
        composable<Routes.HomeScreen>(
            enterTransition = NavigationTransitionUtil.enterSlideTransition(),
            exitTransition = NavigationTransitionUtil.exitSlideTransition()
        ) {

            HomeScreen(
                navController,
                viewModel
            )
        }
        composable<Routes.DetailEntryScreen> {
            DetailScreen(
                navController,
                viewModel
            )
        }
    }

}