package com.example.bishnu.datasyncapp;

import android.content.Context;

import com.amazonaws.auth.CognitoCachingCredentialsProvider;
import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.regions.Regions;

/**
 * Created by Bishnu.Reddy on 9/15/2017.
 */

public class CognitoSyncApdater {
    private static final String IDENTITY_POOL_ID = "us-east-1:058657d2-fb5f-4384-90dc-8a425f4e0257";
    private static CognitoSyncManager cognitoSyncManager;
    private static CognitoSyncApdater cognitoSyncApdater;

    private CognitoSyncApdater(Context context) {
        cognitoSyncManager = new CognitoSyncManager(
                context,
                Regions.US_EAST_1,
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
                , Regions.US_EAST_1 // Region
        );
        return credentialsProvider;
    }
}
