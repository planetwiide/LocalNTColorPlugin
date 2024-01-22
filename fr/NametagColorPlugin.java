package fr;

import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

public class NametagColorPlugin extends JavaPlugin implements Listener {
  public void onEnable() {
    getLogger().info("NametagColorPlugin est desormais actif!");
    reloadConfig();
    restorePlayerNametagColors();
    getCommand("color").setExecutor(new NametagColorCommand(this));
    Bukkit.getPluginManager().registerEvents(this, (Plugin)this);
  }
  
  public void onDisable() {
    getLogger().info("NametagColorPlugin est desormais inactif!");
  }
  
  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event) {
    restorePlayerNametagColors();
  }
  
  public void setPlayerNametagColor(Player viewer, Player target, ChatColor color) {
    Scoreboard scoreboard = viewer.getScoreboard();
    Team team = scoreboard.getTeam(target.getName());
    if (team == null) {
      team = scoreboard.registerNewTeam(target.getName());
      team.addEntry(target.getName());
    } 
    team.setPrefix(color.toString());
    team.setSuffix(ChatColor.RESET.toString());
    savePlayerNametagColor(target.getUniqueId(), viewer.getUniqueId(), color);
    viewer.setScoreboard(scoreboard);
  }
  
  private void savePlayerNametagColor(UUID playerId, UUID viewerId, ChatColor color) {
    String path = "players." + playerId.toString() + "." + viewerId.toString();
    getConfig().set(path, color.name());
    saveConfig();
  }
  
  private ChatColor getPlayerNametagColor(UUID playerId, UUID viewerId) {
    String path = "players." + playerId.toString() + "." + viewerId.toString();
    String colorName = getConfig().getString(path);
    if (colorName != null)
      return ChatColor.valueOf(colorName); 
    return ChatColor.RESET;
  }
  
  private void restorePlayerNametagColors() {
    if (getConfig().contains("players")) {
      ConfigurationSection playersSection = getConfig().getConfigurationSection("players");
      for (String playerIdString : playersSection.getKeys(false)) {
        UUID playerId = UUID.fromString(playerIdString);
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) {
          ConfigurationSection viewersSection = playersSection.getConfigurationSection(playerIdString);
          for (String viewerIdString : viewersSection.getKeys(false)) {
            UUID viewerId = UUID.fromString(viewerIdString);
            ChatColor color = ChatColor.valueOf(viewersSection.getString(viewerIdString));
            Player viewer = Bukkit.getPlayer(viewerId);
            if (viewer != null && viewer.isOnline())
              setPlayerNametagColor(viewer, player, color); 
          } 
        } 
      } 
    } 
  }
  
  public class NametagColorCommand implements CommandExecutor {
    private final NametagColorPlugin plugin;
    
    public NametagColorCommand(NametagColorPlugin plugin) {
      this.plugin = plugin;
    }
    
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!(sender instanceof Player))
        return true; 
      if (args.length < 2)
        return true; 
      Player target = this.plugin.getServer().getPlayer(args[0]);
      if (target == null || !target.isOnline())
        return true; 
      ChatColor color = ChatColor.getByChar(args[1]);
      if (color == null)
        return true; 
      this.plugin.setPlayerNametagColor((Player)sender, target, color);
      return true;
    }
  }
}
