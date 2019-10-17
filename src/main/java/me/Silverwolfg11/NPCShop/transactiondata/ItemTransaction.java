package me.Silverwolfg11.NPCShop.transactiondata;

public class ItemTransaction {

    private int sells = 0;
    private int buys = 0;

    public void bought(int amt) {
        buys += amt;
    }

    public int getBuys() { return buys; }

    public void sold(int amt) { sells += amt; }

    public int getSells() { return sells; }

}
