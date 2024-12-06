/*
 * Copyright 2023 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.google.credentialmanager.sample

import android.content.Context
import androidx.appcompat.app.AlertDialog
import com.google.credentialmanager.sample.ui.AuthenticationResponse
import com.google.credentialmanager.sample.ui.SigninPasskey
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException

fun Context.readFromAsset(fileName: String): String {
    var data = ""
    this.assets.open(fileName).bufferedReader().use {
        data = it.readText()
    }
    return data
}

fun Context.showErrorAlert(msg: String) {
    AlertDialog.Builder(this)
        .setTitle("An error occurred")
        .setMessage(msg)
        .setNegativeButton("Ok", null)
        .setIcon(android.R.drawable.ic_dialog_alert)
        .show()
}

fun String.parseAuthenticationResponse(): AuthenticationResponse? {
    return try {
        val gson = Gson()
        gson.fromJson(this, AuthenticationResponse::class.java)
    } catch (e: JsonSyntaxException) {
        e.printStackTrace()
        null // 변환 실패 시 null 반환
    }
}

fun String.toPasskey(): SigninPasskey? {
    return try {
        val gson = Gson()
        gson.fromJson(this, SigninPasskey::class.java)
    } catch (e: JsonSyntaxException) {
        e.printStackTrace()
        null // 변환 실패 시 null 반환
    }
}