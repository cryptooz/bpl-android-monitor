package com.vrlc92.bplmonitor.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vrlc92.bplmonitor.R;
import com.vrlc92.bplmonitor.MainActivity;
import com.vrlc92.bplmonitor.adapters.VotersAdapter;
import com.vrlc92.bplmonitor.models.Account;
import com.vrlc92.bplmonitor.models.Settings;
import com.vrlc92.bplmonitor.models.Voters;
import com.vrlc92.bplmonitor.services.ArkService;
import com.vrlc92.bplmonitor.services.RequestListener;
import com.vrlc92.bplmonitor.utils.Utils;

public class VotersFragment extends Fragment implements RequestListener<Voters> {

    private SwipeRefreshLayout mSwipeRefreshLayout;

    public VotersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        return inflater.inflate(R.layout.fragment_voters, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.voters_refresh_layout);
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary,
                R.color.colorPrimaryDark,
                R.color.colorAccent);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshContent();
            }
        });

        if (Utils.isOnline(getActivity())) {
            loadVoters();
        } else {
            Utils.showMessage(getResources().getString(R.string.internet_off), view);
        }
    }

    @Override
    public void onDestroy() {
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.hideLoadingIndicatorView();
        }
        super.onDestroy();
    }

    private void loadVoters() {
        showLoadingIndicatorView();

        Settings settings = Utils.getSettings(getActivity());
        if (Utils.validatePublicKey(settings.getPublicKey())) {
            ArkService.getInstance().requestVoters(settings, this);
        } else {
            loadAccount();
        }
    }

    @Override
    public void onFailure(Exception e) {
        if (!isAdded()) {
            return;
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.showMessage(getString(R.string.unable_to_retrieve_data), getView());

                hideLoadingIndicatorView();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    @Override
    public void onResponse(final Voters voters) {
        if (!isAdded()) {
            return;
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View view = getView();

                RecyclerView rvVoters = (RecyclerView) view.findViewById(R.id.rvVoters);

                VotersAdapter adapter = new VotersAdapter(getContext(), voters);
                rvVoters.setAdapter(adapter);
                rvVoters.setLayoutManager(new LinearLayoutManager(getActivity()));

                hideLoadingIndicatorView();
                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void refreshContent() {
        Settings settings = Utils.getSettings(getActivity());

        if (Utils.validatePublicKey(settings.getPublicKey())) {
            ArkService.getInstance().requestVoters(settings, this);
        } else {
            loadAccount();
        }
    }

    private void loadAccount() {
        final Settings settings = Utils.getSettings(getActivity());

        ArkService.getInstance().requestAccount(settings, new RequestListener<Account>() {
            @Override
            public void onFailure(Exception e) {
                if (!isAdded()) {
                    return;
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideLoadingIndicatorView();
                        mSwipeRefreshLayout.setRefreshing(false);

                        Utils.showMessage(getString(R.string.unable_to_retrieve_data), getView());
                    }
                });
            }

            @Override
            public void onResponse(final Account account) {
                if (!isAdded()) {
                    return;
                }

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        hideLoadingIndicatorView();
                        mSwipeRefreshLayout.setRefreshing(false);

                        settings.setPublicKey(account.getPublicKey());
                        if (Utils.saveSettings(getActivity(), settings)) {
                            loadVoters();
                        }
                    }
                });
            }
        });
    }

    private void showLoadingIndicatorView() {
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.showLoadingIndicatorView();
        }
    }

    private void hideLoadingIndicatorView() {
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.hideLoadingIndicatorView();
        }
    }
}
