package org.csu.lumtrace.app.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.csu.lumtrace.app.ui.home.HomeScreen
import org.csu.lumtrace.app.ui.login.LoginScreen
import org.csu.lumtrace.app.ui.record.RecordScreen
import org.csu.lumtrace.app.ui.reminder.ReminderScreen
import org.csu.lumtrace.app.ui.result.ResultScreen
import org.csu.lumtrace.app.ui.symptom.SymptomScreen

object Routes {
    const val LOGIN = "login"
    const val HOME = "home"
    const val SYMPTOM = "symptom"
    const val RESULT = "result"
    const val RECORD = "record"
    const val REMINDER = "reminder"
}

@Composable
fun AppNavHost() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Routes.LOGIN) {
        composable(Routes.LOGIN) {
            LoginScreen(
                onLoggedIn = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                },
                onGuest = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.LOGIN) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.HOME) {
            HomeScreen(
                onOpenSymptom = { navController.navigate(Routes.SYMPTOM) },
                onOpenRecords = { navController.navigate(Routes.RECORD) },
                onOpenReminders = { navController.navigate(Routes.REMINDER) },
                onLogout = {
                    navController.navigate(Routes.LOGIN) {
                        popUpTo(Routes.HOME) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.SYMPTOM) {
            SymptomScreen(
                onBack = { navController.popBackStack() },
                onSubmitted = { navController.navigate(Routes.RESULT) }
            )
        }
        composable(Routes.RESULT) {
            ResultScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.RECORD) {
            RecordScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.REMINDER) {
            ReminderScreen(onBack = { navController.popBackStack() })
        }
    }
}