package com.example.cv0318.instagramclone.Dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.example.cv0318.instagramclone.R;

public class ConfirmPasswordDialog extends android.support.v4.app.DialogFragment
{
    private static final String TAG = String.format("%s_TAG",
        ConfirmPasswordDialog.class.getSimpleName());

    public interface OnConfirmPasswordListener
    {
        public void onConfirmPassword(String password);
    }
    OnConfirmPasswordListener m_onConfirmPasswordListener;

    //vars
    private TextView m_password;
    
    @Nullable
    @Override
    public View onCreateView(
        LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.dialog_confirm_password, container, false);
        m_password = view.findViewById(R.id.confirm_password);
        Log.d(TAG, "onCreateView: started");

        TextView cancelDialog = view.findViewById(R.id.dialogCancel);
        cancelDialog.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "onClick: closing the dialog");
                getDialog().dismiss();
            }
        });

        TextView confirmDialog = view.findViewById(R.id.dialogConfirm);
        confirmDialog.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Log.d(TAG, "onClick: confirming captured password");
                String password = m_password.getText().toString();
                if (!password.isEmpty())
                {
                    m_onConfirmPasswordListener.onConfirmPassword(password);
                    getDialog().dismiss();
                }
                else 
                {
                    Toast.makeText(getActivity(), "You must enter a password.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        return view;
    }

    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);

        try
        {
            m_onConfirmPasswordListener = (OnConfirmPasswordListener) getTargetFragment();
        }
        catch (ClassCastException e)
        {
            Log.e(TAG, "onAttach: ClassCastException", e);
        }
    }
}
