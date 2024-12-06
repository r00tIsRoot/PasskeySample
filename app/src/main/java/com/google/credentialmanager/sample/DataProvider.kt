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
import android.content.SharedPreferences
import com.google.credentialmanager.sample.ui.AuthenticationResponse
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

object DataProvider {

    private lateinit var sharedPreference: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    private const val IS_SIGNED_IN = "isSignedIn"
    private const val IS_SIGNED_IN_THROUGH_PASSKEYS = "isSignedInThroughPasskeys"
    private const val PREF_NAME = "CREDMAN_PREF"

    private const val RAW_IDS = "RAW_IDS"

    fun initSharedPref(context: Context) {
        sharedPreference =
            context.applicationContext.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE)
        editor = sharedPreference.edit()
    }

    fun setPasskey(response: AuthenticationResponse) {
        editor.putString(response.rawId, response.toJsonString())
        editor.commit()

        setRawId(response.rawId)
    }

    fun setRawId(rawId: String) {
        editor.putString(
            RAW_IDS,
            Gson().toJson(
                getRawIds().apply {
                    this.add(rawId)
                }
            )
        )
        editor.commit()
    }

    fun getRawIds(): MutableList<String> {
        val gson = Gson()
        val existingListJson = sharedPreference.getString(RAW_IDS, null)
        val type = object : TypeToken<MutableList<String>>() {}.type
        val existingList: MutableList<String> = if (existingListJson != null) {
            gson.fromJson(existingListJson, type) ?: mutableListOf()
        } else {
            mutableListOf()
        }

        return existingList
    }

    fun getPasskey(rawId: String): AuthenticationResponse? {
        return sharedPreference.getString(rawId, null)?.parseAuthenticationResponse()
    }

    //Set if the user is signed in or not
    fun configureSignedInPref(flag: Boolean) {
        editor.putBoolean(IS_SIGNED_IN, flag)
        editor.commit()
    }

    //Set if signed in through passkeys or not
    fun setSignedInThroughPasskeys(flag: Boolean) {
        editor.putBoolean(IS_SIGNED_IN_THROUGH_PASSKEYS, flag)
        editor.commit()
    }

    fun isSignedIn(): Boolean {
        return sharedPreference.getBoolean(IS_SIGNED_IN, false)
    }

    fun isSignedInThroughPasskeys(): Boolean {
        return sharedPreference.getBoolean(IS_SIGNED_IN_THROUGH_PASSKEYS, false)
    }
}
