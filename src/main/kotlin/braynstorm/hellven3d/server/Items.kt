package braynstorm.hellven3d.server


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

class Item(val id: Int, val type: Short, val subtype: Short?) {
	companion object {
		private val items = mutableListOf<Item>()

		fun reloadItems() {
			val tripplets = DB.internalGetItemList()
			items.clear()
			tripplets.mapTo(items, {
				Item(it.first, it.second, it.third)
			})

		}
	}
}

class ItemStack(val item: Item, val amount: Int = 1)
