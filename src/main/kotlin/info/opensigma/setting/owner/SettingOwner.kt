package info.opensigma.setting.owner

import info.opensigma.setting.Setting
import info.opensigma.system.ElementRepository
import info.opensigma.system.INameable

@Suppress("UNCHECKED_CAST")
class SettingOwner<T>(owner: INameable) : ElementRepository<Setting<T>>("${owner.name}-settings",
    toScan = arrayOf(owner))
