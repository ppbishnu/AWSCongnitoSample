package com.example.bishnu.datasyncapp;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.amazonaws.mobileconnectors.cognito.CognitoSyncManager;
import com.amazonaws.mobileconnectors.cognito.Dataset;
import com.amazonaws.mobileconnectors.cognito.Record;
import com.amazonaws.mobileconnectors.cognito.SyncConflict;
import com.amazonaws.mobileconnectors.cognito.exceptions.DataStorageException;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private static final int REQUEST_CODE_PERMISSION = 25;
    private static final String DATABASE_NAME = "memodatabase";
    private String[] permissionRequiredList = new String[]{Manifest.permission.ACCESS_NETWORK_STATE, Manifest.permission.INTERNET};
    private CognitoSyncManager client;
    private Dataset dataset;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //request permission for internet and access netwrok state
        if (!requestPermissions()) {
            // Initialize the Amazon Cognito credentials provider
            initCredentialProvider();
        }
    }

    private void initCredentialProvider() {
        if (client == null) {
            client = CognitoSyncApdater.getCognitoSyncManagerClient(getApplicationContext());
        }
        dataset = openOrCreateDatabase();
        addData();
    }

    private void addData() {
        if (dataset != null) {
            dataset.put("lunch", "01:30");
            syncData(dataset);
        }
    }

    private Dataset openOrCreateDatabase() {
        if (client != null) {
            Dataset dataset = client.openOrCreateDataset(DATABASE_NAME);
            return dataset;
        } else {
            return null;
        }
    }

    private void syncData(Dataset dataset) {
        dataset.synchronize(new Dataset.SyncCallback() {
            @Override
            public void onSuccess(Dataset dataset, List<Record> updatedRecords) {
                Log.d("TAG", "onSuccess: " + dataset.get("lunch").toString());
            }

            @Override
            public boolean onConflict(Dataset dataset, List<SyncConflict> conflicts) {
                List<Record> resolvedRecords = new ArrayList<Record>();
                for (SyncConflict conflict : conflicts) {
                    /* resolved by taking remote records */
                    resolvedRecords.add(conflict.resolveWithRemoteRecord());

                    /* alternately take the local records */
                    // resolvedRecords.add(conflict.resolveWithLocalRecord());

                    /* or customer logic, say concatenate strings */
                    // String newValue = conflict.getRemoteRecord().getValue()
                    //     + conflict.getLocalRecord().getValue();
                    // resolvedRecords.add(conflict.resolveWithValue(newValue);
                }
                dataset.resolve(resolvedRecords);

                // return true so that synchronize() is retried after conflicts are resolved
                return true;
            }

            @Override
            public boolean onDatasetDeleted(Dataset dataset, String datasetName) {
                return false;
                // return true to delete the local copy of the dataset
                //return true;
            }

            @Override
            public boolean onDatasetsMerged(Dataset dataset, List<String> datasetNames) {
                // return false to handle Dataset merge outside the synchronization callback
                return false;
            }

            @Override
            public void onFailure(DataStorageException dse) {
                Log.d("TAG", "onSuccess: " + dse.toString());
            }
        });
    }

    private boolean requestPermissions() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(),
                permissionRequiredList[0]) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(getApplicationContext(),
                permissionRequiredList[1]) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(permissionRequiredList, REQUEST_CODE_PERMISSION);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        boolean allGranted = true;
        if (requestCode == REQUEST_CODE_PERMISSION) {
            for (int i = 0; i < grantResults.length; i++) {
                int result = grantResults[i];
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allGranted = false;
                    showToastIfPermissionIsNotGranted(permissionRequiredList[i]);
                }
            }
            if (allGranted) {
                initCredentialProvider();
            } else {
                this.finish();
            }
        }
    }

    private void showToastIfPermissionIsNotGranted(String requestedPermission) {
        Toast.makeText(this,  new StringBuilder("Please accept the " + requestedPermission + "permission or application will be closed" ).toString()
                , Toast.LENGTH_SHORT).show();
        }
    }

