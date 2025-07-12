<h1 align = "center">  ImagePicker </h1>
<!-- Add Gif -->
<p align = "center">
<img src = "https://github.com/user-attachments/assets/3dc78705-e90d-42e4-859c-79e9b28ff8b9" />
<img src = "https://github.com/user-attachments/assets/2d6daad9-a499-443a-b7c7-282ad2c69177" />
<img src = "https://github.com/user-attachments/assets/9531c4a4-9603-47b7-a716-d74aecf75c8f" />
<img src = "https://github.com/user-attachments/assets/64724a9e-669a-4e8a-9bc7-773c440bc755" />
</p>

<div align = "center">
  
[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)
[![](https://jitpack.io/v/minsuk-jang/ImagePicker.svg)](https://jitpack.io/#minsuk-jang/ImagePicker)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

### A fully customizable, DSL-based image picker for Jetpack Compose

ImagePicker is a Jetpack Compose library for displaying and selecting media from the device gallery.<br>
ImagePicker uses a declarative DSL structure to define screens within a navigation graph, similar to NavHost and composable in Jetpack Navigation


</div>

## Features
- 🧩 **DSL-based Navigation Graph**: Declare screens inside `ImagePickerNavHost` like `NavHost`
- 📦 **Fully customizable UI** for album selector, preview bar, image cells and preview screen
- 🖐️ **Multi-selection** with drag gesture support
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

## Permissions
Make sure to include the following in your AndroidManifest.xml
```xml
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES"/>
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE"/>
<uses-permission android:name="android.permission.CAMERA" />
```

## 🚀 Quick Start
<img src = "https://github.com/user-attachments/assets/2d6daad9-a499-443a-b7c7-282ad2c69177"  align="right" />

Declare your image picker UI using `ImagePickerNavHost`, just like `NavHost` in Jetpack Navigation:
```kotlin
ImagePickerNavHost(state = state) {
    ImagePickerScreen(
        albumTopBar = { ... },
        previewTopBar = { ... },
        cellContent = { ... }
    )

    PreviewScreen {
        // Full-screen preview UI
    }
}
```


Each slot (albumTopBar, previewTopBar, cellContent, PreviewScreen) gives access to its own custom scope to help you build highly flexible UIs.
                                                                      
## Slot APIs and Their Scopes
Each slot in ImagePickerScreen or PreviewScreen is powered by a custom scope.
These scopes provide the necessary state and event handlers you need to build fully customized UIs.

Below is a breakdown of each slot, its associated scope, and what you can do inside it.

### 🎛️  albumTopBar → ImagePickerAlbumScope
Use this slot to show album-related UI such as a dropdown or album selector.
The ImagePickerAlbumScope gives you access to:

| Property / Function    | 	Description                     |
|------------------------|----------------------------------|
| `albums: List<Album>`  | List of all albums on the device |
| `selectedAlbum: Album?`  | Currently selected album         |
| `onClick(album: Album)`  | Select the given album           |

Example usage:
```kotlin
albumTopBar = {
    var expanded by remember { mutableStateOf(false) }
  
    Box {
        Text(
            text = selectedAlbum?.name ?: "All Albums",
            modifier = Modifier.clickable { expanded = true }
        )
  
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            albums.forEach { album ->
                DropdownMenuItem(
                    text = { Text("${album.name} (${album.count})") },
                    onClick = {
                        expanded = false
                        onClick(album) // Select the album
                    }
                )
            }
        }
  }
}
```

### 🎛️ previewTopBar → ImagePickerPreviewScope
This slot allows you to preview currently selected media contents in a custom layout.   
The `ImagePickerPreviewScope` gives you access to:

| Property / Function                         | 	Description                     |
|---------------------------------------------|----------------------------------|
| `selectedMediaContents: List<MediaContent>` | List of selected media content   |
| `onDeselect(mediaContent: MediaContent)`    | Deselect the given media content |

Example usage:
```kotlin
previewTopBar = {
    Row {
        selectedMediaContents.forEach { media ->
            AsyncImage(
                model = media.uri,
                contentDescription = null,
                modifier = Modifier.clickable {
                    onDeselect(media) // Deselect
                }
            )
        }
    }
}
```

### 🎛️ cellContent → ImagePickerCellScope
This slot renders each image cell in the grid. Only the `MediaContent` is provided - the rest is up to you.<br>
Use this slot to:
- Display thumbnails
- Indicate selected state (e.g., with a badge or overlay)
- Navigate to the preview screen

The `ImagePickerCellScope` gives you access to:

| Property / Function                       | 	Description                               |
|-------------------------------------------|--------------------------------------------|
| `mediaContent: MediaContent`              | The media content represented by this cell |
| `onNavigateToPreviewScreen(mediaContent)` | Triggers navigation to the Preview Screen  |

```kotlin
cellContent = {
    Box(modifier = Modifier.clickable {
        onNavigateToPreviewScreen(mediaContent)
    }) {
        AsyncImage(model = mediaContent.uri, contentDescription = null)
        if (mediaContent.selected) {
            Text(
                text = "#${mediaContent.selectedOrder}",
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    }
}
```
💡 You can fully control the UI — whether it's adding badges, applying blur, or animating selection — by customizing this slot.

### 🎛️ PreviewScreen → PreviewScreenScope
This slot allows you to define the full-screen preview UI for selected media content.   
The `PreviewScreenScope` provides:   

| Property / Function               | 	Description                                 |
|-----------------------------------|----------------------------------------------|
| `mediaContent: MediaContent`      | The currently visible media content          |
| `onBack()`                        | Navigate back to the picker screen           |
| `onToggleSelection(mediaContent)` | Selects or deselects the given media content |

> ⚠️ **Important** <br>
PreviewScreen must be explicitly declared inside ImagePickerNavHost.<br>
If omitted, calling onNavigateToPreviewScreen() from a cell will cause a runtime crash.



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