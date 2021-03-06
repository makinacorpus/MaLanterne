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

```
git clone https://github.com/makinacorpus/MaLanterne.git
cd MaLanterne
./gradlew clean assemble<Flavor>Debug
```

where **Flavor** is a variant chosen of the application to build (Camel case):

* Amiens
* Bfm
* Gdl

## Deploying the application
Ensure that you have a connected device with Android 2.3.x or higher running and execute the
following command after build:

```
./gradlew install<Flavor>Debug
```

You can combine a full build and deploy the application in a same command:

```
./gradlew clean install<Flavor>Debug
```

## Using Offline Maps (MBTiles)

### copy all MBTiles on the device
MBTiles sources should be copied locally on the device.
First create a new directory inside `/mnt/sdcard/Android/data/` according to the current flavor used
(lower case):

* amiens
* bfm
* gdl
```
adb shell mkdir /mnt/sdcard/Android/data/com.makina.osmnav.<flavor>/
```

then copy all `*.mbtiles` within this directory:

```
find *.mbtiles -exec adb push {} /mnt/sdcard/Android/data/com.makina.osmnav.<flavor>/ \;
```

### configure the layer settings
Create a new `JSON` file within the `assets/` folder named `mbtiles.json`:

```
{
    "name": "Paris Gare de Lyon",
    "bbox": [
        48.8487,
        2.3866,
        48.8368,
        2.3664
    ],
    "layers": {
        "base": "gdl.mbtiles",
        "layers": [
            "gdl_2.0.mbtiles",
            "gdl_1.0.mbtiles",
            "gdl_0.0.mbtiles",
            "gdl_-0.25.mbtiles",
            "gdl_-0.5.mbtiles",
            "gdl_-0.75.mbtiles",
            "gdl_-1.0.mbtiles",
            "gdl_-2.0.mbtiles",
            "gdl_-3.0.mbtiles"
        ]
    }
}
```

where:

* **`name`**: the name of this layers settings
* **`bbox`**: the global bounding box to use (used to limit and center the map): `LatNorth, LonEast, LatSouth, LonWest`
* **`layers`**: layers to use
  * **`base`**: the main layer to use as base layer
  * **`layers`**: a set of additional layers to display (used to display each level).
  The naming of these layers must follow this rule to determine automatically the right level:
  `name_of_the_layer_<level>.mbtiles`
