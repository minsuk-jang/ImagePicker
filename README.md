<h1 align = "center">  GallerySelector </h1>
<!-- Add Gif -->
<p align = "center">
<img src= "https://github.com/minsuk-jang/GallerySelector/assets/26684848/2139f56c-a401-45a0-8cf8-3c092cffb666"/>
<img src = "https://github.com/minsuk-jang/GallerySelector/assets/26684848/4c31231f-a00f-43e0-a4f9-a5007f63ae07"/>
<img src = "https://github.com/minsuk-jang/GallerySelector/assets/26684848/0fbd38e1-d7e8-441f-92a2-70ef02e405ff"/>
</p>

<div align = "center"> The Gallery Selector is an Image Picker library created in the Compose language. <br>
It allows customization of the select frame and supports both single and multiple selections.<br> 
Additionally, it enables numbering for selected items and provides real-time access to the selected items.

<br><br>
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)
[![](https://jitpack.io/v/minsuk-jang/GallerySelector.svg)](https://jitpack.io/#minsuk-jang/GallerySelector)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

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

//For SDK over 33
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES"/>
```

### GalleryScreen
``` kotlin
@Composable
fun GalleryScreen(
    state: GalleryState = rememberGalleryState(), // state used in GalleryScreen
    content: @Composable BoxScope.(Gallery.Image) -> Unit //Media Content Cell Composable 
)
```

### State
- Get Selected Media Contents
- Set Max Size to Select Media Content
``` kotlin 
@Stable
class GalleryState(
    val max: Int = Constants.MAX_SIZE
) {
    val selectedImagesState: State<List<Gallery.Image>> = _selectedImages
}
```



<!--
- Gallery Screen Parameter 설명
- state 설명
- gif 추가 (select ordering / Custom 선택창 표현)
- selectedImages 표현

-->

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
