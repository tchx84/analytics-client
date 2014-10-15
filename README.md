analytics-client
==================
This is an Android service to collect information about apps usage.

Compiling
---------

To package this service, you will need Android API level 18. To setup your development environment
I recommend http://fedoramagazine.org/start-developing-android-apps-on-fedora-in-10-minutes/, this
article still works with Fedora 19.

```
$ git clone git@github.com:tchx84/analytics-client.git
$ cd analytics-client;
$ ant debug 
```

Running it
---------

0. Setup your analytics-server.
1. Change hardcoded settings in HarvestSettings, such as SERVER and KEY.
2. Compile it.
3. install your .apk on your device.
