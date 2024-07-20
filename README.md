<h1 align = "center">  GallerySelector </h1>
<!-- Add Gif -->
<p align = "center">
<img src= "https://github.com/minsuk-jang/GallerySelector/assets/26684848/2139f56c-a401-45a0-8cf8-3c092cffb666" width="245"/>
<img src = "https://github.com/minsuk-jang/GallerySelector/assets/26684848/0fbd38e1-d7e8-441f-92a2-70ef02e405ff" width="245"/>
<img src = "https://github.com/minsuk-jang/GallerySelector/assets/26684848/7d5abdf6-edef-4447-992f-5f47a057f24d" width="245"/>
<img src = "https://github.com/user-attachments/assets/6147ad64-53cd-44b6-a504-05c031f66316" width="245"/>
</p>

<div align = "center">
  
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)
[![](https://jitpack.io/v/minsuk-jang/GallerySelector.svg)](https://jitpack.io/#minsuk-jang/GallerySelector)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
  
<br>
 The Gallery Selector is an Image Picker library created in the Compose language. <br>
It allows customization of the content cell and supports both single and multiple selections.<br> 
Additionally, it enables numbering for selected contents and get latest total selected contents.

</div>

## ✅ Feature
- [x] Custom Content Cell UI
- [x] Content's Selected Order
- [ ] Multi Select Behaivor
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
    implementation 'com.github.minsuk-jang:GallerySelector:1.0.5'
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
`GalleryScreen` fetches the list of contents from the device. and customize each content cell through `Content` parameter.<br>
``` kotlin
@Composable
fun GalleryScreen(
    state: GalleryState = rememberGalleryState(), // state used in GalleryScreen
    content: @Composable BoxScope.(Gallery.Image) -> Unit //Media content cell composable 
)
```

<img src = "https://github.com/minsuk-jang/GallerySelector/assets/26684848/0fbd38e1-d7e8-441f-92a2-70ef02e405ff" align="right" width="270"/>

Here is an example where it is represented by a checkmark when selected
``` kotlin
 GalleryScreen(
    state = state,
    content = {
      if (it.selected) {
        Box(modifier = Modifier.fillMaxSize()) {
          Icon(
            modifier = Modifier.align(Alignment.TopEnd),
            painter = painterResource(id = R.drawable.done_black_24dp),
            contentDescription = null,
            tint = Color.Green
          )
      }
    }
  }
)

```

### Image
When you click content cell, get image class inherited from Gallery. You can use `selectedOrder` and `selected` to check the selection order, status. It's properties as below.
``` kotlin
class Image(
  val id: Long, //Media content id
  val title: String, //Media content title
  val dateAt: Long, // Media content date token
  val data: String, //Media content data (File size)
  val uri: Uri,
  val mimeType: String, 
  val album: String, //Media content album name
  val albumId: String //Media conten album id
  val selectedOrder: Int = Constants.NO_ORDER, //Media content selected order
  val selected : Boolean = false //Current selected flag
) : Gallery
```

### Album

``` kotlin
class Album(
  val id: String? = null, //album id
  val name: String = "", //album name
  val count: Int = 0, //number of images in the album
)
```


### GalleryState
GalleryState sets the required configurations for `GalleryScreen` and provides contents state. 
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

#### max
You can set the maximum selection value using the `max` variable. Here is an example showing the order of selected content and the total number of selected contents.

<div display= "inline-block;" >
<img src = "https://github.com/minsuk-jang/GallerySelector/assets/26684848/7d5abdf6-edef-4447-992f-5f47a057f24d" align = "right" width="270"/> 
</div>

``` kotlin
val state = rememberGalleryState(max = 10)
val list = state.selectedImagesState.value

Scaffold(
    topBar = {
        Text(
            text = "${list.size} / ${state.max}"
        )
    }
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

#### autoSelectAfterCapture

`autoSelectAfterChange` is a flag indicating whether the taken photo will be automatically selected after being captured. When you set `autoSelectAfterChange` flag true, it will be selected automatically

<img src = "https://github.com/minsuk-jang/GallerySelector/assets/26684848/a2e28762-998f-4404-99fe-569c2b961dba" align="right" width ="270"/>

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

<br><br><br>

## License
```
MIT License

Copyright (c) 2024 Minsuk-Jang

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
