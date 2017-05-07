/*
 * Copyright 2016 John Grosh (jagrosh).
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package vortex.commands;

import java.util.LinkedList;
import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.utils.PermissionUtil;
import vortex.Constants;
import vortex.ModLogger;
import vortex.utils.FormatUtil;

/**
 *
 * @author John Grosh (jagrosh)
 */
public class BanCmd extends Command {
    
    private final ModLogger modlog;
    public BanCmd(ModLogger modlog)
    {
        this.modlog = modlog;
        this.name = "ban";
        this.category = new Category("Moderation");
        this.arguments = "@user [@user...]";
        this.help = "bans all mentioned users";
        this.userPermissions = new Permission[]{Permission.BAN_MEMBERS};
        this.botPermissions = new Permission[]{Permission.BAN_MEMBERS};
        this.guildOnly = true;
    }

    @Override
    protected void execute(CommandEvent event) {
        if(event.getMessage().getMentionedUsers().isEmpty())
        {
            event.reply(String.format(Constants.NEED_MENTION, "user"));
            return;
        }
        if(event.getMessage().getMentionedUsers().size()>20)
        {
            event.reply(event.getClient().getError()+" Up to 20 users can be banned at once.");
            return;
        }
        if(event.getMessage().getMentionedUsers().size()>5)
        {
            event.reactSuccess();
        }
        StringBuilder builder = new StringBuilder();
        LinkedList<User> users = new LinkedList<>();
        event.getMessage().getMentionedUsers().stream().forEach((u) -> {
            Member m = event.getGuild().getMember(u);
            if(m==null)
            {
                users.add(u);
            }
            else if(!PermissionUtil.canInteract(event.getMember(), m))
            {
                builder.append("\n").append(event.getClient().getError()).append(" You do not have permission to ban ").append(FormatUtil.formatUser(u));
            }
            else if (!PermissionUtil.canInteract(event.getSelfMember(), m))
            {
                builder.append("\n").append(event.getClient().getError()).append(" I do not have permission to ban ").append(FormatUtil.formatUser(u));
            }
            else
            {
                users.add(u);
            }
        });
        if(users.isEmpty())
            event.reply(builder.toString());
        else
        {
            for(int i=0; i<users.size(); i++)
            {
                User u = users.get(i);
                boolean last = i+1==users.size();
                event.getGuild().getController().ban(u, 1).reason(event.getAuthor().getName()+" #"+event.getAuthor().getDiscriminator()+" used the ban command.").queue((v) -> {
                        builder.append("\n").append(event.getClient().getSuccess()).append(" Successfully banned ").append(u.getAsMention());
                        if(last)
                            event.reply(builder.toString());
                    }, (t) -> {
                        builder.append("\n").append(event.getClient().getError()).append(" I failed to ban ").append(FormatUtil.formatUser(u));
                        if(last)
                            event.reply(builder.toString());
                    });
            }
        }
        modlog.logCommand(event.getMessage());
    }
}
