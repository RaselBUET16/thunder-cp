package com.sharif.thunder.commands.utilities;

import com.jagrosh.jdautilities.command.CommandEvent;
import com.jagrosh.jdautilities.commons.JDAUtilitiesInfo;
import com.jagrosh.jdautilities.doc.standard.CommandInfo;
import com.jagrosh.jdautilities.examples.doc.Author;
import com.sharif.thunder.commands.UtilitiesCommand;
import com.sun.management.OperatingSystemMXBean;
import java.awt.*;
import java.lang.management.ManagementFactory;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDAInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ApplicationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@CommandInfo(name = "About", description = "Gets information about the bot.")
@Author("Sharif")
public class AboutCommand extends UtilitiesCommand {
  private boolean IS_AUTHOR = true;
  private String REPLACEMENT_ICON = "+";
  private final Color color;
  private final String description;
  private final Permission[] perms;
  private String oauthLink;
  private final String[] features;
  private final OperatingSystemMXBean osb;
  private Runtime rt;
  private final String jvmVersion;

  public AboutCommand(Color color, String description, String[] features, Permission... perms) {
    this.color = color;
    this.description = description;
    this.features = features;
    this.name = "about";
    this.help = "shows info about the bot.";
    this.guildOnly = false;
    this.perms = perms;
    this.aliases = new String[] {"stats", "botinfo"};
    this.botPermissions = new Permission[] {Permission.MESSAGE_EMBED_LINKS};

    osb = (OperatingSystemMXBean) ManagementFactory.getOperatingSystemMXBean();
    rt = Runtime.getRuntime();
    jvmVersion =
        System.getProperty("java.vm.version", "unknown")
            + " ("
            + System.getProperty("java.vendor", "unknown")
            + ")";
  }

  public void setIsAuthor(boolean value) {
    this.IS_AUTHOR = value;
  }

  public void setReplacementCharacter(String value) {
    this.REPLACEMENT_ICON = value;
  }

  @Override
  protected void execute(CommandEvent event) {
    long free = rt.freeMemory() / (1024 * 1024);
    long total = rt.totalMemory() / (1024 * 1024);
    long used = total - free;

    if (oauthLink == null) {
      try {
        ApplicationInfo info = event.getJDA().retrieveApplicationInfo().complete();
        oauthLink = info.isBotPublic() ? info.getInviteUrl(0L, perms) : "";
      } catch (Exception e) {
        Logger log = LoggerFactory.getLogger("OAuth2");
        log.error("Could not generate invite link ", e);
        oauthLink = "";
      }
    }
    EmbedBuilder builder = new EmbedBuilder();
    builder.setColor(
        event.getGuild() == null ? color : event.getGuild().getSelfMember().getColor());
    builder.setAuthor(
        "All about " + event.getSelfUser().getName() + "!",
        null,
        event.getSelfUser().getAvatarUrl());
    boolean join =
        !(event.getClient().getServerInvite() == null
            || event.getClient().getServerInvite().isEmpty());
    boolean inv = !oauthLink.isEmpty();
    String invline =
        "\n"
            + (join
                ? "Join my server [`here`](" + event.getClient().getServerInvite() + ")"
                : (inv ? "Please " : ""))
            + (inv ? (join ? ", or " : "") + "[`invite`](" + oauthLink + ") me to your server" : "")
            + "!";
    String author =
        event.getJDA().getUserById(event.getClient().getOwnerId()) == null
            ? "<@" + event.getClient().getOwnerId() + ">"
            : event.getJDA().getUserById(event.getClient().getOwnerId()).getName();
    StringBuilder descr =
        new StringBuilder()
            .append("Hello! I am **")
            .append(event.getSelfUser().getName())
            .append("**, ")
            .append(description)
            .append("\nI ")
            .append(IS_AUTHOR ? "was written in Java" : "am owned")
            .append(" by **")
            .append(author)
            .append(
                "** using "
                    + JDAUtilitiesInfo.AUTHOR
                    + "'s [Commands Extension]("
                    + JDAUtilitiesInfo.GITHUB
                    + ") (")
            .append(JDAUtilitiesInfo.VERSION)
            .append(") and the [JDA library](https://github.com/DV8FromTheWorld/JDA) (")
            .append(JDAInfo.VERSION)
            .append(") using JVM version ")
            .append(jvmVersion)
            .append("\nType `")
            .append(event.getClient().getTextualPrefix())
            .append(event.getClient().getHelpWord())
            .append("` to see my commands!")
            .append(join || inv ? invline : "")
            .append("\n\nSome of my features include: ```css");
    for (String feature : features)
      descr
          .append("\n")
          .append(
              event.getClient().getSuccess().startsWith("<")
                  ? REPLACEMENT_ICON
                  : event.getClient().getSuccess())
          .append(" ")
          .append(feature);
    descr.append(" ```");
    builder.setDescription(descr);
    builder.addField(
        "Stats",
        event.getJDA().getGuilds().size() + " servers\n" + event.getJDA().getShardInfo(),
        true);
    builder.addField(
        "Users",
        event.getJDA().getUsers().size()
            + " unique\n"
            + event.getJDA().getGuilds().stream().mapToInt(g -> g.getMembers().size()).sum()
            + " total",
        true);
    builder.addField(
        "Channels",
        event.getJDA().getTextChannels().size()
            + " Text\n"
            + event.getJDA().getVoiceChannels().size()
            + " Voice",
        true);
    int cpuLoad = (int) (Math.max(osb.getProcessCpuLoad(), 0.0) * 100.0);
    String usage = Integer.toString(cpuLoad);
    builder.addField("CPU usage", usage + "%", true);
    builder.addField(
        "Memory usage",
        "Free: " + free + "MB\n" + "Allocated: " + total + "MB\n" + "Used: " + used + "MB",
        true);
    builder.setFooter("Last restart", null);
    builder.setTimestamp(event.getClient().getStartTime());
    event.reply(builder.build());
  }
}
