<h1 align = "center">  ImagePicker </h1>
<!-- Add Gif -->
<p align = "center">
<img src = "https://github.com/minsuk-jang/GallerySelector/assets/26684848/0fbd38e1-d7e8-441f-92a2-70ef02e405ff" width="240"/>
<img src = "https://github.com/minsuk-jang/GallerySelector/assets/26684848/7d5abdf6-edef-4447-992f-5f47a057f24d" width="240"/>
<img src = "https://github.com/user-attachments/assets/6147ad64-53cd-44b6-a504-05c031f66316" width="240"/>
<br>
<img src = "https://github.com/user-attachments/assets/8f382893-d6de-4e6b-b3c2-d67bf52a8a32" width="240"/>
<img src = "https://github.com/user-attachments/assets/7b5c3674-617f-496a-8d2f-ac426a529df4" width="240"/>

</p>

<div align = "center">
  
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)
[![](https://jitpack.io/v/minsuk-jang/ImagePicker.svg)](https://jitpack.io/#minsuk-jang/ImagePicker)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

**ImagePicker** is a Jetpack Compose library for displaying and selecting media from the device gallery. It supports full UI customization, single and multi-selection, album filtering, and camera integration
</div>

## Features
- Fully customizable UI for content cells
- Support for selecting multiple items with drag gestures
- Support for selected image preview bar, album bar
- Display the selected order of items
- Camera support
- Preview screen
- Pagination for loading large image sets
- Album-based image grouping

## Installation
Step 1. Add it in your root build.gradle at the end of repositories:
```gradle
dependencyResolutionManagement {
  ...
  repositories {
    maven { url 'https://jitpack.io' }
  }
}
```

Step 2. Add the dependency
```gradle
dependencies {
    implementation 'com.github.minsuk-jang:ImagePicker:1.0.14'
}
```

## Usage
### Add permission in AndroidManifest.xml file:
```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.CAMERA" />
```

### ImagePickerScreen
ImagePickerScreen fetches the list of media contents using the Paging 3 Library. Customize each content cell's UI freely by receiving an Image through the Content parameter. It also supports drag gestures for selecting and deselecting multiple items.
```kotlin
@Composable
fun ImagePickerScreen(
    state: ImagePickerState = rememberImagePickerState(), // Configuration and state
    albumTopBar: @Composable ImagePickerAlbumScope.() -> Unit = {}, // // Slot for rendering a custom album selector UI. Use scope to access album list and current selected album.
    previewTopBar: @Composable PreviewTopBarScope.() -> Unit = {}, // Slot for rendering a preview UI of selected images 
    content: @Composable BoxScope.(Gallery.Image) -> Unit // Image cell Composable
)

//TODO add Slot api explanation

//TODO Preview screen explanation

```

<!--
<img src = "https://github.com/minsuk-jang/GallerySelector/assets/26684848/0fbd38e1-d7e8-441f-92a2-70ef02e405ff" width="270"/>
<img src = "https://github.com/minsuk-jang/GallerySelector/assets/26684848/7d5abdf6-edef-4447-992f-5f47a057f24d" width="270"/> 
<img src = "https://github.com/user-attachments/assets/1314c2e5-2d7b-4127-9048-4a085cf34ba5" width="270" />
-->

### ImagePickerState
ImagePickerState configures the ImagePickerScreen and provides the current state of content.
```kotlin 
@Stable
class ImagePickerState(
    val max: Int = Constants.MAX_SIZE,  // Maximum number of selectable items 
    val autoSelectAfterCapture: Boolean = false, // Automatically select the photo after capture
) {
    // List of currently selected images
    val mediaContents: State<List<MediaContent>> = _mediaContents
}
```


<!--
<img src = "https://github.com/user-attachments/assets/6147ad64-53cd-44b6-a504-05c031f66316" width ="270"/>
-->

## Classes
### MediaContent
```kotlin
@Stable
data class MediaContent(
  val id: Long, //Media content id
  val title: String, // Title of the content
  val dateAt: Long, // Date token
  val data: String, // File size
  val uri: Uri, // Content Uri
  val mimeType: String, // MIME type
  val album: String, // Album name
  val albumId: String // Album ID
  val selectedOrder: Int = Constants.NO_ORDER, // Selected order
  val selected : Boolean = false // Selection Status
)
```

### Album
```kotlin
class Album(
  val id: String? = null, // Album id
  val name: String = "", // Album name
  val count: Int = 0, // Number of images in the album
)
```
