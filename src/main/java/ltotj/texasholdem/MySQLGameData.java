package ltotj.texasholdem;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MySQLGameData {

    private Connection connection;
    private final String host;
    private final String database;
    private final String user;
    private final String pass;
    private final String port;

    MySQLGameData() throws SQLException{
        host=GlobalClass.config.getString("mysql.host");
        database=GlobalClass.config.getString("mysql.database");
        user=GlobalClass.config.getString("mysql.user");
        pass=GlobalClass.config.getString("mysql.pass");
        port=GlobalClass.config.getString("mysql.port");

        if(!Connect()){
            GlobalClass.playable=false;
            Bukkit.getServer().getLogger().info("データベースに接続できませんでした");
        }
        else if(connection==null||connection.isClosed()){
            GlobalClass.playable=false;
            Bukkit.getServer().getLogger().info("データベースを開くことができません");
        }
        else{
            execute("CREATE TABLE IF NOT EXISTS texasholdem_gamedata (startTime DATETIME,endTime DATETIME,P1 TEXT,P2 TEXT,P3 TEXT,P4 TEXT,chipRate DECIMAL,firstChips INT,P1Chips INT,P2Chips INT,P3Chips INT,P4Chips INT);");
            Bukkit.getServer().getLogger().info("データベースに接続しました");
        }
    }

    private boolean Connect(){
        try{
            Class.forName("com.mysql.jdbc.Driver");
            connection=DriverManager.getConnection("jdbc:mysql://"+host+":"+port+"/"+database+"?autoReconnect=true&useSSL=false", user, pass);
            return true;
        } catch (ClassNotFoundException | SQLException e) {
            GlobalClass.playable=false;
            e.printStackTrace();
            return false;
        }
    }

    public void connectionClose() throws SQLException{
        if(connection!=null){
            connection.close();
        }
    }

    private void execute(String sql) throws SQLException{
        PreparedStatement preparedStatement=connection.prepareStatement(sql);
        preparedStatement.executeUpdate();
    }

    public void saveGameData(TexasField texasField) throws SQLException{
        String playersdata[]=new String[8];
        PreparedStatement preparedStatement= connection.prepareStatement("INSERT INTO texasholdem_gamedata (startTime,endTime,P1,P2,P3,P4,chipRate,firstChips,P1Chips,P2Chips,P3Chips,P4Chips) VALUES (?,?,?,?,?,?,?,?,?,?,?,?);");
        preparedStatement.setString(1,getDateForMySQL(texasField.startTime));
        preparedStatement.setString(2,getDateForMySQL(texasField.endTime));
        for(int pcount=0;pcount<texasField.seatmap.size();pcount++){
            playersdata[pcount]=texasField.seatmap.get(pcount).player.getName();
            playersdata[pcount+4]=String.valueOf(texasField.seatmap.get(pcount).playerChips);
        }
        for(int count=0;count<4;count++){
            preparedStatement.setString(count+3,playersdata[count]);
            preparedStatement.setString(count+9,playersdata[count+4]);

        }
        preparedStatement.setString(7, String.valueOf(texasField.tip));
        preparedStatement.setString(8, String.valueOf(texasField.firstChips));
        preparedStatement.executeUpdate();
    }

    private String getDateForMySQL(Date date){
        DateFormat df=new SimpleDateFormat("yyyy-MM-dd HHH:mm:ss");
        return df.format(date);
    }

}
