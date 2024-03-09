<h1 align = "center">  GallerySelector </h1>
<!-- Add Gif -->
<p align = "center">
<img src= "https://github.com/minsuk-jang/GallerySelector/assets/26684848/2139f56c-a401-45a0-8cf8-3c092cffb666" width="270"/>
<img src = "https://github.com/minsuk-jang/GallerySelector/assets/26684848/0fbd38e1-d7e8-441f-92a2-70ef02e405ff" width="270"/>
<img src = "https://github.com/minsuk-jang/GallerySelector/assets/26684848/7d5abdf6-edef-4447-992f-5f47a057f24d" width="270"/>
</p>

<div align = "center">
  
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)
[![](https://jitpack.io/v/minsuk-jang/GallerySelector.svg)](https://jitpack.io/#minsuk-jang/GallerySelector)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)
  
<br>
 The Gallery Selector is an Image Picker library created in the Compose language. <br>
It allows customization of the select frame and supports both single and multiple selections.<br> 
Additionally, it enables numbering for selected items and provides real-time access to the selected items.

</div>

## ✅ Feature
- [x] Custom Cell UI
- [x] Content's Selected Order
- [ ] Multi Select Behaivor
- [x] Load Content by Paging
- [ ] Camera
- [ ] Preview Image
- [ ] Sort Content
- [ ] Improve Performance
- [ ] To be Next...

## Table of Contents
- [Installation](#installation)
- [Usage](#usage)
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
    implementation 'com.github.minsuk-jang:GallerySelector:1.0.3'
}
```

## 🎨 Usage
### Add permission in AndroidManifest.xml file:
``` AndroidManifest.xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES"/> //For SDK over 33
```

### GalleryScreen
`GalleryScreen` fetches the list of images from the device. and customize each Image cell through `Content` parameter.<br>
``` kotlin
@Composable
fun GalleryScreen(
    state: GalleryState = rememberGalleryState(), // state used in GalleryScreen
    content: @Composable BoxScope.(Gallery.Image) -> Unit //Media Image Content Cell Composable 
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
When you click image cell then can get Image class. it's properties as below. You can use `selectedOrder`, `selected` to check the selection order, status
``` kotlin
class Image(
  val id: Long, //Media content id
  val title: String, //Media content title
  val dateAt: Long, // Media content date token
  val data: String, //Media content data (File size)
  val uri: Uri,
  val mimeType: String, 
  val album: String, //Media content album name
  val selectedOrder: Int = Constants.NO_ORDER, //Media content selected order
  val selected : Boolean = false //Current selected flag
) : Gallery
```


### State
Using State, you can get selected contents and set their values.

``` kotlin 
@Stable
class GalleryState(
    val max: Int //Select max size
) {
    val selectedImagesState: State<List<Gallery.Image>> //Current selected Content Images State
}
```

For example, 

<img src = "https://github.com/minsuk-jang/GallerySelector/assets/26684848/7d5abdf6-edef-4447-992f-5f47a057f24d" align="right" width="270"/>

``` kotlin
val state = rememberGalleryState(max = 10)
val list = state.selectedImagesState.value

Scaffold(
    topBar = {
        ...
        Text(
            text = "${list.size} / ${state.max}",
            fontWeight = FontWeight.Medium,
            fontStyle = FontStyle.Normal,
        )
        ...
    }
) {
    GalleryScreen(
        modifier = Modifier
            .fillMaxSize()
            .padding(it),
        state = state,
        content = {
            if (it.selected) {
                Box(
                    modifier = Modifier
                        .border(width = 3.5.dp, color = Purple40)
                        .background(color = Gray.copy(0.5f))
                        .fillMaxSize()
                ) {
                    Text(
                        modifier = Modifier
                            .background(
                                color = Purple40,
                                shape = CircleShape
                            )
                            .size(25.dp)
                            .align(Alignment.TopEnd),
                        text = "${it.selectedOrder + 1}",
                        textAlign = TextAlign.Center,
                        color = Color.White
                    )
                }
            }
        }
    )
}
```


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
