package hellven3d.server


/**
 * Equipment slots for a character.
 */
enum class EquipmentSlot(val value: Int) {
	MAIN_HAND(0),
	OFF_HAND(1),

	HEAD(2),
	SHOULDER(3),
	CHEST(4),
	WAIST(5),
	WRIST(6),
	HANDS(7),
	LEGS(8),
	FEET(9),

	ACCESSORY_1(10),
	ACCESSORY_2(11),
	ACCESSORY_3(12),
	ACCESSORY_4(13),
	ACCESSORY_5(14);

	companion object {
		operator fun get(id: Int): EquipmentSlot = EquipmentSlot.values().associateBy { it.value }[id] ?: throw IllegalArgumentException()
	}
}

class Item(val id: Int, val type: Short, val subtype: Short) {
	companion object {
		private val logger by lazyLogger()
		private val items = hashMapOf<Int, Item>()

		fun reloadItems() {
			logger.warn("Reloading items.")

			val triplets = DB.internalGetItemList()

			items.clear()

			triplets.forEach {
				items += it.first to Item(it.first, it.second, it.third)
			}

		}

		operator fun get(id: Int): Item? = items[id]
	}
}

class ItemStack(val item: Item, val amount: Int = 1, val itemData: Map<String, Int> = emptyMap())
