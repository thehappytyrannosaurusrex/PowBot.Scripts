package org.thehappytyrannosaurusrex.varrockmuseum

import org.powbot.api.Condition
import org.powbot.api.Random
import org.powbot.api.Tile
import org.powbot.api.rt4.*
import org.powbot.api.script.ScriptCategory
import org.powbot.api.script.ScriptConfiguration
import org.powbot.api.script.ScriptManifest
import org.powbot.api.script.OptionType
import org.powbot.api.script.paint.Paint
import org.powbot.api.script.paint.PaintBuilder
import org.powbot.api.script.tree.TreeComponent
import org.powbot.api.script.tree.TreeScript
import org.thehappytyrannosaurusrex.api.chat.DialogueHandler
import DropUtils
import org.thehappytyrannosaurusrex.api.inventory.InventoryManagement
import org.thehappytyrannosaurusrex.api.ui.CameraController
import org.thehappytyrannosaurusrex.api.ui.CameraInitProfile
import org.thehappytyrannosaurusrex.api.ui.ViewportUi
import org.thehappytyrannosaurusrex.api.utils.Logger
import org.thehappytyrannosaurusrex.api.utils.BankUtils
import org.thehappytyrannosaurusrex.api.data.WidgetIds
import org.thehappytyrannosaurusrex.varrockmuseum.config.Config
import org.thehappytyrannosaurusrex.varrockmuseum.config.LampSkill
import org.thehappytyrannosaurusrex.varrockmuseum.config.Options
import org.thehappytyrannosaurusrex.varrockmuseum.config.buildConfig
import org.thehappytyrannosaurusrex.varrockmuseum.data.MuseumConstants as C
import org.thehappytyrannosaurusrex.varrockmuseum.tree.MuseumBranches

@ScriptManifest(
    name = "Varrock Museum Cleaner",
    description = "Cleans Varrock Museum specimens and uses Antique lamps on a chosen skill.",
    version = "2.0.0",
    author = "thehappytyrannosaurusrex",
    category = ScriptCategory.Other
)
@ScriptConfiguration.List(
    [
        ScriptConfiguration(
            name = Options.Keys.LAMP_SKILL,
            description = "Which skill should Antique lamps be used on?",
            optionType = OptionType.STRING,
            allowedValues = [
                Options.Values.ATTACK,
                Options.Values.STRENGTH,
                Options.Values.DEFENCE,
                Options.Values.RANGED,
                Options.Values.MAGIC,
                Options.Values.HITPOINTS,
                Options.Values.PRAYER,
                Options.Values.AGILITY,
                Options.Values.HERBLORE,
                Options.Values.THIEVING,
                Options.Values.CRAFTING,
                Options.Values.FLETCHING,
                Options.Values.SLAYER,
                Options.Values.HUNTER,
                Options.Values.MINING,
                Options.Values.SMITHING,
                Options.Values.FISHING,
                Options.Values.COOKING,
                Options.Values.FIREMAKING,
                Options.Values.WOODCUTTING,
                Options.Values.FARMING,
                Options.Values.RUNECRAFTING,
                Options.Values.CONSTRUCTION,
                Options.Values.SAILING
            ],
            defaultValue = Options.Values.SLAYER
        ),
        ScriptConfiguration(
            name = Options.Keys.KEEP_ITEMS,
            description = "Comma-separated item names to NEVER drop (they can still be banked).",
            optionType = OptionType.STRING,
            defaultValue = "Coins,"
        )
    ]
)

class VarrockMuseumCleaner : TreeScript() {

    private val SCRIPT_NAME = "Varrock Museum Cleaner"

    // ------------------------------------------------------------------------
    // High level stage machine
    // ------------------------------------------------------------------------

    enum class Stage {
        PRE_BANK_CLEAN,     // startup inventory sanity + initial bank if needed
        TRAVEL_TO_MUSEUM,   // walk + door/gate handling
        ENSURE_KIT,         // make sure we have tools + gear
        INSIDE_NORMALISE,   // clean up any semi-clean startup inventory
        MAIN_LOOP           // steady-state behaviour inside cleaning area
    }

    var stage: Stage = Stage.PRE_BANK_CLEAN

    lateinit var config: Config
        private set

    val lampSkill: LampSkill
        get() = config.lampSkill

    private var startTime: Long = 0L
    private var paint: Paint? = null

    var lampsUsed: Int = 0
        private set

    // Cleaning tools keep in the kit (at most one copy of each)
    private val cleaningToolIds: Set<Int> =
        setOf(C.TROWEL, C.ROCK_PICK, C.SPECIMEN_BRUSH)

    // Lower-cased groupings for artefacts / rewards
    private val uniqueArtefactNamesLc =
        C.UNIQUE_ARTEFACT_NAMES.map { it.lowercase() }.toSet()

    private val commonArtefactNamesLc =
        C.COMMON_ARTEFACT_NAMES.map { it.lowercase() }.toSet()

    private val unremarkableFindNamesLc =
        C.UNREMARKABLE_FIND_NAMES.map { it.lowercase() }.toSet()

    private val storageRewardNamesLc =
        C.STORAGE_CRATE_REWARD_NAMES.map { it.lowercase() }.toSet()

    // Shared UI helpers
    private val cameraController = CameraController()
    private val viewportUi = ViewportUi()

    // Root of the behaviour tree – wired via MuseumBranches
    override val rootComponent: TreeComponent<*>
        get() = MuseumBranches.buildRoot(this)

    // ------------------------------------------------------------------------
    // Logging helper
    // ------------------------------------------------------------------------

    private fun log(tag: String, message: String) {
        Logger.info(SCRIPT_NAME, tag, message)
    }


    // ------------------------------------------------------------------------
    // Lifecycle
    // ------------------------------------------------------------------------

    override fun onStart() {

        try {
            log("STARTUP", "Varrock Museum Cleaner started (rewrite).")

            startTime = System.currentTimeMillis()

            // Build config from script options
            config = buildConfig(this)
            log("STARTUP", "Using Antique lamps on: ${lampSkill.displayName}")
            if (config.keepItemNames.isNotEmpty()) {
                log("STARTUP", "Keep list: ${config.keepItemNames.joinToString()}")
            }

            // Camera + viewport + inventory
            cameraController.init(
                CameraInitProfile(
                    yaw = 190,
                    zoom = 10.0,
                    minPitch = 85,
                    maxPitch = 95
                )
            )
            viewportUi.tidyOnStart()
            DropUtils.enableTapToDrop()
            DropUtils.ensureInventoryTab()

            // Paint
            initPaint()

            // Requirements
            if (!isDigSiteCompleted()) {
                log("STARTUP", "The Dig Site is NOT completed. Stopping script.")
                controller.stop()
                return
            }

            if (!checkSelectedSkillLevelOrStop()) {
                return
            }

            // From here start in PRE_BANK_CLEAN – everything else is driven by tick()
            stage = Stage.PRE_BANK_CLEAN

        } catch (t: Throwable) {
            log(
                "STARTUP",
                "Unexpected error in onStart: ${t::class.java.simpleName}: ${t.message}"
            )
        }
    }

    override fun onStop() {
        log("STARTUP", "Varrock Museum Cleaner stopped.")
        log("STATS", "Lamps used: $lampsUsed")
        super.onStop()
    }

    // ------------------------------------------------------------------------
    // Stage: startup inventory + initial bank
    // ------------------------------------------------------------------------

    fun handlePreBankCleanup() {
        // Use any Antique lamps as early as possible
        if (hasAntiqueLamp()) {
            useLamps()
            return
        }

        // Drop museum junk (unique artefacts, storage rewards, unremarkable finds, extra tools, etc.)
        if (hasJunkToDrop()) {
            dropInventoryExceptKeep("startup junk cleanup")
            return
        }

        // Move non-museum gloves / boots into inventory so the bank can grab them.
        unequipNonMuseumGlovesAndBoots()

        // If still have “foreign” items (not museum kit, finds or keep items), send them to bank.
        if (inventoryHasNonMuseumItems()) {
            val done = performStartupBank()
            if (done) {
                stage = Stage.TRAVEL_TO_MUSEUM
                Condition.sleep(Random.nextInt(600, 1000))
            }
            return
        }

        // Inventory is now “clean enough” – head to the museum.
        stage = Stage.TRAVEL_TO_MUSEUM
    }

    // Items consider fine to have *before* heading to the museum:
    // - the cleaning kit (tools + leather gloves/boots)
    // - Uncleaned finds
    // - common artefacts (for the crate)
    // - Antique lamps
    // - user “keep items”
    private fun inventoryHasNonMuseumItems(): Boolean {
        val allowedIds = mutableSetOf(
            C.TROWEL,
            C.ROCK_PICK,
            C.SPECIMEN_BRUSH,
            C.LEATHER_GLOVES,
            C.LEATHER_BOOTS,
            C.UNCLEANED_FIND,
            C.ANTIQUE_LAMP
        )

        val keepNames = config.keepItemNames
        val predicates = mutableListOf<(String) -> Boolean>()

        if (keepNames.isNotEmpty()) {
            predicates += { name -> name.lowercase().trim() in keepNames }
        }

        // Allow common artefacts to be carried to the museum; they'll be crated.
        if (commonArtefactNamesLc.isNotEmpty()) {
            predicates += { name -> name.lowercase().trim() in commonArtefactNamesLc }
        }

        return InventoryManagement.hasNonAllowedItems(
            allowedItemIds = allowedIds,
            allowedByName = predicates
        )
    }

    private fun performStartupBank(): Boolean {
        val keepIds = C.INITIAL_KEEP_FOR_BANK.toMutableSet()

        // Always preserve Antique lamps if any are in the inventory.
        keepIds += C.ANTIQUE_LAMP

        // Preserve any keep-list items and common artefacts that are currently in the inventory.
        val keepNames = config.keepItemNames + commonArtefactNamesLc
        Inventory.stream()
            .filtered { it.valid() && it.id() != -1 }
            .forEach { item ->
                val normName = item.name().lowercase().trim()
                if (normName in keepNames) {
                    keepIds += item.id()
                }
            }

        val done = BankUtils.depositAllExceptToNearestBank(
            keepItemIds = keepIds.toIntArray(),
            preferredBankTile = C.VARROCK_EAST_BANK_TILE,
            maxPreferredDistance = 200.0,
            logPrefix = "[Varrock Museum] BANK (startup) |"
        )

        if (done) {
            log("BANK", "Startup bank cleanup complete – inventory now only contains museum items + keep items.")
        } else {
            log("BANK", "Walking to/opening bank for startup cleanup.")
        }

        return done
    }

    private fun unequipNonMuseumGlovesAndBoots() {
        var changed = false

        // HANDS slot (gloves)
        val gloves = Equipment.itemAt(Equipment.Slot.HANDS)
        if (gloves.valid() && gloves.id() != -1 && gloves.id() != C.LEATHER_GLOVES) {
            log("GEAR", "Unequipping non-leather gloves: ${gloves.name()}")
            if (gloves.interact("Remove")) {
                Condition.wait(
                    { !Equipment.itemAt(Equipment.Slot.HANDS).valid() },
                    200,
                    10
                )
            }
            changed = true
        }

        // FEET slot (boots)
        val boots = Equipment.itemAt(Equipment.Slot.FEET)
        if (boots.valid() && boots.id() != -1 && boots.id() != C.LEATHER_BOOTS) {
            log("GEAR", "Unequipping non-leather boots: ${boots.name()}")
            if (boots.interact("Remove")) {
                Condition.wait(
                    { !Equipment.itemAt(Equipment.Slot.FEET).valid() },
                    200,
                    10
                )
            }
            changed = true
        }

        if (changed) {
            log(
                "GEAR",
                "Non-leather gloves/boots moved to inventory; they will be deposited in the startup bank step."
            )
        }
    }

    // ------------------------------------------------------------------------
    // Stage: travel to museum (used both from startup and after bank trips)
    // ------------------------------------------------------------------------

    fun handleTravelToMuseum() {
        if (C.CLEANING_AREA.contains(Players.local())) {
            stage = Stage.ENSURE_KIT
            return
        }

        walkToMuseum()
    }

    private fun walkToMuseum() {
        try {
            Movement.moveTo(C.MUSEUM_TARGET_TILE)
        } catch (t: Throwable) {
            log("NAV", "Movement.moveTo failed: ${t::class.java.simpleName}: ${t.message}")
        }
        handleMuseumDoorsAndGates()
        Condition.sleep(600)
    }

    private fun handleMuseumDoorsAndGates() {
        // If 're already inside, no need to fiddle with doors/gates
        if (C.CLEANING_AREA.contains(Players.local())) {
            return
        }

        // Try door first (outside), then gate (inside)
        if (openNamedObjectAtTiles("Door", "Open", C.DOOR_TILES)) {
            return
        }

        openNamedObjectAtTiles("Gate", "Open", C.GATE_TILES)
    }

    private fun openNamedObjectAtTiles(
        name: String,
        action: String,
        tiles: Array<Tile>
    ): Boolean {
        val obj = Objects.stream()
            .name(name)
            .filtered { o -> tiles.any { it == o.tile() } }
            .nearest()
            .first()

        if (!obj.valid()) {
            return false
        }

        if (!obj.inViewport()) {
            Camera.turnTo(obj)
        }

        if (!obj.interact(action)) {
            return false
        }

        log("NAV", "Interacted with $name at ${obj.tile()} using '$action'.")

        // After clicking a gate/door, there may be guard dialogue.
        DialogueHandler.spamClickContinue(maxAttempts = 10)

        Condition.sleep(600)
        return true
    }

    // ------------------------------------------------------------------------
    // Stage: ensure cleaning kit (tools + leather gloves/boots)
    // ------------------------------------------------------------------------

    fun handleEnsureKit() {
        if (!C.CLEANING_AREA.contains(Players.local())) {
            // Got moved – go back to travel stage.
            stage = Stage.TRAVEL_TO_MUSEUM
            return
        }

        if (!hasCleaningKit()) {
            log("TOOLS", "Missing part of the cleaning kit – grabbing it from the Tools rack.")
            takeCleaningKit()
            equipCleaningGear()
            return
        }

        // Kit looks good → move on to normalising any startup inventory may be carrying.
        stage = Stage.INSIDE_NORMALISE
        Condition.sleep(Random.nextInt(600, 1000))
    }

    private fun hasCleaningKit(): Boolean {
        val hasTools = Inventory.stream().id(C.TROWEL).isNotEmpty() &&
                Inventory.stream().id(C.ROCK_PICK).isNotEmpty() &&
                Inventory.stream().id(C.SPECIMEN_BRUSH).isNotEmpty()
        val glovesEquipped = Equipment.stream().id(C.LEATHER_GLOVES).isNotEmpty()
        val bootsEquipped = Equipment.stream().id(C.LEATHER_BOOTS).isNotEmpty()
        return hasTools && glovesEquipped && bootsEquipped
    }

    private fun takeCleaningKit() {
        val tools = Objects.stream()
            .name("Tools")
            .at(C.TOOLS_TILE)
            .first()

        if (!tools.valid()) {
            log("TOOLS", "Tools object not found at ${C.TOOLS_TILE}.")
            return
        }

        if (!tools.inViewport()) {
            Camera.turnTo(tools)
        }

        if (tools.interact("Take")) {
            // Wait for chat prompt
            Condition.wait({ Chat.chatting() || Chat.canContinue() }, 600, 10)

            val completed = try {
                Chat.completeChat("Yes.")
            } catch (_: Throwable) {
                false
            }

            if (!completed) {
                try {
                    Chat.sendInput("1")
                } catch (_: Throwable) {
                    DialogueHandler.spamClickContinue(maxAttempts = 5)
                }
            }

            Condition.sleep(1200)
        }
    }

    private fun equipCleaningGear() {
        viewportUi.ensureInventoryOpen()

        if (Equipment.stream().id(C.LEATHER_GLOVES).isEmpty()) {
            val gloves = Inventory.stream().id(C.LEATHER_GLOVES).first()
            if (gloves.valid() && gloves.interact("Wear")) {
                Condition.sleep(800)
            }
        }

        if (Equipment.stream().id(C.LEATHER_BOOTS).isEmpty()) {
            val boots = Inventory.stream().id(C.LEATHER_BOOTS).first()
            if (boots.valid() && boots.interact("Wear")) {
                Condition.sleep(800)
            }
        }
    }

    // ------------------------------------------------------------------------
    // Stage: inside cleaning area – normalise any “semi-clean” startup inventory
    // ------------------------------------------------------------------------

     fun handleInsideNormalise() {
        if (!C.CLEANING_AREA.contains(Players.local())) {
            stage = Stage.TRAVEL_TO_MUSEUM
            return
        }

        // Lamps first
        if (hasAntiqueLamp()) {
            useLamps()
            return
        }

        // Clean any Uncleaned finds happened to start with.
        if (hasUncleanedFinds()) {
            cleanOnTables()
            return
        }

        // Store any crate-eligible finds started with.
        if (hasFindsToStore()) {
            storeFinds()
            return
        }

        // Drop leftover junk (unique artefacts, crate rewards, etc.)
        if (hasJunkToDrop()) {
            dropInventoryExceptKeep("inside normalise")
            return
        }

        // At point inventory should be “clean” – start the steady-state loop.
        stage = Stage.MAIN_LOOP
         Condition.sleep(Random.nextInt(600, 1000))

     }

    // ------------------------------------------------------------------------
    // Stage: main loop inside cleaning area
    // ------------------------------------------------------------------------

    fun handleMainLoop() {
        // 1) If inventory is in a “bank keep items” state, do that first.
        if (shouldBankKeepItems()) {
            val done = bankKeepItems()
            if (done) {
                // After finishing a bank trip ’ll be standing at a bank.
                // Next tick go back to the museum.
                stage = Stage.TRAVEL_TO_MUSEUM
            }
            return
        }

        // 2) If ’re not inside the cleaning area (e.g. just left the bank), walk back.
        if (!C.CLEANING_AREA.contains(Players.local())) {
            stage = Stage.TRAVEL_TO_MUSEUM
            return
        }

        // 3) Lamps always come first.
        if (hasAntiqueLamp()) {
            useLamps()
            return
        }

        // 4) Clean any uncleaned finds.
        if (hasUncleanedFinds()) {
            cleanOnTables()
            return
        }

        // 5) Store crate-eligible finds in the storage crate.
        if (hasFindsToStore()) {
            storeFinds()
            return
        }

        // 6) Drop junk while respecting keep items.
        if (hasJunkToDrop()) {
            dropInventoryExceptKeep("main loop")
            return
        }

        // 7) If still have space, fill the inventory from specimen rocks.
        if (!Inventory.isFull()) {
            fillInventoryFromRocks()
            return
        }

        // Nothing immediate to do – short, human-ish idle.
        Condition.sleep(Random.nextInt(600, 1200))

    }

    // ------------------------------------------------------------------------
    // Museum actions – rocks, tables, crate
    // ------------------------------------------------------------------------

    private fun hasAntiqueLamp(): Boolean =
        Inventory.stream().id(C.ANTIQUE_LAMP).isNotEmpty()

    private fun hasUncleanedFinds(): Boolean =
        Inventory.stream().id(C.UNCLEANED_FIND).isNotEmpty()

    private fun hasFindsToStore(): Boolean =
        Inventory.stream()
            .filtered { shouldTreatAsFind(it) }
            .isNotEmpty()

    private fun hasJunkToDrop(): Boolean =
        computeJunkItems().isNotEmpty()

    private fun fillInventoryFromRocks() {
        if (!C.CLEANING_AREA.contains(Players.local())) {
            log("FILL", "Not in cleaning area; handling doors/gates.")
            handleMuseumDoorsAndGates()
            return
        }

        if (Inventory.isFull()) return

        val rock = Objects.stream()
            .id(*C.SPECIMEN_ROCK_IDS)
            .within(C.CLEANING_AREA)
            .nearest()
            .first()

        if (!rock.valid()) {
            log("FILL", "No specimen rocks found.")
            return
        }

        if (!rock.inViewport()) {
            Camera.turnTo(rock)
            Condition.sleep(Random.nextInt(300, 500))
        }

        log("FILL", "Taking Uncleaned finds until inventory full.")

        // Single interact call - the game continues giving finds automatically
        // Condition.wait handles the waiting and is interruptible by PowBot
        while (!Inventory.isFull()) {
            rock.click() || rock.interact("Take")
            Condition.sleep(Random.nextInt(400, 700))}


        if (Inventory.isFull()) {
            log("FILL", "Inventory is now full.")
        }
    }

    private fun cleanOnTables() {
        log("CLEAN", "Cleaning finds at specimen tables.")

        val table = Objects.stream()
            .id(C.SPECIMEN_TABLE_ID)
            .within(C.CLEANING_AREA)
            .nearest()
            .first()

        if (!table.valid()) {
            log("CLEAN", "Specimen table not found.")
            return
        }

        if (!table.inViewport()) {
            Camera.turnTo(table)
            Condition.sleep(Random.nextInt(300, 500))
        }

        val before = Inventory.stream().id(C.UNCLEANED_FIND).count().toInt()

        // Single click - game auto-cleans all
        if (table.click() || table.interact("Clean")) {
            Condition.wait(
                { Inventory.stream().id(C.UNCLEANED_FIND).isEmpty() },
                2000,
                60
            )

            if (Chat.chatting() || Chat.canContinue()) {
                DialogueHandler.spamClickContinue(maxAttempts = 5)
            }

            val after = Inventory.stream().id(C.UNCLEANED_FIND).count().toInt()
            val cleaned = before - after
            if (cleaned > 0) {
                log("CLEAN", "Cleaned $cleaned finds.")
            }
        }
    }

    private fun storeFinds() {
        log("STORE", "Adding finds to Storage crate.")

        val crate = Objects.stream()
            .id(C.STORAGE_CRATE_ID)
            .within(C.CLEANING_AREA)
            .nearest()
            .first()

        if (!crate.valid()) {
            log("STORE", "Storage crate not found.")
            return
        }

        if (!crate.inViewport()) {
            Camera.turnTo(crate)
            Condition.sleep(Random.nextInt(300, 500))
        }

        val beforeCount = Inventory.stream()
            .filtered { shouldTreatAsFind(it) }
            .count()
            .toInt()

        if (beforeCount == 0) {
            log("STORE", "No crate-eligible finds to add.")
            return
        }

        // Single click - game auto-adds all common artefacts
        if (crate.click() || crate.interact("Add finds")) {
            Condition.wait(
                {!(Inventory.stream()
                .filtered { shouldTreatAsFind(it) }
                .isNotEmpty())},
                1200,1800
            )


            if (Chat.chatting() || Chat.canContinue()) {
                DialogueHandler.spamClickContinue(maxAttempts = 5)
            }

            val afterCount = Inventory.stream()
                .filtered { shouldTreatAsFind(it) }
                .count()
                .toInt()
            val stored = beforeCount - afterCount

            if (stored > 0) {
                log("STORE", "Added $stored find(s) to storage crate.")
            }
        }
    }

    private fun shouldTreatAsFind(item: Item): Boolean {
        if (!item.valid() || item.id() == -1) {
            return false
        }

        val normName = item.name().lowercase().trim()

        // User explicitly wants to keep item → never store it in the crate.
        if (normName in config.keepItemNames) {
            return false
        }

        // Only common artefacts are crate-eligible.
        return normName in commonArtefactNamesLc
    }

    // ------------------------------------------------------------------------
    // Junk classification + dropping
    // ------------------------------------------------------------------------

    private fun computeJunkItems(): List<Item> {
        if (Inventory.isEmpty()) {
            return emptyList()
        }

        val keepNames = config.keepItemNames
        val cleaningCounts = mutableMapOf<Int, Int>()
        val junk = mutableListOf<Item>()

        Inventory.stream()
            .filtered { it.valid() && it.id() != -1 }
            .forEach { item ->
                val id = item.id()
                val normName = item.name().lowercase().trim()

                // Never drop explicit keep-list items.
                if (normName in keepNames) {
                    return@forEach
                }

                // Never drop Uncleaned finds – those should be cleaned.
                if (id == C.UNCLEANED_FIND) {
                    return@forEach
                }

                // Lamps are always consumed via useLamps().
                if (id == C.ANTIQUE_LAMP) {
                    return@forEach
                }

                // Cleaning tools – keep at most one of each.
                if (id in cleaningToolIds) {
                    val count = cleaningCounts.getOrDefault(id, 0)
                    if (count == 0) {
                        cleaningCounts[id] = 1
                    } else {
                        junk.add(item)
                    }
                    return@forEach
                }

                // Leather gloves/boots – prefer them equipped; treat extras in inventory as junk.
                if (id == C.LEATHER_GLOVES || id == C.LEATHER_BOOTS) {
                    val equipped = Equipment.stream().id(id).isNotEmpty()
                    if (equipped) {
                        junk.add(item)
                    }
                    return@forEach
                }

                // Unique artefacts are “junk” once turned in.
                if (normName in uniqueArtefactNamesLc) {
                    junk.add(item)
                    return@forEach
                }

                // Storage crate rewards + unremarkable finds are also junk.
                if (normName in storageRewardNamesLc || normName in unremarkableFindNamesLc) {
                    junk.add(item)
                    return@forEach
                }

                // Common artefacts are kept for the storage crate.
                if (normName in commonArtefactNamesLc) {
                    return@forEach
                }

                // Everything else is considered junk.
                junk.add(item)
            }

        return junk
    }

    private fun dropInventoryExceptKeep(reason: String) {
        val toDrop = computeJunkItems()
        if (toDrop.isEmpty()) {
            log("DROP", "No junk to drop for $reason.")
            return
        }

        log("DROP", "Dropping ${toDrop.size} junk item(s) for $reason.")
        DropUtils.ensureInventoryTab()

        toDrop.shuffled().forEach { item ->
            if (!item.valid()) return@forEach
            if (DropUtils.dropItemWithTapSupport(item)) {
                Condition.sleep(Random.nextInt(350, 650))
            }
        }
    }

    private fun shouldBankKeepItems(): Boolean {
        if (!Inventory.isFull()) return false
        if (hasUncleanedFinds()) return false
        if (hasFindsToStore()) return false
        if (hasJunkToDrop()) return false

        val keepNames = config.keepItemNames
        if (keepNames.isEmpty()) {
            return false
        }

        val hasKeepItem = Inventory.stream()
            .filtered {
                it.valid() &&
                        it.id() != -1 &&
                        it.name().lowercase().trim() in keepNames
            }
            .isNotEmpty()

        return hasKeepItem
    }

    private fun bankKeepItems(): Boolean {
        val done = BankUtils.depositAllExceptToNearestBank(
            keepItemIds = C.INITIAL_KEEP_FOR_BANK,
            preferredBankTile = C.VARROCK_EAST_BANK_TILE,
            maxPreferredDistance = 200.0,
            logPrefix = "[Varrock Museum] BANK (keep items) |"
        )

        if (done) {
            log("BANK", "Deposited keep items at Varrock East bank; returning to museum.")
        } else {
            log("BANK", "Walking to Varrock East bank to deposit keep items.")
        }

        return done
    }

    // ------------------------------------------------------------------------
    // Requirements & tap-to-drop
    // ------------------------------------------------------------------------

    private fun isDigSiteCompleted(): Boolean {
        try {
            val varpState = Varpbits.value(C.QUEST_DIGSITE_ID)
            log("QUEST", "Dig Site varp value (29) = $varpState")
        } catch (_: Throwable) {
            // Ignore failures here; just logging.
        }

        return try {
            val done = Quests.Quest.THE_DIG_SITE.completed()
            log("QUEST", "The Dig Site completed() = $done")
            done
        } catch (t: Throwable) {
            log(
                "QUEST",
                "Error checking Quests.Quest.THE_DIG_SITE.completed(): " +
                        "${t::class.java.simpleName}: ${t.message}. Treating quest as completed for now."
            )
            true
        }
    }

    private fun checkSelectedSkillLevelOrStop(): Boolean {
        val trackedSkill = lampSkill.trackedSkill
        if (trackedSkill == null) {
            log(
                "STARTUP",
                "No trackedSkill mapping for ${lampSkill.displayName} (e.g. Sailing). " +
                        "Skipping level 10 pre-check; game will enforce requirement."
            )
            return true
        }

        return try {
            val lvl = Skills.realLevel(trackedSkill.index)
            if (lvl < 10) {
                log(
                    "STARTUP",
                    "${lampSkill.displayName} level is $lvl (<10). " +
                            "The Dig Site reward lamps require level 10 in the chosen skill. Stopping script."
                )
                controller.stop()
                false
            } else {
                log("STARTUP", "${lampSkill.displayName} level is $lvl (>=10).")
                true
            }
        } catch (t: Throwable) {
            log(
                "STARTUP",
                "Failed to read level for ${lampSkill.displayName}: " +
                        "${t::class.java.simpleName}: ${t.message}. Skipping pre-check; game will enforce requirement."
            )
            true
        }
    }

    private fun checkTapToDrop() {
        log("SETUP", "Checking tap-to-drop / shift-drop status.")

        val enabled = DropUtils.requireTapToDrop {
            log("SETUP", "TAP-TO-DROP IS DISABLED.")
            log("SETUP", "The script expects tap-to-drop for fast, simple drops.")
            log("SETUP", "Please enable it in-game (DROP hotkey icon or Settings → Controls → Tap to Drop).")
            log("SETUP", "Stopping script because tap-to-drop is disabled.")
            controller.stop()
        }

        if (enabled) {
            log("SETUP", "Tap-to-drop is ENABLED.")
        }
    }

    // ------------------------------------------------------------------------
    // Lamps + paint
    // ------------------------------------------------------------------------

    private fun lampsPerHour(): Int {
        val runMillis = System.currentTimeMillis() - startTime
        if (runMillis <= 0L || lampsUsed <= 0) return 0

        return ((lampsUsed.toDouble() * 3_600_000.0) / runMillis.toDouble()).toInt()
    }

    private fun initPaint() {
        val chosen = lampSkill

        val builder = PaintBuilder.newBuilder()
            .x(10)
            .y(45)

        chosen.trackedSkill?.let { builder.trackSkill(it) }

        builder
            .addString("Lamp skill:") { chosen.displayName }
            .addString("Lamps used:") { lampsUsed.toString() }
            .addString("Lamps/hr:") { lampsPerHour().toString() }

        paint = builder.build()
        addPaint(paint!!)
    }

    private fun canUseLampOnSelectedSkill(): Boolean {
        val trackedSkill = lampSkill.trackedSkill
        if (trackedSkill == null) {
            log(
                "LAMP",
                "No trackedSkill mapping for ${lampSkill.displayName} (e.g. Sailing). " +
                        "Skipping level 10 check; letting game enforce."
            )
            return true
        }

        return try {
            val lvl = Skills.realLevel(trackedSkill.index)
            if (lvl < 10) {
                log(
                    "LAMP",
                    "${lampSkill.displayName} is only level $lvl (<10). " +
                            "Cannot use lamp on this skill. Stopping script."
                )
                controller.stop()
                false
            } else {
                true
            }
        } catch (t: Throwable) {
            log(
                "LAMP",
                "Failed to read level for ${lampSkill.displayName}: " +
                        "${t::class.java.simpleName}: ${t.message}. Allowing lamp use; game will enforce requirement."
            )
            true
        }
    }

    private fun useLamps() {
        try {
            val count = Inventory.stream().id(C.ANTIQUE_LAMP).count().toInt()
            log("LAMP", "Found $count Antique lamp(s). Using on ${lampSkill.displayName}.")

            DropUtils.ensureInventoryTab()

            repeat(count) {
                val lamp = Inventory.stream().name(C.ANTIQUE_LAMP).first()
                if (!lamp.valid()) return

                if (!canUseLampOnSelectedSkill()) {
                    return
                }

                if (!lamp.interact("Rub")) {
                    log("LAMP", "Failed to interact with Antique lamp.")
                    return
                }

                // Wait for the Antique lamp widget to be present.
                Condition.wait(
                    { WidgetIds.AntiqueLamp.ROOT.widget().valid() },
                    500,
                    20
                )

                val widget = WidgetIds.AntiqueLamp.ROOT.widget()
                if (!widget.valid()) {
                    log("LAMP", "Skill selection widget (${WidgetIds.AntiqueLamp.ROOT.group}) not found.")
                    return
                }

                // Click skill by action name (e.g. "Slayer")
                val skillComponent = findSkillComponentInLampWidget(widget, lampSkill)
                if (skillComponent == null || !skillComponent.valid()) {
                    log("LAMP", "Could not find skill '${lampSkill.displayName}' in lamp widget.")
                    return
                }

                log(
                    "LAMP",
                    "Interacting with component ${lampSkill.widgetIndex} using action '${lampSkill.displayName}'."
                )

                if (!skillComponent.interact(lampSkill.displayName)) {
                    log("LAMP", "interact('${lampSkill.displayName}') failed, falling back to click().")
                    if (!skillComponent.click()) {
                        log("LAMP", "Failed to click skill '${lampSkill.displayName}'.")
                        return
                    }
                }

                // Wait for confirm component and click it.
                Condition.wait(
                    { WidgetIds.AntiqueLamp.CONFIRM.component().visible() },
                    500,
                    20
                )

                val confirm = WidgetIds.AntiqueLamp.CONFIRM.component()
                if (confirm.valid() && confirm.visible()) {
                    if (!confirm.interact("Confirm") && !confirm.click()) {
                        log("LAMP", "Failed to confirm lamp XP choice.")
                        return
                    }
                } else {
                    log("LAMP", "Confirm component not visible; assuming lamp was already consumed.")
                }

                // Let XP drop and dialogues settle.
                Condition.sleep(1200)
                DialogueHandler.spamClickContinue(maxAttempts = 5)

                lampsUsed++
            }
        } catch (t: Throwable) {
            log(
                "LAMP",
                "Unexpected error using Antique lamps: ${t::class.java.simpleName}: ${t.message}"
            )
        }
    }

    private fun findSkillComponentInLampWidget(widget: Widget, skill: LampSkill): Component? {
        // Direct index
        val indexed = widget.component(skill.widgetIndex)
        if (indexed.valid()) return indexed

        // Fallback: search by text/tooltip
        return Components.stream()
            .widget(widget.id())
            .filtered { comp ->
                comp.valid() &&
                        (comp.text().contains(skill.displayName, ignoreCase = true) ||
                                comp.tooltip().contains(skill.displayName, ignoreCase = true))
            }
            .first()
    }
}
