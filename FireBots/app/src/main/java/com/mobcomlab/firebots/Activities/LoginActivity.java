package com.mobcomlab.firebots.Activities;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.mobcomlab.firebots.Firebase.FBUser;
import com.mobcomlab.firebots.Helpers.DialogHelper;
import com.mobcomlab.firebots.R;
import com.mobcomlab.firebots.Views.ButtonMedium;
import com.mobcomlab.firebots.Views.EditTextNormal;

public class LoginActivity extends MainActivity implements View.OnClickListener,
        TextWatcher {

    // Properties
    // Properties normal
    private EditTextNormal usernameEditText;
    private ButtonMedium loginButton;

    public LoginActivity() {
        super(R.layout.activity_login);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        usernameEditText = (EditTextNormal) findViewById(R.id.username_edit_text);
        loginButton = (ButtonMedium) findViewById(R.id.login_button);

        loginButton.setOnClickListener(this);

        usernameEditText.addTextChangedListener(this);
    }

    //region TextWatcher
    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

    }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) {

    }

    @Override
    public void afterTextChanged(Editable s) {
        canLogin();
    }
    //endregion

    private void canLogin() {
        loginButton.setEnabled(!TextUtils.isEmpty(usernameEditText.getText()));
    }

    //region OnClick
    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.login_button:
                hideKeyboard();
                DialogHelper.showIndicator(this, getResources().getString(R.string.login_progress));
                final String username = usernameEditText.getText().toString();

                FirebaseAuth.getInstance().signInAnonymously().addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FBUser.setupUser(task.getResult().getUser().getUid(), username);
                            swapToChatroomActivity();
                            DialogHelper.dismissIndicator();
                            finish();
                        } else {
                            String title = getResources().getString(R.string.login_fail);
                            try {
                                throw task.getException();
                            } catch (FirebaseNetworkException e) {
                                title = getResources().getString(R.string.network_error);
                            }catch (Exception e) {
                                e.printStackTrace();
                            }
                            DialogHelper.dismissIndicator();
                            DialogHelper.showOkAlert(LoginActivity.this, title, title, null);
                        }
                    }
                });
                break;
        }
    }
    //endregion

    private void hideKeyboard() {
        InputMethodManager imm = (InputMethodManager) this.getSystemService(Activity.INPUT_METHOD_SERVICE);
        // check if no view has focus:
        View view = this.getCurrentFocus();
        if (view == null)
            return;
        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
    }
}
