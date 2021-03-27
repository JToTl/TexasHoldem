package ltotj.texasholdem;


import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class Config {

    private final Plugin plugin;
    private FileConfiguration config=null;

    public Config(Plugin plugin){
        this.plugin=plugin;
        load();
    }
    public void load(){
        plugin.saveDefaultConfig();
        if(config!=null){
            plugin.reloadConfig();
        }
        config=plugin.getConfig();
    }
    public String getString(String string){
        try {
            return config.getString(string);
        }catch(Exception exception){
            return "";
        }
    }

}
