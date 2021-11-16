package Data;

import Control.Vector2;

import java.sql.*;

public class DataBase
{
    private Connection connection;
    private Statement st;

    public void connect()
    {
        try
        {
            connection = DriverManager.getConnection("jdbc:h2:./lib/ConwayGameOfLife", "sa", "");
            st = connection.createStatement();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void disconnect()
    {
        try
        {
            st.close();
            connection.close();
        }
        catch(Exception e)
        {
            e.printStackTrace();
        }
    }

    public void sql(String statement)
    {
        try
        {
            st.executeUpdate(statement);
        } catch (SQLException throwables)
        {
            throwables.printStackTrace();
        }
    }

    public ResultSet sqlRS(String statement)
    {
        try
        {
            return st.executeQuery(statement);
        } catch (SQLException throwables)
        {
            throwables.printStackTrace();
        }

        return null;
    }

    public void createDataBase()
    {
        connect();
        sql("create table generationInfo(id int not null auto_increment, name varchar, width int, height int)");
        sql("create table generationData(id int, x int, y int)");
        disconnect();

    }

    public void deleteDataBase()
    {
        connect();
        sql("drop table generationInfo");
        sql("drop table generationData");
        disconnect();
    }

    public void saveGrid(boolean[][] grid, String name) throws SQLException
    {
        connect();

        sql("insert into generationInfo(name, width, height) values ('"+name+"', "+grid.length+", "+grid[0].length+")");
        ResultSet rs = sqlRS("select * from generationInfo order by id desc limit 1");
        rs.next();
        int id = rs.getInt("id");
        for(int x = 0; x < grid.length; x++)
        {
            for(int y = 0; y < grid[0].length; y++)
            {
                if(grid[x][y])
                {
                    sql("insert into generationData (id, x, y) values ("+id+", "+x+", "+y+")");
                }
            }
        }
        disconnect();
    }

    public boolean[][] loadGrid(String name) throws SQLException
    {
        boolean[][] grid;

        connect();

        try
        {
            ResultSet info = sqlRS("select id, width, height, from generationInfo where name = '" + name + "'");
            info.next();
            int width = info.getInt("width");
            int height = info.getInt("height");
            int id = info.getInt("id");
            info.close();

            grid = new boolean[width][height];

            for(int x = 0; x < width; x++)
            {
                for(int y = 0; y < height; y++)
                {
                    ResultSet value = sqlRS("select * from generationData where id = " + id + " and x = " + x + " and y = " + y);
                    if(value.next())
                    {
                        grid[x][y] = true;
                    }
                    else
                    {
                        grid[x][y] = false;
                    }
                    value.close();
                }
            }

            disconnect();
        }
        catch(Exception e)
        {
            grid = new boolean[50][50];
            for(int x = 0; x < 50; x++)
            {
                for(int y = 0; y < 50; y++)
                {
                    grid[x][y] = false;
                }
            }
        }

        return grid;
    }
}