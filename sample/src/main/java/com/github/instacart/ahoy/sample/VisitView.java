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

import android.content.Context;
import android.content.res.Resources;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.github.instacart.ahoy.AhoySingleton;
import com.github.instacart.ahoy.Visit;

import butterknife.BindView;
import butterknife.ButterKnife;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class VisitView extends LinearLayout {

    @BindView(R.id.visit_fetch_progress) ProgressBar visitFetchProgress;
    @BindView(R.id.visit_token) TextView visitTokenView;
    @BindView(R.id.visitor_token) TextView visitorTokenView;

    private Disposable mDisposable;

    public VisitView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    @Override protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        mDisposable = AhoySingleton.visitStream()
                .startWith(AhoySingleton.visit())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(visit -> updateViews(AhoySingleton.visitorToken(), visit));
    }

    @Override protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        mDisposable.dispose();
    }

    public void updateViews(String visitorToken, Visit visit) {
        Resources resources = getResources();
        visitorTokenView.setText(resources.getString(R.string.visit_token, visitorToken));
        boolean isVisitValid = visit != null && visit.isValid();
        visitFetchProgress.setVisibility(isVisitValid ? View.INVISIBLE : View.VISIBLE);
        if (isVisitValid) {
            visitTokenView.setText(resources.getString(R.string.visit_token, visit.visitToken()));
        } else {
            visitTokenView.setText(resources.getString(R.string.visit_token, ""));
        }
    }
}
