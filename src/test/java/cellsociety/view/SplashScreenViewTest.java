package cellsociety.view;

import cellsociety.controller.MainController;
import cellsociety.utility.CreateNewSimulation;
import javafx.stage.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import util.DukeApplicationTest;
import util.TestUtils;

import static cellsociety.config.MainConfig.getMessage;
import static org.mockito.Mockito.*;
import static org.testfx.util.WaitForAsyncUtils.waitForFxEvents;

@ExtendWith(MockitoExtension.class)
public class SplashScreenViewTest extends DukeApplicationTest {

  // I used ChatGPT for assistance in overriding the start method to get the view to show in TestFX and in refactoring my code into helper methods
  private MainController myMainController;
  private TestUtils myTestUtils;

  @Override
  public void start(Stage stage) {
    myTestUtils = new TestUtils();
    CreateNewSimulation createNewSimulation = new CreateNewSimulation(stage);
    myMainController = Mockito.spy(createNewSimulation.launchNewSimulation());
  }

  @BeforeEach
  public void setup() {
    reset(myMainController);
  }

  @Test
  void languageDropdown_TestSpanishChange_ExpectSpanishText() {
    verifyLanguageChange("Spanish");
  }

  @Test
  void languageDropdown_TestEnglishChange_ExpectEnglishText() {
    verifyLanguageChange("English");
  }

  @Test
  void languageDropdown_TestPigLatinChange_ExpectPigLatinText() {
    verifyLanguageChange("PigLatin");
  }

  /**
   * Helper method to test language changes and verify UI text updates
   *
   * @param language the language to select from the dropdown
   */
  private void verifyLanguageChange(String language) {
    clickOn("#languageDropdown");
    clickOn(language);
    waitForFxEvents();  // Ensure that all UI events and updates are processed before assertions

    // Verify text elements and buttons change on language change
    myTestUtils.verifyText("#splashScreenTitle", getMessage("SPLASH_HEADER"));
    myTestUtils.verifyText("#splashDescription", getMessage("SPLASH_DESCRIPTION"));
    myTestUtils.verifyText("#splashInstructions", getMessage("SPLASH_INSTRUCTIONS"));
    myTestUtils.verifyButtonText("#splashChooseFileButton", "CHOOSE_FILE_BUTTON");
    myTestUtils.verifyButtonText("#createSimulationButton", "CREATE_NEW_GRID_HEADER");
  }
}
