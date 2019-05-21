package com.cryonicsinstitute;

import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

/**
 * For Activities that require a Google API Client
 */
public class BaseGoogleAPIActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = BaseGoogleAPIActivity.class.getSimpleName();

    protected GoogleApiClient googleApiClient;
    // Request code to use when launching the resolution activity
    private static final int REQUEST_RESOLVE_ERROR = 1001;
    // Unique tag for the error dialog fragment
    private static final String DIALOG_ERROR = "dialog_error";
    // Bool to track whether the app is already resolving an error
    private boolean isResolvingGoogleClientError = false;
    protected Location lastKnownLocation = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        buildGoogleApiClient();
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (!isResolvingGoogleClientError) {
            googleApiClient.connect();
        }
    }

    @Override
    protected void onStop() {
        if(googleApiClient != null) {
            googleApiClient.disconnect();
        }
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_RESOLVE_ERROR) {
            isResolvingGoogleClientError = false;
            if (resultCode == RESULT_OK) {
                // Make sure the app is not already connected or attempting to connect
                if (!googleApiClient.isConnecting() &&
                        !googleApiClient.isConnected()) {
                    googleApiClient.connect();
                }
            }
        }
    }

    private synchronized void buildGoogleApiClient() {
        googleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    @SuppressWarnings("unused")
    /* Creates a dialog for an error message */
    protected void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getSupportFragmentManager(), "errordialog");
    }

    /* Called from ErrorDialogFragment when the dialog is dismissed. */
    public void onDialogDismissed() {
        isResolvingGoogleClientError = false;
    }

    /* A fragment to display an error dialog */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = this.getArguments().getInt(DIALOG_ERROR);
            return GooglePlayServicesUtil.getErrorDialog(errorCode,
                    this.getActivity(), REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((BaseGoogleAPIActivity)getActivity()).onDialogDismissed();
        }
    }

    // GoogleApiClient listener methods

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Google API Client connected");
        lastKnownLocation
                = LocationServices.FusedLocationApi.getLastLocation(googleApiClient);
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.w(TAG, String.format("Google API Client connection suspended %d", i));
    }

    @Override
    public void onConnectionFailed(ConnectionResult result) {
        Log.e(TAG, String.format("Google API Client connection failed %d", result.getErrorCode()));
        // we don't need to interrupt the user if it failed
//        if (isResolvingGoogleClientError) {
//            // Already attempting to resolve an error.
//        } else if (result.hasResolution()) {
//            try {
//                isResolvingGoogleClientError = true;
//                result.startResolutionForResult(this, REQUEST_RESOLVE_ERROR);
//            } catch (IntentSender.SendIntentException e) {
//                // There was an error with the resolution intent. Try again.
//                googleApiClient.connect();
//            }
//        } else {
//            // Show dialog using GooglePlayServicesUtil.getErrorDialog()
//            showErrorDialog(result.getErrorCode());
//            isResolvingGoogleClientError = true;
//        }
    }

    @SuppressWarnings("unused")
    public GoogleApiClient getGoogleApiClient() {
        return googleApiClient;
    }

    @SuppressWarnings("unused")
    public boolean hasGoogleApiClientConnected() {
        return googleApiClient != null && googleApiClient.isConnected();
    }
}

