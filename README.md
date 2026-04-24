<h1 align = "center">ImagePicker</h1>

<p align = "center">
<img src = "https://github.com/user-attachments/assets/3dc78705-e90d-42e4-859c-79e9b28ff8b9" width="200"/>
<img src = "https://github.com/user-attachments/assets/2d6daad9-a499-443a-b7c7-282ad2c69177" width="200"/>
<img src = "https://github.com/user-attachments/assets/9531c4a4-9603-47b7-a716-d74aecf75c8f" width="200"/>
<img src = "https://github.com/user-attachments/assets/64724a9e-669a-4e8a-9bc7-773c440bc755" width="200"/>
</p>

<div align = "center">

[![API](https://img.shields.io/badge/API-21%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=21)
[![](https://jitpack.io/v/minsuk-jang/ImagePicker.svg)](https://jitpack.io/#minsuk-jang/ImagePicker)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](https://opensource.org/licenses/MIT)

### A fully customizable, DSL-based image picker for Jetpack Compose

ImagePicker is a Jetpack Compose library for displaying and selecting media from the device gallery.  
It uses a declarative DSL structure to define screens within a navigation graph, similar to `NavHost` in Jetpack Navigation.

</div>

---

## Features

- **DSL-based Navigation Graph** — Declare screens inside `ImagePickerNavHost` like `NavHost`
- **Fully customizable UI** — Control album selector, preview bar, image cells, and preview screen independently
- **Multi-selection with drag gesture** — Long-press and drag to batch-select images
- **Visual selection order** — Display selection index (1st, 2nd, ...) on each cell
- **Full preview screen** — Swipeable full-screen preview for selected images
- **Pagination** — Smooth loading of large galleries via Paging 3
- **Album filtering** — Dynamic album-based grouping and switching

---

## Installation

**Step 1.** Add JitPack to your root `settings.gradle`:
```gradle
dependencyResolutionManagement {
    repositories {
        maven { url 'https://jitpack.io' }
    }
}
```

**Step 2.** Add the dependency:
```gradle
dependencies {
    implementation 'com.github.minsuk-jang:ImagePicker:1.0.16'
}
```

---

## Permissions

Add the appropriate permissions to your `AndroidManifest.xml` based on the target API level:

```xml
<!-- API 32 and below -->
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />

<!-- API 33 and above -->
<uses-permission android:name="android.permission.READ_MEDIA_IMAGES" />
```

> You should request these permissions at runtime before launching `ImagePickerNavHost`.

---

## Concept

ImagePicker is built around three principles:

### 1. Declarative Navigation DSL
Screens are declared inside `ImagePickerNavHost { }`, just like Jetpack Navigation's `NavHost`:

```kotlin
ImagePickerNavHost(state = state) {
    ImagePickerScreen(...)
    PreviewScreen { ... }
}
```

### 2. Scoped Slot APIs
Each UI slot receives a dedicated scope that exposes only the data and actions relevant to that screen:

| Slot            | Scope                       | Responsibility                              |
|-----------------|-----------------------------|---------------------------------------------|
| `albumTopBar`   | `ImagePickerAlbumScope`     | Album list and selection                    |
| `previewTopBar` | `ImagePickerPreviewScope`   | Selected media preview and deselection      |
| `cellContent`   | `ImagePickerCellScope`      | Image cell UI and navigation to preview     |
| `PreviewScreen` | `PreviewScreenScope`        | Full-screen preview actions                 |

### 3. Shared Picker State
`ImagePickerNavHostState` bridges the picker and your app, exposing the final selection result:

```kotlin
val state = rememberImagePickerNavHostState(max = 10)

// Read selected results anywhere in your composable
val selected = state.selectedMediaContents
```

---

## Quick Start

A complete example from setup to reading results:

```kotlin
// 1. Create state
val state = rememberImagePickerNavHostState(max = 10)

// 2. Declare the picker
ImagePickerNavHost(state = state) {
    ImagePickerScreen(
        albumTopBar = {
            // Show album selector using 'albums', 'selectedAlbum', 'onClick'
        },
        previewTopBar = {
            // Show selected thumbnails using 'selectedMediaContents', 'onDeselect'
        },
        cellContent = {
            // Render each cell using 'mediaContent', 'onNavigateToPreviewScreen'
        }
    )

    PreviewScreen {
        // Full-screen preview using 'mediaContent', 'onBack', 'onToggleSelection'
    }
}

// 3. Read selected results
val selected = state.selectedMediaContents
```

<p>
<img src = "https://github.com/user-attachments/assets/2d6daad9-a499-443a-b7c7-282ad2c69177" width="250"/>
<img src = "https://github.com/user-attachments/assets/dcf74aef-64ac-4552-a005-5f63721c65e7" width="250"/>
<img src = "https://github.com/user-attachments/assets/34a1a634-32b8-42e1-b519-134118118f6f" width="250"/>
</p>

---

## Slot APIs

### `albumTopBar` — `ImagePickerAlbumScope`

Renders the album selector UI. The scope provides:

| Property / Function       | Description                      |
|---------------------------|----------------------------------|
| `albums: List<Album>`     | All albums available on device   |
| `selectedAlbum: Album?`   | Currently selected album         |
| `onClick(album: Album)`   | Switch to the given album        |

```kotlin
albumTopBar = {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Text(
            text = selectedAlbum?.name ?: "All",
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
                        onClick(album)
                    }
                )
            }
        }
    }
}
```

---

### `previewTopBar` — `ImagePickerPreviewScope`

Renders selected media in a preview bar. The scope provides:

| Property / Function                          | Description                      |
|----------------------------------------------|----------------------------------|
| `selectedMediaContents: List<MediaContent>`  | Currently selected media         |
| `onDeselect(mediaContent: MediaContent)`     | Deselect the given item          |

```kotlin
previewTopBar = {
    Row {
        selectedMediaContents.forEach { media ->
            AsyncImage(
                model = media.uri,
                contentDescription = null,
                modifier = Modifier.clickable { onDeselect(media) }
            )
        }
    }
}
```

---

### `cellContent` — `ImagePickerCellScope`

Renders each image cell in the grid. The scope provides:

| Property / Function                                        | Description                                    |
|------------------------------------------------------------|------------------------------------------------|
| `mediaContent: MediaContent`                               | The media item for this cell                   |
| `onSelect()`                                               | Toggle the selection state of this cell        |
| `onNavigateToPreviewScreen(mediaContent: MediaContent)`    | Navigate to the full preview screen            |

> **Note:** Selection toggling is no longer handled internally. You must call `onSelect()` yourself (e.g. inside a `clickable` modifier) to toggle the selection state.

```kotlin
cellContent = {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .clickable { onSelect() }
    ) {
        AsyncImage(model = mediaContent.uri, contentDescription = null)

        if (mediaContent.selected) {
            Text(
                text = "${mediaContent.selectedOrder + 1}",
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
    }
}
```

---

### `PreviewScreen` — `PreviewScreenScope`

Renders the full-screen swipeable preview. The scope provides:

| Property / Function                              | Description                                  |
|--------------------------------------------------|----------------------------------------------|
| `mediaContent: MediaContent`                     | The currently visible media item             |
| `onBack()`                                       | Navigate back to the picker screen           |
| `onToggleSelection(mediaContent: MediaContent)`  | Select or deselect the current item          |

> **Note:** `PreviewScreen` must be explicitly declared inside `ImagePickerNavHost`.  
> Omitting it while calling `onNavigateToPreviewScreen()` from a cell will cause a runtime crash.

---

## ImagePickerNavHostState

`ImagePickerNavHostState` holds picker configuration and exposes the selection result to your app.

### Parameters

| Parameter | Description                                           |
|-----------|-------------------------------------------------------|
| `max`     | Maximum number of media items that can be selected    |

### Properties

| Property                 | Type                  | Description                                |
|--------------------------|-----------------------|--------------------------------------------|
| `selectedMediaContents`  | `List<MediaContent>`  | Currently selected media items             |

```kotlin
val state = rememberImagePickerNavHostState(max = 10)

// Pass state to the picker
ImagePickerNavHost(state = state) { ... }

// Read results
val selected = state.selectedMediaContents
```
