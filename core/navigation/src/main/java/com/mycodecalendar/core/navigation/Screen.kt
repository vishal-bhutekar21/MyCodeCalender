package com.mycodecalendar.core.navigation

import kotlinx.serialization.Serializable

sealed interface Screen {
    @Serializable
    data object Onboarding : Screen
    
    @Serializable
    data object Home : Screen
    
    @Serializable
    data object Contests : Screen
    
    @Serializable
    data class ContestDetail(val contestId: String) : Screen
    
    @Serializable
    data object Platforms : Screen
    
    @Serializable
    data class PlatformDetail(val platformId: String) : Screen
    
    @Serializable
    data object Settings : Screen
    
    @Serializable
    data object Resources : Screen
}
