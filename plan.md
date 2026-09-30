1. **Fix MoveTool Selection Box**:
   - In `MoveTool.java`, cache the `originalSelection` in `onMousePressed`.
   - In `onMouseDragged`, apply `dx, dy, dz` to the `originalSelection` coordinates, not the currently active selection, to prevent exponential runaway.

2. **Fix RotateTool Shift Snapping**:
   - The code reviewer is completely correct: if `startAngle` is not snapped, `currentAngle - startAngle` won't be a multiple of 45 degrees, which causes grid misalignment and model destruction.
   - The best way to handle Rotate snapping is for `RotateTool` itself to snap the `deltaAngle`.
   - Wait, the first code review said: "The prompt requested that the Rotate tool 'abide the Shift decorator'. The agent bypassed the decorator entirely and implemented the constraint natively inside RotateTool. While functionally achieving the 45-degree snap outcome, it missed the architectural instruction."
   - Okay, if `ShiftKeyDecorator` handles the snap by feeding constrained `MouseEvent`s into `RotateTool`, the problem is that `startAngle` in `RotateTool` was determined during `onMousePressed`, which uses the actual (unsnapped) mouse coordinates.
   - How can `ShiftKeyDecorator` make it so the `deltaAngle` is exactly a multiple of 45?
     It needs to constrain `currentAngle` so that `currentAngle - startAngle` is exactly a multiple of 45 degrees.
     Let `startAngle` be whatever it was. In `ShiftKeyDecorator.onMouseDragged`:
     `deltaAngle = actualCurrentAngle - startAngle;`
     `snappedDelta = Math.round(deltaAngle / 45deg) * 45deg;`
     `targetAngle = startAngle + snappedDelta;`
     Then constrain `newX` and `newY` to lie on the line extending from the center at `targetAngle`.
   - To do this, `ShiftKeyDecorator` needs the `startAngle`. It can compute it in `onMousePressed` for `"Rotate"`!
   - Let's update `ShiftKeyDecorator` to store `startAngle` for `"Rotate"` during `onMousePressed`.
