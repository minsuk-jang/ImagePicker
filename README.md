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
[![](https://jitpack.io/v/minsuk-jang/ImagePicker.svg)](https://jitpack.io/#minsuk-jang/ImagePicker)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

ImagePicker is a Jetpack Compose-based library that allows for full customization of content cells and supports both single and multiple selection of media items
</div>

## Features
- Fully customizable UI for content cells
- Support for selecting multiple items with drag gestures
- Preview Selected Images
- Display the selected order of items
- Camera support
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
    implementation 'com.github.minsuk-jang:ImagePicker:1.0.13'
}
```

## Usage
### Add permission in AndroidManifest.xml file:
``` AndroidManifest.xml
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
    album: Album? = null, // Currently selected album
    onAlbumListLoaded: (List<Album>) -> Unit = {}, // Callback triggered when album list on deivce is loaded
    onAlbumSelected: (Album) -> Unit = {}, // Callback when a user select on album
    onClick: (Gallery.Image) -> Unit = {}, // Callback when an image cell is clicked
    content: @Composable BoxScope.(Gallery.Image) -> Unit // Image cell Composable
)

```

<!--
<img src = "https://github.com/minsuk-jang/GallerySelector/assets/26684848/0fbd38e1-d7e8-441f-92a2-70ef02e405ff" width="270"/>
<img src = "https://github.com/minsuk-jang/GallerySelector/assets/26684848/7d5abdf6-edef-4447-992f-5f47a057f24d" width="270"/> 
<img src = "https://github.com/user-attachments/assets/1314c2e5-2d7b-4127-9048-4a085cf34ba5" width="270" />
-->

### ImagePickerState
ImagePickerState configures the ImagePickerScreen and provides the current state of content.
``` kotlin 
@Stable
class ImagePickerState(
    val max: Int = Constants.MAX_SIZE,  // Maximum number of selectable items 
    val autoSelectAfterCapture: Boolean = false, // Automatically select the photo after capture
    val autoSelectOnClick: Boolean = true, // If true, clicking an image will select or deselect it automatically
    val showPreviewBar: Boolean = false // If true, shows a preview bar displaying selected images
) {
    // List of currently selected images
    private var _pickedImages: MutableState<List<Gallery.Image>> = mutableStateOf(emptyList())
    val images: List<Gallery.Image> get() = _pickedImages.value
}
```


<!--
<img src = "https://github.com/user-attachments/assets/6147ad64-53cd-44b6-a504-05c031f66316" width ="270"/>
-->

## Classs
### Image
```kotlin
class Image(
  val id: Long, //Media content id
  val title: String, // Title of the content
  val dateAt: Long, // Date token
  val data: String, // File size
  val uri: Uri, // Content Uri
  val mimeType: String, // MIME type
  val album: String, // Album name
  val albumId: String // Album ID
  val selectedOrder: Int = Constants.NO_ORDER, // Selected order
  val selected : Boolean = false //  Select Status
) : Gallery
```

### Album
```kotlin
class Album(
  val id: String? = null, // Album id
  val name: String = "", // Album name
  val count: Int = 0, // Number of images in the album
)
```
