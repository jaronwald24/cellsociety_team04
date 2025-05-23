package cellsociety.model.simulation.instructions;

import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cellsociety.model.Grid;
import cellsociety.model.cell.CellUpdate;
import cellsociety.model.cell.DarwinCell;
import cellsociety.model.simulation.Instruction;

/**
 * Abstract class to define the turning instructions
 */
public abstract class TurnInstruction implements Instruction {

  private int stepSize;

  /**
   * Setter for the step size
   *
   * @param stepSize - the number of directions to look towards for each configuration
   */
  @Override
  public void setStepSize(int stepSize) {
    this.stepSize = stepSize;
  }

  /**
   * Setter for the layers
   *
   * @param layers - the number of cells to look forward
   */
  @Override
  public void setLayers(int layers) {
    // Not needed
  }

  /**
   * Method that executes the instructions
   *
   * @param darwinCell - the cell that the instruction is executed on
   * @param arguments  - the list of instructions for the given cell
   * @param grid       - the collection of cell objects
   * @return - empty List due to no CellUpdates needed
   */
  @Override
  public List<CellUpdate> executeInstruction(DarwinCell darwinCell, List<String> arguments, Grid grid, Map<Point2D, DarwinCell> occupiedCells, Set<Point2D> movingCells) {
    int degrees = Integer.parseInt(arguments.get(1));
    turn(darwinCell, degrees, stepSize);
    List<CellUpdate> updates = new ArrayList<>();
    updates.add(new CellUpdate(darwinCell.getLocation(), darwinCell));
    return updates;
  }

  /**
   * Abstract method for turning the cell.
   */
  protected abstract void turn(DarwinCell darwinCell, int degrees, int stepSize);
}