package cellsociety;

import cellsociety.utility.CreateNewSimulation;
import javafx.application.Application;
import javafx.stage.Stage;

/**
 * Main file for application.
 *
 * @author Owen Jennings
 * @author Justin Aronwald
 */
public class Main extends Application {

    /**
   * Required entry point for launching JavaFX from environments like VSCode.
   */
  public static void main(String[] args) {
    launch(args);  // Calls start(Stage stage)
  }

  /**
   * Initialize what will be displayed.
   */
  @Override
  public void start(Stage stage) {
    CreateNewSimulation simulationManager = new CreateNewSimulation();
    simulationManager.launchNewSimulation();
  }

}
