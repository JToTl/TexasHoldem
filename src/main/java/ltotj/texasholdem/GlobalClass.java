package ltotj.texasholdem;

import org.bukkit.entity.Player;

import java.util.HashMap;

public class GlobalClass {
    public static Config config;
    public static HashMap<Player,TexasField> texasholdemtable=new HashMap<>();
    public static HashMap<Player,Player> currentplayer=new HashMap<>();
}
