package io.github.treesoid.mongoauthkeys;

import io.github.treesoid.mongoauthkeys.util.MAKConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.impl.gui.FabricGuiEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.security.*;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;

public class MongoAuthKeys implements ModInitializer {
    public static final String modid = "mongo-auth-keys";
    public static String version = "invalid";
    public static Logger LOGGER = LogManager.getLogger();
    private static KeyPair keyPair;
    public static List<String> allowedServers;

    @Override
    public void onInitialize() {
        FabricLoader.getInstance().getModContainer(modid).ifPresent(modContainer -> version = modContainer.getMetadata().getVersion().toString());

        keyPair = MAKConfig.readKeypair();
        if (keyPair == null) {
            try {
                KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
                kpg.initialize(2048);
                keyPair = kpg.generateKeyPair();
            } catch (NoSuchAlgorithmException e) {
                FabricGuiEntry.displayCriticalError(e, true);
            }
            try {
                MAKConfig.writeKeypair(keyPair);
            } catch (IOException e) {
                FabricGuiEntry.displayCriticalError(new IOException("Error writing key pair!", e), true);
            }
        }

        allowedServers = MAKConfig.readAllowedServers();
    }

    public static void registerRecivers() {
        ClientLoginNetworking.registerReceiver(new Identifier("mongo-auth", "login"), (client, handler, buf, listenerAdder) -> {
            PacketByteBuf response = PacketByteBufs.create();

            String type = buf.readString();
            if (type.equals("keypair")) {
                byte[] payload = buf.readByteArray();
                response.writeBoolean(true);
                response.writeBoolean(true);
                response.writeByteArray(sign(payload));
            } else if (type.equals("none")) {
                response.writeBoolean(true);
                response.writeBoolean(false);
                response.writeByteArray(getPubkey());
            } else {
                response.writeBoolean(false);
            }
            return CompletableFuture.completedFuture(response);
        });
    }

    public static byte[] sign(byte[] data) {
        try {
            KeyPair keyPair = getKeyPair();
            Signature sgn = Signature.getInstance("SHA256WithRSA");
            sgn.initSign(keyPair.getPrivate());
            sgn.update(data);
            return sgn.sign();
        } catch (InvalidKeyException | SignatureException | NoSuchAlgorithmException e) {
            LOGGER.error("Error signing!", e);
        }
        return new byte[0];
    }

    public static KeyPair getKeyPair() {
        return keyPair;
    }

    public static byte[] getPubkey() {
        return getKeyPair().getPublic().getEncoded();
    }

    public static boolean useOnCurrentServer() {
        try {
            String address = Objects.requireNonNull(MinecraftClient.getInstance().getCurrentServerEntry()).address;
            return allowedServers.contains(address.toLowerCase(Locale.ROOT));
        } catch (NullPointerException e) {
            return false;
        }
    }

    public static void addAllowedServer(String address) {
        if (allowedServers.contains(address)) return;
        allowedServers.add(address);
        MAKConfig.writeAllowedServers(allowedServers);
    }

    public static void removeAllowedServer(String address) {
        if (allowedServers.remove(address)) {
            MAKConfig.writeAllowedServers(allowedServers);
        }
    }

    public static boolean isAllowedOnServer(String address) {
        return allowedServers.contains(address);
    }
}
