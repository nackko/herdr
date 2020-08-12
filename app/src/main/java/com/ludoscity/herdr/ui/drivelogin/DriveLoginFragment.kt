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

package com.ludoscity.herdr.ui.drivelogin

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.ludoscity.herdr.R
import com.ludoscity.herdr.common.base.Response
import com.ludoscity.herdr.common.domain.entity.AuthClientRegistration
import com.ludoscity.herdr.common.domain.entity.UserCredentials
import com.ludoscity.herdr.common.ui.drivelogin.*
import com.ludoscity.herdr.databinding.FragmentDriveLoginBinding
import com.ludoscity.herdr.ui.CustomTabsNavigator
import com.ludoscity.herdr.utils.afterTextChanged
import dev.icerock.moko.mvvm.MvvmEventsFragment
import dev.icerock.moko.mvvm.createViewModelFactory
import dev.icerock.moko.mvvm.dispatcher.eventsDispatcherOnMain
import net.openid.appauth.*
import java.io.IOException

class DriveLoginFragment : MvvmEventsFragment<FragmentDriveLoginBinding, DriveLoginViewModel,
        DriveLoginViewModel.DriveLoginFragmentEventListener>(), DriveLoginViewModel.DriveLoginFragmentEventListener {
    override val layoutId: Int = R.layout.fragment_drive_login
    override val viewModelVariableId: Int = com.ludoscity.herdr.BR.driveLoginViewModel
    override val viewModelClass: Class<DriveLoginViewModel> = DriveLoginViewModel::class.java

    override fun viewModelFactory(): ViewModelProvider.Factory {
        return createViewModelFactory {
            DriveLoginViewModel(
                eventsDispatcher = eventsDispatcherOnMain()
            )
        }
    }

    override fun routeToCreateAccount() {
        val cozyCreateAccountBundle = Bundle()

        cozyCreateAccountBundle.putString(
            CustomTabsNavigator.URL_BUNDLE_KEY,
            "https://manager.cozycloud.cc/cozy/create?lang=en")

        this.findNavController().navigate(
            R.id.action_driveLoginFragment_to_create_account,
            cozyCreateAccountBundle
        )
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        super.onCreateView(inflater, container, savedInstanceState)

        //register observer
        viewModel.authClientRegistrationResult.addObserver { getClientRegistrationState(it) }

        viewModel.userCredentialsResult.addObserver { getUserCredentialsState(it) }

        viewModel.requestAuthFlowEvent.addObserver {

            if (it) {
                val authInfo = ((viewModel.authClientRegistrationResult.value as SuccessAuthClientRegistration)
                        .response as Response.Success)
                        .data

                launchAuthorizationFlow(authInfo)
                viewModel.authFlowRequestProcessed()
            }
        }

        binding.usernameOrCustomDomain.apply {
            //define what happens when user press <enter< opn virtual keyboard
            setOnEditorActionListener { _, actionId, _ ->
                when (actionId) {
                    EditorInfo.IME_ACTION_DONE ->
                        viewModel.registerAuthClient()
                }
                false
            }

            //define what happens when TextView content is edited
            afterTextChanged {
                viewModel.urlChanged(text.toString())
            }


        }
        return binding.root
    }

    private fun getUserCredentialsState(state: UserCredentialsState) {
        when (state) {
            is SuccessUserCredentials -> {
                val response = state.response as Response.Success
                binding.activityHerdrButtonLogout.visibility = View.VISIBLE
                binding.connectProgressBar.visibility = View.INVISIBLE
                onUserCredentialsSuccess(userCredentials = response.data)
            }
            is InProgressUserCredentials -> {
                binding.connectProgressBar.visibility = View.VISIBLE
                binding.activityHerdrCredentialsTv.text = "In progress..."
            }
            is ErrorUserCredentials -> {
                binding.connectProgressBar.visibility = View.INVISIBLE
                binding.driveConnectButton.visibility = View.VISIBLE
                val response = state.response as Response.Error
                showError(
                        "message: ${response.message}|e.message:${response.exception.message ?: ""}",
                        binding.activityHerdrCredentialsTv
                )
            }
        }
    }

    private fun getClientRegistrationState(state: AuthClientRegistrationState) {
        when (state) {
            is SuccessAuthClientRegistration -> {
                binding.driveConnectButton.visibility = View.INVISIBLE
                binding.connectProgressBar.visibility = View.VISIBLE
                val response = state.response as Response.Success
                onClientRegistrationSuccess(registrationInfo = response.data)
            }
            is InProgressAuthClientRegistration -> {
                binding.activityHerdrRegistrationTv.text = "In progress..."
                binding.driveConnectButton.visibility = View.INVISIBLE
                binding.connectProgressBar.visibility = View.VISIBLE
                binding.activityHerdrButtonLogout.visibility = View.INVISIBLE
            }
            is ErrorAuthClientRegistration -> {
                binding.driveConnectButton.visibility = View.VISIBLE
                binding.connectProgressBar.visibility = View.INVISIBLE
                binding.activityHerdrButtonLogout.visibility = View.INVISIBLE
                val response = state.response as Response.Error
                showError(
                        "message: ${response.message}|e.message:${response.exception.message ?: ""}",
                        binding.activityHerdrCredentialsTv
                )
            }
        }
    }

    private fun onClientRegistrationSuccess(registrationInfo: AuthClientRegistration) {

        //debug
        binding.activityHerdrCredentialsTv.text = "Registration = $registrationInfo"
    }

    private fun onUserCredentialsSuccess(userCredentials: UserCredentials) {

        //debug -- We are fully logged in
        binding.activityHerdrCredentialsTv.text = "Credentials = $userCredentials"
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

        val authService = AuthorizationService(requireActivity())
        val authIntent = authService.getAuthorizationRequestIntent(authRequestBuilder.build())

        startActivityForResult(authIntent, RC_AUTH)
    }

    private fun showError(message: String?, tv: TextView) {
        tv.text = message
        //Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_AUTH) {
            data?.let {
                val resp = AuthorizationResponse.fromIntent(it)
                val ex = AuthorizationException.fromIntent(it)

                if (resp != null) {
                    viewModel.exchangeCodeForAccessAndRefreshToken(resp.authorizationCode!!)
                } else {
                    ex?.let { authException ->
                        viewModel.setErrorUserCredentials(authException.cause
                                ?: IOException("Login error"))
                        viewModel.unregisterAuthClient()
                    }
                }
            }
        }
    }

    companion object {
        private const val RC_AUTH: Int = 1
    }
}
