package Control;

import Data.DataBase;
import Gui.GUI;
import Gui.GameWindow;
import Gui.MenuWindow;

import javax.swing.*;
import java.sql.SQLException;
import java.util.List;

public class Control
{
    private GUI gui;
    private StartGenerator startGenerator;
    private DataBase db;

    private boolean[][] grid;
    private boolean run = false;

    private boolean[][] originalGrid;
    private int generation = 0;

    public Control()
    {
        db = new DataBase();
        gui = new GUI(this);
        startGenerator = new StartGenerator();
    }

    public void openMenu()
    {
        run = false;
        gui.openMenuWindowExclusive();
    }

    //start the game with grid size, and or start arguments
    public void startGame(Vector2 gridSize, String startArgs)
    {
        //the game shouldnt autorun
        run = false;
        //create a new grid
        grid = new boolean[gridSize.x][gridSize.y];

        //get the startgrid from the startgenerator
        grid = startGenerator.generateStartGeneration(startArgs, grid);

        //open the game window
        gui.openGameWindowExclusive();
        gui.drawGeneration(grid);
        gui.showGenerationIndex(generation);

        //save the base grid
        originalGrid = new boolean[grid.length][grid[0].length];
        for(int x = 0; x < grid.length; x++)
        {
            for(int y = 0; y < grid[0].length; y++)
            {
                originalGrid[x][y] = grid[x][y];
            }
        }

        generation = 0;
        gui.showGenerationIndex(generation);
    }

    //generation += 1
    public void onClickStep(int generation)
    {
        //calculate next generator if generation paramter is -1
        if(generation == -1)
        {
            calculateNextGeneration();
        }
        //go to the generation given in generation parameter
        else
        {
            this.generation = 0;
            for(int x = 0; x < grid.length; x++)
            {
                for(int y = 0; y < grid[0].length; y++)
                {
                    grid[x][y] = originalGrid[x][y];
                }
            }

            for(int i = 0; i < generation; i++)
            {
                calculateNextGeneration();
            }
        }

        //update the frame
        gui.showGenerationIndex(this.generation);
        gui.drawGeneration(grid);
    }

    //enable auto run
    public void onClickRun()
    {
        run = !run;
    }

    //function that is continuous called
    public void update()
    {
        //if game is in auto run mode
        if(run)
        {
            calculateNextGeneration();
            gui.drawGeneration(grid);
        }
    }

    //calculate the next generation
    private void calculateNextGeneration()
    {
        //increase generation index
        generation++;
        gui.showGenerationIndex(generation);
        boolean[][] _grid = new boolean[grid.length][grid[0].length];

        //calculate surviving, dying and new born cells
        for(int x = 0; x < grid.length; x++)
        {
            for(int y = 0; y < grid[0].length; y++)
            {
                _grid[x][y] = grid[x][y];

                int neighbours = countNeighbours(new Vector2(x, y));

                if(neighbours < 2)
                {
                    _grid[x][y] = false;
                }
                else if(neighbours > 3)
                {
                    _grid[x][y] = false;
                }
                else if(grid[x][y])
                {
                    _grid[x][y] = true;
                }
                else if(neighbours == 3)
                {
                    _grid[x][y] = true;
                }
            }
        }

        //set grid to temp grid
        grid = _grid;

        gui.drawGeneration(grid);
    }

    //count neighbours of each cell based on vector rotation
    private int countNeighbours(Vector2 pos)
    {
        int count = 0;
        Vector2 offset = new Vector2(0, -1);

        for(int i = 0; i < 8; i++)
        {
            Vector2 v = Vector2.add(pos, offset);
            if(isInGrid(v))
            {
                if(grid[v.x][v.y])
                {
                    count++;
                }
            }

            offset.rotate(45);
        }

        return count;
    }

    //check if a vector position is in grid
    private boolean isInGrid(Vector2 pos)
    {
        if(pos.x < 0 || pos.y < 0 || pos.x >= grid.length || pos.y >= grid[0].length)
        {
            return false;
        }

        return true;
    }

    //set a cell _x and _y to value
    public void setCell(float _x, float _y, boolean value)
    {
        int x = 0;
        int y = 0;

        float ratio = (float) (grid[0].length) / (float) (grid.length);

        if (grid[0].length > grid.length)
        {

            x = (int) ((float) _x / (800f / ((float) grid.length * ratio)));
            y = (int) ((float) _y / (800f / ((float) grid[0].length)));
            if(x >= 0 && x < grid.length && y >= 0 && y < grid[0].length)
            {
                grid[x][y] = value;
                if(this.generation == 0)
                {
                    originalGrid[x][y] = value;
                }
            }

        } else
        {
            x = (int) ((float) _x / (800f / ((float) grid.length)));
            y = (int) ((float) _y / (800f / ((float) grid[0].length / ratio)));
            if(x >= 0 && x < grid.length && y >= 0 && y < grid[0].length)
            {
                grid[x][y] = value;
                if(this.generation == 0)
                {
                    originalGrid[x][y] = value;
                }
            }

        }
    }

    //save grid to database
    public void saveGrid(String name)
    {
        try
        {
           db.saveGrid(grid, name);
        } catch (SQLException throwables)
        {
            throwables.printStackTrace();
        }
    }

    //delete all entries in the database
    public void clearDB()
    {
        db.deleteDataBase();
        db.createDataBase();
    }

    //open the select from database window
    public void onOpenSelectionClick()
    {
        gui.openSelectionWindowExclusive();
    }

    //get all possible options/grids from database
    public List<String> readOptionsFromDataBase()
    {
        try
        {
            return db.getOptions();
        } catch (SQLException throwables)
        {
            throwables.printStackTrace();
        }

        return null;
    }
}
