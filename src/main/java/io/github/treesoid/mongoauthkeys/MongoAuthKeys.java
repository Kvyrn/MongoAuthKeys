package io.github.treesoid.mongoauthkeys;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.networking.v1.ClientLoginNetworking;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.PacketByteBuf;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.security.*;
import java.util.concurrent.CompletableFuture;

public class MongoAuthKeys implements ModInitializer {
    public static final String modid = "mongo-auth-keys";
    public static String version = "invalid";
    public static Logger LOGGER = LogManager.getLogger();
    private static KeyPair keyPair;

    @Override
    public void onInitialize() {
        FabricLoader.getInstance().getModContainer(modid).ifPresent(modContainer -> version = modContainer.getMetadata().getVersion().toString());

        try {
            KeyPairGenerator kpg = KeyPairGenerator.getInstance("RSA");
            kpg.initialize(2048);
            keyPair = kpg.generateKeyPair();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }
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

    // TODO
    private static KeyPair getKeyPair() {
        return keyPair;
    }

    public static byte[] getPubkey() {
        return getKeyPair().getPublic().getEncoded();
    }
}
