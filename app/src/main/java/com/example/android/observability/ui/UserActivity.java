package com.example.android.observability.ui;


import android.arch.lifecycle.ViewModelProviders;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.example.android.observability.Injection;
import com.example.android.observability.persistence.UserFan;
import com.example.android.persistence.R;

import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.schedulers.Schedulers;

public class UserActivity extends AppCompatActivity {

    private static final String TAG = UserActivity.class.getSimpleName();

    private TextView mUserNameRx;
    private TextView mUserNameLiveData;
    private TextView mLiveDataResult;
    private TextView mLiveDataResultNetwork;

    private EditText mUserNameInput;

    private Button mUpdateButtonRx;
    private Button mUpdateButtonLiveData;
    private Button mUpdateLiveDataResult;
    private Button mUpdateLiveDataResultNetwork;

    private ViewModelFactory mViewModelFactory;

    private UserViewModel mViewModel;

    private final CompositeDisposable mDisposable = new CompositeDisposable();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        mUserNameRx = findViewById(R.id.user_name_rx);
        mUserNameLiveData = findViewById(R.id.user_name_livedata);
        mLiveDataResult = findViewById(R.id.livedataresult);
        mLiveDataResultNetwork = findViewById(R.id.livedataresultnetwork);

        mUserNameInput = findViewById(R.id.user_name_input);


        mUpdateButtonRx = findViewById(R.id.update_userrx);
        mUpdateButtonLiveData = findViewById(R.id.update_userlivedata);
        mUpdateLiveDataResult = findViewById(R.id.update_livedataresult);
        mUpdateLiveDataResultNetwork = findViewById(R.id.update_livedataresult_network);

        mViewModelFactory = Injection.provideViewModelFactory(this);
        mViewModel = ViewModelProviders.of(this, mViewModelFactory).get(UserViewModel.class);

        mUpdateButtonLiveData.setOnClickListener(v -> updateUserNameLiveData());
        mUpdateButtonRx.setOnClickListener(v -> updateUserName());
        mUpdateLiveDataResult.setOnClickListener(v -> launchRxOperation());
        mUpdateLiveDataResultNetwork.setOnClickListener(v -> launchRxOperationNetwork());

    }

    private void launchRxOperation() {
/*
        When the LiveData becomes inactive, the subscription is cleared. LiveData holds the last value emitted by the Publisher when the LiveData was active.
        Therefore, in the case of a hot RxJava Observable, when a new LiveData Observer is added, it will automatically notify with the last value held in LiveData, which might not be the last value emitted by the Publisher.
*/
        mViewModel.LaunchRxOperation();
        mViewModel.getRxOpResult().observe(this, result -> {
            mLiveDataResult.setText("Rx to Livedata: " + result);
        });

    }

    private void launchRxOperationNetwork() {
/*
        When the LiveData becomes inactive, the subscription is cleared. LiveData holds the last value emitted by the Publisher when the LiveData was active.
        Therefore, in the case of a hot RxJava Observable, when a new LiveData Observer is added, it will automatically notify with the last value held in LiveData, which might not be the last value emitted by the Publisher.
*/
        mLiveDataResultNetwork.setText("Loading 2 network stream and rx zipping them");
        mViewModel.LaunchRxNetworkOperation();
        mViewModel.getRxZipNetwork().observe(this, result -> {
            String dump = "";
            for (int i = 0; i < result.size(); i++) {
                dump = dump + ((UserFan) result.get(i)).firstname + " " + ((UserFan) result.get(i)).lastname + "\n";
            }
            mLiveDataResultNetwork.setText("Rx to Livedata Network\nArray Size:" + result.size() + "\nDUMP\n" + dump);


        });

    }


    @Override
    protected void onStart() {
        super.onStart();
        // Subscribe to the emissions of the user name from the view model.
        // Update the user name text view, at every onNext emission.
        // In case of error, log the exception.
        mDisposable.add(mViewModel.getUserName()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(userName -> mUserNameRx.setText("Rx: " + userName),
                        throwable -> Log.e(TAG, "Unable to update username", throwable)));


        mViewModel.getUserFromLiveData().observe(this, userName -> {
            if (userName != null) {
                mUserNameLiveData.setText("LiveData: " + userName);
            }

        });


    }

    @Override
    protected void onStop() {
        super.onStop();

        // clear all the subscriptions
        mDisposable.clear();
    }

    private void updateUserName() {
        String userName = mUserNameInput.getText().toString();
        // Disable the update button until the user name update has been done
        mUpdateButtonRx.setEnabled(false);
        // Subscribe to updating the user name.
        // Re-enable the button once the user name has been updated
        mDisposable.add(mViewModel.updateUserNameRx(userName)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(() -> mUpdateButtonRx.setEnabled(true),
                        throwable -> Log.e(TAG, "Unable to update username", throwable)));


    }


    private void updateUserNameLiveData() {
        String userName = mUserNameInput.getText().toString();
        mViewModel.updateUserNameLiveData(userName);

    }


}
