package cellsociety.model.simulation.rules;

import cellsociety.model.simulation.GetNeighbors;
import cellsociety.model.simulation.InvalidParameterException;
import cellsociety.model.simulation.Parameter;
import cellsociety.model.simulation.SimulationRules;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;

import cellsociety.model.Grid;
import cellsociety.model.cell.Cell;
import cellsociety.model.cell.CellUpdate;
import cellsociety.model.cell.WaTorWorldCell;
import java.util.Set;

/**
 * The rules implementation for simulation WaTor World. Handles fish movement, shark movement,
 * reproduction, and energy loss.
 *
 * @author Owen Jennings
 * @author Justin Aronwald
 */
public class WaTorWorldRules extends SimulationRules {

  public static final String SHARK_ENERGY_GAIN = "sharkEnergyGain";
  public static final String SHARK_REPRODUCTION_TIME = "sharkReproductionTime";
  public static final String FISH_REPRODUCTION_TIME = "fishReproductionTime";
  private final Random random = new Random();
  private final int mySharkEnergyGain;
  private final int mySharkReproductionTime;
  private final int myFishReproductionTime;

  /**
   * The default constructor of a WaTorWorld rules.
   *
   * @param parameters The required parameters map
   * @throws InvalidParameterException This is thrown for invalid parameters provided.
   */
  public WaTorWorldRules(Map<String, Parameter<?>> parameters, GetNeighbors myGetNeighbors)
      throws InvalidParameterException {
    super(parameters, myGetNeighbors);
    if (parameters == null || parameters.isEmpty()) {
      setParameters(setDefaultParameters());
    }
    checkMissingParameterAndThrowException(SHARK_ENERGY_GAIN);
    checkMissingParameterAndThrowException(SHARK_REPRODUCTION_TIME);
    checkMissingParameterAndThrowException(FISH_REPRODUCTION_TIME);
    mySharkEnergyGain = getParameters().get(SHARK_ENERGY_GAIN).getInteger();
    mySharkReproductionTime = getParameters().get(SHARK_REPRODUCTION_TIME).getInteger();
    myFishReproductionTime = getParameters().get(FISH_REPRODUCTION_TIME).getInteger();
    validateParameterRange();
  }

  private void validateParameterRange() throws InvalidParameterException {
    if (mySharkEnergyGain < 0) {
      throwInvalidParameterException(SHARK_ENERGY_GAIN);
    }
    if (mySharkReproductionTime < 0) {
      throwInvalidParameterException(SHARK_REPRODUCTION_TIME);
    }
    if (myFishReproductionTime < 0) {
      throwInvalidParameterException(FISH_REPRODUCTION_TIME);
    }
  }

  /**
   * Get a list of all required parameters for a simulation.
   *
   * @return A list of strings representing the required parameter keys for this simulation
   */
  public static List<String> getRequiredParameters() {
    return List.of(SHARK_REPRODUCTION_TIME, SHARK_ENERGY_GAIN, FISH_REPRODUCTION_TIME);
  }

  private Map<String, Parameter<?>> setDefaultParameters() {
    Map<String, Parameter<?>> parameters = new HashMap<>();

    parameters.put(FISH_REPRODUCTION_TIME, new Parameter<>(3));
    parameters.put(SHARK_REPRODUCTION_TIME, new Parameter<>(4));
    parameters.put(SHARK_ENERGY_GAIN, new Parameter<>(1));

    return parameters;
  }

  /**
   * An enum to store the state of the simulation I asked ChatGPT for help with implementing this
   * enum.
   */
  public enum State {
    EMPTY, FISH, SHARK;

    /**
     * Get the ordinal value of the state.
     *
     * @return An int representing the state
     */
    public int getValue() {
      return ordinal();
    }
  }

  @Override
  public int getNumberStates() {
    return 3;
  }


  /**
   * Get the next states for all cells in the grid based on WaTor World rules.
   *
   * @param grid - the grid object containing the cell objects
   * @return - A list containing the updates that will happen to cells
   */
  // I asked ChatGPT for assistance with implementing the state transitions in this method
  @Override
  public List<CellUpdate> getNextStatesForAllCells(Grid grid) {
    List<CellUpdate> nextStates = new ArrayList<>();
    Set<Point2D> updatedCells = new HashSet<>();

    List<Cell> sharkCells = new ArrayList<>();
    List<Cell> fishCells = new ArrayList<>();

    Iterator<Cell> cellIterator = grid.getCellIterator();
    while (cellIterator.hasNext()) {
      Cell cell = cellIterator.next();
      if (cell.getState() == State.SHARK.getValue()) {
        sharkCells.add(cell);
      } else if (cell.getState() == State.FISH.getValue()) {
        fishCells.add(cell);
      }
      // if a cell is neither a shark nor fish, then it must be empty, and we do not need to update
    }

    //process sharks first
    for (Cell shark : sharkCells) {
      processSharkMovement(shark, grid, nextStates, updatedCells, fishCells);
    }

    for (Cell fish : fishCells) {
      processFishMovement(fish, grid, nextStates, updatedCells);
    }

    return nextStates;
  }

  private void processSharkMovement(Cell cell, Grid grid, List<CellUpdate> nextStates,
      Set<Point2D> updatedCells, List<Cell> fishCells) {
    WaTorWorldCell shark = (WaTorWorldCell) cell;
    int health = shark.getHealth() - 1; // decrease health by 1

    if (health <= 0) {
      Cell newEmpty = new WaTorWorldCell(State.EMPTY.getValue(), shark.getLocation());
      nextStates.add(new CellUpdate(shark.getLocation(), newEmpty));
      updatedCells.add(shark.getLocation());
      return;
    }

    boolean shouldReproduce =
        shark.getReproductionEnergy() >= mySharkReproductionTime;
    List<Cell> fishNeighbors = getNeighborsByState(shark, grid, State.FISH.getValue());

    if (!fishNeighbors.isEmpty()) {
      Cell fishCell = fishNeighbors.get(random.nextInt(fishNeighbors.size()));
      if (!updatedCells.contains(fishCell.getLocation())) {
        fishCells.remove(fishCell);
        health += mySharkEnergyGain;
        checkReproductionAndMoveOutSharkCell(nextStates, shark, health, shouldReproduce, fishCell,
            updatedCells);
        return;
      }
    }

    moveSharkToNewLocation(grid, nextStates, updatedCells, shark, health, shouldReproduce);
  }

  private void moveSharkToNewLocation(Grid grid, List<CellUpdate> nextStates,
      Set<Point2D> updatedCells,
      WaTorWorldCell shark, int health, boolean shouldReproduce) {
    List<Cell> emptyNeighbors = getNeighborsByState(shark, grid, State.EMPTY.getValue());
    if (!emptyNeighbors.isEmpty()) {
      Cell newLocation = emptyNeighbors.get(random.nextInt(emptyNeighbors.size()));
      if (!updatedCells.contains(newLocation.getLocation())) {
        checkReproductionAndMoveOutSharkCell(nextStates, shark, health, shouldReproduce,
            newLocation,
            updatedCells);
      }
    }
  }

  private void checkReproductionAndMoveOutSharkCell(List<CellUpdate> nextStates,
      WaTorWorldCell shark, int health,
      boolean shouldReproduce, Cell newLocation, Set<Point2D> updatedCells) {
    if (!updatedCells.contains(newLocation.getLocation())) {
      // update grid with a shark in a new location that retains the original shark's health and
      // reproductive energy + 1 when it moves
      Cell sharkNewPosition = new WaTorWorldCell(State.SHARK.getValue(), newLocation.getLocation(),
          health, shark.getReproductionEnergy() + 1);
      nextStates.add(new CellUpdate(newLocation.getLocation(), sharkNewPosition));
      updatedCells.add(newLocation.getLocation());
    }

    if (shouldReproduce) {
      // create new shark/offspring at original location of shark
      Cell newShark = new WaTorWorldCell(State.SHARK.getValue(), shark.getLocation(),
          WaTorWorldCell.DEFAULT_HEALTH, 0);
      nextStates.add(new CellUpdate(newShark.getLocation(), newShark));
      // reset reproductive energy of parent
      Cell parentResetReproductive = new WaTorWorldCell(State.SHARK.getValue(),
          newLocation.getLocation(), health, 0);
      nextStates.add(new CellUpdate(newLocation.getLocation(), parentResetReproductive));
      updatedCells.add(shark.getLocation());

    } else if (!newLocation.getLocation().equals(shark.getLocation())) {
      Cell newEmpty = new WaTorWorldCell(State.EMPTY.getValue(), shark.getLocation());
      nextStates.add(new CellUpdate(shark.getLocation(), newEmpty)); // moves out
      updatedCells.add(shark.getLocation());
    }
  }

  private void processFishMovement(Cell cell, Grid grid, List<CellUpdate> nextStates,
      Set<Point2D> updatedCells) {
    WaTorWorldCell fish = (WaTorWorldCell) cell;
    List<Cell> emptyNeighbors = getNeighborsByState(fish, grid, State.EMPTY.getValue());

    if (!emptyNeighbors.isEmpty()) {
      Cell newLocation = emptyNeighbors.get(random.nextInt(emptyNeighbors.size()));

      boolean shouldReproduce =
          fish.getReproductionEnergy() >= myFishReproductionTime;

      moveFishToNewLocation(nextStates, updatedCells, newLocation, fish);

      handleFishReproduction(nextStates, updatedCells, shouldReproduce, newLocation, fish);

    }
  }

  private static void handleFishReproduction(List<CellUpdate> nextStates, Set<Point2D> updatedCells,
      boolean shouldReproduce, Cell newLocation, WaTorWorldCell fish) {
    if (shouldReproduce) {
      Cell offspringFish = new WaTorWorldCell(State.FISH.getValue(), newLocation.getLocation(),
          fish.getHealth(), 0);
      nextStates.add(new CellUpdate(fish.getLocation(), offspringFish)); // Offspring
      Cell parentFishResetReproductive = new WaTorWorldCell(State.FISH.getValue(),
          newLocation.getLocation(), fish.getHealth(), 0);
      nextStates.add(new CellUpdate(newLocation.getLocation(), parentFishResetReproductive));
      updatedCells.add(fish.getLocation());
    } else if (!updatedCells.contains(fish.getLocation())) {
      Cell newEmpty = new WaTorWorldCell(State.EMPTY.getValue(), fish.getLocation());
      nextStates.add(new CellUpdate(fish.getLocation(), newEmpty)); // Move out
      updatedCells.add(fish.getLocation());
    }
  }

  private static void moveFishToNewLocation(List<CellUpdate> nextStates, Set<Point2D> updatedCells,
      Cell newLocation, WaTorWorldCell fish) {
    if (!updatedCells.contains(newLocation.getLocation())) {
      // set new cell to have original cells reproductive energy + 1 and new location
      Cell newFishLocation = new WaTorWorldCell(State.FISH.getValue(), newLocation.getLocation(),
          WaTorWorldCell.DEFAULT_HEALTH, fish.getReproductionEnergy() + 1);
      nextStates.add(
          new CellUpdate(newLocation.getLocation(), newFishLocation)); // Move in
      updatedCells.add(newLocation.getLocation());
    }
  }

  private List<Cell> getNeighborsByState(Cell cell, Grid grid, int state) {
    List<Cell> allNeighbors = getNeighbors(cell, grid);
    List<Cell> neighborsByState = new ArrayList<>();

    for (Cell neighbor : allNeighbors) {
      if (neighbor.getState() == state) {
        neighborsByState.add(neighbor);
      }
    }
    return neighborsByState;
  }
}