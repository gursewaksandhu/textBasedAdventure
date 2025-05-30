package AdventureModel.weapons;

import AdventureModel.Player;

public class PoisonBehaviour implements IWeapon {
    public void show(){
        System.out.println("A bottle of steaming, bubbly poison");
    }

    @Override
    public void fight(Player player){
        player.damage(25);
        System.out.println(player.getName() + " has been poisoned!");
    }
}
