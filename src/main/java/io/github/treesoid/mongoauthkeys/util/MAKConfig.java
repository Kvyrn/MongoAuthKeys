package io.github.treesoid.mongoauthkeys.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import io.github.treesoid.mongoauthkeys.MongoAuthKeys;
import net.fabricmc.loader.api.FabricLoader;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Path;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class MAKConfig {
    public static KeyPair readKeypair() {
        Path path = FabricLoader.getInstance().getConfigDir().resolve("mongo-auth-keys").resolve("key.txt");
        try {
            BufferedReader reader = new BufferedReader(new FileReader(path.toFile()));
            String pubkey = reader.readLine();
            String privkey = reader.readLine();
            reader.close();
            KeyFactory factory = KeyFactory.getInstance("RSA");
            PublicKey publicKey = factory.generatePublic(new X509EncodedKeySpec(Base64.getDecoder().decode(pubkey)));
            PrivateKey privateKey = factory.generatePrivate(new PKCS8EncodedKeySpec(Base64.getDecoder().decode(privkey)));
            reader.close();
            return new KeyPair(publicKey, privateKey);
        } catch (IOException | NoSuchAlgorithmException | InvalidKeySpecException e) {
            return null;
        }
    }

    public static void writeKeypair(KeyPair keyPair) throws IOException {
        createParent();
        Path path = FabricLoader.getInstance().getConfigDir().resolve("mongo-auth-keys").resolve("key.txt");
        if (path.toFile().isFile()) {
            path.toFile().delete();
        }
        FileWriter writer = new FileWriter(path.toFile());
        writer.write(Base64.getEncoder().encodeToString(keyPair.getPublic().getEncoded()) + "\n");
        writer.write(Base64.getEncoder().encodeToString(keyPair.getPrivate().getEncoded()) + "\n");
        writer.close();
    }

    public static List<String> readAllowedServers() {
        Gson gson = new Gson();
        try {
            FileReader reader = new FileReader(FabricLoader.getInstance().getConfigDir().resolve("mongo-auth-keys").resolve("allowed-servers.json").toFile());
            AllowedServersWrapper allowedServersWrapper = gson.fromJson(reader, AllowedServersWrapper.class);
            reader.close();
            return allowedServersWrapper.allowedServers;
        } catch (IOException | NullPointerException e) {
            return new ArrayList<>();
        }
    }

    public static void writeAllowedServers(List<String> servers) {
        createParent();
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try {
            FileWriter writer = new FileWriter(FabricLoader.getInstance().getConfigDir().resolve("mongo-auth-keys").resolve("allowed-servers.json").toFile());
            gson.toJson(new AllowedServersWrapper(servers), writer);
            writer.close();
        } catch (IOException e) {
            MongoAuthKeys.LOGGER.warn("Error writing allowed servers!", e);
        }
    }

    private static class AllowedServersWrapper {
        public List<String> allowedServers;

        public AllowedServersWrapper(List<String> allowedServers) {
            this.allowedServers = allowedServers;
        }
    }

    private static void createParent() {
        FabricLoader.getInstance().getConfigDir().resolve("mongo-auth-keys").toFile().mkdir();
    }
}
