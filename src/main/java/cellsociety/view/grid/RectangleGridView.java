package cellsociety.view.grid;

import cellsociety.controller.MainController;
import cellsociety.view.cell.CellView;
import cellsociety.view.cell.RectangleCellView;

/**
 * A rectangle grid implementation.
 *
 * @author Owen Jennings
 */
public class RectangleGridView extends GridView {

  /**
   * Create a grid view.
   *
   * @param width          :      Width of the view
   * @param height         :     Height of the view
   * @param numRows        :    Number of rows in the grid
   * @param numColumns     : Number of cells per row in the grid
   * @param mainController : The main controller of this grid
   */
  public RectangleGridView(int width, int height, int numRows, int numColumns,
      MainController mainController) {
    super(width, height, numRows, numColumns, mainController);
  }

  @Override
  protected CellView[][] initializeGrid() {
    int cellWidth = getWidth() / getNumColumns();
    int cellHeight = getHeight() / getNumRows();
    CellView[][] grid = new CellView[getNumRows()][getNumColumns()];
    for (int row = 0; row < getNumRows(); row++) {
      for (int column = 0; column < getNumColumns(); column++) {
        CellView cellView = new RectangleCellView(cellWidth * column, cellHeight * row, cellWidth,
            cellHeight);
        grid[row][column] = cellView;
      }
    }
    return grid;
  }
}
