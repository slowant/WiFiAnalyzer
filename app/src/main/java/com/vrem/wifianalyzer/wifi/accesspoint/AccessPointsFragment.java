/*
 * WiFiAnalyzer
 * Copyright (C) 2019  VREM Software Development <VREMSoftwareDevelopment@gmail.com>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>
 */

package com.vrem.wifianalyzer.wifi.accesspoint;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ExpandableListView;

import com.vrem.wifianalyzer.MainContext;
import com.vrem.wifianalyzer.R;
import com.vrem.wifianalyzer.wifi.model.WiFiDetail;
import com.vrem.wifianalyzer.wifi.refresh.RefreshAction;
import com.vrem.wifianalyzer.wifi.refresh.RefreshListener;

import trip.taobao.com.wificonnect.wifilibrary.WiFiManager;

public class AccessPointsFragment extends Fragment implements RefreshAction {
    private SwipeRefreshLayout swipeRefreshLayout;
    private AccessPointsAdapter accessPointsAdapter;
    private WiFiManager mWiFiManager;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.access_points_content, container, false);

        swipeRefreshLayout = view.findViewById(R.id.accessPointsRefresh);
        swipeRefreshLayout.setOnRefreshListener(new RefreshListener(this));

        accessPointsAdapter = new AccessPointsAdapter();
        ExpandableListView expandableListView = view.findViewById(R.id.accessPointsView);
        expandableListView.setAdapter(accessPointsAdapter);
        accessPointsAdapter.setExpandableListView(expandableListView);

        MainContext.INSTANCE.getScannerService().register(accessPointsAdapter);

        // WIFI管理器
        mWiFiManager = WiFiManager.getInstance(getContext().getApplicationContext());

        accessPointsAdapter.setItemClickListener(new AccessPointsAdapter.ItemClickListener() {
            @Override
            public void onItemClick(WiFiDetail wiFiDetail) {
                switch (mWiFiManager.getSecurityMode(wiFiDetail.getCapabilities())) {
                    case WPA:
                    case WPA2:
                        mWiFiManager.connectWPA2Network(wiFiDetail.getSSID(), "12345678");
                        break;
                    case WEP:
                        mWiFiManager.connectWEPNetwork(wiFiDetail.getSSID(), "12345678");
                        break;
                    case OPEN: // 开放网络
                        mWiFiManager.connectOpenNetwork(wiFiDetail.getSSID());
                        break;
                }
            }
        });

        return view;
    }

    @Override
    public void refresh() {
        swipeRefreshLayout.setRefreshing(true);
        MainContext.INSTANCE.getScannerService().update();
        swipeRefreshLayout.setRefreshing(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        refresh();
    }

    @Override
    public void onDestroy() {
        MainContext.INSTANCE.getScannerService().unregister(accessPointsAdapter);
        super.onDestroy();
    }

    AccessPointsAdapter getAccessPointsAdapter() {
        return accessPointsAdapter;
    }

}
