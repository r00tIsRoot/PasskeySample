package com.google.credentialmanager.sample

import android.content.Intent
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.ui.Modifier
import androidx.credentials.CreatePublicKeyCredentialRequest
import androidx.credentials.CreatePublicKeyCredentialResponse
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetPasswordOption
import androidx.credentials.GetPublicKeyCredentialOption
import androidx.credentials.PasswordCredential
import androidx.credentials.PublicKeyCredential
import androidx.credentials.exceptions.CreateCredentialCancellationException
import androidx.credentials.exceptions.CreateCredentialCustomException
import androidx.credentials.exceptions.CreateCredentialException
import androidx.credentials.exceptions.CreateCredentialInterruptedException
import androidx.credentials.exceptions.CreateCredentialProviderConfigurationException
import androidx.credentials.exceptions.CreateCredentialUnknownException
import androidx.credentials.exceptions.publickeycredential.CreatePublicKeyCredentialDomException
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import java.security.SecureRandom

class LoginActivity : ComponentActivity() {
    private lateinit var credentialManager: CredentialManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PasskeySampleTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    LoginScreen(
                        modifier = Modifier.padding(innerPadding),
                        onSignup = { signUpWithPasskeys(it) },
                        onSignin = { signInWithSavedCredentials(configureGetCredentialRequest()) }
                    )
                }
            }
        }
        DataProvider.initSharedPref(applicationContext)
        credentialManager = CredentialManager.create(this)
    }

    // for Signup
    private fun signUpWithPasskeys(userName: String?) {
        if (userName.isNullOrEmpty()) return

        lifecycleScope.launch {
            val data = createPasskey(userName)

            data?.let {
                registerResponse(data)
                DataProvider.setSignedInThroughPasskeys(true)

                moveToVerified()
            }
        }
    }

    private suspend fun createPasskey(userName: String): CreatePublicKeyCredentialResponse? {
        val request = CreatePublicKeyCredentialRequest(fetchRegistrationJsonFromServer(userName))
        var response: CreatePublicKeyCredentialResponse? = null
        try {
            response = credentialManager.createCredential(
                applicationContext,
                request
            ) as CreatePublicKeyCredentialResponse
        } catch (e: CreateCredentialException) {
            handlePasskeyFailure(e)
        }
        return response
    }

    private fun fetchRegistrationJsonFromServer(userName: String): String {

        val response = applicationContext.readFromAsset("RegFromServer")

        //모의 객체에서 userId, name 및 Display name 업데이트
        return response.replace("<userId>", getEncodedUserId())
            .replace("<userName>", userName)
            .replace("<userDisplayName>", userName)
            .replace("<challenge>", getEncodedChallenge())
    }

    private fun getEncodedUserId(): String {
        val random = SecureRandom()
        val bytes = ByteArray(64)
        random.nextBytes(bytes)
        return Base64.encodeToString(
            bytes,
            Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING
        )
    }

    private fun getEncodedChallenge(): String {
        val random = SecureRandom()
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        return Base64.encodeToString(
            bytes,
            Base64.NO_WRAP or Base64.URL_SAFE or Base64.NO_PADDING
        )
    }

    private fun registerResponse(data: CreatePublicKeyCredentialResponse?): Boolean {
        val response = data?.registrationResponseJson?.parseAuthenticationResponse() ?: return false
        DataProvider.setPasskey(response)

        Log.d("isRoot", "local passkey : ${DataProvider.getPasskey(response.rawId)}")

        return true
    }

    private fun moveToVerified() {
        startActivity(Intent(this@LoginActivity, VerifiedActivity::class.java))
    }

    // for Signin
    private fun signInWithSavedCredentials(getCredentialRequest: GetCredentialRequest) {
        lifecycleScope.launch {
            val data = getSavedCredentials(getCredentialRequest)
            signin(data)
        }
    }

    private fun signin(data: String?) {
        data?.removePrefix("Passkey: ")?.toPasskey()?.let {
            val savedPasskey = DataProvider.getPasskey(it.rawId)

            savedPasskey?.let {
                if (savedPasskey.response.authenticatorData == it.response
                        .authenticatorData
                ) {
                    moveToVerified()
                } else {
                    Toast.makeText(applicationContext, "인증에 실패했습니다.", Toast.LENGTH_SHORT)
                        .show()
                }
            } ?: Toast.makeText(applicationContext, "인증에 실패했습니다.", Toast.LENGTH_SHORT)::show
        }
    }

    private fun configureGetCredentialRequest(): GetCredentialRequest {
        val getPublicKeyCredentialOption =
            GetPublicKeyCredentialOption(fetchAuthJsonFromServer(), null)
        val getPasswordOption = GetPasswordOption()
        val getCredentialRequest = GetCredentialRequest(
            listOf(
                getPublicKeyCredentialOption,
                getPasswordOption
            )
        )
        return getCredentialRequest
    }

    private fun fetchAuthJsonFromServer(): String {
        return applicationContext.readFromAsset("AuthFromServer")
    }

    private suspend fun getSavedCredentials(getCredentialRequest: GetCredentialRequest): String? {

        val result = try {
            credentialManager.getCredential(
                this,
                getCredentialRequest,
            )
        } catch (e: Exception) {
            Log.e("Auth", "getCredential failed with exception: " + e.message.toString())
            this.showErrorAlert(
                "저장된 자격 증명을 통해 인증하는 동안 오류가 발생했습니다. 추가 세부 정보는 로그를 확인하십시오."
            )
            return null
        }

        if (result.credential is PublicKeyCredential) {
            val cred = result.credential as PublicKeyCredential
            DataProvider.setSignedInThroughPasskeys(true)
            return "Passkey: ${cred.authenticationResponseJson}"
        }
        if (result.credential is PasswordCredential) {
            val cred = result.credential as PasswordCredential
            DataProvider.setSignedInThroughPasskeys(false)
            return "Got Password - User:${cred.id} Password: ${cred.password}"
        }
        if (result.credential is CustomCredential) {
            // 외부 로그인 라이브러리도 사용하는 경우 여기에서
            // 유틸리티 기능이 제공됩니다.
        }
        return null
    }


    // 이러한 오류는 암호 키를 만드는 동안 발생할 수 있는 오류 유형입니다.
    private fun handlePasskeyFailure(e: CreateCredentialException) {
        val msg = when (e) {
            is CreatePublicKeyCredentialDomException -> {
                // WebAuthn 사양에 따라 e.domError를 사용하여
                // 패스키 DOM 오류를 처리합니다.
                "패스키를 만드는 동안 오류가 발생했습니다. 자세한 내용은 로그를 확인하십시오."
            }

            is CreateCredentialCancellationException -> {
                // 사용자가 의도적으로 작업을 취소하고,
                // 자격 증명을 등록하지 않기로 선택했습니다.
                "사용자가 의도적으로 작업을 취소하고 자격 증명을 등록하지 않도록 선택했습니다. 자세한 내용은 로그를 확인하세요."
            }

            is CreateCredentialInterruptedException -> {
                // 재시도 가능한 오류입니다. 통화를 다시 시도하는 것이 좋습니다.
                "작업이 중단되었습니다. 호출을 다시 시도하십시오. 자세한 내용은 로그를 확인하세요."
            }

            is CreateCredentialProviderConfigurationException -> {
                // 앱에 provider 구성 종속성이 없습니다.
                // 아마도 "credentials-play-services-auth"가 누락된 것 같습니다.
                "앱에 공급자 구성 종속성이 없습니다. 자세한 내용은 로그를 확인하세요."
            }

            is CreateCredentialUnknownException -> {
                "암호를 만드는 동안 알 수 없는 오류가 발생했습니다. 자세한 내용은 로그를 확인하세요."
            }

            is CreateCredentialCustomException -> {
                // 3rd-party SDK에서 오류가 발생했습니다.
                // API 호출 시 CreateCustomCredentialRequest의 서브클래스를 사용한 경우,
                // 해당 SDK 내에서 정의된 특정 예외 타입 상수를 확인하여 e.type과 비교해야 합니다.
                // 만약 특정 예외 타입이 없다면, 예외를 무시하거나 로그로 남기십시오.
                "제3자 SDK에서 알 수 없는 오류가 발생했습니다. 자세한 내용은 로그를 확인하세요."
            }

            else -> {
                Log.w("Auth", "Unexpected exception type ${e::class.java.name}")
                "알 수 없는 오류가 발생했습니다."
            }
        }
        Log.e("Auth", "createPasskey failed with exception: " + e.message.toString())
        this.showErrorAlert(msg)
    }

}