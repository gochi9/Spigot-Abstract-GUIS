package com.deadshotmdf.spigot_abstract_GUIS.General.Objects;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Set;

//extended by abstract gui manager classes to create a yml file for each manager type.
//automatically creates the folder and .yml needed
//nice little cheat when you want to easily store/retrieve some information
//if the amount of information is too large, then I'll encourage you to use a database instead of using this
public abstract class InformationHolder {

    private final static Set<String> EMPTY = Collections.emptySet();

    protected final FileConfiguration config;
    private final File file;

    public InformationHolder(JavaPlugin plugin, File file) {
        this.file = file;

        if(!this.file.exists())
            createFile(plugin);

        this.config = YamlConfiguration.loadConfiguration(this.file);
    }

    public Set<String> getKeys(String key, boolean deep){
        ConfigurationSection section = this.config.getConfigurationSection(key);

        if(section == null)
            return EMPTY;

        return section.getKeys(deep);
    }

    public abstract void loadInformation();
    public abstract void saveInformation();

    public void saveC(){
        try {
            this.config.save(this.file);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void createFile(JavaPlugin plugin){
        new File(plugin.getDataFolder(), "data/").mkdirs();

        try {this.file.createNewFile();}
        catch (IOException e) {throw new RuntimeException(e);}
    }

}
