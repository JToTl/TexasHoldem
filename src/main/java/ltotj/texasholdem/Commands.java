package ltotj.texasholdem;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

public class Commands implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0)return false;
        if(sender.hasPermission("operator")&&args[0].equals("switch")){
            GlobalClass.playable=!GlobalClass.playable;
            if(GlobalClass.playable) sender.sendMessage("playableをtrueに設定しました");
            else sender.sendMessage("playableをfalseに設定しました");
            return true;
        }
        if (!(sender instanceof Player)) {
            sender.sendMessage("プレイヤー以外は実行できません");
            return true;
        }
            Player p=(Player) sender;
            UUID puniq=p.getUniqueId();
            if(!GlobalClass.playable) {
                p.sendMessage("プラグイン[TexasHoldem]はただいま停止中です");
                return true;
            }
            switch (args[0]) {
                case"start":
                    if(GlobalClass.currentplayer.containsKey(puniq)){
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
                    GlobalClass.texasholdemtable.put(p.getUniqueId(),new TexasField(p,Double.parseDouble(args[1]),Integer.parseInt(args[2])));
                    GlobalClass.texasholdemtable.get(puniq).texasHoldem.putCoin(0,1);
                    GlobalClass.texasholdemtable.get(puniq).texasHoldem.putPlayerHead(0);
                    GlobalClass.texasholdemtable.get(puniq).texasHoldem.othersTurnInv(0);
                    GlobalClass.texasholdemtable.get(puniq).seatmap.get(0).texasGui.openInventory(p);
                    Bukkit.getServer().broadcastMessage("§l"+p.getName()+"§aが§cチップ一枚"+args[1]+"円§r、§l§e最大募集人数"+args[2]+"人§aで§7§lテキサスホールデム§aを募集中！§r/poker join "+p.getName()+" §l§aで参加しましょう！ §4注意 参加必要金額"+Double.parseDouble(args[1])*Integer.parseInt(GlobalClass.config.getString("firstNumberOfChips")));
                    GlobalClass.texasholdemtable.get(puniq).texasHoldem.start();
                    return true;
                case "join":
                    if(GlobalClass.currentplayer.containsKey(puniq)){
                        p.sendMessage("あなたは既にゲームに参加しています！/poker open でゲーム画面を開きましょう！");
                        return true;
                    }
                    else if(args.length<2){
                        p.sendMessage("/poker join <募集している人のID>");
                        return true;
                    }
                    else if(Bukkit.getServer().getPlayer(args[1])==null){
                        p.sendMessage(args[1]+"さんはサーバーにいません");
                        return true;
                    }
                    else if(!GlobalClass.texasholdemtable.containsKey(Bukkit.getPlayer(args[1]).getUniqueId())){
                        p.sendMessage(args[1]+"さんはゲームを開催していません。");
                        return true;
                    }
                    Player masplayer=sender.getServer().getPlayer(args[1]);
                    TexasField field=GlobalClass.texasholdemtable.get(masplayer.getUniqueId());
                    if(field.isrunning){
                        p.sendMessage("既にゲームが始まっています /poker start <チップ一枚あたりの金額> <最大募集人数:4人まで> で新しいゲームを作成し参加者を募りましょう！");
                        return true;
                    }
                    else if(GlobalClass.vault.getBalance(p.getUniqueId())<field.tip*field.firstChips){
                        p.sendMessage("所持金が足りません");
                        return true;
                    }
                    field.setPlayer(p);
                    field.texasHoldem.putCoin(field.playermap.get(p.getUniqueId()),1);
                    field.seatmap.get(field.playermap.get(p.getUniqueId())).texasGui.inv.setContents(field.seatmap.get(0).texasGui.inv.getContents());
                    field.seatmap.get(field.playermap.get(p.getUniqueId())).texasGui.openInventory(p);
                    break;
                case "help":
                    p.sendMessage("/poker start <チップ一枚あたりの金額:10000円以上> <最大募集人数:1〜4人> :テキサスホールデムの参加者を募集します 参加人数分だけゲームが行われます §4注意 設定金額×"+Integer.parseInt(GlobalClass.config.getString("firstNumberOfChips"))+"が必要です");
                    p.sendMessage("/poker join <募集している人のID> :テキサスホールデムに参加します");
                    p.sendMessage("/poker open :参加中のゲーム画面を開きます");
                    return true;
                case "open":
                    if(GlobalClass.currentplayer.containsKey(p.getUniqueId())){
                        TexasField openfield=GlobalClass.texasholdemtable.get(GlobalClass.currentplayer.get(p.getUniqueId()));
                        openfield.seatmap.get(openfield.playermap.get(p.getUniqueId())).texasGui.openInventory(p);
                    }
                    else p.sendMessage("あなたはテキサスホールデムに参加していません。");
                    return true;
            }
        return false;
    }
}
