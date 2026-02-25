package io.github.sst.remake.gui.screen.altmanager;

import com.google.gson.JsonObject;
import io.github.sst.remake.Client;
import io.github.sst.remake.data.alt.Account;
import io.github.sst.remake.data.alt.AccountCompareType;
import io.github.sst.remake.data.alt.AccountSorter;
import io.github.sst.remake.gui.framework.core.GuiComponent;
import io.github.sst.remake.gui.framework.core.Screen;
import io.github.sst.remake.gui.framework.widget.internal.AlertComponent;
import io.github.sst.remake.gui.framework.widget.internal.ComponentType;
import io.github.sst.remake.gui.framework.widget.*;
import io.github.sst.remake.util.IMinecraft;
import io.github.sst.remake.util.http.CookieLoginUtils;
import io.github.sst.remake.util.http.MicrosoftUtils;
import io.github.sst.remake.util.system.io.FileUtils;
import io.github.sst.remake.util.system.io.audio.SoundUtils;
import io.github.sst.remake.util.math.anim.AnimationUtils;
import io.github.sst.remake.util.math.color.ClientColors;
import io.github.sst.remake.util.math.color.ColorHelper;
import io.github.sst.remake.util.math.vec.VecUtils;
import io.github.sst.remake.util.render.RenderUtils;
import io.github.sst.remake.util.render.font.FontUtils;
import io.github.sst.remake.util.render.image.Resources;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.client.option.ServerList;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AltManagerScreen extends Screen implements IMinecraft {
    private int backgroundOffsetX;
    private float backgroundOffsetY;
    private boolean backgroundOffsetInit = true;
    private ScrollablePanel accountListPanel;
    private final ScrollablePanel accountDetailPanelContainer;
    private Alert loginDialog;
    private Alert deleteAlert;
    private final float leftPaneRatio = 0.65F;
    private final float rightPaneRatio = 1.0F - this.leftPaneRatio;
    private final int titleOffset = 30;
    private final AccountDetailPanel accountDetailsPanel;
    private AccountCompareType accountSortType = AccountCompareType.ADDED;
    private String accountFilter = "";
    private final TextField searchBox;

    public static AltManagerScreen instance;

    public AltManagerScreen() {
        super("Alt Manager");
        instance = this;
        this.setListening(false);
        List<String> sortingOptions = new ArrayList<>();
        sortingOptions.add("Alphabetical");
        sortingOptions.add("Bans");
        sortingOptions.add("Date Added");
        sortingOptions.add("Last Used");
        sortingOptions.add("Use count");
        List<String> servers = new ArrayList<>();
        ServerList serverList = new ServerList(client);
        serverList.loadFile();
        int serverListSize = serverList.size();

        for (int i = 0; i < serverListSize; i++) {
            ServerInfo server = serverList.get(i);
            if (!servers.contains(server.address)) {
                servers.add(server.address);
            }
        }

        this.getLoginDialog();
        this.deleteAltAlert();
        this.addToList(
                this.accountListPanel = new ScrollablePanel(
                        this,
                        "alts",
                        0,
                        114,
                        (int) ((float) client.getWindow().getWidth() * this.leftPaneRatio) - 4,
                        client.getWindow().getHeight() - 119 - this.titleOffset
                )
        );
        this.addToList(
                this.accountDetailPanelContainer = new ScrollablePanel(
                        this,
                        "altView",
                        (int) ((float) client.getWindow().getWidth() * this.leftPaneRatio),
                        114,
                        (int) ((float) client.getWindow().getWidth() * this.rightPaneRatio) - this.titleOffset,
                        client.getWindow().getHeight() - 119 - this.titleOffset
                )
        );
        this.accountListPanel.setListening(false);
        this.accountDetailPanelContainer.setListening(false);
        this.accountListPanel.setScissorEnabled(false);
        this.accountDetailPanelContainer
                .addToList(
                        this.accountDetailsPanel = new AccountDetailPanel(
                                this.accountDetailPanelContainer,
                                "info",
                                (int) (
                                        (float) client.getWindow().getWidth() * this.rightPaneRatio
                                                - (float) ((int) ((float) client.getWindow().getWidth() * this.rightPaneRatio))
                                )
                                        / 2
                                        - 10,
                                this.getDetailsPanelYOffset(),
                                (int) ((float) client.getWindow().getWidth() * this.rightPaneRatio),
                                500
                        )
                );
        Dropdown filterDropdown = new Dropdown(this, "drop", (int) ((float) client.getWindow().getWidth() * this.leftPaneRatio) - 220, 44, 200, 32, sortingOptions, 0);
        filterDropdown.addSubMenu(servers, 1);
        filterDropdown.setIndex(2);
        this.addToList(filterDropdown);
        filterDropdown.onPress(interactiveWidget -> {
            switch (filterDropdown.getIndex()) {
                case 0:
                    this.accountSortType = AccountCompareType.ALPHABETICAL;
                    break;
                case 1:
                    this.accountSortType = AccountCompareType.BANS;
                    List<String> banList = filterDropdown.getSubMenu(1).getValues();
                    int index = filterDropdown.getSubMenu(1).getSelectedIndex();

                    if (!banList.isEmpty() && index < banList.size()) {
                        this.accountFilter = banList.get(index);
                    } else {
                        this.accountFilter = "";
                    }
                    break;
                case 2:
                    this.accountSortType = AccountCompareType.ADDED;
                    break;
                case 3:
                    this.accountSortType = AccountCompareType.LAST_USED;
                    break;
                case 4:
                    this.accountSortType = AccountCompareType.USE_COUNT;
            }

            this.updateAccountList(false);
        });
        this.addToList(
                this.searchBox = new TextField(
                        this,
                        "textbox",
                        (int) ((float) client.getWindow().getWidth() * this.leftPaneRatio),
                        44,
                        150,
                        32,
                        TextField.DEFAULT_COLORS,
                        "",
                        "Search..."
                )
        );
        this.searchBox.setFont(FontUtils.HELVETICA_LIGHT_18);
        this.searchBox.addChangeListener(var1 -> this.updateAccountList(false));
        TextButton addButton;
        this.addToList(addButton = new TextButton(this, "btnt", this.getWidth() - 90, 43, 70, 30, ColorHelper.DEFAULT_COLOR, "Add +", FontUtils.HELVETICA_LIGHT_25));
        this.accountListPanel.requestFocus();
        addButton.onClick((parent, mouseButton) -> {
            if (this.canOpenLoginDialog()) {
                this.loginDialog.setOpen(!this.loginDialog.isHovered());
            }
        });
    }

    private void addAccountEntry(Account account, boolean animateIn) {
        AccountListEntry accountListEntry;
        this.accountListPanel.addToList(
                accountListEntry = new AccountListEntry(
                        this.accountListPanel,
                        account.name,
                        this.titleOffset,
                        (100 + this.titleOffset / 2) * this.getAccountEntryCount(),
                        this.accountListPanel.getWidth() - this.titleOffset * 2 + 4,
                        100,
                        account
                )
        );
        if (!animateIn) {
            accountListEntry.entrySlideAnim = new AnimationUtils(0, 0);
        }

        if (Client.INSTANCE.accountManager.currentAccount == account) {
            accountListEntry.setAccountListRefreshing(true);
        }

        accountListEntry.addMouseButtonCallback((screen, mouseButton) -> {
            if (mouseButton != 0) {
                this.deleteAlert.onPress(interactiveWidget -> {
                    Client.INSTANCE.accountManager.remove(accountListEntry.selectedAccount);
                    this.accountDetailsPanel.handleSelectedAccount(null);
                    this.updateAccountList(false);
                });
                this.deleteAlert.setFocused(true);
                this.deleteAlert.setOpen(true);
            } else {
                this.loginToAccount(accountListEntry);

                this.accountDetailsPanel.handleSelectedAccount(accountListEntry.selectedAccount);

                for (GuiComponent var7 : this.accountListPanel.getChildren()) {
                    if (!(var7 instanceof VerticalScrollBar)) {
                        for (GuiComponent var9 : var7.getChildren()) {
                            ((AccountListEntry) var9).setSelected(false);
                        }
                    }
                }

                accountListEntry.setSelected(true);
            }
        });

        if (Client.INSTANCE.accountManager.currentAccount == account) {
            this.accountDetailsPanel.handleSelectedAccount(accountListEntry.selectedAccount);
            accountListEntry.setSelected(true, true);
        }
    }

    public void loginToAccount(AccountListEntry account) {
        account.setLoadingIndicator(true);

        new Thread(() -> {
            if (!Client.INSTANCE.accountManager.login(account.selectedAccount)) {
                account.setErrorBlinkTicks(114);
                SoundUtils.play("error");
                account.setLoadingIndicator(false);
                return;
            }

            this.clearRefreshingState();
            account.setAccountListRefreshing(true);
            SoundUtils.play("connect");
            this.updateAccountList(false);
            account.setLoadingIndicator(false);
        }).start();
    }

    private void getLoginDialog() {
        AlertComponent header = new AlertComponent(ComponentType.HEADER, "Add Alt", 50);
        AlertComponent firstline1 = new AlertComponent(ComponentType.FIRST_LINE, "Login with your minecraft", 15);
        AlertComponent firstline2 = new AlertComponent(ComponentType.FIRST_LINE, "account here!", 25);
        AlertComponent usernameInput = new AlertComponent(ComponentType.SECOND_LINE, "Username", 50);
        AlertComponent button = new AlertComponent(ComponentType.BUTTON, "Cracked login", 50);
        AlertComponent button2 = new AlertComponent(ComponentType.BUTTON, "Cookie login", 50);
        AlertComponent button3 = new AlertComponent(ComponentType.BUTTON, "Web login", 50);
        this.addToList(this.loginDialog = new Alert(this, "Add alt dialog", header, firstline1, firstline2, usernameInput, button, button2, button3));

        this.loginDialog.onPress(interactiveWidget -> {
            Button clickedButton = this.loginDialog.getClickedButton();
            if (clickedButton != null) {
                switch (clickedButton.getText()) {
                    case "Cracked login": {
                        String username = this.loginDialog.getInputMap().get("Username");
                        if (username != null && !username.isEmpty()) {
                            Account account = new Account(username, "0", Account.STEVE_UUID);
                            if (!Client.INSTANCE.accountManager.has(account)) {
                                Client.INSTANCE.accountManager.add(account);
                            }
                        }
                        this.updateAccountList(false);
                        break;
                    }
                    case "Cookie login": {
                        File file = FileUtils.openTxtFile();
                        if (file != null) {
                            try {
                                CookieLoginUtils.LoginData session = CookieLoginUtils.loginWithCookie(file);
                                if (session == null) {
                                    SoundUtils.play("error");
                                    return;
                                }

                                Account account = new Account(session.username, session.playerID, session.token);
                                if (!Client.INSTANCE.accountManager.has(account)) {
                                    Client.INSTANCE.accountManager.add(account);
                                }

                                this.updateAccountList(false);
                            } catch (Exception e) {
                                SoundUtils.play("error");
                                this.updateAccountList(false);
                            }
                        }
                        break;
                    }
                    case "Web login": {
                        ExecutorService executor = Executors.newSingleThreadExecutor();
                        MicrosoftUtils.acquireMSAuthCode(executor)
                                .thenComposeAsync(msAuthCode -> MicrosoftUtils.acquireMSAccessToken(msAuthCode, executor), executor)
                                .thenComposeAsync(msAccessToken -> MicrosoftUtils.acquireXboxAccessToken(msAccessToken, executor), executor)
                                .thenComposeAsync(xboxAccessToken -> MicrosoftUtils.acquireXboxXstsToken(xboxAccessToken, executor), executor)
                                .thenComposeAsync(xboxXstsData -> MicrosoftUtils.acquireMCAccessToken(xboxXstsData.get("Token"), xboxXstsData.get("uhs"), executor), executor)
                                .thenComposeAsync(mcToken -> MicrosoftUtils.login(mcToken, executor), executor)
                                .thenAccept(session -> {
                                    Account account = new Account(session.getUsername(), session.getAccessToken(), session.getUuid());
                                    if (!Client.INSTANCE.accountManager.has(account)) {
                                        Client.INSTANCE.accountManager.add(account);
                                    }

                                    this.updateAccountList(false);
                                })
                                .exceptionally(error -> {
                                    Client.LOGGER.error("Failed to login", error);
                                    SoundUtils.play("error");
                                    this.updateAccountList(false);
                                    return null;
                                });
                        break;
                    }
                }
            }
        });
    }

    private void deleteAltAlert() {
        AlertComponent title = new AlertComponent(ComponentType.HEADER, "Delete?", 50);
        AlertComponent firstLine = new AlertComponent(ComponentType.FIRST_LINE, "Are you sure you want", 15);
        AlertComponent secondLine = new AlertComponent(ComponentType.FIRST_LINE, "to delete this alt?", 40);
        AlertComponent button = new AlertComponent(ComponentType.BUTTON, "Delete", 50);
        this.addToList(this.deleteAlert = new Alert(this, "delete", title, firstLine, secondLine, button));
    }

    @Override
    public void draw(float partialTicks) {
        this.drawParallaxBackground();
        RenderUtils.drawFloatingPanel(
                (int) ((float) client.getWindow().getWidth() * this.leftPaneRatio),
                114,
                (int) ((float) client.getWindow().getWidth() * this.rightPaneRatio) - this.titleOffset,
                client.getWindow().getHeight() - 119 - this.titleOffset,
                ClientColors.LIGHT_GREYISH_BLUE.getColor()
        );
        this.updateEntrySlideAnimations();
        this.drawHeaderTitle();
        super.draw(partialTicks);
    }

    private void drawHeaderTitle() {
        int xPos = this.x + this.titleOffset;
        int yPos = this.y + this.titleOffset;
        int color = ColorHelper.applyAlpha(ClientColors.DEEP_TEAL.getColor(), 0.8F);
        RenderUtils.drawString(FontUtils.HELVETICA_LIGHT_40, (float) xPos, (float) yPos, "Jello", color);
        RenderUtils.drawString(FontUtils.HELVETICA_LIGHT_25, (float) (xPos + 87), (float) (yPos + 15), "Alt Manager", color);
    }

    private void updateEntrySlideAnimations() {
        float var3 = 1.0F;

        for (GuiComponent var5 : this.accountListPanel.getChildren()) {
            if (!(var5 instanceof VerticalScrollBar)) {
                for (GuiComponent var7 : var5.getChildren()) {
                    if (var7 instanceof AccountListEntry) {
                        AccountListEntry accountListEntry = (AccountListEntry) var7;
                        if (var7.getY() <= client.getWindow().getHeight() && this.accountListPanel.getScrollOffset() == 0) {
                            if (var3 > 0.2F) {
                                accountListEntry.entrySlideAnim.changeDirection(AnimationUtils.Direction.BACKWARDS);
                            }

                            float var9 = VecUtils.interpolate(accountListEntry.entrySlideAnim.calcPercent(), 0.51, 0.82, 0.0, 0.99);
                            accountListEntry.setTranslateX((int) (-((1.0F - var9) * (float) (var7.getWidth() + 30))));
                            var3 = accountListEntry.entrySlideAnim.calcPercent();
                        } else {
                            accountListEntry.setTranslateX(0);
                            accountListEntry.entrySlideAnim.changeDirection(AnimationUtils.Direction.BACKWARDS);
                        }
                    }
                }
            }
        }
    }

    private void clearRefreshingState() {
        for (GuiComponent screen : this.accountListPanel.getChildren()) {
            if (!(screen instanceof VerticalScrollBar)) {
                for (GuiComponent child : screen.getChildren()) {
                    AccountListEntry accountListEntry = (AccountListEntry) child;
                    accountListEntry.setAccountListRefreshing(false);
                }
            }
        }
    }

    private boolean canOpenLoginDialog() {
        for (GuiComponent var5 : this.accountListPanel.getChildren()) {
            if (!(var5 instanceof VerticalScrollBar)) {
                for (GuiComponent var7 : var5.getChildren()) {
                    if (var7.getTranslateX() != 0 && var7.getX() > this.width) {
                        return false;
                    }
                }
            }
        }

        return true;
    }

    private int getAccountEntryCount() {
        int var3 = 0;

        for (GuiComponent var5 : this.accountListPanel.getChildren()) {
            if (!(var5 instanceof VerticalScrollBar)) {
                for (GuiComponent ignored : var5.getChildren()) {
                    var3++;
                }
            }
        }

        return var3;
    }

    private void drawParallaxBackground() {
        int var3 = this.getMouseX() * -1;
        float var4 = (float) this.getMouseY() / (float) this.getWidth() * -114.0F;
        if (this.backgroundOffsetInit) {
            this.backgroundOffsetY = (float) ((int) var4);
            this.backgroundOffsetX = var3;
            this.backgroundOffsetInit = false;
        }

        float var5 = var4 - this.backgroundOffsetY;
        float var6 = (float) (var3 - this.backgroundOffsetX);
        RenderUtils.drawImage((float) this.backgroundOffsetX, this.backgroundOffsetY, (float) (this.getWidth() * 2), (float) (this.getHeight() + 114), Resources.MENU_PANORAMA);
        float var7 = 0.5F;
        if (var4 != this.backgroundOffsetY) {
            this.backgroundOffsetY += var5 * var7;
        }

        if (var3 != this.backgroundOffsetX) {
            this.backgroundOffsetX = (int) ((float) this.backgroundOffsetX + var6 * var7);
        }

        RenderUtils.drawRoundedRect(0.0F, 0.0F, (float) this.getWidth(), (float) this.getHeight(), ColorHelper.applyAlpha(ClientColors.LIGHT_GREYISH_BLUE.getColor(), 0.95F));
    }

    @Override
    public void keyPressed(int keyCode) {
        super.keyPressed(keyCode);
        if (keyCode == 256) {
            client.openScreen(new TitleScreen());
        }
    }

    @Override
    public JsonObject toConfigWithExtra(JsonObject config) {
        Client.INSTANCE.accountManager.shutdown();
        return config;
    }

    @Override
    public void loadConfig(JsonObject config) {
        for (GuiComponent var5 : this.accountListPanel.getChildren()) {
            if (!(var5 instanceof VerticalScrollBar)) {
                for (GuiComponent var7 : var5.getChildren()) {
                    this.accountListPanel.queueChildRemoval(var7);
                }
            }
        }

        this.updateAccountList(true);
    }

    public void updateAccountList(boolean forceRefresh) {
        List<Account> accounts = AccountSorter.sortByInputAltAccounts(this.accountSortType, this.accountFilter, this.searchBox.getText());
        this.addRunnable(() -> {

            int var3 = 0;
            if (accountListPanel != null) {
                var3 = accountListPanel.getScrollOffset();
                this.removeChildren(accountListPanel);
            }

            GuiComponent var4 = this.getChildByName("alts");
            if (var4 != null) {
                this.removeChildren(var4);
            }

            this.showAlert(this.accountListPanel = new ScrollablePanel(
                    this,
                    "alts",
                    0,
                    114,
                    (int) ((float) client.getWindow().getWidth() * this.leftPaneRatio) - 4,
                    client.getWindow().getHeight() - 119 - this.titleOffset
            ));

            for (Account var6 : accounts) {
                this.addAccountEntry(var6, forceRefresh);
            }

            this.accountListPanel.setScrollOffset(var3);
            this.accountListPanel.setListening(false);
            this.accountListPanel.setScissorEnabled(false);
        });
    }

    public int getDetailsPanelYOffset() {
        return client.getWindow().getHeight() / 12 + 280 + client.getWindow().getHeight() / 12;
    }
}