<p align="center">

# 🏠 RoomCraft Designer 2.0

<img src="https://readme-typing-svg.herokuapp.com?font=Orbitron&size=30&duration=3000&color=6A5ACD&center=true&vCenter=true&width=800&lines=Interior+Design+%2B+3D+Visualization+System;Built+with+Java+Swing;Custom+Software+Rasterizer+Engine;Supabase+Authentication+Backend" />

</p>

<p align="center">

![Java](https://img.shields.io/badge/Java-Swing-orange?style=for-the-badge\&logo=java)
![Version](https://img.shields.io/badge/Version-2.1-blue?style=for-the-badge)
![Backend](https://img.shields.io/badge/Backend-Supabase-3ECF8E?style=for-the-badge\&logo=supabase)
![Build](https://img.shields.io/badge/Build-Stable-brightgreen?style=for-the-badge)
![License](https://img.shields.io/badge/License-Academic-red?style=for-the-badge)

</p>

---

# ✨ Project Overview

RoomCraft Designer 2.0 is a **professional interior design and 3D visualisation system** built using **Java Swing** and a **custom 3D software rasterization engine**.

Users can design room layouts in **2D**, preview them in **real-time 3D**, and export their projects as **high-resolution floor plans and bills of materials**.

---

# 🚀 Key Features

🧩 **2D Floor Plan Editor**
Drag-and-drop furniture with smart snapping and collision detection.

🌐 **Real-Time 3D Visualisation**
Custom Java software rasterizer with **Blinn-Phong lighting**.

🪑 **Furniture Catalog**
38 furniture items across multiple categories.

🏠 **Room Templates**
5 fully designed room layouts ready to use.

📐 **Measurement System**
Dimension arrows, area labels, and alignment guides.

📤 **Export Tools**
Export designs as **PNG floor plans** or **HTML Bill of Materials**.

🔐 **Authentication System**
Secure login/register using **Supabase Auth API**.

---

# ⚙️ How to Run

```bash
# Clean previous build
rmdir /s /q bin
mkdir bin

# Compile project
javac -encoding UTF-8 -d bin -sourcepath src src/RoomCraftApp.java

# Run application
java -cp bin RoomCraftApp
```

### Requirements

✔ JDK 8 or higher
✔ No external libraries required
✔ Works on Windows / Linux / macOS

---

# 🧰 Core System Modules

| Module           | Description                                            |
| ---------------- | ------------------------------------------------------ |
| UI System        | Dark themed Swing interface with sidebar navigation    |
| 2D Editor        | Interactive floor-plan editor with furniture placement |
| 3D Renderer      | Custom software rasterizer with lighting               |
| Furniture Engine | Catalog system with materials and categories           |
| Export Manager   | PNG floor plan + HTML report export                    |
| Supabase Client  | Remote authentication via REST API                     |

---

# 🏡 Room Templates

| Template          | Description                  |
| ----------------- | ---------------------------- |
| 🛋 Living Room    | Modern sofa layout           |
| 🛏 Master Bedroom | Bed + wardrobe design        |
| 💻 Home Office    | Desk workspace setup         |
| 🍳 Kitchen        | Island kitchen layout        |
| 🛁 Bathroom       | Bathtub and sink arrangement |

Each template includes:

✔ Furniture placement
✔ Color scheme
✔ Room dimensions

---

# ⌨️ Keyboard Shortcuts

### 2D Editor

| Key          | Action               |
| ------------ | -------------------- |
| Scroll Wheel | Zoom in / out        |
| Space + Drag | Pan canvas           |
| Ctrl + C     | Copy furniture       |
| Ctrl + V     | Paste furniture      |
| Ctrl + Z     | Undo action          |
| Ctrl + Y     | Redo action          |
| Delete       | Remove selected item |
| M            | Toggle measurements  |

---

### 3D Mode

| Key        | Action                   |
| ---------- | ------------------------ |
| W / S      | Walk forward / backward  |
| A / D      | Strafe left / right      |
| Arrow Keys | Orbit camera             |
| F          | Toggle first-person mode |
| R          | Reset camera             |

---

# 🏗 Project Structure

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

| Technology           | Purpose                |
| -------------------- | ---------------------- |
| ☕ Java Swing         | UI framework           |
| 🎨 Java2D            | Rendering engine       |
| 🌐 Supabase          | Authentication backend |
| 🗄 PostgreSQL        | Data storage           |
| 🧮 Custom Rasterizer | 3D graphics engine     |

---

# 👥 Team Contributions

| Member                         | Role                    |
| ------------------------------ | ----------------------- |
| 👨‍💻 Kanewalage Siriwardhana  | UI Overhaul & Branding  |
| 🎮 Wanigathungage Gimhana      | 3D Rendering Engine     |
| 🪑 Kumarapeli Induranga        | Furniture System        |
| 🏠 Semasinghe Semasinghe       | Room Templates          |
| 📏 Udawaththa Wijegunawardhana | Measurements & Export   |
| ⚙ Karunanayaka Wijekoon        | UX & Smart Interactions |

---

# ⭐ Highlights

✔ 38 furniture objects
✔ 5 room templates
✔ 2D + 3D visualisation
✔ Supabase authentication
✔ PNG & HTML export
✔ Real-time rendering engine

---

# 📜 License

This project is part of the **PUSL3122 academic coursework**.

© 2026 — RoomCraft Designer
All rights reserved.

Unauthorized redistribution, reproduction, or commercial use of this project is prohibited.
