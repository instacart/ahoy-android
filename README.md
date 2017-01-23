# Ahoy Android

Visit attribution library for Android that works on top of Rails [Ahoy](http://github.com/ankane/ahoy) foundation.

:tangerine: Battle-tested at [Instacart](https://www.instacart.com/)

:waning_gibbous_moon: User visit tracking

:inbox_tray: Visit attribution through UTM & referrer parameters

# Motivation
No matter which Analytics solution we tried, we always use our own database as the source of truth. 
Ahoy Android stores visit attribution (UTM & referrer parameters) in your data storage of choice (through [Ahoy](http://github.com/ankane/ahoy) on Rails) and allows to associate business data (e.x. user and order records), 
with visits & funnels (attributions).

# Using ahoy-android
## Visit
Visit corresponds to user's session in the app. Visit's extra parameters (UTMs, referrer) are stored for the [duration of the visit](https://github.com/ankane/ahoy#visit-duration). After visit expires extra parameters are cleared.

## Tracking visits

```
public class App extends Application {

    @Override public void onCreate() {
        super.onCreate();

        boolean autoStart = /* start Ahoy on first Activity::onCreate invocation */ true;
        long visitExpiration = 60 * 60 * 1000;
        DeviceInfo deviceInfo = new SimpleDeviceInfo();
        Retrofit2Delegate delegate = Retrofit2Delegate.factory("http://www.example.com", visitExpiration, deviceInfo, true);
        AhoySingleton.init(this, delegate, autoStart);
    }
}
```

``` AhoySingleton``` is a convenience wrapper. See ```RxAhoy``` for visits stream. For Dependency Injection, create ```Ahoy``` instance directly. 

To start Ahoy manually, pass ```autoStart = false``` and call  ```Ahoy.ensureFreshVisit``` when appropriate. Ahoy stops refreshing visits when application is backgrounded. If initialized with ```autoStart = false```, it needs to be restarted.

Please use our Sample and Tests projects for reference.

[Ahoy backend demo](https://murmuring-ocean-69755.herokuapp.com) is at your service.

## UTMs
To save additional parameters (UTM, referrer):
```
   Map<String, Object> params = ...
   // starts a new visit
   Ahoy.newVisit(params)
```


## Visits & Signups

[Ahoy](http://github.com/ankane/ahoy) associates signups and signins with visits using ```Ahoy-Visitor``` and ```Ahoy-Visit``` headers. E.x. okhttp3 [application interceptor](https://github.com/square/okhttp/wiki/Interceptors) passing them to backend:

```
public class AhoyRequestInterceptor implements okhttp3.Interceptor {

    private Ahoy ahoy;

    @Override public Response intercept(Chain chain) throws IOException {
        if (ahoy == null) {
            return chain.proceed(chain.request());
        }

        String visitToken = ahoy.visit().visitToken();
        Request request = chain.request();
        Builder builder = request.newBuilder();
        if (!TextUtils.isEmpty(visitToken)) {
            builder.header("Ahoy-Visit", visitToken);
        }
        builder.header("Ahoy-Visitor", ahoy.visitorToken());
        return chain.proceed(builder.build());
    }

    public void setAhoy(Ahoy ahoy) {
        this.ahoy = ahoy;
    }
}
```

## Ahoy & other Analytics solutions
At [Instacart](https://www.instacart.com) we use [Segment](https://www.segment.io) to send events a Date Warehouse and [Amplitude](https://www.amplitude.com). Whenever we make requests to Segment we include Ahoy visit token and extra parameters.


## Installation
Add this to your application's `build.gradle` file:

```groovy
repositories {
    maven {
        url "https://jitpack.io"
    }
}

dependencies {
    // ...
    compile 'com.github.instacart:ahoy-android:v0.2'
}
```

# License

```
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
