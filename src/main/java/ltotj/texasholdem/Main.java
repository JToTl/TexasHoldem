package ltotj.texasholdem;

import org.bukkit.plugin.java.JavaPlugin;

import java.sql.SQLException;

public class Main extends JavaPlugin{

    @Override
    public void onEnable(){
        GlobalClass.playable=false;
        GlobalClass.config=new Config(this);
        GlobalClass.config.load();
        GlobalClass.vault=new VaultManager(this);
        new EventList(this);
        getCommand("poker").setExecutor(new Commands());
    }

    @Override
    public void onDisable(){
        try {
            GlobalClass.mySQLGameData.connectionClose();
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

}
