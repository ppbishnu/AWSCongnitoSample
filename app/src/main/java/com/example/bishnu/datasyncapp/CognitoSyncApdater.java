package com.example.bishnu.datasyncapp;

import android.content.Context;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.regions.Regions;

/**
 * Created by Bishnu.Reddy on 9/15/2017.
 */

public class CognitoSyncApdater {
    private static final String IDENTITY_POOL_ID = "ap-south-1:74e91870-520c-4db1-9f3d-92ee7310a9f0";
    private static CognitoSyncManager cognitoSyncManager;
    private static CognitoSyncApdater cognitoSyncApdater;

    private CognitoSyncApdater(Context context) {
        cognitoSyncManager = new CognitoSyncManager(
                context,
                Regions.AP_SOUTH_1,
                getCredentialProvider(context));
    }

    public static CognitoSyncManager getCognitoSyncManagerClient(Context context) {
        if (cognitoSyncApdater == null) {
            cognitoSyncApdater = new CognitoSyncApdater(context);
            return cognitoSyncManager;
        } else {
            return cognitoSyncManager;
        }
    }

    public CognitoCachingCredentialsProvider getCredentialProvider(Context context) {
        CognitoCachingCredentialsProvider credentialsProvider = new CognitoCachingCredentialsProvider(
                context,
                IDENTITY_POOL_ID // Identity pool ID
                , Regions.AP_SOUTH_1 // Region
        );
        return credentialsProvider;
    }
}
