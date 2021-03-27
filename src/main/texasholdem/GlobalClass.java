package ltotj.texasholdem;

import org.bukkit.entity.Player;

import java.util.HashMap;

public class GlobalClass {
    public static boolean playable;
    public static MySQLGameData mySQLGameData;
    public static VaultManager vault;
    public static Config config;
    public static HashMap<Player,TexasField> texasholdemtable=new HashMap<>();
    public static HashMap<Player,Player> currentplayer=new HashMap<>();
}
