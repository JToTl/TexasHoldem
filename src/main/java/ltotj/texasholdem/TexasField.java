package ltotj.texasholdem;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public class TexasField {
    List<Card> deck=new ArrayList<>();
    HashMap<Integer,Seat> seatmap=new HashMap<>();
    HashMap<Player,Integer> playermap=new HashMap<>();
    List<Card> community=new ArrayList<>();
    static Player masterplayer;
    double tip;
    int pot,bet,foldcount,maxseat;
    boolean isrun=false;
    TexasHoldem texasHoldem;

    static class Card {
        int number;
        int suit;

        public ItemStack getCard() {
            Material material = Material.valueOf(GlobalClass.config.getString(suit + "." + number ));
            final ItemStack item = new ItemStack(material, 1);
            final ItemMeta meta = item.getItemMeta();
            meta.setDisplayName(getSuit(suit) + "の" + ((number-1)%13+1));
            item.setItemMeta(meta);
            return item;
        }

        public String getSuit(int i) {
            switch (i) {
                case 0:
                    return "スペード";
                case 1:
                    return "ダイヤ";
                case 2:
                    return "ハート";
                case 3:
                    return "クローバー";
            }
            return "";
        }
    }

    class Seat {
        TexasGui texasGui= new TexasGui();
        Player player;
        List<Card> myhands=new ArrayList<>();
        int addtip=0,instancebet,playertips=20;
        double hand=0;
        String action="";
        boolean folded=false;
        ItemStack head;

        public Seat(Player p) {
            player = p;
            GlobalClass.currentplayer.put(p, masterplayer);
            head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta skull = (SkullMeta) head.getItemMeta();
            assert skull != null;
            skull.setOwningPlayer(p);
            head.setItemMeta(skull);

            //シート作成時に徴収しまーす
            Main.vault.withdraw(p,tip*20);
        }
    }

    class TexasGui{
        public final Inventory inv;
        public TexasGui(){
            inv= Bukkit.createInventory(null,54,"TexasHoldem");
        }
        public void openInventory(final Player player){
            player.openInventory(inv);
        }
        public void ownTurnInv(int i){
            for(int j=45;j<53;j++) {
                seatmap.get(i).texasGui.putCustomItem(j,Material.valueOf(GlobalClass.config.getString("texasholdeminv" + "." + j+".material")),GlobalClass.config.getString("texasholdeminv" + "." + j+".name"),GlobalClass.config.getString("texasholdeminv" + "." + j+".lore"));
            }
        }
        protected ItemStack createGUIItem(final Material material,final String name,final String... lore){
            final ItemStack item=new ItemStack(material,1);
            final ItemMeta meta=item.getItemMeta();
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
            return item;
        }
        protected ItemStack createGUIItem(int i,final Material material,final String name,final String... lore){
            final ItemStack item=new ItemStack(material,i);
            final ItemMeta meta=item.getItemMeta();
            meta.setDisplayName(name);
            meta.setLore(Arrays.asList(lore));
            item.setItemMeta(meta);
            return item;
        }
        protected void putCustomItem(int i,final Material material,final String name,final String... lore){
            inv.setItem(i,createGUIItem(material,name,lore));
        }
        protected void putCustomItem(int i,int j,final Material material,final String name,final String... lore){
            inv.setItem(i,createGUIItem(j,material,name,lore));
        }
    }
    class TexasHoldem extends Thread{
        int i,j,k,seatsize,db,count;
        double p;

        private int dealButtom(){
            return (db-1)%seatsize;
        }

        private void playSoundAllPlayer(Sound sound){
            for(i=0;i<seatmap.size();i++){
                Player player=seatmap.get(i).player;
                player.playSound(player.getLocation(),sound,2,2);
            }
        }

        public void putPlayerHead(int t){
            putItemAllPlayer(seatmap.get(t).head,cardPosition(t)-1);
        }

        private void putItemExceptForOne(ItemStack item,int t,int l){
            for(int k=0;k<seatmap.size();k++){
                if(k!=l)seatmap.get(k).texasGui.inv.setItem(t,item);
            }
        }

        private void putItemAllPlayer(ItemStack item,int t){
            for(j=0;j<seatmap.size();j++){
                seatmap.get(j).texasGui.inv.setItem(t,item);
            }
        }

        private void putItemAllPlayer(int t,int s,final Material material,final String name,final String... lore){
            for(j=0;j<seatmap.size();j++){
                seatmap.get(j).texasGui.putCustomItem(t,s,material,name,lore);
            }
        }

        public void raiseButtomReset(int t){
            seatmap.get(t).texasGui.putCustomItem(49,Material.valueOf(GlobalClass.config.getString("raise.49.material")),GlobalClass.config.getString("raise.49.name"),"§c"+seatmap.get(t).addtip+"枚追加");
        }

        public void raiseInv(int t){
            for(int s=45;s<53;s++){
                seatmap.get(t).texasGui.putCustomItem(s,Material.valueOf(GlobalClass.config.getString("raise."+s+".material")),GlobalClass.config.getString("raise."+s+".name"),"");
            }
        }

        public void othersTurnInv(int t){
            for(j=45;j<54;j++){
                seatmap.get(t).texasGui.inv.setItem(j,new ItemStack(Material.WHITE_STAINED_GLASS_PANE));
            }
        }

        private void sleep(int t){
            try {
                Thread.sleep(t);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        private int getDigit(double f,int t,int s){
            double l=Math.floor(f/Math.pow(10,11-s));
            return (int) (Math.round(l-100*Math.floor(l/100)));
        }

        //席順とforのiがズレるからその対処
        private int convertInt(int t){
            return (t+db)%(seatsize);
        }

        private void openCommunityCard(int t){
            putItemAllPlayer(community.get(t).getCard(),20+t);
        }

        private void dealCard(int t) {
            int random=new Random().nextInt(deck.size());
            seatmap.get(t).myhands.add(deck.get(random));
            deck.remove(random);
        }

        private int cardPosition(int t){
            switch (t){
                case 0:
                    return 42;
                case 1:
                    return 37;
                case 2:
                    return 1;
                case 3:
                    return 6;
            }
            return 0;
        }

        private int tipPosition(int t){
            switch(t){
                case 0:
                    return 35;
                case 1:
                    return 30;
                case 2:
                    return 12;
                case 3:
                    return 17;
            }
            return 0;
        }

        private void putTip(int t,int s,int r){
            for(int ti=s;ti<r+1;ti++){
                playSoundAllPlayer(Sound.BLOCK_CHAIN_STEP);
                putItemAllPlayer(tipPosition(t),ti,Material.GOLD_NUGGET,"§l§yチップ","§c"+tip+"円");
                sleep(200);
            }
        }

        public void putCoin(int t,int s){
            putItemAllPlayer(cardPosition(t)+2,s%65,Material.GOLD_INGOT,"§c§l"+seatmap.get(t).player.getName()+"§r§wのチップ","§e"+seatmap.get(t).playertips+"§w枚");
        }

        private void putPot(){
            for(int t=0;t<seatsize;t++)pot=pot+seatmap.get(t).instancebet;
            putItemAllPlayer(25,1,Material.GOLD_BLOCK,"§6現在の賭けチップ合計","§e§l"+pot+"§w枚");
        }

        //tはSBからどれだけズラして実行するかの値 基本は0
        private void actionTime(int t){
            for (int b = 0;!canNextTurn(convertInt(b+t))||b<seatsize; b++) {
                if(foldcount==seatsize-1||seatmap.get(convertInt(b+t)).action.equals("AllIn")||seatmap.get(convertInt(b+t)).folded)continue;
                k = convertInt(b + t );
                seatmap.get(k).texasGui.ownTurnInv(k);
                putItemAllPlayer(tipPosition(k)-3,1,Material.DIAMOND_BLOCK,"§l§wターンプレイヤー","");
                for (int c = 600; c > -1; c--) {
                    if (seatmap.get(k).folded) break;
                    sleep(50);
                    if (c % 20 == 0) {
                        playSoundAllPlayer(Sound.BLOCK_STONE_BUTTON_CLICK_ON);
                        putItemAllPlayer(new ItemStack(Material.CLOCK, c / 20), 26);
                    }
                    if (c == 0) {
                        seatmap.get(k).addtip=0;
                        othersTurnInv(k);
                        if (seatmap.get(k).playertips >= bet) Call(k);
                        else {
                            othersTurnInv(k);
                            foldcount=foldcount+1;
                            Fold(k);
                        }
                    }//強制コールチェックフォールドのどれか
                    switch (seatmap.get(k).action) {
                        case "Fold":
                            Fold(k);
                            foldcount=foldcount+1;
                            break;
                        case "Call":
                            Call(k);
                            break;
                        case "AllIn":
                            AllIn(k);
                            break;
                    }
                    if(!seatmap.get(k).action.equals("")){
                        othersTurnInv(k);
                        break;
                    }
                }
                putItemAllPlayer(tipPosition(k)-3,0,Material.STONE,"","");
                if(!seatmap.get(k).action.equals("AllIn"))seatmap.get(k).action="";
                putCoin(k,1);
                putItemAllPlayer(new ItemStack(Material.CLOCK, 0), 26);
            }
            for(int m=0;m<seatsize;m++){
                if(!seatmap.get(m).folded&&!seatmap.get(m).action.equals("AllIn"))putItemAllPlayer(tipPosition(m),0,Material.STONE,"","");
                playSoundAllPlayer(Sound.BLOCK_GRAVEL_STEP);
                sleep(500);
            }
            putPot();
        }

        private List<Card> sortCard(List<Card> cards){//数字を優先して並び替え
            for (i = 0; i < cards.size(); i++) {
                for (j = i + 1; j < cards.size(); j++) {
                    if (cards.get(j).number < cards.get(i).number) {
                        Card icard = cards.get(j);
                        cards.set(j, cards.get(i));
                        cards.set(i, icard);
                    } else if (cards.get(j).number == cards.get(i).number && cards.get(j).suit < cards.get(i).suit) {
                        Card icard = cards.get(j);
                        cards.set(j, cards.get(i));
                        cards.set(i, icard);
                    }
                }
            }
            return cards;
        }

        List<Card> wow= new ArrayList<>(community);
        int a;
        private int isFlash(int t){
            a=getDigit(seatmap.get(t).hand,0,1);
            if(a==5||a==8||a==9){
                wow.removeAll(wow);
                wow.addAll(community);
                sortCard(wow);
                return wow.get(2).suit;
            }
            return 4;
        }

        private List<Card> sortCardSuit(List<Card> cards){
            for (i = 0; i < cards.size(); i++) {
                for (j = i + 1; j < cards.size(); j++) {
                    if(cards.get(j).suit<cards.get(i).suit){
                        Card icard = cards.get(j);
                        cards.set(j, cards.get(i));
                        cards.set(i, icard);
                    }
                    else if(cards.get(j).suit==cards.get(i).suit&&cards.get(j).number<cards.get(i).number){
                        Card icard = cards.get(j);
                        cards.set(j, cards.get(i));
                        cards.set(i, icard);
                    }
                }
            }
            return cards;
        }

        //手の判定　多分一番なげえ
        private double judgeHand(int t) {

            List<Card> inst= new ArrayList<>(community);
            inst.addAll(seatmap.get(t).myhands);
            List<Card> judgedhand=new ArrayList<>(sortCard(inst)),cards = new ArrayList<>();
            int dupnum = 0, dupsuit = 0, numsum = 0;
            double r = 0;
            boolean straight = false;

            //数字・スーツ被りの和
            for (i = 0; i < 7; i++) {
                for (j = i + 1; j < 7; j++) {
                    if (judgedhand.get(i).number == judgedhand.get(j).number) dupnum = dupnum + 1;
                    if (judgedhand.get(i).suit == judgedhand.get(j).suit) dupsuit = dupsuit + 1;
                }
            }

            //ストレートフラッシュかどうか　ついでにフラッシュならそれはそれでって感じ
            if(dupsuit>9){
                cards.addAll(sortCardSuit(judgedhand));
                for (i = 1; i < 7; i++) if (judgedhand.get(i).suit == judgedhand.get(3).suit) cards.add(judgedhand.get(i));
                for (i = cards.size() - 5; i > -1; i--) {
                    numsum = 0;
                    for (j = 4; j > -1; j--) numsum = numsum + cards.get(i + j).number;
                    if (numsum * (cards.get(i + 4).number - cards.get(i).number) == 20 * cards.get(i + 2).number) {
                        if(cards.get(i+4).number==14)r=190000000000.0;
                        else r=180000000000.0+100000000*cards.get(i+4).number;
                        straight=true;
                        break;
                    }
                }
                if(!straight){
                    r=150000000000.0;
                    k=4;
                    for(i=cards.size()-1;k>-1;i--){
                        r=r+cards.get(i).number*Math.pow(100,k);
                        k=k-1;
                    }
                }
            }
            //ストレートかどうか
            if (dupnum < 4&&dupsuit<10) {
                cards.removeAll(cards);
                cards.add(judgedhand.get(0));
                for (i = 1; i < 7; i++) if (judgedhand.get(i).number != judgedhand.get(i - 1).number) cards.add(judgedhand.get(i));
                for (i = cards.size() - 5; i > -1; i--) {
                    numsum = 0;
                    for (j = 4; j > -1; j--) numsum = numsum + cards.get(i + j).number;
                    if (numsum * (cards.get(i + 4).number - cards.get(i).number) == 20 * cards.get(i + 2).number) {
                        r = 140000000000.0 + 100000000 * cards.get(i + 4).number;
                        straight = true;
                        break;
                    }
                    if(straight)break;
                    //常にfalseだって言われる　なんでぇなの
                }
            }

            switch (dupnum) {
                case 9:
                case 7://4C確定
                    r=170000000000.0+101010100*judgedhand.get(3).number+(judgedhand.get(2).number+judgedhand.get(6).number-judgedhand.get(3).number);
                    break;
                case 6://4CかFH
                    if(judgedhand.get(1).number==judgedhand.get(2).number&&judgedhand.get(4).number==judgedhand.get(5).number) {
                        r=160000000000.0+101010000*judgedhand.get(4).number+judgedhand.get(2).number*101;
                    }
                    else r=170000000000.0+101010100*judgedhand.get(3).number+(judgedhand.get(2).number+judgedhand.get(6).number-judgedhand.get(3).number);
                    break;
                case 5:
                case 4://FH確定
                    for(i=6;i>1;i--) {
                        if (judgedhand.get(i).number == judgedhand.get(i - 2).number) {
                            r = 160000000000.0 + judgedhand.get(i).number * 101010000;
                            break;
                        }
                    }
                    for(j=6;j>0;j--){
                        if(judgedhand.get(j).number==judgedhand.get(j-1).number&&judgedhand.get(j).number!=judgedhand.get(i).number){
                            r=r+judgedhand.get(j).number*101;
                            break;
                        }
                    }
                    break;
            }

            if(!straight&&dupsuit<10) {//3C以下のやつら
                switch (dupnum) {
                    case 3://3Cか2P
                        if ((judgedhand.get(6).number == judgedhand.get(5).number && judgedhand.get(6).number != judgedhand.get(4).number) || (judgedhand.get(0).number == judgedhand.get(1).number && judgedhand.get(0).number != judgedhand.get(2).number)) {
                            r = 120000000000.0 + judgedhand.get(5).number * 101000000 + judgedhand.get(3).number * 10100 + (judgedhand.get(6).number + judgedhand.get(4).number + judgedhand.get(2).number - judgedhand.get(5).number - judgedhand.get(3).number);
                        } else {
                            for (i = 6; i > 1; i--) {
                                if (judgedhand.get(i).number == judgedhand.get(i - 1).number) {
                                    r = 130000000000.0 + judgedhand.get(i).number * 101010000;
                                    break;
                                }
                            }
                            k = 1;
                            for (j = 6; k > -1; j--) {
                                if (judgedhand.get(j).number == judgedhand.get(i).number) j = j - 3;
                                r = r + Math.pow(100, k) * judgedhand.get(j).number;
                                k = k - 1;
                            }
                        }
                        break;
                    case 2://2P確定
                        for (i = 6; i > 2; i--) {
                            if (judgedhand.get(i).number == judgedhand.get(i - 1).number) {
                                r = 120000000000.0 + 101000000 * judgedhand.get(i).number;
                                break;
                            }
                        }
                        for (j = i - 2; j > 0; j--) {
                            if (judgedhand.get(j).number == judgedhand.get(j - 1).number) {
                                r = r + 10100 * judgedhand.get(j).number;
                                break;
                            }
                        }
                        for (k = 6; k > 3; k--) {
                            if (judgedhand.get(k).number != judgedhand.get(i).number && judgedhand.get(k).number != judgedhand.get(j).number) {
                                r = r + judgedhand.get(k).number;
                                break;
                            }
                        }
                        break;
                    case 1://1P確定
                        for(i=6;i>0;i--) {
                            if (judgedhand.get(i).number == judgedhand.get(i - 1).number) {
                                r = 110000000000.0 + judgedhand.get(i).number * 101000000;
                                break;
                            }
                        }
                        k=2;
                        for(j=6;k>-1;j--) {
                            if (judgedhand.get(j).number == judgedhand.get(i).number) j = j - 2;
                            r = r + Math.pow(100, k) * judgedhand.get(j).number;
                            k = k - 1;
                        }
                        break;
                    case 0://HC確定
                        r=100000000000.0;
                        for(i=6;i>1;i--) r=r+Math.pow(100,i-2)*judgedhand.get(i).number;
                        break;
                }
            }
            return Math.round(r);
        }

        @Override
        public void run() {
            for(int m=0;m<60;m++) {
                if(m%10==0)Bukkit.getServer().broadcastMessage(masterplayer.getName()+"がテキサスホールデムを募集中・・・残り"+(60-m)+"秒 /poker join "+masterplayer.getName()+"で参加");
                if(seatmap.size()==maxseat)break;
                sleep(1000);
            }

            //参加人数の取得、チップの設定
            isrun=true;
            seatsize = seatmap.size();
            if(seatsize==1){
                Bukkit.getServer().broadcastMessage(masterplayer.getName()+"さんのテキサスホールデムは人が集まらなかったので中止しました");
                GlobalClass.currentplayer.remove(masterplayer);
                GlobalClass.texasholdemtable.remove(masterplayer);
                return;
            }
            db = 0;
            int winnerseat,winners;

            for (int n = 0; n < seatsize; n++) {
                //掛け金、合計掛け金、フォールド状態、カードの初期化
                foldcount=0;
                Reset();
                bet = 0;
                pot = 0;
                putPot();
                community.removeAll(community);
                for (i = 0; i < seatsize; i++) {
                    putItemAllPlayer(tipPosition(i),0,Material.STONE,"","");
                    seatmap.get(i).hand=0;
                    seatmap.get(i).instancebet=0;
                    seatmap.get(i).folded = false;
                    seatmap.get(i).action="";
                    seatmap.get(i).myhands.removeAll(seatmap.get(i).myhands);
                    dealCard(i);
                    dealCard(i);
                    if(seatmap.get(i).playertips==0){
                        seatmap.get(i).folded = true;
                        foldcount=foldcount+1;
                    }
                }
                for (i = 0; i < 5; i++) {
                    int random=new Random().nextInt(deck.size());
                    community.add(deck.get(random));
                    deck.remove(random);
                }
                //カード配布
                for (int m = 0; m < seatsize; m++) {
                    playSoundAllPlayer(Sound.ITEM_BOOK_PAGE_TURN);
                    putItemExceptForOne(new ItemStack(Material.PINK_DYE), cardPosition(m), m);
                    seatmap.get(m).texasGui.inv.setItem(cardPosition(m),seatmap.get(m).myhands.get(0).getCard());
                    sleep(500);

                    playSoundAllPlayer(Sound.ITEM_BOOK_PAGE_TURN);
                    putItemExceptForOne(new ItemStack(Material.PINK_DYE), cardPosition(m) + 1, m);
                    seatmap.get(m).texasGui.inv.setItem(cardPosition(m)+1, seatmap.get(m).myhands.get(1).getCard());
                    sleep(500);
                }

                //プリフロップ SBとBBの強制ベット 額がなければ次へ
                for(int m=0;m<2;m++){
                    if(seatmap.get(convertInt(m)).playertips<m+1)continue;
                    seatmap.get(convertInt(m)).addtip=1;
                    Call(convertInt(m));
                    putCoin(convertInt(m),1);
                }
                //2からスタート 人数分の回数実行の後、foldedがfalseの人のinstancebetが一致するまで続ける
                actionTime(2);

                //フロップ
                for(int m=0;m<seatsize;m++){
                    seatmap.get(m).instancebet=0;
                }
                bet=0;
                for (int m = 0; m < 3; m++) {
                    playSoundAllPlayer(Sound.ITEM_BOOK_PAGE_TURN);
                    openCommunityCard(m);
                    sleep(1000);
                }
                actionTime(0);

                //ターン
                for(int m=0;m<seatsize;m++){
                    seatmap.get(m).instancebet=0;
                }
                bet = 0;
                playSoundAllPlayer(Sound.ITEM_BOOK_PAGE_TURN);
                openCommunityCard(3);
                actionTime(0);

                //リバー
                for(int m=0;m<seatsize;m++){
                    seatmap.get(m).instancebet=0;
                }
                bet=0;
                playSoundAllPlayer(Sound.ITEM_BOOK_PAGE_TURN);
                openCommunityCard(4);
                actionTime(0);

                //役の確定とカードのオープン
                playSoundAllPlayer(Sound.ITEM_BOOK_PAGE_TURN);
                for(int m=0;m<seatsize;m++) {
                    if (!seatmap.get(m).folded) {
                        seatmap.get(m).hand = judgeHand(m);
                        putItemAllPlayer(seatmap.get(m).myhands.get(0).getCard(), cardPosition(m));
                        putItemAllPlayer(seatmap.get(m).myhands.get(1).getCard(), cardPosition(m) + 1);
                    }
                }

                //勝敗判定
                winners=0;
                winnerseat=0;
                for(i=0;i<seatsize;i++){
                    for(j=0;j<seatsize;j++){
                        if(seatmap.get(i).folded)break;
                        else if(i!=j&&seatmap.get(j).hand>seatmap.get(i).hand)break;
                    }
                    if(j==seatsize){
                        winnerseat=i;
                        winners=winners+1;
                    }
                }

                sleep(2000);

                //役の表示
                for(int m=0;m<seatsize;m++) {
                    if (seatmap.get(m).folded)continue;
                    count = 0;
                    p = seatmap.get(m).hand;
                    int flsuit = isFlash(m);
                    playSoundAllPlayer(Sound.BLOCK_BEACON_ACTIVATE);
                    for (int x = 0; x < 5; x++) {
                        for (int y = 0; y < 5; y++) {
                            if (community.get(x).number == getDigit(p, 2 * y + 2, 2 * y + 3) && (flsuit == 4 || flsuit == community.get(x).suit)) {
                                ItemStack item = community.get(x).getCard();
                                ItemMeta meta = item.getItemMeta();
                                meta.addEnchant(Enchantment.LURE, 1, true);
                                item.setItemMeta(meta);
                                putItemAllPlayer(item, 20 + x);
                                count = count + 1;
                                break;
                            }
                            if (y == 4) putItemAllPlayer(new ItemStack(Material.STONE, 0), 20 + x);
                        }
                    }
                    int z;
                    for (int x = 0; x < 2; x++) {
                        z = 5;
                        for (z = 0; z < 5 && count != 5; z++) {
                            if (seatmap.get(m).myhands.get(x).number == getDigit(p, 2 * z + 2, 2 * z + 3) && (flsuit == 4 || flsuit == seatmap.get(m).myhands.get(x).suit)) {
                                ItemStack item = seatmap.get(m).myhands.get(x).getCard();
                                ItemMeta meta = item.getItemMeta();
                                meta.addEnchant(Enchantment.LURE, 1, true);
                                item.setItemMeta(meta);
                                putItemAllPlayer(item, cardPosition(m) + x);
                                count = count + 1;
                                z = 1;
                                break;
                            }
                        }
                        if (z % 5 == 0) putItemAllPlayer(new ItemStack(Material.STONE, 0), cardPosition(m) + x);
                    }
                    sleep(5000);
                    putItemAllPlayer(seatmap.get(m).myhands.get(0).getCard(),cardPosition(m));
                    putItemAllPlayer(seatmap.get(m).myhands.get(1).getCard(),cardPosition(m)+1);
                    for(int x=0;x<5;x++)openCommunityCard(x);
                }

                //勝者にチップを全部渡す
                //勝者が複数いたら等分にして、あまりをBBかなんかそんな感じの位置の人に渡す・・・らしい！しらんけど
                for(int m=0;m<seatsize;m++){
                    if(seatmap.get(m).hand==seatmap.get(winnerseat).hand)seatmap.get(m).playertips=seatmap.get(m).playertips+pot/winners;
                }
                if(winners==1){
                    for(int m=0;m<8;m++){
                        if(m%2==1){
                            putItemAllPlayer(seatmap.get(winnerseat).head,20);
                            putItemAllPlayer(21,1,Material.GOLD_BLOCK,"§l§aW§bI§cN§dN§eE§dR§f!","");
                            putItemAllPlayer(seatmap.get(winnerseat).head,22);
                            putItemAllPlayer(23,1,Material.GOLD_BLOCK,"§l§aW§bI§cN§dN§eE§dR§f!","");
                            putItemAllPlayer(seatmap.get(winnerseat).head,24);
                        }
                        else{
                            putItemAllPlayer(20,1,Material.GOLD_BLOCK,"§l§aW§bI§cN§dN§eE§dR§f!","");
                            putItemAllPlayer(seatmap.get(winnerseat).head,21);
                            putItemAllPlayer(22,1,Material.GOLD_BLOCK,"§l§aW§bI§cN§dN§eE§dR§f!","");
                            putItemAllPlayer(seatmap.get(winnerseat).head,23);
                            putItemAllPlayer(24,1,Material.GOLD_BLOCK,"§l§aW§bI§cN§dN§eE§dR§f!","");
                        }
                        playSoundAllPlayer(Sound.ENTITY_FIREWORK_ROCKET_TWINKLE_FAR);
                        sleep(500);
                    }
                }
                else{
                    seatmap.get(convertInt(2)).playertips=seatmap.get(convertInt(2)).playertips+pot%winners;
                    for(int m=0;m<5;m++){
                        putItemAllPlayer(20+m,1,Material.BARRIER,"DRAW","引き分け 同率１位のプレイヤーに賞金が分配されます");
                    }
                }
                sleep(4000);

                db = db + 1;

                for(int m=0;m<seatsize;m++){
                    putItemAllPlayer(new ItemStack(Material.STONE,0),cardPosition(m));
                    putItemAllPlayer(new ItemStack(Material.STONE,0),cardPosition(m)+1);
                    putCoin(m,1);
                }
                for(int m=0;m<5;m++){
                    putItemAllPlayer(new ItemStack(Material.STONE,0),20+m);
                }
            }

            Bukkit.broadcastMessage(masterplayer.getName()+"が募集したテキサスホールデムが終了しました");

            //結果表示ついでにチップを換金して配布、テーブル削除
            //closeinventoryがメインスレッドでしか動かんらしいから自分で閉じてもらおう
            for(int m=0;m<seatsize;m++){
                for(int x=0;x<seatsize;x++){
                    seatmap.get(m).player.sendMessage(seatmap.get(x).player.getName()+":のチップ：20枚→"+seatmap.get(x).playertips);
                }
                for(int x=0;x<54;x++){
                    seatmap.get(m).texasGui.putCustomItem(x,1,Material.BARRIER,"終了です","Eボタンを押して画面を閉じてください");
                }
                Main.vault.deposit(seatmap.get(m).player,seatmap.get(m).playertips*tip);
                GlobalClass.currentplayer.remove(seatmap.get(m).player);
            }
            GlobalClass.texasholdemtable.remove(masterplayer);
        }
    }

    public void Fold(int i){
        texasHoldem.putItemAllPlayer(texasHoldem.tipPosition(i),1,Material.BARRIER,"§rフォールド","");
        seatmap.get(i).folded=true;
    }

    //現在のbetの値との差額を払う
    //Callって書いてあるけどRaiseもCheckも全部これ
    public void Call(int i){
        bet=bet+seatmap.get(i).addtip;
        seatmap.get(i).playertips=seatmap.get(i).playertips-bet+seatmap.get(i).instancebet;
        texasHoldem.putTip(i,seatmap.get(i).instancebet,bet);
        seatmap.get(i).instancebet=bet;
        seatmap.get(i).addtip=0;
    }

    public void AllIn(int i){
        pot=pot+seatmap.get(i).playertips;
        texasHoldem.putItemAllPlayer(texasHoldem.tipPosition(i),1,Material.NETHER_STAR,"§a§lオールイン","§c"+seatmap.get(i).playertips);
        seatmap.get(i).playertips=0;
    }

    public boolean canNextTurn(int i){
        if(seatmap.get(i).instancebet==bet||seatmap.get(i).playertips==0)return true;
        return false;
    }

    public void Reset(){
        deck.removeAll(deck);
        for(int i=0;i<seatmap.size();i++){
            seatmap.get(i).myhands.removeAll(seatmap.get(i).myhands);
        }
        for (int i = 0; i < 4; i++) {
            for (int j = 2; j < 15; j++) {
                Card card = new Card();
                card.suit = i;
                card.number = j;
                deck.add(card);
            }
        }
    }

    public void setPlayer(Player p){
        playermap.put(p,seatmap.size());
        seatmap.put(seatmap.size(),new Seat(p));
        texasHoldem.putPlayerHead(playermap.get(p));
    }

    public TexasField(Player p,double monnney,int maxxxseat) {
        tip=monnney;
        maxseat=maxxxseat;
        masterplayer=p;
        playermap.put(p,0);
        seatmap.put(0, new Seat(masterplayer));
        seatmap.get(0).player=p;
        texasHoldem= new TexasHoldem();
        Reset();
    }

}