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

**ImagePicker** is a Jetpack Compose library for displaying and selecting media from the device gallery.<br>
It supports full UI customization, single and multi-selection, album filtering, and camera integration
</div>

## Features
- 📦 **Fully customizable UI** for each image cell
- 🖐️ **Multi-selection** with drag gesture support
- 🧩 **Composable Slot APIs** for album & preview bar customization
- 🔢 **Visual selection order** (e.g., 1st, 2nd...)
- 📷 **Camera integration** with optional auto-select after capture
- 🖼️ **Full Preview screen** for selected images
- 🔄 **Pagination** for smooth loading of large image sets (via Paging 3)
- 🗂️ **Album-based grouping** with dynamic filtering

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
ImagePickerScreen fetches a list of media contents using the Paging 3 Library. <br>
It allows full customization of image cells and supports drag gestures for selecting or deselecting multiple items.

```kotlin
@Composable
fun ImagePickerScreen(
    // Configuration and state
    state: ImagePickerState = rememberImagePickerState(), 
    // Slot for rendering a custom album selector UI.
    // Provides access to album list and the currently selected album.
    albumTopBar: @Composable ImagePickerAlbumScope.() -> Unit = {},
    // Slot for rendering a preview UI of selected images.
    // Provides access to the current selection list.
    previewTopBar: @Composable PreviewTopBarScope.() -> Unit = {},
    // Image cell Composable
    content: @Composable BoxScope.(MediaContent) -> Unit 
)
```

### Slot APIs
The library provides two powerful slot APIs for customizing the album bar and preview bar.<br>
Each slot gives access to scoped data as show below:

```kotlin
@Stable
interface ImagePickerAlbumScope {
    val albums: List<Album>             // List of albums available on the device
    val selectedAlbum: Album?           // Currently selected album
    fun onSelect(album: Album)          // Function to change the selected album
}

@Stable
interface PreviewTopBarScope {
    val selectedMediaContents: List<MediaContent> // List of selected media items
    fun onClick(mediaContent: MediaContent)       // Toggle selection for the given item
}
```
you can use these properties and functions inside the respective slot lambdas to build your own UI components:
```kotlin
ImagePickerScreen(
    albumTopBar = {
        Text("Album: ${selectedAlbum?.name ?: "None"}")
        albums.forEach { album ->
            Text(
                text = album.name,
                modifier = Modifier.clickable { onSelect(album) }
            )
        }
    },
    previewTopBar = {
        Row {
            selectedMediaContents.forEach { media ->
                Image(
                    painter = rememberAsyncImagePainter(media.uri),
                    contentDescription = null,
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { onClick(media) }
                )
            }
        }
    },
    content = { media ->
        // Your image cell content
    }
)
```


### PreviewScreen
The library supports a fullscreen preview screen for selected images. it supports horizontal swiping and selection toggling.

<!-- scrolling or select, unselect preview screen gif -->

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
