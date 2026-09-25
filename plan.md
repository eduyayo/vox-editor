1. **Clone Support in VoxelModel**:
   Add a method `public VoxelModel clone()` to `VoxelModel` that performs a deep copy of its `voxels`, `projections`, and `referenceSystem`. Since `Vector3D` and `Projection` are records, they are immutable, so we can just copy the collections.

2. **Undo/Redo Manager**:
   Create an `UndoManager` class that manages an `undoStack` and a `redoStack`.
   Each stack stores `VoxelModel` clones.
   Methods:
   - `void recordState(VoxelModel currentState)`: pushes a clone of `currentState` to `undoStack`, clears `redoStack`.
   - `VoxelModel undo(VoxelModel currentState)`: pushes `currentState` clone to `redoStack`, pops from `undoStack`, and returns the popped model (or null if empty).
   - `VoxelModel redo(VoxelModel currentState)`: pushes `currentState` clone to `undoStack`, pops from `redoStack`, and returns it (or null if empty).

3. **Integrate into EditorFrame**:
   - Initialize `UndoManager` in `EditorFrame`.
   - Wire `undoItem`, `redoItem`, and toolbar Undo/Redo buttons to call `undo()` / `redo()` methods in `EditorFrame`.
   - When `undo()` or `redo()` is called:
     - Update the frame's `VoxelModel` with the new state (we need to be careful to pass the new model to all views, or copy the state *into* the existing `VoxelModel` to preserve references). Wait, copying state *into* the existing `VoxelModel` is much easier since `ViewPanel`, `GridPanel`, etc. already have references to `voxelModel`.
   - Actually, let's implement `VoxelModel.copyFrom(VoxelModel other)` to overwrite its state.

4. **Recording State (the tricky part)**:
   When should we record state? Every time a tool modifies the model.
   - `BrushTool`: modifies on `mouseDragged` and `mousePressed`. We shouldn't record every single dragged voxel separately. We should record on `mousePressed` (before the action starts). Wait, if we record on `mousePressed`, then undo will revert to the state before the drag started. That's perfect. But wait, `BrushTool` modifies multiple times during a drag.
   - `SquareTool`, `CircleTool`: modifies on `mouseReleased`. We should record on `mousePressed` (state before action).

   Better idea: we can add a callback or event in `EditorFrame` to notify when an action starts.
   Or we can modify `VoxelModel` to have a `beginAction()` and `endAction()`? No, tools handle mouse events.
   Let's modify `Tool` interface or the way `ToolManager` calls tools to take a snapshot before `onMousePressed`.
   Wait, if we snapshot before `onMousePressed`, what if the click doesn't modify anything? (e.g. just a click with wrong button).
   It's better to manage undo state in `ViewPanel`'s `MouseAdapter` where we can hook into `mousePressed` (record pre-action state, but only if something actually changed on `mouseReleased`... wait, this is getting complicated).

   Simple approach:
   In `ViewPanel`'s `MouseAdapter`:
   - On `mousePressed`: take a snapshot of `voxelModel` -> `snapshot = voxelModel.clone()`.
   - On `mouseReleased`: check if `voxelModel` is different from `snapshot`. If so, `undoManager.recordState(snapshot)`.
   To check if it's different, we can implement `equals()` on `VoxelModel`, or just set a `modified` flag on `VoxelModel`. Let's add a `modified` flag or change counter to `VoxelModel` which increments on `setVoxel`, `removeVoxel`, `addProjection`, `deleteProjection`.
   Or just `recordState` if any modification occurred during the drag.

   Let's add `public boolean hasChanged(VoxelModel other)` or just a `changeCount` in `VoxelModel`.

   Let's use `changeCount`.
   In `VoxelModel`: `private int changeCount = 0;` incremented on modifications.
   In `ViewPanel`'s `MouseAdapter`:
   ```java
   int initialChangeCount;
   VoxelModel snapshot;

   public void mousePressed(MouseEvent e) {
       snapshot = model.clone();
       initialChangeCount = model.getChangeCount();
       tool.onMousePressed(e, ...);
   }

   public void mouseReleased(MouseEvent e) {
       tool.onMouseReleased(e);
       if (model.getChangeCount() != initialChangeCount) {
           editorFrame.getUndoManager().pushUndo(snapshot);
       }
   }
   ```
   This requires `ViewPanel` to have access to `EditorFrame` or an `UndoManager`. We can pass `EditorFrame` or `UndoManager` to `ViewPanel` constructor.

5. **Completing EditorFrame Menu Actions**:
   - Undo/Redo shortcuts (Ctrl+Z, Ctrl+Y) are already set up.
   - Attach action listeners to buttons/menu items.

6. **Updating State**:
   ```java
   public void copyFrom(VoxelModel other) {
       this.voxels.clear();
       this.voxels.putAll(other.voxels);
       this.projections.clear();
       this.projections.addAll(other.projections);
       this.referenceSystem = other.referenceSystem;
       this.changeCount++;
   }
   ```
