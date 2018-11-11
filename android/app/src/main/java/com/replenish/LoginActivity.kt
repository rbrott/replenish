package com.replenish

import android.content.Intent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import com.fitbit.authentication.AuthenticationHandler
import com.fitbit.authentication.AuthenticationManager
import com.fitbit.authentication.AuthenticationResult

class LoginActivity : AppCompatActivity(), AuthenticationHandler {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        AuthenticationManager.login(this)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data);
        AuthenticationManager.onActivityResult(requestCode, resultCode, data, this)
    }

    override fun onAuthFinished(authenticationResult: AuthenticationResult) {
        if (authenticationResult.isSuccessful) {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("accessToken", authenticationResult.accessToken.accessToken)
            startActivity(intent)
        } else {
            AuthenticationManager.logout(this)
        }
        finish()
    }
}
