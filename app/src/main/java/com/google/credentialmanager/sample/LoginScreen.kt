package com.google.credentialmanager.sample

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LoginScreen(
    modifier: Modifier,
    onSignup: (String) -> Unit = {},
    onSignin: () -> Unit = {},
) {
    var userName by remember { mutableStateOf(TextFieldValue("")) }

    val commonModifier = modifier.fillMaxWidth()
    Surface(modifier = commonModifier) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Row {
                Text(
                    text = "ID : ",
                    style = TextStyle(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                    )
                )
                BasicTextField(
                    modifier = Modifier
                        .background(
                            color = Color.Gray
                        )
                        .fillMaxWidth(0.8f)
                        .padding(vertical = 0.dp),
                    maxLines = 1,
                    value = userName,
                    onValueChange = { userName = it },
                    textStyle = TextStyle(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        color = Color.White,
                    )
                )
            }

            Button(
                modifier = Modifier,
                onClick = { onSignup(userName.text) }
            ) {
                Text(
                    text = "Signup with passkey",
                    style = TextStyle(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = Color.White,
                    )
                )
            }

            Button(
                modifier = Modifier,
                onClick = { onSignin() }
            ) {
                Text(
                    text = "Signin with passkey",
                    style = TextStyle(
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 18.sp,
                        color = Color.White,
                    )
                )
            }

        }
    }
}