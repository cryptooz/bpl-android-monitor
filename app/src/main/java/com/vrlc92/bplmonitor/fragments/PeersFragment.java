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
import com.vrlc92.bplmonitor.adapters.PeersAdapter;
import com.vrlc92.bplmonitor.models.Peer;
import com.vrlc92.bplmonitor.models.Settings;
import com.vrlc92.bplmonitor.services.ArkService;
import com.vrlc92.bplmonitor.services.RequestListener;
import com.vrlc92.bplmonitor.utils.Utils;

import java.util.List;

public class PeersFragment extends Fragment implements RequestListener<List<Peer>> {

    private SwipeRefreshLayout mSwipeRefreshLayout;

    public PeersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_peers, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.peers_refresh_layout);
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
            loadPeers();
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

    private void loadPeers() {
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.showLoadingIndicatorView();
        }

        Settings settings = Utils.getSettings(getActivity());

        ArkService.getInstance().requestPeers(settings, this);
    }

    @Override
    public void onFailure(final Exception e) {
        if (!isAdded()) {
            return;
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Utils.showMessage(getString(R.string.unable_to_retrieve_data), getView());

                mSwipeRefreshLayout.setRefreshing(false);

                MainActivity activity = (MainActivity) getActivity();
                if (activity != null) {
                    activity.hideLoadingIndicatorView();
                }
            }
        });
    }

    @Override
    public void onResponse(final List<Peer> peers) {
        if (!isAdded()) {
            return;
        }

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View view = getView();

                RecyclerView rvPeers = (RecyclerView) view.findViewById(R.id.rvPeers);

                PeersAdapter adapter = new PeersAdapter(getContext(), peers);
                rvPeers.setAdapter(adapter);
                rvPeers.setLayoutManager(new LinearLayoutManager(getActivity()));

                MainActivity activity = (MainActivity) getActivity();
                if (activity != null) {
                    activity.hideLoadingIndicatorView();
                }

                mSwipeRefreshLayout.setRefreshing(false);
            }
        });
    }

    private void refreshContent() {
        Settings settings = Utils.getSettings(getActivity());
        ArkService.getInstance().requestPeers(settings, this);
    }
}
