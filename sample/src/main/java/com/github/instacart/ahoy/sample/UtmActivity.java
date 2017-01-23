/*
 * Copyright (C) 2016 Maplebear Inc., d/b/a Instacart
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
package com.github.instacart.ahoy.sample;

import android.os.Bundle;
import android.support.v4.util.ArrayMap;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.github.instacart.ahoy.AhoySingleton;
import com.github.instacart.ahoy.Visit;
import com.github.instacart.ahoy.utils.TypeUtil;

import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;

public class UtmActivity extends AppCompatActivity {

    @BindView(R.id.utm_campaign) TextView utmCampaign;
    @BindView(R.id.utm_content) TextView utmContent;
    @BindView(R.id.utm_medium) TextView utmMedium;
    @BindView(R.id.utm_source) TextView utmSource;
    @BindView(R.id.utm_term) TextView utmTerm;

    private Subscription mSubscription;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.utm_activity);
        ButterKnife.bind(this);

        setTitle(getString(R.string.utm_activity));
    }

    @Override protected void onResume() {
        super.onResume();

        mSubscription = AhoySingleton.visitStream()
                .startWith(AhoySingleton.visit())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<Visit>() {
                    @Override public void call(Visit visit) {
                        showUtmParams(visit);
                    }
                });
    }

    @Override protected void onPause() {
        super.onPause();
        mSubscription.unsubscribe();
    }

    private void showUtmParams(Visit visit) {
        utmCampaign.setText((String) visit.extra(Visit.UTM_CAMPAIGN));
        utmContent.setText((String) visit.extra(Visit.UTM_CONTENT));
        utmMedium.setText((String) visit.extra(Visit.UTM_MEDIUM));
        utmSource.setText((String) visit.extra(Visit.UTM_SOURCE));
        utmTerm.setText((String) visit.extra(Visit.UTM_TERM));
    }

    private static void saveContent(Map<String, Object> params, String key, TextView textView) {
        String value = textView.getText().toString();
        if (!TypeUtil.isEmpty(value)) {
            params.put(key, value);
        }
    }

    @OnClick({R.id.save_utm_params})
    public void onClick(View view) {
        final Map<String, Object> utmParams = new ArrayMap<>();
        saveContent(utmParams, Visit.UTM_CAMPAIGN, utmCampaign);
        saveContent(utmParams, Visit.UTM_CONTENT, utmContent);
        saveContent(utmParams, Visit.UTM_MEDIUM, utmMedium);
        saveContent(utmParams, Visit.UTM_SOURCE, utmSource);
        saveContent(utmParams, Visit.UTM_TERM, utmTerm);

        switch (view.getId()) {
            case R.id.save_utm_params:
                AhoySingleton.saveExtras(utmParams);
                break;
            default: break;
        }
    }
}
