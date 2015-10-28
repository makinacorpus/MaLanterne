# OSMNav

Simple demo app using [OSMDroid](https://github.com/osmdroid/osmdroid) to display offline maps from
MBTiles sources.

## Prerequisites

* [JDK 7](https://jdk7.java.net/download.html)
* [Android SDK](http://developer.android.com/sdk/index.html)

## Components used

* [Android Support Library](http://developer.android.com/tools/support-library/index.html)
* [OSMDroid](https://github.com/osmdroid/osmdroid)
* [OSMBonusPack](https://github.com/MKergall/osmbonuspack)
* [Gsjon](https://github.com/google/gson)

## Full Build

Install and run the application:

    git clone https://github.com/makinacorpus/MaLanterne.git
    cd MaLanterne
    ./gradlew clean assembleDebug

## Deploying the application
Ensure that you have a connected device with Android 2.3.x or higher running and execute the
following command after build:

    ./gradlew installDebug

You can combine a full build and deploy the application in a same command:

    ./gradlew clean installDebug

## Using Offline Maps (MBTiles)

MBTiles sources should be copied locally on the device.
First create a new directory inside `/mnt/sdcard/Android/data/`:

    adb shell
    cd /mnt/sdcard/Android/data/
    mkdir com.makina.osmnav

then copy all `*.mbtiles` within this directory:

    find *.mbtiles -exec adb push {} /mnt/sdcard/Android/data/com.makina.osmnav/ \;

**Note:** The mobile application use hardcoded names of the MBtiles to load following these rules:

* `gdl.mbtiles` as main MBTiles source
* `gdl_<level>.mbtiles` as MBTiles source for each level to display





