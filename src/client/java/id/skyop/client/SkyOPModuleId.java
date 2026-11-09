package id.skyop.client;

/** Stable identifier, title and category metadata for all registered modules. */
    public enum SkyOPModuleId {
        KILL_AURA("KillAura", "Prioritises a nearby configured target.", SkyOPCategory.COMBAT),
        TRIGGER_BOT("TriggerBot", "Attacks a valid target under the crosshair.", SkyOPCategory.COMBAT),
        AUTO_CLICKER("AutoClicker", "Repeats attacks while the attack key is held.", SkyOPCategory.COMBAT),
        AUTO_HUNT("Auto Hunt", "Automatically attacks the nearest loaded hostile mob without camera movement.", SkyOPCategory.COMBAT),

        AUTO_SPRINT("Auto Sprint", "Sprints while moving forward.", SkyOPCategory.MOVEMENT),
        SPEED("Speed", "Applies a configurable horizontal movement boost.", SkyOPCategory.MOVEMENT),
        FLIGHT("Flight", "Local flight controls using jump and sneak.", SkyOPCategory.MOVEMENT),
        AUTO_JUMP("Auto Jump", "Automatically jumps while moving forward.", SkyOPCategory.MOVEMENT),
        STEP_ASSIST("Step Assist", "Jumps when forward movement meets a wall.", SkyOPCategory.MOVEMENT),
        SPIDER("Spider", "Climbs a wall while moving into it.", SkyOPCategory.MOVEMENT),
        AUTO_BRIDGE("Auto Bridge", "Places a selected hotbar block beneath the player while moving.", SkyOPCategory.MOVEMENT),
        INFINITE_JUMP("Infinite Jump", "Allows another jump after leaving the ground when jump is pressed again.", SkyOPCategory.MOVEMENT),
        JUMP_BOOST("Jump Boost", "Adjusts the local jump impulse multiplier.", SkyOPCategory.MOVEMENT),

        ENTITY_ESP("Entity ESP", "Highlights nearby loaded living entities and dropped items.", SkyOPCategory.VISUAL),
        ITEM_ESP("Item ESP", "Includes dropped item entities in the Entity ESP highlight filter.", SkyOPCategory.VISUAL),
        WAYPOINTS("Waypoints", "Displays saved coordinates and distances; press V to save a waypoint.", SkyOPCategory.VISUAL),
        MINIMAP("Mini Map", "Shows nearby entities already loaded by the client.", SkyOPCategory.VISUAL),
        GAMMA_BOOST("Gamma Boost", "Sets vanilla gamma to its maximum normal value.", SkyOPCategory.VISUAL),
        NIGHT_VISION("Night Vision", "Improves local visibility using the client gamma setting.", SkyOPCategory.VISUAL),
        HUD("HUD", "Displays enabled modules, coordinates and FPS.", SkyOPCategory.VISUAL),
        TARGET_HUD("Target HUD", "Shows health and distance for the crosshair target.", SkyOPCategory.VISUAL),
        KEYSTROKES_HUD("Keystrokes", "Displays movement key states on screen.", SkyOPCategory.VISUAL),
        ARMOR_HUD("Armor HUD", "Displays health and armor status.", SkyOPCategory.VISUAL),
        SPEEDOMETER("Speedometer", "Displays horizontal movement speed in blocks per second.", SkyOPCategory.VISUAL),
        CPS_HUD("CPS HUD", "Displays successful attack actions per second.", SkyOPCategory.VISUAL),
        POTION_HUD("Potion HUD", "Shows active potion effects and remaining duration.", SkyOPCategory.VISUAL),
        DURABILITY_HUD("Durability HUD", "Displays main-hand item durability.", SkyOPCategory.VISUAL),
        COMPASS_HUD("Compass HUD", "Shows compass direction and yaw.", SkyOPCategory.VISUAL),
        DIRECTION_HUD("Direction HUD", "Shows cardinal direction and coordinates.", SkyOPCategory.VISUAL),
        PING_HUD("Ping HUD", "Shows your current multiplayer latency.", SkyOPCategory.VISUAL),
        FPS_HUD("FPS HUD", "Shows the current frame rate.", SkyOPCategory.VISUAL),
        RADAR_HUD("Nearby Entity Radar", "Lists nearby entities already loaded by the client.", SkyOPCategory.VISUAL),
        TEXT_GUI("Text GUI", "Displays a configurable custom HUD message.", SkyOPCategory.VISUAL),

        NO_FALL("NoFall", "Clears the local fall-distance accumulator.", SkyOPCategory.PLAYER),

        AUTO_EAT("Auto Eat", "Uses safe food from your hotbar when hunger is low.", SkyOPCategory.INVENTORY),
        AUTO_ARMOR("Auto Armor", "Equips armor into empty armor slots.", SkyOPCategory.INVENTORY),
        AUTO_TOOL("Auto Tool", "Selects the best hotbar tool for the targeted block.", SkyOPCategory.INVENTORY),
        AUTO_REFILL("Auto Refill", "Refills low hotbar stacks from matching inventory items.", SkyOPCategory.INVENTORY),
        AUTO_FISH("Auto Fish", "Automatically reels on bite and recasts your fishing rod.", SkyOPCategory.INVENTORY),
        INVENTORY_CLEANER("Inventory Cleaner", "Consolidates matching stacks without dropping items.", SkyOPCategory.INVENTORY),
        HOTBAR_PRESETS("Hotbar Presets", "Saves and restores three hotbar layouts.", SkyOPCategory.INVENTORY),
        PROFILE_MANAGER("Profile Manager", "Manages JSON profiles, imports, exports and backups.", SkyOPCategory.INVENTORY);

        private final String title;
        private final String description;
        private final SkyOPCategory category;

        SkyOPModuleId(String title, String description, SkyOPCategory category) {
            this.title = title;
            this.description = description;
            this.category = category;
        }

        public String title() { return SkyOPLanguage.moduleTitle(name(), title); }
        public String description() { return SkyOPLanguage.moduleDescription(name(), description); }
        public SkyOPCategory category() { return category; }
    }


