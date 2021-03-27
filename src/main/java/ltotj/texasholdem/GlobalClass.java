package ltotj.texasholdem;


import java.util.HashMap;
import java.util.UUID;


public class GlobalClass {
    public static boolean playable;
    public static MySQLGameData mySQLGameData;
    public static VaultManager vault;
    public static Config config;
    public static HashMap<UUID,TexasField> texasholdemtable=new HashMap<>();
    public static HashMap<UUID, UUID> currentplayer=new HashMap<>();
}
