package io.github.sst.remake.gui.screen.altmanager;

import com.google.gson.JsonObject;
import io.github.sst.remake.Client;
import io.github.sst.remake.data.alt.Account;
import io.github.sst.remake.data.alt.AccountCompareType;
import io.github.sst.remake.data.alt.AccountSorter;
import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.Screen;
import io.github.sst.remake.gui.framework.widget.*;
import io.github.sst.remake.gui.framework.widget.internal.AlertComponent;
import io.github.sst.remake.gui.framework.widget.internal.ComponentType;
import io.github.sst.remake.gui.framework.widget.internal.DropdownMenu;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.http.CookieLoginUtils;
import io.github.sst.remake.util.http.MicrosoftUtils;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.math.vec.VecUtils;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.image.Resources;
import io.github.sst.remake.util.system.io.FileUtils;
import io.github.sst.remake.util.system.io.audio.SoundUtils;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.option.ServerList;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AltManagerScreen extends Screen implements IMinecraft {
    private int backgroundOffsetX;
    private float backgroundOffsetY;
    private boolean isBackgroundInitialized = true;

    private ScrollablePanel accountListPanel;
    private final ScrollablePanel accountDetailPanelContainer;
    private final AccountDetailPanel accountDetailsPanel;
    private final TextField searchBox;

    private Alert loginDialog;
    private Alert deleteAlert;

    private final float leftPaneRatio = 0.65F;
    private final float rightPaneRatio = 1.0F - this.leftPaneRatio;
    private final int titleOffset = 30;

    private AccountCompareType accountSortType = AccountCompareType.ADDED;
    private String accountFilter = "";

    public static AltManagerScreen instance;

    public AltManagerScreen() {
        super("Alt Manager");
        instance = this;
        this.setListening(false);

        List<String> sortingOptions = Arrays.asList("Alphabetical", "Bans", "Date Added", "Last Used", "Use count");
        List<String> servers = fetchAvailableServers();

        setupDialogs();

        int width = client.getWindow().getWidth();
        int height = client.getWindow().getHeight();

        this.addToList(this.accountListPanel =
                new ScrollablePanel(this, "alts", 0, 114, (int) (width * leftPaneRatio) - 4, height - 119 - titleOffset));

        this.addToList(this.accountDetailPanelContainer =
                new ScrollablePanel(this, "altView", (int) (width * leftPaneRatio), 114, (int) (width * rightPaneRatio) - titleOffset, height - 119 - titleOffset));

        this.accountListPanel.setListening(false);
        this.accountDetailPanelContainer.setListening(false);
        this.accountListPanel.setScissorEnabled(false);

        this.accountDetailPanelContainer.addToList(this.accountDetailsPanel =
                new AccountDetailPanel(
                        this.accountDetailPanelContainer,
                        "info",
                        (int) ((width * rightPaneRatio - (int) (width * rightPaneRatio)) / 2) - 10,
                        getDetailsPanelYOffset(),
                        (int) (width * rightPaneRatio),
                        500
                ));

        Dropdown filterDropdown = new Dropdown(this, "drop", (int) (width * leftPaneRatio) - 220, 44, 200, 32, sortingOptions, 0);
        filterDropdown.addSubMenu(servers, 1);
        filterDropdown.setSelectedIndex(2);
        filterDropdown.onPress(widget -> handleFilterChange(filterDropdown));
        this.addToList(filterDropdown);

        this.addToList(this.searchBox =
                new TextField(this, "textbox", (int) (width * leftPaneRatio), 44, 150, 32, TextField.DEFAULT_COLORS, "", "Search..."));
        this.searchBox.setFont(FontUtils.HELVETICA_LIGHT_18);
        this.searchBox.addChangeListener(text -> updateAccountList(false));

        TextButton addButton = new TextButton(this, "btnt", this.getWidth() - 90, 43, 70, 30, ColorHelper.DEFAULT_COLOR, "Add +", FontUtils.HELVETICA_LIGHT_25);
        addButton.onClick((parent, mouseButton) -> {
            if (canOpenLoginDialog()) loginDialog.setOpen(!loginDialog.isHovered());
        });
        this.addToList(addButton);

        // Populate on open
        updateAccountList(true);
    }

    private List<String> fetchAvailableServers() {
        List<String> servers = new ArrayList<>();
        ServerList list = new ServerList(client);
        list.loadFile();
        for (int i = 0; i < list.size(); i++) {
            String address = list.get(i).address;
            if (!servers.contains(address)) servers.add(address);
        }
        return servers;
    }

    private void handleFilterChange(Dropdown dropdown) {
        int index = dropdown.getSelectedIndex();

        // Reset filter unless BANS is chosen
        if (index != 1) this.accountFilter = "";

        if (index == 1) {
            DropdownMenu sub = dropdown.getSubMenu(1);
            List<String> banList = sub.getValues();
            this.accountFilter = (!banList.isEmpty() && sub.getSelectedIndex() < banList.size())
                    ? banList.get(sub.getSelectedIndex())
                    : "";
            this.accountSortType = AccountCompareType.BANS;
        } else {
            switch (index) {
                case 0:
                    this.accountSortType = AccountCompareType.ALPHABETICAL;
                    break;
                case 3:
                    this.accountSortType = AccountCompareType.LAST_USED;
                    break;
                case 4:
                    this.accountSortType = AccountCompareType.USE_COUNT;
                    break;
                case 2:
                default:
                    this.accountSortType = AccountCompareType.ADDED;
                    break;
            }
        }

        updateAccountList(false);
    }

    private void setupDialogs() {
        AlertComponent header = new AlertComponent(ComponentType.HEADER, "Add Alt", 50);
        this.addToList(this.loginDialog = new Alert(this, "Add alt dialog", header,
                new AlertComponent(ComponentType.FIRST_LINE, "Login with your minecraft", 15),
                new AlertComponent(ComponentType.FIRST_LINE, "account here!", 25),
                new AlertComponent(ComponentType.SECOND_LINE, "Username", 50),
                new AlertComponent(ComponentType.BUTTON, "Cracked login", 50),
                new AlertComponent(ComponentType.BUTTON, "Cookie login", 50),
                new AlertComponent(ComponentType.BUTTON, "Web login", 50)));

        this.loginDialog.onPress(widget -> {
            Button clicked = loginDialog.getClickedButton();
            if (clicked == null) return;

            switch (clicked.getText()) {
                case "Cracked login":
                    processCrackedLogin();
                    break;
                case "Cookie login":
                    processCookieLogin();
                    break;
                case "Web login":
                    processWebLogin();
                    break;
            }
        });

        this.addToList(this.deleteAlert = new Alert(this, "delete",
                new AlertComponent(ComponentType.HEADER, "Delete?", 50),
                new AlertComponent(ComponentType.FIRST_LINE, "Are you sure you want", 15),
                new AlertComponent(ComponentType.FIRST_LINE, "to delete this alt?", 40),
                new AlertComponent(ComponentType.BUTTON, "Delete", 50)));
    }

    private void processCrackedLogin() {
        String username = this.loginDialog.getInputMap().get("Username");
        if (username != null && !username.isEmpty()) {
            Account account = new Account(username, "0", Account.STEVE_UUID);
            if (!Client.INSTANCE.accountManager.has(account)) {
                Client.INSTANCE.accountManager.add(account);
                Client.INSTANCE.configManager.saveAlts();
            }
        }
        updateAccountList(false);
    }

    private void processCookieLogin() {
        File file = FileUtils.openTxtFile();
        if (file == null) return;

        try {
            CookieLoginUtils.LoginData session = CookieLoginUtils.loginWithCookie(file);
            if (session == null) {
                SoundUtils.play("error");
                return;
            }

            Account account = new Account(session.username, session.playerID, session.token);
            if (!Client.INSTANCE.accountManager.has(account)) {
                Client.INSTANCE.accountManager.add(account);
                Client.INSTANCE.configManager.saveAlts();
            }

            updateAccountList(false);
        } catch (Exception e) {
            SoundUtils.play("error");
            updateAccountList(false);
        }
    }

    private void processWebLogin() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        MicrosoftUtils.acquireMSAuthCode(executor)
                .thenComposeAsync(code -> MicrosoftUtils.acquireMSAccessToken(code, executor), executor)
                .thenComposeAsync(access -> MicrosoftUtils.acquireXboxAccessToken(access, executor), executor)
                .thenComposeAsync(xbox -> MicrosoftUtils.acquireXboxXstsToken(xbox, executor), executor)
                .thenComposeAsync(xsts -> MicrosoftUtils.acquireMCAccessToken(xsts.get("Token"), xsts.get("uhs"), executor), executor)
                .thenComposeAsync(mc -> MicrosoftUtils.login(mc, executor), executor)
                .thenAccept(session -> {
                    try {
                        Account account = new Account(session.getUsername(), session.getAccessToken(), session.getUuid());
                        if (!Client.INSTANCE.accountManager.has(account)) {
                            Client.INSTANCE.accountManager.add(account);
                            Client.INSTANCE.configManager.saveAlts();
                        }
                        updateAccountList(false);
                    } finally {
                        executor.shutdown();
                    }
                })
                .exceptionally(err -> {
                    try {
                        Client.LOGGER.error("Auth failed", err);
                        SoundUtils.play("error");
                        updateAccountList(false);
                    } finally {
                        executor.shutdown();
                    }
                    return null;
                });
    }

    private void addAccountEntry(Account account, boolean animateIn) {
        AccountListEntry entry;
        this.accountListPanel.addToList(entry = new AccountListEntry(
                this.accountListPanel,
                account.name,
                this.titleOffset,
                (100 + this.titleOffset / 2) * getAccountEntryCount(),
                this.accountListPanel.getWidth() - this.titleOffset * 2 + 4,
                100,
                account
        ));

        // If you don't want the slide-in animation for a refresh, match old behavior.
        if (!animateIn) {
            // Assumes your new AccountListEntry exposes this like the old one did.
            entry.slideAnim = new AnimationUtils(0, 0);
        }

        if (Client.INSTANCE.accountManager.currentAccount == account) {
            entry.setAccountListRefreshing(true);
        }

        entry.addMouseButtonCallback((screen, mouseButton) -> {
            // Right click => delete
            if (mouseButton != 0) {
                this.deleteAlert.onPress(w -> {
                    // remove the entry account
                    Client.INSTANCE.accountManager.remove(entry.account);
                    this.accountDetailsPanel.handleSelectedAccount(null);
                    this.updateAccountList(false);
                });
                this.deleteAlert.setFocused(true);
                this.deleteAlert.setOpen(true);
                return;
            }

            // Left click => login + select
            this.loginToAccount(entry);
            this.accountDetailsPanel.handleSelectedAccount(entry.account);

            // Clear selection
            for (GuiComponent container : this.accountListPanel.getChildren()) {
                if (container instanceof VerticalScrollBar) continue;
                for (GuiComponent child : container.getChildren()) {
                    if (child instanceof AccountListEntry) {
                        ((AccountListEntry) child).setSelected(false);
                    }
                }
            }

            entry.setSelected(true);
        });

        // If currently logged in, keep it selected and show details
        if (Client.INSTANCE.accountManager.currentAccount == account) {
            this.accountDetailsPanel.handleSelectedAccount(entry.account);
            entry.setSelected(true, true);
        }
    }

    private int getAccountEntryCount() {
        int count = 0;
        for (GuiComponent container : this.accountListPanel.getChildren()) {
            if (container instanceof VerticalScrollBar) continue;
            for (GuiComponent ignored : container.getChildren()) {
                count++;
            }
        }
        return count;
    }

    public void loginToAccount(AccountListEntry entry) {
        entry.setLoadingIndicator(true);
        new Thread(() -> {
            boolean success = Client.INSTANCE.accountManager.login(entry.account);
            this.addRunnable(() -> {
                if (!success) {
                    entry.setErrorBlinkTicks(114);
                    SoundUtils.play("error");
                } else {
                    clearRefreshingState();
                    entry.setAccountListRefreshing(true);
                    SoundUtils.play("connect");
                    updateAccountList(false);
                }
                entry.setLoadingIndicator(false);
            });
        }).start();
    }

    @Override
    public void draw(float partialTicks) {
        drawParallaxBackground();
        RenderUtils.drawFloatingPanel(
                (int) (client.getWindow().getWidth() * leftPaneRatio),
                114,
                (int) (client.getWindow().getWidth() * rightPaneRatio) - titleOffset,
                client.getWindow().getHeight() - 119 - titleOffset,
                ClientColors.LIGHT_GREYISH_BLUE.getColor()
        );
        updateEntrySlideAnimations();
        drawHeaderTitle();
        super.draw(partialTicks);
    }

    private void drawHeaderTitle() {
        int xPos = this.x + titleOffset;
        int yPos = this.y + titleOffset;
        int color = ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.8F);
        RenderUtils.drawString(FontUtils.HELVETICA_LIGHT_40, (float) xPos, (float) yPos, "Jello", color);
        RenderUtils.drawString(FontUtils.HELVETICA_LIGHT_25, (float) (xPos + 87), (float) (yPos + 15), "Alt Manager", color);
    }

    private void updateEntrySlideAnimations() {
        for (GuiComponent container : this.accountListPanel.getChildren()) {
            if (container instanceof VerticalScrollBar) continue;
            for (GuiComponent child : container.getChildren()) {
                if (!(child instanceof AccountListEntry)) continue;
                AccountListEntry entry = (AccountListEntry) child;

                if (child.getY() <= client.getWindow().getHeight() && accountListPanel.getScrollOffset() == 0) {
                    entry.slideAnim.changeDirection(AnimationUtils.Direction.BACKWARDS);
                    float prog = VecUtils.interpolate(entry.slideAnim.calcPercent(), 0.51, 0.82, 0.0, 0.99);
                    entry.setTranslateX((int) (-((1.0F - prog) * (child.getWidth() + 30))));
                } else {
                    entry.setTranslateX(0);
                    entry.slideAnim.changeDirection(AnimationUtils.Direction.BACKWARDS);
                }
            }
        }
    }

    private void clearRefreshingState() {
        for (GuiComponent container : this.accountListPanel.getChildren()) {
            if (container instanceof VerticalScrollBar) continue;
            for (GuiComponent child : container.getChildren()) {
                if (child instanceof AccountListEntry) {
                    ((AccountListEntry) child).setAccountListRefreshing(false);
                }
            }
        }
    }

    private boolean canOpenLoginDialog() {
        for (GuiComponent container : this.accountListPanel.getChildren()) {
            if (container instanceof VerticalScrollBar) continue;
            for (GuiComponent child : container.getChildren()) {
                if (child.getTranslateX() != 0 && child.getX() > this.width) return false;
            }
        }
        return true;
    }

    private void drawParallaxBackground() {
        int mouseX = -this.getMouseX();
        float mouseY = -(float) this.getMouseY() / this.getWidth() * 114.0F;

        if (isBackgroundInitialized) {
            backgroundOffsetY = (int) mouseY;
            backgroundOffsetX = mouseX;
            isBackgroundInitialized = false;
        }

        backgroundOffsetY += (mouseY - backgroundOffsetY) * 0.5F;
        backgroundOffsetX += (int) ((mouseX - backgroundOffsetX) * 0.5F);

        RenderUtils.drawImage((float) backgroundOffsetX, backgroundOffsetY, (float) (getWidth() * 2), (float) (getHeight() + 114), Resources.MENU_PANORAMA);
        RenderUtils.drawRoundedRect(0.0F, 0.0F, (float) getWidth(), (float) getHeight(), ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.95F));
    }

    public void updateAccountList(boolean forceRefresh) {
        List<Account> accounts = AccountSorter.sortByInputAltAccounts(accountSortType, accountFilter, searchBox.getText());
        this.addRunnable(() -> {
            int oldScroll = 0;

            if (accountListPanel != null) {
                oldScroll = accountListPanel.getScrollOffset();
                this.removeChildren(accountListPanel);
            }

            GuiComponent existing = this.getChildByName("alts");
            if (existing != null) {
                this.removeChildren(existing);
            }

            this.showAlert(this.accountListPanel = new ScrollablePanel(
                    this,
                    "alts",
                    0,
                    114,
                    (int) (client.getWindow().getWidth() * leftPaneRatio) - 4,
                    client.getWindow().getHeight() - 119 - titleOffset
            ));

            for (Account acc : accounts) {
                addAccountEntry(acc, forceRefresh);
            }

            accountListPanel.setScrollOffset(oldScroll);
            accountListPanel.setListening(false);
            accountListPanel.setScissorEnabled(false);
        });
    }

    public int getDetailsPanelYOffset() {
        return client.getWindow().getHeight() / 12 + 280 + client.getWindow().getHeight() / 12;
    }

    @Override
    public void keyPressed(int keyCode) {
        super.keyPressed(keyCode);
        if (keyCode == 256) client.openScreen(new TitleScreen());
    }

    @Override
    public JsonObject toPersistedConfig(JsonObject config) {
        Client.INSTANCE.accountManager.shutdown();
        return config;
    }

    @Override
    public void loadPersistedConfig(JsonObject config) {
        updateAccountList(true);
    }
}
