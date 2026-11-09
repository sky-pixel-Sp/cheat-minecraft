package id.skyop.client;

/** Stable identifier, title and category metadata for all registered modules. */
    public enum SkyOPModuleId {
        KILL_AURA("KillAura", "Prioritises a nearby configured target.", SkyOPCategory.COMBAT),
        TRIGGER_BOT("TriggerBot", "Attacks a valid target under the crosshair.", Category.COMBAT),
        AUTO_CLICKER("AutoClicker", "Repeats attacks while the attack key is held.", Category.COMBAT),
        AUTO_HUNT("Auto Hunt", "Automatically attacks the nearest loaded hostile mob without camera movement.", Category.COMBAT),

        AUTO_SPRINT("Auto Sprint", "Sprints while moving forward.", SkyOPCategory.MOVEMENT),
        SPEED("Speed", "Applies a configurable horizontal movement boost.", Category.MOVEMENT),
        FLIGHT("Flight", "Local flight controls using jump and sneak.", Category.MOVEMENT),
        AUTO_JUMP("Auto Jump", "Automatically jumps while moving forward.", Category.MOVEMENT),
        STEP_ASSIST("Step Assist", "Jumps when forward movement meets a wall.", Category.MOVEMENT),
        SPIDER("Spider", "Climbs a wall while moving into it.", Category.MOVEMENT),
        AUTO_BRIDGE("Auto Bridge", "Places a selected hotbar block beneath the player while moving.", Category.MOVEMENT),
        INFINITE_JUMP("Infinite Jump", "Allows another jump after leaving the ground when jump is pressed again.", Category.MOVEMENT),
        JUMP_BOOST("Jump Boost", "Adjusts the local jump impulse multiplier.", Category.MOVEMENT),

        ENTITY_ESP("Entity ESP", "Highlights nearby loaded living entities and dropped items.", SkyOPCategory.VISUAL),
        ITEM_ESP("Item ESP", "Includes dropped item entities in the Entity ESP highlight filter.", Category.VISUAL),
        WAYPOINTS("Waypoints", "Displays saved coordinates and distances; press V to save a waypoint.", Category.VISUAL),
        MINIMAP("Mini Map", "Shows nearby entities already loaded by the client.", Category.VISUAL),
        GAMMA_BOOST("Gamma Boost", "Sets vanilla gamma to its maximum normal value.", Category.VISUAL),
        NIGHT_VISION("Night Vision", "Improves local visibility using the client gamma setting.", Category.VISUAL),
        HUD("HUD", "Displays enabled modules, coordinates and FPS.", Category.VISUAL),
        TARGET_HUD("Target HUD", "Shows health and distance for the crosshair target.", Category.VISUAL),
        KEYSTROKES_HUD("Keystrokes", "Displays movement key states on screen.", Category.VISUAL),
        ARMOR_HUD("Armor HUD", "Displays health and armor status.", Category.VISUAL),
        SPEEDOMETER("Speedometer", "Displays horizontal movement speed in blocks per second.", Category.VISUAL),
        CPS_HUD("CPS HUD", "Displays successful attack actions per second.", Category.VISUAL),
        POTION_HUD("Potion HUD", "Shows active potion effects and remaining duration.", Category.VISUAL),
        DURABILITY_HUD("Durability HUD", "Displays main-hand item durability.", Category.VISUAL),
        COMPASS_HUD("Compass HUD", "Shows compass direction and yaw.", Category.VISUAL),
        DIRECTION_HUD("Direction HUD", "Shows cardinal direction and coordinates.", Category.VISUAL),
        PING_HUD("Ping HUD", "Shows your current multiplayer latency.", Category.VISUAL),
        FPS_HUD("FPS HUD", "Shows the current frame rate.", Category.VISUAL),
        RADAR_HUD("Nearby Entity Radar", "Lists nearby entities already loaded by the client.", Category.VISUAL),
        TEXT_GUI("Text GUI", "Displays a configurable custom HUD message.", Category.VISUAL),

        NO_FALL("NoFall", "Clears the local fall-distance accumulator.", SkyOPCategory.PLAYER),

        AUTO_EAT("Auto Eat", "Uses safe food from your hotbar when hunger is low.", SkyOPCategory.INVENTORY),
        AUTO_ARMOR("Auto Armor", "Equips armor into empty armor slots.", Category.INVENTORY),
        AUTO_TOOL("Auto Tool", "Selects the best hotbar tool for the targeted block.", Category.INVENTORY),
        AUTO_REFILL("Auto Refill", "Refills low hotbar stacks from matching inventory items.", Category.INVENTORY),
        AUTO_FISH("Auto Fish", "Automatically reels on bite and recasts your fishing rod.", Category.INVENTORY),
        INVENTORY_CLEANER("Inventory Cleaner", "Consolidates matching stacks without dropping items.", Category.INVENTORY),
        HOTBAR_PRESETS("Hotbar Presets", "Saves and restores three hotbar layouts.", Category.INVENTORY),
        PROFILE_MANAGER("Profile Manager", "Manages JSON profiles, imports, exports and backups.", Category.INVENTORY);

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


