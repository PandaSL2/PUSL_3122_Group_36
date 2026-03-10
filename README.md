# RoomCraft Designer 2.0

**Module:** PUSL3122 – HCI, Computer Graphics & Visualisation  
**Type:** Group Assignment  
**Language:** Java (Swing) – No external dependencies  
**Backend:** Supabase (Auth + PostgreSQL)  
**GitHub:** https://github.com/PandaSL2/PUSL_3122_Group_36  
**Group Size:** 6 Members  
**Version:** 2.1 — Supabase Edition

---

## Overview

RoomCraft Designer 2.0 is a professional-grade interior design and 3D visualisation system. Users can design room layouts in 2D, visualise them in real-time 3D using a custom software rasterizer, choose from 5 room templates with pre-furnished layouts, and export their designs as high-resolution PNG floor plans or HTML bills of materials.

---

## How to Run

```bash
# 1. Clean previous build
rmdir /s /q bin
mkdir bin

# 2. Compile
javac -encoding UTF-8 -d bin -sourcepath src src/RoomCraftApp.java

# 3. Run
java -cp bin RoomCraftApp
```

**Requirements:** JDK 8 or higher.

---

## Key Features

| Feature | Description |
|---------|-------------|
| **Supabase Auth** | Remote Sign In / Sign Up with JWT tokens via Supabase REST API |
| **Design Storage** | Local serialisation + Supabase sync hook for designs |
| **Modern Sidebar** | Vertical icon navigation bar (Home, Projects, Objects, Settings) |
| Authentication | Secure login/register with session management |
| Animated Splash Screen | 3-second gradient startup screen with progress bar |
| Dark Theme UI | Premium dark gradient + glassmorphism design throughout |
| 2D Floor Plan Editor | Drag-and-drop furniture with grid snap (10cm) |
| 3D Visualisation | Real-time software rasterizer — no OpenGL |
| Ambient + Specular Lighting | Blinn-Phong shading model with directional light |
| Wood Floor Texture | Procedural alternating plank tones |
| Camera Controls | Orbit (drag), zoom (scroll), WASD walk, first-person |
| Furniture Catalog | 38 items across 6 categories with search |
| Room Templates | 5 ready-made layouts (Living Room, Bedroom, Kitchen, Office, Bathroom) |
| Color Customisation | Floor, wall, and ceiling color pickers |
| Snap & Align Guides | Blue wall-snaps, pink edge-alignment guides |
| Collision Detection | Red highlight + position revert on overlap |
| Measurement Overlay | Dimension arrows, area label, 1m tick marks |
| Compass Rose | N/S orientation indicator in 2D view |
| Rubber-Band Select | Click-drag to select multiple furniture items |
| Copy / Paste | Ctrl+C / Ctrl+V with offset |
| Undo / Redo | Full command history (50 steps) |
| Export PNG | 1600×1200 floor plan with labels and arrows |
| Export BOM Report | HTML bill of materials with color swatches |
| Status Bar | Live cursor position, zoom %, room area |
| Design Portfolio | Save, search, load and delete designs |

---

## Project Structure

```
src/
  RoomCraftApp.java          # Entry point with splash screen
  ui/
    MainFrame.java            # Main window, dark theme, sidebar, F11 fullscreen
    SplashScreen.java         # Animated startup screen
    PortfolioDashboard.java   # Dark card-based portfolio view
    RoomSetupPanel.java       # Room creation with templates & color pickers
    auth/
      LoginPanel.java         # Gradient dark login card (Supabase auth)
    components/
      ModernButton.java       # Rounded hover button
      SidebarPanel.java       # NEW: Vertical icon navigation sidebar
      StatusBar.java          # Bottom bar: cursor, zoom, room info
    editor/
      EditorPanel.java        # Main editor with dark toolbar
      Canvas2D.java           # 2D floor plan with full interaction
      Canvas3D.java           # 3D view with orbit/walk camera
      CatalogPanel.java       # Category tabs + search + tiles
      PropertiesPanel.java    # Dark-themed properties inspector
  engine3d/
    Renderer3D.java           # Software rasterizer with Blinn-Phong
    Camera.java               # Orbit + first-person camera
    Matrix4x4.java            # 4x4 matrix math
    Vector3.java              # 3D vector math
  models/
    Room.java                 # Room data (with ceilingColor, roomType)
    Furniture.java            # Furniture item (with Material enum)
    FurnitureCatalog.java     # Singleton 38-item catalog
    RoomTemplate.java         # 5 pre-defined room layouts
    User.java                 # User account model (with token field)
  controllers/
    AuthController.java       # Login/register via Supabase
    DesignController.java     # Room + command management
  utils/
    CollisionDetector.java    # Rotated bounding-box overlap check
    SnapHelper.java           # Wall and edge-alignment snapping
    ExportManager.java        # PNG + HTML BOM export
    CommandManager.java       # Undo/redo history
    FileManager.java          # Save/load designs (local + Supabase hook)
    SessionManager.java       # Current user session
    SupabaseClient.java       # NEW: Zero-dep REST client for Supabase Auth
    commands/                 # Command pattern implementations
```

---

## Group — Member Contributions

### Theekshana — UI Overhaul & Branding

**Files owned:**
| File | Contribution |
|------|-------------|
| `ui/SplashScreen.java` | Animated splash with gradient, progress bar, and logo |
| `ui/auth/LoginPanel.java` | Redesigned dark gradient login card with styled inputs |
| `ui/PortfolioDashboard.java` | Dark portfolio with hover cards, search bar, WrapLayout |
| `ui/MainFrame.java` | Dark window, F11 fullscreen, extended session start |
| `ui/components/StatusBar.java` | Bottom status bar: cursor position, zoom %, room info |

**Presentation demo:** Launch app → animated splash screen → dark login → dark dashboard with card hover effects → create design card view.

---

### Achintha — 3D Rendering Engine Upgrades

**Files owned:**
| File | Contribution |
|------|-------------|
| `engine3d/Renderer3D.java` | Blinn-Phong lighting, procedural wood floor texture, sky gradient, ceiling, furniture shadows, selection highlight glow, 10 new furniture shapes |
| `engine3d/Camera.java` | Yaw/pitch smooth orbit, first-person mode at 160 cm eye height, zoom, strafe |
| `ui/editor/Canvas3D.java` | Mouse drag orbit, scroll zoom, WASD walk, arrow-key orbit, F toggle first-person, R reset, FPS counter HUD |

**Presentation demo:** Open any room → toggle to 3D view → drag to orbit → scroll to zoom → press WASD to walk → press F for first-person → observe floor texture, ceiling, ambient lighting on furniture tops.

---

### Nilupul — Furniture System Expansion

**Files owned:**
| File | Contribution |
|------|-------------|
| `models/Furniture.java` | Extended with Material enum, 10 new types (Lamp, Plant, Wardrobe, Bathtub, Toilet, Sink, TV Stand, Curtain, Door, Window), `copy()` method |
| `models/FurnitureCatalog.java` | Singleton catalog with 38 named items across 6 categories with realistic dimensions |
| `ui/editor/CatalogPanel.java` | Sidebar with category tabs, search bar, colour-swatch tiles, one-click placement, result count |

**Presentation demo:** In editor → point to catalog sidebar → click different category tabs → type in search field → click any tile to place on canvas.

---

### Anjana — Room Templates & Multi-Room Setup

**Files owned:**
| File | Contribution |
|------|-------------|
| `models/Room.java` | Extended with `ceilingColor`, `roomType`, `name`, `getAreaM2()` |
| `models/RoomTemplate.java` | 5 full room presets: Modern Living Room, Master Bedroom, Home Office, Modern Kitchen, Modern Bathroom — each with furniture, dimensions and color schemes |
| `ui/RoomSetupPanel.java` | Template selector buttons, color pickers (floor/wall/ceiling), live room preview panel, area display, dimension validation |

**Presentation demo:** Click "+ New Design" → show 5 template buttons → click "Master Bedroom" → all fields auto-fill → adjust width → see live preview update → click "Start Designing".

---

### Isuri — Measurements, Labels & Export

**Files owned:**
| File | Contribution |
|------|-------------|
| `utils/ExportManager.java` | `exportToPNG()` — 1600×1200 hi-res floor plan with dimension arrows, furniture labels, grid; `exportBillOfMaterials()` — styled HTML table with color swatches |
| `ui/editor/Canvas2D.java` (partial) | Measurement dimension overlay with arrows and tick marks, area label, room type display, selected furniture live dimension labels |

**Presentation demo:** In editor with some furniture → click "[M] Measure" to toggle dimension arrows on 2D → click "Export PNG" → save file → open and show the high-res output → click "BOM Report" → HTML report opens in browser.

---

### Buddhini — UX Polish & Smart Interactions

**Files owned:**
| File | Contribution |
|------|-------------|
| `utils/CollisionDetector.java` | Rotated bounding-box collision detection via Java `Area`, room boundary check |
| `utils/SnapHelper.java` | Wall-snap (cyan guides) and furniture edge-align (pink/green guide lines) |
| `ui/editor/Canvas2D.java` (partial) | Rubber-band multi-select, Ctrl+C/V copy/paste, Space+drag pan, scroll-to-zoom anchored at cursor, keyboard shortcuts (Delete, Ctrl+Z/Y, Escape, M), compass rose directional indicator, hint bar, status bar wiring |

**Presentation demo:** Drag furniture near a wall → show cyan snap guide appear → drag one item onto another → show red collision highlight → release — position reverts → Ctrl+C then Ctrl+V to duplicate → rubber-band drag to select multiple → Ctrl+Z to undo.

---

## Keyboard Shortcuts (2D Editor)

| Key | Action |
|-----|--------|
| `Scroll Wheel` | Zoom in/out anchored at cursor |
| `Space + Drag` | Pan the canvas |
| `Delete / Backspace` | Remove selected furniture |
| `Ctrl + C` | Copy selected furniture |
| `Ctrl + V` | Paste furniture (offset by 20cm) |
| `Ctrl + Z` | Undo last action |
| `Ctrl + Y` | Redo last undone action |
| `Escape` | Deselect all |
| `M` | Toggle measurement overlay |
| `F11` | Toggle fullscreen |

## Keyboard Shortcuts (3D View)

| Key | Action |
|-----|--------|
| `W / S` | Walk forward / backward |
| `A / D` | Strafe left / right |
| `Arrow Keys` | Orbit camera |
| `+ / -` | Zoom in / out |
| `F` | Toggle first-person / orbit mode |
| `R` | Reset camera to default position |

---

## Supabase Setup

To activate remote authentication, run the SQL in `supabase_setup.sql` in your [Supabase SQL Editor](https://supabase.com/dashboard/project/urwhdyuugvbadojrxbdh/sql/new).

Credentials are already configured in `src/utils/SupabaseClient.java`. Users are stored securely in Supabase Auth, designs are saved locally with a Supabase sync hook ready to enable.

---

## Credits

Built using standard Java SE libraries only (java.awt, javax.swing, java.io, javax.imageio).  
Backend services via **Supabase** (PostgreSQL + Auth).  
**No additional Java dependencies required.**  
PUSL3122 — HCI, Computer Graphics & Visualisation — Group Project 2026.
