package com.faktocraft.common.config;

import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

public final class BasicConfig {

  public static final Client CLIENT;
  public static final ForgeConfigSpec CLIENT_SPEC;

  public static final Server SERVER;
  public static final ForgeConfigSpec SERVER_SPEC;

  static {
    Pair<Client, ForgeConfigSpec> client = new ForgeConfigSpec.Builder().configure(Client::new);
    CLIENT = client.getLeft();
    CLIENT_SPEC = client.getRight();

    Pair<Server, ForgeConfigSpec> server = new ForgeConfigSpec.Builder().configure(Server::new);
    SERVER = server.getLeft();
    SERVER_SPEC = server.getRight();
  }

  private BasicConfig() {
  }

  public static final class Client {

    public final ForgeConfigSpec.BooleanValue showPipeSupports;

    private Client(ForgeConfigSpec.Builder builder) {
      builder.push("visual");
      showPipeSupports = builder
          .comment("Draw the small legs that hold pipes and cables against nearby blocks.",
              "Purely cosmetic: it changes nothing about how they work.")
          .define("showPipeSupports", true);
      builder.pop();
    }
  }

  public static final class Server {

    public final ForgeConfigSpec.BooleanValue teleportAnchorEnabled;
    public final ForgeConfigSpec.BooleanValue chunkLoaderEnabled;
    public final ForgeConfigSpec.BooleanValue tankBreakPlacesFluid;

    private Server(ForgeConfigSpec.Builder builder) {
      builder.push("tank");
      tankBreakPlacesFluid = builder
          .comment("Whether breaking a tank holding at least 1000mB places one source block of its fluid.",
              "Disabled, the stored fluid is simply discarded when the tank breaks.")
          .define("breakPlacesFluidSource", true);
      builder.pop();
      builder.push("content");
      teleportAnchorEnabled = builder
          .comment("Whether the teleport anchor exists.",
              "Disabled it is hidden from the creative tabs and from JEI.")
          .define("teleportAnchor", true);
      chunkLoaderEnabled = builder
          .comment("Whether the chunk loader exists.",
              "Disabled it is hidden from the creative tabs and from JEI.")
          .define("chunkLoader", true);
      builder.pop();
    }
  }

  private static boolean read(ForgeConfigSpec spec, ForgeConfigSpec.BooleanValue value) {
    return !spec.isLoaded() || value.get();
  }

  public static boolean showPipeSupports() {
    return read(CLIENT_SPEC, CLIENT.showPipeSupports);
  }

  public static boolean teleportAnchorEnabled() {
    return read(SERVER_SPEC, SERVER.teleportAnchorEnabled);
  }

  public static boolean chunkLoaderEnabled() {
    return read(SERVER_SPEC, SERVER.chunkLoaderEnabled);
  }

  public static boolean tankBreakPlacesFluid() {
    return read(SERVER_SPEC, SERVER.tankBreakPlacesFluid);
  }
}
