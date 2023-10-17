package dev.macrohq.macroframework.mixin;

import dev.macrohq.macroframework.event.PacketEvent;
import dev.macrohq.macroframework.util.Ref;
import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

@Mixin(NetworkManager.class)
public class NetworkManagerMixin {
    @Inject(method = "channelRead0*", at = @At("HEAD"))
    private void read(ChannelHandlerContext context, Packet<?> packet, CallbackInfo callback) {
        MinecraftForge.EVENT_BUS.post(new PacketEvent(packet));
    }
}