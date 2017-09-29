package com.mobcomlab.firebots.Helpers;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;

import com.bigkoo.svprogresshud.SVProgressHUD;
import com.google.firebase.database.DatabaseError;
import com.mobcomlab.firebots.R;

public class DialogHelper {

    @SuppressLint("StaticFieldLeak")
    private static SVProgressHUD svProgressHUD;

    public static void showOkAlert(Context context, String title, String message, DialogInterface.OnClickListener onClickListener) {
        AlertDialog alertDialog = new AlertDialog.Builder(context).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, context.getResources().getString(R.string.ok), onClickListener != null ? onClickListener : new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        alertDialog.show();
    }

    public static AlertDialog showAlert(final Context context, String title, String message, String buttonText, DialogInterface.OnClickListener onClickListener, Boolean cancelOutside) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context, R.style.AppTheme_Dialog).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);
        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, buttonText, onClickListener != null ? onClickListener : new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                alertDialog.dismiss();
            }
        });
        alertDialog.setCanceledOnTouchOutside(cancelOutside);
        alertDialog.setOnShowListener(new DialogInterface.OnShowListener() {
            @Override
            public void onShow(DialogInterface arg0) {
                alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(context, R.color.secondary));
            }
        });
        alertDialog.show();
        return alertDialog;
    }

    public static void showYesNoAlert(final Context context, String title, String message, String positiveButtonText, String negativeButtonText, DialogInterface.OnClickListener onPositiveClickListener, DialogInterface.OnClickListener onNegativeClickListener) {
        final AlertDialog alertDialog = new AlertDialog.Builder(context, R.style.AppTheme_Dialog).create();
        alertDialog.setTitle(title);
        alertDialog.setMessage(message);

        alertDialog.setButton(AlertDialog.BUTTON_POSITIVE, positiveButtonText != null ? positiveButtonText : context.getResources().getString(R.string.yes), onPositiveClickListener != null ? onPositiveClickListener : new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.setButton(AlertDialog.BUTTON_NEGATIVE, negativeButtonText != null ? negativeButtonText : context.getResources().getString(R.string.no), onNegativeClickListener != null ? onNegativeClickListener : new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        alertDialog.setCanceledOnTouchOutside(false);
        alertDialog.show();
    }

    // DELETE
    public static void showErrorAlert(Context context, DatabaseError error) {
        showOkAlert(context, error.getCode() + " " + error.getMessage(), error.getDetails(), null);
    }

//    public static void showErrorAlert(Context context, Exception error, DialogInterface.OnClickListener onClickListener) {
//        if (onClickListener != null) {
//            showOkAlert(context, error.getCause().toString(), error.getLocalizedMessage(), onClickListener);
//        } else {
//            showOkAlert(context, error.getCause().toString(), error.getLocalizedMessage(), null);
//        }
//    }

//    public static void showTimeoutAlert(Context context) {
//        showOkAlert(context, context.getResources().getString(R.string.timeout), context.getResources().getString(R.string.unable_to_complete_the_request), null);
//    }

    public static void showIndicator(Context context, String message){
        svProgressHUD = new SVProgressHUD(context);
        svProgressHUD.showWithStatus(message, SVProgressHUD.SVProgressHUDMaskType.Black);
    }

    public static void dismissIndicator(){
        if (svProgressHUD != null) {
            svProgressHUD.dismiss();
        }
    }
}
