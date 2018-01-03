/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.android.observability.ui;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.LiveDataReactiveStreams;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;

import com.example.android.observability.UserDataSource;
import com.example.android.observability.persistence.RefreshResults;
import com.example.android.observability.persistence.User;
import com.example.android.observability.persistence.UserFan;
import com.rx2androidnetworking.Rx2AndroidNetworking;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Completable;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.BiFunction;
import io.reactivex.internal.operators.completable.CompletableFromAction;
import io.reactivex.schedulers.Schedulers;

/**
 * View Model for the {@link UserActivity}
 */
public class UserViewModel extends ViewModel {

    private final UserDataSource mDataSource;

    private User mUser;

    private final LiveData<String> userListLiveData;
    private LiveData<String> rxOpResult;
    private LiveData<List<UserFan>> rxZipNetwork;


    Flowable<String> colors = Flowable.just("red", "green", "blue");
    Flowable<Long> timer = Flowable.interval(2, TimeUnit.SECONDS);


    public UserViewModel(UserDataSource dataSource) {

        mDataSource = dataSource;

        userListLiveData = LiveDataReactiveStreams.fromPublisher(
                mDataSource.getUser()
                        .map(user -> {
                            mUser = user;
                            return user.getUserName();
                        }));


        rxOpResult = new MutableLiveData<>();
        rxZipNetwork = new MutableLiveData<>();


    }


    LiveData<String> getUserFromLiveData() {
        return userListLiveData;
    }

    LiveData<String> getRxOpResult() {

        return rxOpResult;
    }

    LiveData<List<UserFan>> getRxZipNetwork() {
        return rxZipNetwork;
    }

    public void LaunchRxOperation() {
        rxOpResult = LiveDataReactiveStreams.fromPublisher(
                Flowable.zip(colors, timer, (key, val) -> key));
    }

    public void LaunchRxNetworkOperation() {
        rxZipNetwork = LiveDataReactiveStreams.fromPublisher(findUsersWhoLovesBoth());

    }

    /**
     * Get the user name of the user.
     *
     * @return a {@link Flowable} that will emit every time the user name has been updated.
     */
    public Flowable<String> getUserName() {
        return mDataSource.getUser()
                // for every emission of the user, get the user name
                .map(user -> {
                    mUser = user;
                    return user.getUserName();
                });

    }


    /**
     * Update the user name.
     *
     * @param userName the new user name
     * @return a {@link Completable} that completes when the user name is updated
     */
    public Completable updateUserNameRx(final String userName) {
        return new CompletableFromAction(() -> {
            // if there's no use, create a new user.
            // if we already have a user, then, since the user object is immutable,
            // create a new user, with the id of the previous user and the updated user name.
            mUser = mUser == null
                    ? new User(userName)
                    : new User(mUser.getId(), userName);

            mDataSource.insertOrUpdateUser(mUser);
        });
    }


    public void updateUserNameLiveData(final String userName) {

        new RefreshResults() {
            @Override
            protected void refreshResults() {

                mUser = mUser == null
                        ? new User(userName)
                        : new User(mUser.getId(), userName);

                mDataSource.insertOrUpdateUser(mUser);

            }
        };


    }


    private Observable<List<UserFan>> getCricketFansObservable() {
        return Rx2AndroidNetworking.get("https://fierce-cove-29863.herokuapp.com/getAllCricketFans")
                .build()
                .getObjectListObservable(UserFan.class);
    }

    /*
    * This observable return the list of User who loves Football
    */
    private Observable<List<UserFan>> getFootballFansObservable() {
        return Rx2AndroidNetworking.get("https://fierce-cove-29863.herokuapp.com/getAllFootballFans")
                .build()
                .getObjectListObservable(UserFan.class);
    }


    private Flowable findUsersWhoLovesBoth() {
        // here we are using zip operator to combine both request
        return Observable.zip(getCricketFansObservable(), getFootballFansObservable(),
                new BiFunction<List<UserFan>, List<UserFan>, List<UserFan>>() {
                    @Override
                    public List<UserFan> apply(List<UserFan> cricketFans, List<UserFan> footballFans) throws Exception {
                        List<UserFan> userWhoLovesBoth =
                                filterUserWhoLovesBoth(cricketFans, footballFans);
                        return userWhoLovesBoth;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .toFlowable(BackpressureStrategy.BUFFER);

    }


    private List<UserFan> filterUserWhoLovesBoth(List<UserFan> cricketFans, List<UserFan> footballFans) {
        List<UserFan> userWhoLovesBoth = new ArrayList<>();
        for (UserFan cricketFan : cricketFans) {
            for (UserFan footballFan : footballFans) {
                if (cricketFan.id == footballFan.id) {
                    userWhoLovesBoth.add(cricketFan);
                }
            }
        }
        return userWhoLovesBoth;
    }
}
