package AdventureModel.weapons;

import AdventureModel.Player;

public class KnifeBehaviour implements IWeapon {

    public void show(){
        System.out.println("An ornate, bejewelled dagger");
    }

    @Override
    public void fight(Player player) {
        player.damage(30);
        System.out.println(player.getName() + " is stabbed with a dagger!");
    }

}