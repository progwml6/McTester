package org.spongepowered.mctester.internal.framework;

import com.flowpowered.math.vector.Vector3d;
import org.spongepowered.api.data.type.HandType;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;

public interface Client {

    void sendMessage(String text);

    void lookAt(Vector3d targetPos);

    void selectHotbarSlot(int slot);

    void leftClick();

    void rightClick();

    ItemStack getItemInHand(HandType type);

    PlayerInventory getInventory();

}
