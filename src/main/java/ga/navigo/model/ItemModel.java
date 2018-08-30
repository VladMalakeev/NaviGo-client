package ga.navigo.model;

/**
 * Created by ASUS 553 on 15.03.2018.
 */

public class ItemModel {

    public int icon;
    public String name;

    // модель данных используемая в адаптере DrawerItemCustomAdapter
    public ItemModel(int icon, String name) {
        this.icon = icon;
        this.name = name;
    }
}