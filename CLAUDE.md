# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

ImagePicker is an Android Jetpack Compose library (not an app) providing a DSL-based, fully customizable image/gallery picker. Published to JitPack, version 1.0.15, targeting API 21+.

## Build Commands

```bash
./gradlew build                       # Build all modules
./gradlew :ImagePicker:assemble       # Assemble the library AAR
./gradlew :app:assembleDebug          # Build the demo app
./gradlew :ImagePicker:testDebugUnitTest    # Run unit tests
./gradlew :ImagePicker:connectedDebugAndroidTest  # Run instrumented tests
```

Two modules:
- `:ImagePicker` — the library
- `:app` — demo/example application

## Architecture

### Core Design: DSL Navigation + Scoped Slot APIs

The library mirrors Jetpack Navigation's API. Callers declare screens inside `ImagePickerNavHost` using a builder DSL. Each screen slot receives a typed scope that exposes only the data and callbacks relevant to that screen.

```
ImagePickerNavHost (entry point)
  └── ImagePickerGraphBuilder DSL
        ├── pickerScreen { scope -> ... }   // ImagePickerScaffold
        └── previewScreen { scope -> ... }  // PreviewScaffold
```

### Layer Breakdown

**UI Layer** (`ui/`)
- `ImagePickerNavHost` — composable entry point; sets up navigation and injects ViewModel
- `ImagePickerScaffold` — main picker screen with album dropdown, preview bar, and image grid
- `PreviewScaffold` — full-screen swipeable media preview
- `ImageCell` — reusable thumbnail using Coil

**State Management**
- `ImagePickerViewModel` — central state: selected URIs (`LinkedHashSet<OrderedUri>` preserving insertion order), albums, pagination, camera capture
- `ImagePickerNavHostState` — caller-facing configuration (`max` selection count, `autoSelectAfterCapture`) and exposes `selectedMediaContents`

**Scope Interfaces** (`ui/scope/`) — the slot API contracts:
- `ImagePickerAlbumScope` — album list + selected album + click handler
- `ImagePickerPreviewScope` — selected media + deselect handler
- `ImagePickerCellScope` — single media item + navigate-to-preview callback
- `PreviewScreenScope` — media item + back + toggle selection

**Data Layer**
- `LocalMediaContentsDataSource` — wraps MediaStore queries, returns Paging 3 Flow
- `ImagePickerPagingDataSource` — custom `PagingSource`, 30 items per page
- `API21MediaContentManager` / `API29MediaContentManager` — platform-specific `ContentResolver` query strategies (strategy pattern for API 21–28 vs 29+)
- `FileManager` — camera temp file creation and EXIF rotation correction

**Gestures** (`extensions/ModifierExtensions.kt`)
- `photoGridDragHandler` — long-press to start drag, auto-scroll at viewport edges, haptic feedback, batch selection

### Key Technical Decisions

- Selection order is preserved using `LinkedHashSet<OrderedUri>` (insert order = display order number)
- Paging data is combined with selection state via `combine`/`flatMapLatest` in the ViewModel
- Two MediaContentManager implementations share the abstract `MediaContentManager` base — add new API-level variants there
- Navigation uses single-top launch mode with back stack save/restore
- Coil image loading uses write-only memory cache + read-only disk cache

### Dependencies

- Paging 3 (`paging-compose:3.2.1`)
- Navigation Compose (`navigation-compose:2.8.4`)
- Coil (`coil-compose:2.5.0`)
- ExifInterface (`exifinterface:1.3.6`)
- Compose Material3, ViewModel Compose
