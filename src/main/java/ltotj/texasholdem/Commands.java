package ltotj.texasholdem;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class Commands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("プレイヤー以外は実行できません");
            return true;
        }
        if (args.length != 0) {
            Player p=(Player) sender;
            if(!GlobalClass.playable) {
                p.sendMessage("プラグイン[TexasHoldem]はただいま停止中です");
                return true;
            }
            switch (args[0]) {
                case"start":
                    if(GlobalClass.currentplayer.containsKey(p)){
                        p.sendMessage("あなたは既にゲームに参加しています！/poker open でゲーム画面を開きましょう！");
                        return true;
                    }
                    else if(args.length<3||!args[1].matches("-?\\d+")||!args[2].matches("-?\\d+")||Math.abs(Double.parseDouble(args[1])-(Double.parseDouble(GlobalClass.config.getString("maxChipRate"))+10000)/2)>(Double.parseDouble(GlobalClass.config.getString("maxChipRate"))-10000)/2||Math.abs(Integer.parseInt(args[2])-3)>1){
                        p.sendMessage("/poker start <チップ一枚あたりの金額:10000円以上> <最大募集人数:2〜4人>");
                        return true;
                    }
                    else if(GlobalClass.vault.getBalance(p.getUniqueId())<Double.parseDouble(args[1])*Double.parseDouble(GlobalClass.config.getString("firstNumberOfChips"))){
                        p.sendMessage("所持金が足りません");
                        return true;
                    }
                    GlobalClass.texasholdemtable.put(p,new TexasField(p,Double.parseDouble(args[1]),Integer.parseInt(args[2])));
                    GlobalClass.texasholdemtable.get(p).texasHoldem.putCoin(0,1);
                    GlobalClass.texasholdemtable.get(p).texasHoldem.putPlayerHead(0);
                    GlobalClass.texasholdemtable.get(p).texasHoldem.othersTurnInv(0);
                    GlobalClass.texasholdemtable.get(p).seatmap.get(0).texasGui.openInventory(p);
                    Bukkit.getServer().broadcastMessage("§l"+p.getName()+"§aが§cチップ一枚"+args[1]+"円§r、§l§e最大募集人数"+args[2]+"人§aで§7§lテキサスホールデム§aを募集中！§r/poker join "+p.getName()+" §l§aで参加しましょう！");
                    GlobalClass.texasholdemtable.get(p).texasHoldem.start();
                    return true;
                case "join":
                    if(GlobalClass.currentplayer.containsKey(p)){
                        p.sendMessage("あなたは既にゲームに参加しています！/poker open でゲーム画面を開きましょう！");
                        return true;
                    }
                    else if(args.length<2){
                        p.sendMessage("/poker join <募集している人のID>");
                        return true;
                    }
                    else if(!GlobalClass.texasholdemtable.containsKey(Bukkit.getPlayer(args[1]))){
                        p.sendMessage(args[1]+"さんはゲームを開催していません。");
                        return true;
                    }
                    Player masplayer=sender.getServer().getPlayer(args[1]);
                    TexasField field=GlobalClass.texasholdemtable.get(masplayer);
                    if(field.isrunning){
                        p.sendMessage("既にゲームが始まっています /poker start <チップ一枚あたりの金額> <最大募集人数:4人まで> で新しいゲームを作成し参加者を募りましょう！");
                        return true;
                    }
                    else if(GlobalClass.vault.getBalance(p.getUniqueId())<field.tip*field.firstChips){
                        p.sendMessage("所持金が足りません");
                        return true;
                    }
                    field.setPlayer(p);
                    field.texasHoldem.putCoin(field.playermap.get(p),1);
                    field.seatmap.get(field.playermap.get(p)).texasGui.inv.setContents(field.seatmap.get(0).texasGui.inv.getContents());
                    field.seatmap.get(field.playermap.get(p)).texasGui.openInventory(p);
                    break;
                case "help":
                    p.sendMessage("/poker start <チップ一枚あたりの金額:10000円以上> <最大募集人数:1〜4人> :ゲームを開始します");
                    p.sendMessage("/poker join <募集している人のID> :ゲームに参加します");
                    p.sendMessage("/poker open :参加中のゲーム画面を開きます");
                    return true;
                case "open":
                    if(GlobalClass.currentplayer.containsKey(p)){
                        TexasField openfield=GlobalClass.texasholdemtable.get(GlobalClass.currentplayer.get(p));
                        openfield.seatmap.get(openfield.playermap.get(p)).texasGui.openInventory(p);
                    }
                    else p.sendMessage("あなたはゲームに参加していません。");
                    return true;
            }
        }
        return false;
    }
}
