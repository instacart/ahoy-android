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

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.github.instacart.ahoy.AhoySingleton;

import java.util.Collections;

import butterknife.ButterKnife;
import butterknife.OnClick;

public class SimpleActivity extends AppCompatActivity {

    @Override protected void onCreate(Bundle state) {
        super.onCreate(state);
        setContentView(R.layout.simple_activity);
        ButterKnife.bind(this);
    }

    @OnClick({R.id.new_visit, R.id.save_utms })
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.new_visit:
                AhoySingleton.newVisit(Collections.<String, Object>emptyMap());
                break;
            case R.id.save_utms:
                startActivity(new Intent(this, UtmActivity.class));
                break;
            default: break;
        }
    }
}