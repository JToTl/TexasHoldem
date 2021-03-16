package ltotj.texasholdem;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.Objects;

public class EventList implements Listener {

    public EventList(Plugin plugin){
        plugin.getServer().getPluginManager().registerEvents(this,plugin);
    }

    private void clickSound(Player player){
        player.playSound(player.getLocation(), Sound.BLOCK_COMPOSTER_FILL_SUCCESS,2,2);
    }

    @EventHandler
    public void onPokerInvClick(final InventoryClickEvent e) {
        if (!e.getView().getTitle().equals("TexasHoldem")) return;
        e.setCancelled(true);
        Player p=(Player) e.getWhoClicked();
        final ItemStack clickedItem = e.getCurrentItem();
        if (clickedItem == null || clickedItem.getType().equals(Material.AIR)) return;
        TexasField field = GlobalClass.texasholdemtable.get(GlobalClass.currentplayer.get(p));
        int seat = field.playermap.get(p),ptip=field.seatmap.get(seat).addtip;
        switch (Objects.requireNonNull(Objects.requireNonNull(clickedItem.getItemMeta()).getDisplayName())) {
            case "§w§lフォールド":
                clickSound(p);
                field.seatmap.get(seat).action = "Fold";
                break;
            case "§w§lチェック":
                clickSound(p);
                if(field.bet!=0){
                    e.getWhoClicked().sendMessage("賭けチップが0でないのでチェックできません");
                    return;
                }
            case "§w§lコール":
            case "§a§lこの額でレイズ":
                if(field.bet-field.seatmap.get(seat).instancebet>field.seatmap.get(seat).playertips)return;
                clickSound(p);
                field.seatmap.get(seat).action = "Call";
                break;
            case "§w§lベット":
            case "§w§lレイズ":
                if(field.bet-field.seatmap.get(seat).instancebet>field.seatmap.get(seat).playertips)return;
                clickSound(p);
                field.texasHoldem.raiseInv(seat);
                break;
            case "§4§l戻る":
                clickSound(p);
                field.seatmap.get(seat).texasGui.ownTurnInv(seat);
                break;
            case "§c§l賭けチップを一枚減らす":
                clickSound(p);
                if(ptip<1)break;
                field.seatmap.get(seat).addtip=ptip-1;
                field.texasHoldem.raiseButtomReset(seat);
                break;
            case "§9§l賭けチップを一枚増やす":
                clickSound(p);
                if(field.seatmap.get(seat).playertips<field.bet+ptip+1)break;
                field.seatmap.get(seat).addtip=ptip+1;
                field.texasHoldem.raiseButtomReset(seat);
                break;
            case "§w§lオールイン":
                clickSound(p);
                if(field.bet-field.seatmap.get(seat).instancebet<=field.seatmap.get(seat).playertips)return;
                field.seatmap.get(seat).action = "AllIn";
                break;

        }
    }
}

