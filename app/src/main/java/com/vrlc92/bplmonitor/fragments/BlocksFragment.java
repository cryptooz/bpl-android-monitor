package com.vrlc92.bplmonitor.fragments;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.vrlc92.bplmonitor.MainActivity;
import com.vrlc92.bplmonitor.R;
import com.vrlc92.bplmonitor.adapters.BlocksAdapter;
import com.vrlc92.bplmonitor.models.Block;
import com.vrlc92.bplmonitor.models.Settings;
import com.vrlc92.bplmonitor.services.ArkService;
import com.vrlc92.bplmonitor.services.RequestListener;
import com.vrlc92.bplmonitor.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class BlocksFragment extends Fragment implements RequestListener<List<Block>> {

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private List<Block> mBlocks = new ArrayList<>();
    private BlocksAdapter mBlocksAdapter;

    public BlocksFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_blocks, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mBlocksAdapter = new BlocksAdapter(getContext(), mBlocks);

        RecyclerView rvBlocks = (RecyclerView) view.findViewById(R.id.rvBlocks);

        rvBlocks.setAdapter(mBlocksAdapter);
        rvBlocks.setLayoutManager(new LinearLayoutManager(getActivity()));

        mSwipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.blocks_refresh_layout);
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
            loadBlocs();
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

    private void loadBlocs() {
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            activity.showLoadingIndicatorView();
        }

        refreshContent();
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

                MainActivity activity = (MainActivity)getActivity();
                if (activity != null) {
                    activity.hideLoadingIndicatorView();
                }
            }
        });
    }

    @Override
    public void onResponse(final List<Block> blocks) {
        if (!isAdded()) {
            return;
        }

        mBlocks.addAll(blocks);

        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mBlocksAdapter.setBlocks(mBlocks);
                mBlocksAdapter.notifyDataSetChanged();

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

        mBlocks.clear();

        ArkService.getInstance().requestBlocks(settings, this);
    }

}
