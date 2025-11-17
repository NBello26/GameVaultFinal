package com.example.gamevault.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.gamevault.R
import com.example.gamevault.data.SharedPreferencesHelper

@Composable
fun TopBarSection(
    navController: NavController,
    title: String,
    prefs: SharedPreferencesHelper,
    onLogout: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo1),
            contentDescription = "Logo",
            modifier = Modifier
                .size(40.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.weight(1f))

        Text(
            text = title,
            fontSize = 22.sp,
            color = Color(0xFF1E88E5)
        )

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                prefs.logout()
                onLogout()
                navController.navigate("start") {
                    popUpTo("home") { inclusive = true }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = Color.Red
            ),
            modifier = Modifier.height(36.dp)
        ) {
            Text("Logout", color = Color.White, fontSize = 12.sp)
        }
    }
}
