package com.zackhagan.dotmatrix_a;
import org.puredata.core.PdBase;

import select.files.SelectLibrary;



import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.annotation.TargetApi;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;

@TargetApi(Build.VERSION_CODES.HONEYCOMB)
class CopyFragment extends DialogFragment{
	
	
	
    public CopyFragment() {
        getActivity();
    }
    public CopyFragment(Dm_beatbox Dm_beatbox) {
		// TODO Auto-generated constructor stub
	}
	@Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(getActivity());
        alertDialogBuilder.setTitle("Confirm Copy Pattern?");
        alertDialogBuilder.setMessage("Are you sure?");
        //null should be your on click listener
        alertDialogBuilder.setPositiveButton("OK", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
            	
            
            	Dm_beatbox.copyPattern();
                dialog.dismiss();
            	
            	
            }
        });
        alertDialogBuilder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

            @Override
            public void onClick(DialogInterface dialog, int which) {
            	
            
            	//Dm_beatbox.copyPattern();
                dialog.dismiss();
            	
            	
            }
        });


        return alertDialogBuilder.create();
    }

}
