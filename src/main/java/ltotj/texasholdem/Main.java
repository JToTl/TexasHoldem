package ltotj.texasholdem;

import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin{

    static VaultManager vault;

    @Override
    public void onEnable(){
        vault=new VaultManager(this);
        new EventList(this);
        getCommand("poker").setExecutor(new Commands());
        GlobalClass.config=new Config(this);
        GlobalClass.config.load();
    }

    @Override
    public void onDisable(){
    }

}
