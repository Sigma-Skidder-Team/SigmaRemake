# Mappings

https://docs.google.com/spreadsheets/d/1ev4KcR4kmLTDjEDhMO7pITQBLQaDgVToVHkVQLZVafw/edit?usp=sharing

## GUI Package Refactoring Recommendations

This section outlines recommendations for restructuring the GUI codebase to improve clarity, maintainability, and scalability.

### Analysis of Current Structure

The current `gui` package structure exhibits several areas for improvement:

1.  **Mixed Organization:** Packages are organized by a blend of technical layers (e.g., `widget`, `panel`, `interfaces`) and application-specific features (e.g., `widget/impl/alts`, `widget/impl/cgui`). This hybrid approach can hinder code discoverability and modularity.
2.  **Confusing Hierarchy:** The inheritance chain, where an `Element` extends an `AnimatedIconPanel`, is conceptually muddled. Typically, a more fundamental "widget" or "widget" forms the base, with "panels" (which imply containers) built upon them.
3.  **Ambiguous Naming:** The use of `CustomGuiScreen` as the universal base class for all UI components, irrespective of whether they represent a full screen or a smaller widget, is imprecise.
4.  **Inconsistent Screen Locations:** Top-level screen implementations are inconsistently placed across `gui/impl` (e.g., `JelloMenu`) and `gui/screen` (e.g., `AltManagerScreen`).

### Recommendations for Refactoring

The core recommendation is to adopt a "package-by-feature" structure, clearly differentiating the reusable **GUI framework** components from the **application's specific UI screens**.

#### **Part 1: Renaming for Clarity**

More descriptive naming conventions will enhance code comprehension.

*   **`CustomGuiScreen` → `GuiComponent`**: This class serves as the fundamental base for all UI objects. `GuiComponent` is a more accurate and standard identifier for such a core abstraction.
*   **`AnimatedIconPanel` → `Widget`**: This class functions as a general base for many visual components, despite its specific name. `Widget` is a more fitting and generic term for a reusable UI widget.
*   **`Element` → `InteractiveWidget`**: As this class extends `AnimatedIconPanel` and primarily introduces interaction handling (like clicks), `InteractiveWidget` better conveys its purpose as a user-interactable `Widget`.
*   **Remove `I` Prefix from Interfaces**: Adhering to modern Java conventions, interface names should omit the `I` prefix (e.g., `IWidthSetter` → `WidthSetter`, `IGuiEventListener` → `GuiEventListener`).

#### **Part 2: New Package Structure**

The proposed structure organizes files logically by their role within the framework and by their feature within the application.

**1. GUI Framework Packages (`io.github.sst.remake.gui.framework`)**
This new root package will encapsulate all the core, reusable components of the GUI system.

*   **`io.github.sst.remake.gui.framework.core`**:
    *   `GuiComponent.java` (was `CustomGuiScreen`)
    *   `Screen.java` (base for full-window GUIs)
    *   `Widget.java` (was `AnimatedIconPanel`)

*   **`io.github.sst.remake.gui.framework.widget`**: Houses general-purpose, reusable UI widgets.
    *   `Button.java`, `Checkbox.java`, `TextField.java`, `ScrollablePanel.java` (was `ScrollableContentPanel`), etc.
    *   Internal helper classes (e.g., `VerticalScrollBarButton`) can be made inner classes or placed in a dedicated sub-package (e.g., `...widget.internal`).

*   **`io.github.sst.remake.gui.framework.layout`**: Contains classes related to UI layout and positioning.
    *   `ContentSize.java`, `WidthSetter.java`, `GridLayoutVisitor.java`.

*   **`io.github.sst.remake.gui.framework.event`**: Stores all event listener interfaces and related event classes.
    *   `GuiEventListener.java`, `DragListener.java`, etc.

**2. Application UI Packages (`io.github.sst.remake.gui.screen`)**
This package will serve as the root for all application-specific UI screens, with sub-packages for each major feature or module.

*   **`io.github.sst.remake.gui.screen.holder/`**: This package is well-named and its purpose is clear; it should remain as is.

*   **`io.github.sst.remake.gui.screen.mainmenu/`**:
    *   `JelloMenuScreen.java` (was `JelloMenu`)
    *   `MainPage.java`, `ChangelogPage.java`
    *   *Menu-specific components:* `FloatingBubble.java`, `RoundButton.java`

*   **`io.github.sst.remake.gui.screen.altmanager/`**:
    *   `AltManagerScreen.java`
    *   *Alt-manager-specific components:* `AccountUI.java`, `AccountElement.java`, `BanElement.java`

*   **`io.github.sst.remake.gui.screen.clickgui/`**:
    *   `JelloClickGuiScreen.java` (was `JelloScreen`)
    *   *ClickGUI-specific components:* `CategoryPanel.java`, `ModListView.java`, `SettingGroup.java`

Similar feature-specific packages would be created for other application screens, such as `options`, `keybinds`, and `maps`.

### Summary of Benefits

Adopting these recommendations would yield significant improvements:

*   **Clarity:** A clear distinction between core framework components and application-specific UI elements will be established.
*   **Maintainability:** All code pertaining to a particular UI feature (e.g., the alt manager) will be co-located, simplifying development and debugging.
*   **Scalability:** The modular structure facilitates the addition of new screens and features by simply creating new feature packages under `io.github.sst.remake.gui.screen`.
*   **Readability:** Improved naming conventions and logical grouping of classes will enhance overall code readability and understanding.