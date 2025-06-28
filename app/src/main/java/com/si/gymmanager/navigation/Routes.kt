package com.si.gymmanager.navigation

import kotlinx.serialization.Serializable

sealed class Routes {
    @Serializable
    object HomeScreen: Routes()

    @Serializable
    object DetailEntryScreen: Routes()


}