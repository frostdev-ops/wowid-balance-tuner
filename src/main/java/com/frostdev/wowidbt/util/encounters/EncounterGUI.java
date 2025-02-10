package com.frostdev.wowidbt.util.encounters;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;

import javax.swing.*;
import java.awt.*;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;



public class EncounterGUI {

    @SubscribeEvent
    public static void testItemUse(PlayerInteractEvent.RightClickItem event) {
        if (event.getItemStack().getDisplayName().getString().contains("Encounter GUI")) {
            showEncounterGUI();
        }
    }
    public static void showEncounterGUI() {
        List<JsonObject> encounters = fetchEncounters();
        SwingUtilities.invokeLater(() -> {
            JFrame frame = new JFrame("Player Encounters");
            frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            frame.setSize(400, 600);
            frame.setLayout(new BorderLayout());

            JPanel panel = new JPanel();
            panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

            for (JsonObject encounter : encounters) {
                String name = encounter.has("name") ? encounter.get("name").getAsString() : "Unknown Encounter";
                JButton button = new JButton(name);
                button.addActionListener(e -> {
                    // Show details when the button is clicked
                    JOptionPane.showMessageDialog(frame, encounter.toString(), "Encounter Details", JOptionPane.INFORMATION_MESSAGE);
                });
                panel.add(button);
            }

            JScrollPane scrollPane = new JScrollPane(panel);
            frame.add(scrollPane, BorderLayout.CENTER);
            frame.setVisible(true);
        });
    }

    private static List<JsonObject> fetchEncounters() {
        List<JsonObject> encounterList = new ArrayList<>();
        try (FileReader reader = new FileReader("logs/combatlog.json")) {
            JsonElement element = JsonParser.parseReader(reader);
            if (element.isJsonArray()) {
                JsonArray array = element.getAsJsonArray();
                for (JsonElement el : array) {
                    if (el.isJsonObject()) {
                        encounterList.add(el.getAsJsonObject());
                    }
                }
            } else if (element.isJsonObject()) {
                JsonObject obj = element.getAsJsonObject();
                if (obj.has("encounters") && obj.get("encounters").isJsonArray()) {
                    JsonArray array = obj.get("encounters").getAsJsonArray();
                    for (JsonElement el : array) {
                        if (el.isJsonObject()) {
                            encounterList.add(el.getAsJsonObject());
                        }
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return encounterList;
    }
}
