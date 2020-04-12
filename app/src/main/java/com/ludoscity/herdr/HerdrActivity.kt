/*
 *     Copyright (c) 2020. f8full https://github.com/f8full
 *     Herdr is a privacy conscious multiplatform mobile data collector
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Lesser General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Lesser General Public License for more details.
 *
 *     You should have received a copy of the GNU Lesser General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package com.ludoscity.herdr

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsClient
import androidx.browser.customtabs.CustomTabsIntent
import androidx.browser.customtabs.CustomTabsServiceConnection
import androidx.lifecycle.ViewModelProviders
import com.google.android.material.snackbar.Snackbar
import com.ludoscity.herdr.common.domain.entity.UserCredentials
import com.ludoscity.herdr.common.base.Response
import com.ludoscity.herdr.common.domain.entity.AuthClientRegistration
import com.ludoscity.herdr.common.ui.login.*
import com.nimbusds.oauth2.sdk.ParseException
import com.nimbusds.oauth2.sdk.ResponseType
import com.nimbusds.oauth2.sdk.Scope
import com.nimbusds.oauth2.sdk.id.ClientID
import kotlinx.android.synthetic.main.activity_herdr.*
import com.nimbusds.oauth2.sdk.id.State
import com.nimbusds.openid.connect.sdk.*
import sample.hello
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException

class HerdrActivity : AppCompatActivity() {

    lateinit var loginViewModel: LoginViewModel


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_herdr)

        //bind views
        activity_herdr_tv.text = hello()

        activity_herdr_button.setOnClickListener {
            //loginViewModel.registerAuthClient(hello())
            loginViewModel.registerAuthClient("https://f8full.mycozy.cloud")
        }

        //init viewmodel
        loginViewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)

        //register observer
        loginViewModel.authClientRegistrationResult.addObserver { getClientRegistrationState(it) }

        loginViewModel.userCredentialsResult.addObserver { getUserCredentialsState(it) }
    }

    private fun getUserCredentialsState(state: UserCredentialsState) {
        when(state) {
            is SuccessUserCredentials -> {
                //TODO: hide in progress
                val response = state.response as Response.Success
                onUserCredentialsSuccess(userCredentials = response.data)
            }
            is InProgressUserCredentials -> {
                //TODO: show in progress
            }
            is ErrorUserCredentials -> {
                //TODO: hide loading
                val response = state.response as Response.Error
                showError(response.message)
            }
        }
    }

    private fun getClientRegistrationState(state: AuthClientRegistrationState) {
        when (state) {
            is SuccessAuthClientRegistration -> {
                //TODO: hide in progress
                val response = state.response as Response.Success
                onClientRegistrationSuccess(registrationInfo = response.data)
            }
            is InProgressAuthClientRegistration -> {
                //TODO: show in progress
            }
            is ErrorAuthClientRegistration -> {
                //TODO: hide loading
                val response = state.response as Response.Error
                showError(response.message)
            }
        }
    }

    private fun onClientRegistrationSuccess(registrationInfo: AuthClientRegistration) {

        //debug
        activity_herdr_tv.text = "Registration = $registrationInfo"

        launchAuthorizationFlow(registrationInfo)
    }

    private fun onUserCredentialsSuccess(userCredentials: UserCredentials) {

        //debug -- We are fully logged in
        activity_herdr_tv.text = "Credentials = $userCredentials"
    }

    private var authRequestState = State()
    private var authRequestNonce = Nonce()

    private fun launchAuthorizationFlow(registrationInfo: AuthClientRegistration) {

        // Generate random state string for pairing the response to the request
        authRequestState = State()
        // Generate nonce
        authRequestNonce = Nonce()
        // Specify scope
        //TODO: custom scope from UI
        val scope = Scope.parse("openid io.cozy.files io.cozy.oauth.clients")

        // Compose the request
        val authenticationRequest = AuthenticationRequest(
                URI("${registrationInfo.stackBaseUrl}/auth/authorize"),
                ResponseType(ResponseType.Value.CODE),
                scope,
                ClientID(registrationInfo.clientId),
                URI(registrationInfo.redirectUriCollection[0]),
                authRequestState,
                authRequestNonce
        )

        // Open browser
        val connection = object : CustomTabsServiceConnection() {
            override fun onCustomTabsServiceConnected(name: ComponentName, client: CustomTabsClient) {
                val builder = CustomTabsIntent.Builder()
                val intent = builder.build()
                client.warmup(0L) // This prevents backgrounding after redirection
                intent.launchUrl(this@HerdrActivity,
                        Uri.parse(authenticationRequest.toURI().toURL().toString())
                )
            }

            override fun onServiceDisconnected(name: ComponentName?) {

            }
        }

        if (!CustomTabsClient.bindCustomTabsService(
                        this,
                        "com.brave.browser",
                        //"com.android.chrome",
                        connection
                )
        ) {
            Snackbar.make(activity_herdr_root, "Brave browser recommended", Snackbar.LENGTH_INDEFINITE)
                    .setAction("Download") {
                        val intent = Intent(Intent.ACTION_VIEW)
                        intent.data = Uri.parse("market://details?id=com.brave.browser")
                        if (intent.resolveActivity(packageManager) != null) {
                            startActivity(intent)
                        }
                    }
                    .show()
        }
    }

    private fun showError(message: String?) {
        activity_herdr_tv.text = message
        //Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    //capturing intent targeting custom URL scheme defined in manifest
    //for OAuth login flow
    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        val action = intent?.action
        val data = intent?.dataString

        if (action == Intent.ACTION_VIEW && data != null) {
            var authResp: AuthenticationResponse? = null

            try {
                authResp = AuthenticationResponseParser.parse(URI(data.removeSuffix("#")))
            } catch (e: ParseException) {
                loginViewModel.setErrorUserCredentials(e)
            } catch (e: URISyntaxException) {
                loginViewModel.setErrorUserCredentials(e)
            }

            when(authResp) {
                is AuthenticationErrorResponse -> {
                    val error = authResp.errorObject
                    loginViewModel.setErrorUserCredentials(IOException(
                            "Error while authenticating: ${error.toJSONObject()}"
                    ))
                }
                is AuthenticationSuccessResponse -> {
                    if (authResp.state != authRequestState) {
                        loginViewModel.setErrorUserCredentials(IOException(
                                "AuthenticationSuccessResponse state validation failed "
                        ))
                    }

                    //pass back to common code
                    loginViewModel.exchangeCodeForAccessAndRefreshToken(
                            authResp.authorizationCode.value)
                }
            }
        }
    }
}