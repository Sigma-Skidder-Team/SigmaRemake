package info.opensigma.module

import info.opensigma.setting.owner.SettingOwner
import info.opensigma.system.INameable
import info.opensigma.system.IMinecraft
import info.opensigma.OpenSigma
import info.opensigma.module.data.ModuleCategory

open class Module(
    override val name: String,
    val description: String,
    var key: Int = 0,
    val category: ModuleCategory = ModuleCategory.MISC
) : INameable, IMinecraft {

    protected val settings: SettingOwner<Any> = SettingOwner(this)
    private var enabled: Boolean = false

    init {
        OpenSigma.instance.bindManager.add({ key }, this::toggle)
    }

    fun toggle() {
        setEnabled(!enabled)
    }

    fun setEnabled(enabled: Boolean) {
        if (enabled) enable() else disable()
    }

    fun enable() {
        if (this.enabled) return
        enabled = true
        onEnable()
        OpenSigma.instance.eventBus.subscribe(this)
    }

    fun disable() {
        if (!this.enabled) return
        enabled = false
        OpenSigma.instance.eventBus.subscribe(false)
        onDisable()
    }

    @Suppress("EmptyMethod")
    protected open fun onEnable() {}

    @Suppress("EmptyMethod")
    protected open fun onDisable() {}
}