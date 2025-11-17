package com.example.gamevault.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.gamevault.R

@Composable
fun StartScreen(navController: androidx.navigation.NavController) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF101010)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(25.dp)
        ) {

            // Logo opcional (si tienes uno)
            Image(
                painter = painterResource(id = R.drawable.logo1),
                contentDescription = "Logo",
                modifier = Modifier.size(120.dp)
            )

            Text(
                "Bienvenido a GameVault",
                color = Color.White,
                fontSize = 26.sp,
                fontWeight = FontWeight.Bold
            )

            Button(
                onClick = { navController.navigate("login") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(0.7f),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF1E3A8A),contentColor = Color.White)
            ) { Text("Iniciar Sesión") }

            OutlinedButton(
                onClick = { navController.navigate("register") },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth(0.7f),
                colors = ButtonDefaults.outlinedButtonColors(containerColor = Color(0xFF6A0DAD),contentColor = Color.White)
            ) { Text("Crear Cuenta") }
        }
    }
}
