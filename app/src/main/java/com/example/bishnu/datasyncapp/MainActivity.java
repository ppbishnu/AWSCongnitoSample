package com.example.bishnu.datasyncapp;

import android.Manifest;
import android.animation.ValueAnimator;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
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
    private EditText eventDescriptionTV;
    private EditText timingTV;
    private Button syncDataBtn;
    private RecyclerView eventListRV;
    private final List<Record> eventRecordList = new ArrayList<>();
    private EvenListAdapter evenListAdapter;
    private TextView separator;
    private LinearLayout addEventViewLL;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //request permission for internet and access netwrok state
        if (!requestPermissions()) {
            // Initialize the Amazon Cognito credentials provider
            initCredentialProvider();
        }
        initView();
        initListner();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.add_event_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.event_icon: {
                boolean isExpanded =  addEventViewLL.getLayoutParams().height == 0;
                item.setIcon(getDrawable(isExpanded?R.drawable.cross_icon:R.drawable.plus_icon));
                expandAndCollappsEventCardview(isExpanded);
                return true;
            }
            default: {
                return super.onOptionsItemSelected(item);
            }
        }
    }

    private void expandAndCollappsEventCardview(boolean isExpandView) {
        ValueAnimator valueAnimator = ValueAnimator.ofInt(addEventViewLL.getMeasuredHeight(), dpToPx(isExpandView?90:0));
        valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator valueAnimator) {
                ViewGroup.LayoutParams layoutParams = addEventViewLL.getLayoutParams();
                layoutParams.height = (Integer) valueAnimator.getAnimatedValue();
                addEventViewLL.setLayoutParams(layoutParams);
            }
        });
        valueAnimator.setDuration(300);
        valueAnimator.start();
    }

    private int dpToPx(int dp) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, getResources().getDisplayMetrics());
    }

    private void initListner() {
        syncDataBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (timingTV != null && eventDescriptionTV != null) {
                    addData(eventDescriptionTV.getText().toString(), timingTV.getText().toString());
                    eventRecordList.clear();
                    eventRecordList.addAll(dataset.getAllRecords());
                    evenListAdapter.notifyDataSetChanged();
                    eventDescriptionTV.setText("");
                    timingTV.setText("");
                    timingTV.setVisibility(View.GONE);
                    separator.setVisibility(View.GONE);
                    syncDataBtn.setVisibility(View.GONE);
                }
            }
        });
        eventDescriptionTV.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    timingTV.setVisibility(View.VISIBLE);
                    separator.setVisibility(View.VISIBLE);
                } else {
                    timingTV.setVisibility(View.GONE);
                    separator.setVisibility(View.GONE);
                    syncDataBtn.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
        timingTV.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (charSequence.length() > 0) {
                    syncDataBtn.setVisibility(View.VISIBLE);
                } else {
                    syncDataBtn.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });

    }

    private void initView() {
        eventDescriptionTV = (EditText) findViewById(R.id.event_description_textview);
        timingTV = (EditText) findViewById(R.id.timing_textview);
        syncDataBtn = (Button) findViewById(R.id.syncData_button);
        separator = (TextView) findViewById(R.id.separator_textview);
        eventListRV = (RecyclerView) findViewById(R.id.event_list_recycle_view);
        eventRecordList.clear();
        addEventViewLL = (LinearLayout) findViewById(R.id.card_view_coverLL);
        eventRecordList.addAll(dataset.getAllRecords());
        eventListRV.setLayoutManager(new GridLayoutManager(getApplicationContext(), 3));
        evenListAdapter = new EvenListAdapter(eventRecordList, getApplicationContext());
        eventListRV.setAdapter(evenListAdapter);
    }

    private void initCredentialProvider() {
        if (client == null) {
            client = CognitoSyncApdater.getCognitoSyncManagerClient(getApplicationContext());
        }
        dataset = openOrCreateDatabase();
    }

    private void addData(String value, String key) {
        if (dataset != null) {
            dataset.put(value, key);
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

    private void syncData(final Dataset dataset) {
        dataset.synchronize(new Dataset.SyncCallback() {
            @Override
            public void onSuccess(Dataset data, List<Record> updatedRecords) {
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
        Toast.makeText(this, new StringBuilder("Please accept the " + requestedPermission + "permission or application will be closed").toString()
                , Toast.LENGTH_SHORT).show();
    }
}

