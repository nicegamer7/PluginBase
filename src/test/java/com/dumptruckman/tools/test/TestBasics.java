/******************************************************************************
 * Multiverse 2 Copyright (c) the Multiverse Team 2011.                       *
 * Multiverse 2 is licensed under the BSD License.                            *
 * For more information please check the README.md file included              *
 * with this project.                                                         *
 ******************************************************************************/

package com.dumptruckman.tools.test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.File;

import com.dumptruckman.tools.locale.Messages;
import com.dumptruckman.tools.plugin.AbstractPluginBase;
import com.dumptruckman.tools.plugin.PluginBase;
import com.dumptruckman.tools.test.utils.MockMessages;
import junit.framework.Assert;

import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import com.dumptruckman.tools.test.utils.TestInstanceCreator;

@RunWith(PowerMockRunner.class)
@PrepareForTest({ AbstractPluginBase.class })
public class TestBasics {
    TestInstanceCreator creator;
    Server mockServer;
    CommandSender mockCommandSender;

    @Before
    public void setUp() throws Exception {
        creator = new TestInstanceCreator();
        assertTrue(creator.setUp());
        mockServer = creator.getServer();
        mockCommandSender = creator.getCommandSender();
    }

    @After
    public void tearDown() throws Exception {
        creator.tearDown();
    }

    @Test
    public void testEnableDebugMode() {
        // Pull a core instance from the server.
        Plugin plugin = mockServer.getPluginManager().getPlugin("PluginBase");
        PluginBase myPlugin = (PluginBase) plugin;

        // Make sure Core is not null
        assertNotNull(plugin);

        // Make sure Core is enabled
        assertTrue(plugin.isEnabled());

        // Make a fake server folder to fool MV into thinking a world folder exists.
        File serverDirectory = new File(creator.getPlugin().getServerFolder(), "world");
        serverDirectory.mkdirs();

        // Initialize a fake command
        Command mockCommand = mock(Command.class);
        when(mockCommand.getName()).thenReturn("pb");

        // Assert debug mode is off
        Assert.assertEquals(0, myPlugin.getSettings().getDebug());

        // Send the debug command.
        String[] debugArgs = new String[] { "debug", "3" };
        plugin.onCommand(mockCommandSender, mockCommand, "", debugArgs);

        String[] reloadArgs = new String[] { "reload" };
        plugin.onCommand(mockCommandSender, mockCommand, "", reloadArgs);

        Assert.assertEquals(3, myPlugin.getSettings().getDebug());
        
        myPlugin.getMessager().good(MockMessages.TEST_MESSAGE, mockCommandSender, "And a test arg");
    }
}
