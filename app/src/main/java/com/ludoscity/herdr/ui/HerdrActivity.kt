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

package com.ludoscity.herdr.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import com.ludoscity.herdr.R
import com.ludoscity.herdr.common.base.Response
import com.ludoscity.herdr.common.data.SecureDataStore
import com.ludoscity.herdr.common.domain.entity.AuthClientRegistration
import com.ludoscity.herdr.common.domain.entity.UserCredentials
import com.ludoscity.herdr.common.ui.login.*
import kotlinx.android.synthetic.main.activity_herdr.*
import net.openid.appauth.*
import sample.hello

class HerdrActivity : AppCompatActivity() {

    private lateinit var loginViewModel: LoginViewModel


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
        loginViewModel = ViewModelProviders.of(
            this,
            HerdrActivityModelFactory(SecureDataStore())
        ).get(LoginViewModel::class.java)

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

    private fun launchAuthorizationFlow(registrationInfo: AuthClientRegistration) {

        val authorizationServiceConfig = AuthorizationServiceConfiguration(
                Uri.parse("${registrationInfo.stackBaseUrl}/auth/authorize"),
                Uri.parse("${registrationInfo.stackBaseUrl}/auth/access_token")
        )

        val authRequestBuilder = AuthorizationRequest.Builder(
                authorizationServiceConfig,
                registrationInfo.clientId,
                ResponseTypeValues.CODE,
                Uri.parse(registrationInfo.redirectUriCollection[0])
        ).setScope("openid io.cozy.files io.cozy.oauth.clients")

        val authService = AuthorizationService(this)
        val authIntent = authService.getAuthorizationRequestIntent(authRequestBuilder.build())

        startActivityForResult(authIntent, RC_AUTH)
    }

    private fun showError(message: String?) {
        activity_herdr_tv.text = message
        //Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_AUTH) {
            data?.let {
                val resp = AuthorizationResponse.fromIntent(it)
                val ex = AuthorizationException.fromIntent(it)

                if (resp != null) {
                    loginViewModel.exchangeCodeForAccessAndRefreshToken(resp.authorizationCode!!)
                } else {
                    ex?.let { authException ->
                        loginViewModel.setErrorUserCredentials(authException.cause!!)
                    }
                }
            }
        }
    }

    companion object {
        private const val RC_AUTH: Int = 1
    }
}