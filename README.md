
# 🏠 RoomCraft Designer 2.0

<p align="center">

<img src="https://media.giphy.com/media/l0MYt5jPR6QX5pnqM/giphy.gif" width="650">

</p>

### 🎓 Module

**PUSL3122 – HCI, Computer Graphics & Visualisation**

### 👥 Group Assignment

### 💻 Language

Java (Swing)

### 🧠 Backend

Supabase (Auth + PostgreSQL)

### 🔗 Repository

[https://github.com/PandaSL2/PUSL_3122_Group_36](https://github.com/PandaSL2/PUSL_3122_Group_36)

---

# 🚀 Project Overview

RoomCraft Designer 2.0 is a **professional interior design and 3D visualisation system** built entirely with **Java Swing and a custom 3D rasterization engine**.

Users can design rooms in **2D**, visualize them instantly in **real-time 3D**, and export professional **floor plans and bills of materials**.

---

# ✨ Live System Preview

<p align="center">

<img src="https://media.giphy.com/media/3o7aD2saalBwwftBIY/giphy.gif" width="700">

</p>

---

# ⚙️ How to Run

```bash
# Clean previous build
rmdir /s /q bin
mkdir bin

# Compile
javac -encoding UTF-8 -d bin -sourcepath src src/RoomCraftApp.java

# Run
java -cp bin RoomCraftApp
```

### Requirements

✔ JDK 8 or Higher
✔ Windows / Linux / macOS
✔ No external libraries required

---

# 🧩 Core Features

## 🔐 Authentication

* Supabase secure login / signup
* JWT session handling
* Remote user storage

## 🖥️ Modern Interface

<img src="https://media.giphy.com/media/xT9IgzoKnwFNmISR8I/giphy.gif" width="650">

* Dark glassmorphism UI
* Animated splash screen
* Sidebar navigation
* Portfolio dashboard
* Responsive panels

---

## 🏗️ 2D Floor Plan Editor

<img src="https://media.giphy.com/media/26BRv0ThflsHCqDrG/giphy.gif" width="650">

Features:

* Drag & drop furniture
* Grid snap (10cm)
* Multi-select furniture
* Copy / Paste objects
* Undo / Redo history
* Smart alignment guides
* Collision detection

---

## 🌐 Real-Time 3D Visualisation

<img src="https://media.giphy.com/media/3o6Zt481isNVuQI1l6/giphy.gif" width="650">

Custom **software 3D engine**

* Blinn-Phong lighting
* Procedural wood floor
* Furniture shading
* Ceiling & sky gradient
* First-person walk mode
* Orbit camera system

---

# 🧰 Furniture System

📦 **38 Furniture Items**

Categories:

* Living Room
* Bedroom
* Kitchen
* Bathroom
* Office
* Decorations

Each furniture item contains:

* realistic dimensions
* material type
* rotation support
* collision detection

---

# 🏡 Room Templates

5 Pre-built layouts:

| Template       | Description                   |
| -------------- | ----------------------------- |
| Living Room    | Modern sofa layout            |
| Master Bedroom | Bed + wardrobe setup          |
| Kitchen        | Compact kitchen island layout |
| Home Office    | Desk workspace                |
| Bathroom       | Bathtub and sink layout       |

Each template automatically fills:

* Room size
* Color scheme
* Furniture layout

---

# 📏 Measurements & Export

### PNG Floor Plan Export

<img src="https://media.giphy.com/media/3o7aD6Gx6fXyKk0G2k/giphy.gif" width="650">

Exports:

* 1600×1200 resolution
* Dimension arrows
* Furniture labels
* Grid overlay

---

### Bill of Materials (HTML)

Example output:

| Item  | Quantity | Material |
| ----- | -------- | -------- |
| Sofa  | 1        | Fabric   |
| Table | 1        | Wood     |
| Lamp  | 2        | Metal    |

---

# ⌨️ Keyboard Shortcuts

### 2D Editor

| Key          | Action              |
| ------------ | ------------------- |
| Scroll       | Zoom                |
| Space + Drag | Pan                 |
| Ctrl + C     | Copy                |
| Ctrl + V     | Paste               |
| Ctrl + Z     | Undo                |
| Ctrl + Y     | Redo                |
| Delete       | Remove object       |
| M            | Toggle measurements |

---

### 3D Mode

| Key        | Action              |
| ---------- | ------------------- |
| W / S      | Walk forward/back   |
| A / D      | Strafe              |
| Arrow Keys | Orbit camera        |
| F          | First-person toggle |
| R          | Reset camera        |

---

# 🏗️ Project Structure

```
src/

RoomCraftApp.java

ui/
 MainFrame.java
 SplashScreen.java
 PortfolioDashboard.java
 RoomSetupPanel.java

ui/auth/
 LoginPanel.java

ui/components/
 ModernButton.java
 SidebarPanel.java
 StatusBar.java

ui/editor/
 EditorPanel.java
 Canvas2D.java
 Canvas3D.java
 CatalogPanel.java
 PropertiesPanel.java

engine3d/
 Renderer3D.java
 Camera.java
 Matrix4x4.java
 Vector3.java

models/
 Room.java
 Furniture.java
 FurnitureCatalog.java
 RoomTemplate.java
 User.java

controllers/
 AuthController.java
 DesignController.java

utils/
 CollisionDetector.java
 SnapHelper.java
 ExportManager.java
 CommandManager.java
 FileManager.java
 SessionManager.java
 SupabaseClient.java
```

---

# 🧠 Technologies Used

| Technology        | Purpose         |
| ----------------- | --------------- |
| Java Swing        | UI Framework    |
| Java2D            | Rendering       |
| Custom Rasterizer | 3D graphics     |
| Supabase          | Authentication  |
| PostgreSQL        | Backend storage |

---

# 👥 Team Contributions

| Member                      | Role                    |
| --------------------------- | ----------------------- |
| Kanewalage Siriwardhana     | UI Design & Branding    |
| Wanigathungage Gimhana      | 3D Rendering Engine     |
| Kumarapeli Induranga        | Furniture System        |
| Semasinghe Semasinghe       | Room Templates          |
| Udawaththa Wijegunawardhana | Measurements & Export   |
| Karunanayaka Wijekoon       | UX & Smart Interactions |

---

# ⭐ Highlights

✔ 38 furniture objects
✔ 5 room templates
✔ 2D + 3D visualisation
✔ Supabase authentication
✔ PNG & HTML export
✔ Real-time interaction

---

# 📜 License

This project is part of the **PUSL3122 academic coursework**.

Copyright © 2026
All rights reserved.

Unauthorized redistribution, reproduction, or commercial use of this code is prohibited.

---
