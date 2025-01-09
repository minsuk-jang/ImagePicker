<h1 align = "center">  ImagePicker </h1>
<!-- Add Gif -->
<p align = "center">
<img src= "https://github.com/minsuk-jang/GallerySelector/assets/26684848/2139f56c-a401-45a0-8cf8-3c092cffb666" width="240"/>
<img src = "https://github.com/minsuk-jang/GallerySelector/assets/26684848/0fbd38e1-d7e8-441f-92a2-70ef02e405ff" width="240"/>
<img src = "https://github.com/minsuk-jang/GallerySelector/assets/26684848/7d5abdf6-edef-4447-992f-5f47a057f24d" width="240"/>
<br>
<img src = "https://github.com/user-attachments/assets/6147ad64-53cd-44b6-a504-05c031f66316" width="240"/>
<img src = "https://github.com/user-attachments/assets/1314c2e5-2d7b-4127-9048-4a085cf34ba5" width="240"/>

</p>

<div align = "center">
  
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)
[![](https://jitpack.io/v/minsuk-jang/GallerySelector.svg)](https://jitpack.io/#minsuk-jang/GallerySelector)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
  
<br>
 The Image Picker library created in the Compose language. <br>
It allows customization of the content cell and supports both single and multiple selections.<br> 
</div>

## ✅ Feature
- [x] Custom Content Cell UI
- [x] Content's Selected Order
- [x] Multi Select Behaivor
- [x] Load Content by Paging
- [x] Camera
- [x] Album
- [ ] Preview Image
- [ ] Improve Performance
- [ ] To be Next...

## Table of Contents
- [Installation](#installation)
- [Usage](#-usage)
- [License](#license)

## Installation
Step 1. Add it in your root build.gradle at the end of repositories:
``` gradle
dependencyResolutionManagement {
  ...
  repositories {
    maven { url 'https://jitpack.io' }
  }
}
```
Step 2. Add the dependency
``` gradle
dependencies {
    implementation 'com.github.minsuk-jang:GallerySelector:1.0.10'
}
```

## 🎨 Usage
### Add permission in AndroidManifest.xml file:
``` AndroidManifest.xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.CAMERA" />
```

### GalleryScreen
GalleryScreen fetches the list of contents from the device using Paging 3 Library. Customize each content cell through the `Content` parameter, where the Image class is passed to enable flexible UI customization. It also supports Multi-Select behavior through drag gestures, allowing users to select multiple items intuitively.

``` kotlin
@Composable
fun GalleryScreen(
    state: GalleryState = rememberGalleryState(), // state used in GalleryScreen
    content: @Composable BoxScope.(Gallery.Image) -> Unit //Media content cell composable 
)
```

<img src = "https://github.com/minsuk-jang/GallerySelector/assets/26684848/0fbd38e1-d7e8-441f-92a2-70ef02e405ff" width="270"/>
<img src = "https://github.com/minsuk-jang/GallerySelector/assets/26684848/7d5abdf6-edef-4447-992f-5f47a057f24d" width="270"/> 
<img src = "https://github.com/user-attachments/assets/1314c2e5-2d7b-4127-9048-4a085cf34ba5" width="270" />

<br>

### GalleryState
GalleryState sets the required configurations for GalleryScreen and provides contents state. 
Becuase of using `State` type, you can always get the most latest value.

``` kotlin 
@Stable
class GalleryState(
    val max: Int //Select max size
    val autoSelectAfterCapture //auto select flag after taking picture 
) {
    val selectedImagesState: State<List<Gallery.Image>> //Current selected Content Images
    val albums: State<List<Album>> //list of albums in device
    val selectedAlbum: MutableState<Album> //current selected album
}
```

<img src = "https://github.com/user-attachments/assets/6147ad64-53cd-44b6-a504-05c031f66316" width ="270"/>

#### autoSelectAfterCapture
`autoSelectAfterChange` is a flag indicating whether the taken photo will be automatically selected after being captured. When you set `autoSelectAfterChange` flag true, it will be selected automatically

``` kotlin
val state = rememberGalleryState(
    max = 3,
    autoSelectAfterCapture = true
)

Scaffold(
    ...
) {
    GalleryScreen(
        state = state,
        content = {
            if (it.selected) {
                Text(
                    text = "${it.selectedOrder + 1}",
                )
            }
        })
}
```

#### albums
The variables `albums` and `selectedAlbum`, which are of type `State`, are in GalleryState. `albums` provides a list of albums in device. Also by passing the selected album as a parameter to GalleryScreen, you can fetch the images in the album.

```kotlin
//a list of albums
val albums = state.albums.value
//current selected album
var selectedAlbum by state.selectedAlbum 

Text(
    text = "${selectedAlbum.name} | ${selectedAlbum.count}"
)
DropdownMenu {
    albums.forEach {
        DropdownMenuItem(
            text = {
                Text(text = "${it.name},  ${it.count}")
            },
            onClick = {
                selectedAlbum = it
            }
        )
    }
}

GalleryScreen(
    album = selectedAlbum,
    state = state,
)
```

### Image
|Field|Type|Description|
|---|---|---|
|id|Long| Media content Id |
|title|String| Media content Title |
|dateAt|Long| Media content date token |
|data|String| File size |
|uri|Uri| Media Content Uri |
|mimeType|String| Media Content mimeType |
|album|String| Album name |
|albumId|String| Album Id |
|selectedOrder| Int | Selected Order |
|selected| Boolean | Current selected flag |

### Album
|Field|Type|Description|
|---|---|---|
|id|String?| album Id |
|name|String| album name |
|count| Int | number of images in the album |

