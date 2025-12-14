# ImagenZ (JavaFX)

ImagenZ is a small JavaFX desktop tool that helps you generate **number-picture cards** and save them as **PNG captures**.  
You pick an image (or two images), choose the number(s), and the app draws a clean grid of repeated icons. Then you can export the result as an image file.

---

## Features

- **Single mode**: generate one grid (e.g., 7 apples).
- **Multiple mode**: generate two grids side-by-side (e.g., 3 cats + 5 dogs).
- **Image library with categories** (Animals, Fruits, …) and thumbnails.
- **Auto-resizing canvas** based on the drawn content.
- **Capture/export to PNG** with different file naming strategies:
  - ascending numbering
  - random naming
  - content-based naming (includes numbers + image names)
- **Batch generation** (“Programme”) to create multiple captures automatically (ranges, random numbers/images, etc.).
- **Open output folder** quickly from the app.

---

## Where files are stored

### Captures (exported PNG)
Saved in:
- `~/Pictures/ImagenZ/`

### Image library (user images + default images)
On Windows, the app uses:
- `%LOCALAPPDATA%/ImagenZ/images/`

At first run, the app creates the folder structure and copies **default images** (and initializes default categories).

---

## Requirements

- **Java 17+** (recommended for JavaFX projects)
- **JavaFX** (SDK or bundled via your build tool)
- Dependency:
  - **Gson** (used to store/load categories data)

---

## How to run (general)

1. Make sure JavaFX is available (SDK or dependency).
2. Ensure resources are on the classpath (FXML, icons, sounds, default images).
3. Run the JavaFX entry point:

- `Main.java` (launches `MainLayout.fxml`)

If you run from command line, you typically need something like:

```bash
java --module-path /path/to/javafx/lib --add-modules javafx.controls,javafx.fxml,javafx.media -cp "out;libs/*" Main
