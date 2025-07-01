package com.si.gymmanager.navigation

import kotlinx.serialization.Serializable

// navigation screen routes
sealed class Routes {
    @Serializable
    object HomeScreen: Routes()

    @Serializable
    object DetailEntryScreen: Routes()

    @Serializable
    object RevenueScreen: Routes()


}